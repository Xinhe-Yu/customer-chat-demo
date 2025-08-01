import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface ChatMessage {
  senderType: 'CLIENT' | 'AGENT' | 'SYSTEM';
  senderName?: string;
  content: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient?: Client;
  private connectionStatus = new BehaviorSubject<boolean>(false);
  public connectionStatus$ = this.connectionStatus.asObservable();

  constructor(private authService: AuthService) { }

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      const token = this.authService.getToken();
      if (!token) {
        reject('No authentication token available');
        return;
      }

      this.stompClient = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        onConnect: () => {
          this.connectionStatus.next(true);
          resolve();
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.connectionStatus.next(false);
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame);
          this.connectionStatus.next(false);
          // Clear the client on error to prevent hanging connections
          this.stompClient = undefined;
          reject(frame);
        }
      });

      this.stompClient.activate();
    });
  }

  disconnect(): void {
    if (this.stompClient) {
      try {
        // Force immediate disconnection
        this.stompClient.forceDisconnect();
        this.stompClient.deactivate();
      } catch (error) {
        console.warn('Error during WebSocket disconnection:', error);
      } finally {
        this.connectionStatus.next(false);
        this.stompClient = undefined;
      }
    }
  }

  subscribeToTicket(ticketId: string): Observable<ChatMessage> {
    return new Observable(observer => {
      if (!this.stompClient || !this.stompClient.connected) {
        observer.error('WebSocket not connected');
        return;
      }

      const subscription = this.stompClient.subscribe(
        `/topic/tickets/${ticketId}`,
        (message: IMessage) => {
          try {
            const chatMessage: ChatMessage = JSON.parse(message.body);
            observer.next(chatMessage);
          } catch (error) {
            observer.error('Failed to parse message: ' + error);
          }
        }
      );

      return () => subscription.unsubscribe();
    });
  }

  subscribeToAgentTickets(): Observable<any> {
    return new Observable(observer => {
      if (!this.stompClient || !this.stompClient.connected) {
        observer.error('WebSocket not connected');
        return;
      }

      const subscription = this.stompClient.subscribe(
        '/topic/agent/open-tickets',
        (message: IMessage) => {
          try {
            const ticketUpdate = JSON.parse(message.body);
            observer.next(ticketUpdate);
          } catch (error) {
            observer.error('Failed to parse message: ' + error);
          }
        }
      );

      return () => subscription.unsubscribe();
    });
  }

  subscribeToTicketStatusUpdates(): Observable<any> {
    return new Observable(observer => {
      if (!this.stompClient || !this.stompClient.connected) {
        observer.error('WebSocket not connected');
        return;
      }

      const subscription = this.stompClient.subscribe(
        '/topic/agent/ticket-status-updates',
        (message: IMessage) => {
          try {
            const statusUpdate = JSON.parse(message.body);
            console.log('WebSocket received ticket status update:', statusUpdate);
            observer.next(statusUpdate);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
            observer.error('Failed to parse message: ' + error);
          }
        }
      );

      return () => subscription.unsubscribe();
    });
  }

  sendMessage(ticketId: string, message: ChatMessage): void {
    if (!this.stompClient || !this.stompClient.connected) {
      throw new Error('WebSocket not connected');
    }

    this.stompClient.publish({
      destination: `/app/tickets/${ticketId}/messages`,
      body: JSON.stringify(message)
    });
  }

  isConnected(): boolean {
    return this.stompClient?.connected || false;
  }
}
