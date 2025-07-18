import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { WebsocketService, ChatMessage } from '../../services/websocket.service';
import { TicketService } from '../../services/ticket.service';
import { AuthService, User } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chatroom',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatBadgeModule,
    MatChipsModule
  ],
  templateUrl: './chatroom.component.html',
  styleUrl: './chatroom.component.scss'
})
export class ChatroomComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;
  
  ticketId!: string;
  messageForm: FormGroup;
  messages: ChatMessage[] = [];
  currentUser: User | null = null;
  isConnected = false;
  private subscriptions: Subscription[] = [];
  private shouldScrollToBottom = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private websocketService: WebsocketService,
    private ticketService: TicketService,
    private authService: AuthService
  ) {
    this.messageForm = this.fb.group({
      message: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    this.ticketId = this.route.snapshot.paramMap.get('ticketId') || '';
    if (!this.ticketId) {
      this.router.navigate(['/']);
      return;
    }

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    this.loadTicketHistory();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.websocketService.disconnect();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private loadTicketHistory(): void {
    this.ticketService.getTicket(this.ticketId).subscribe({
      next: (ticket) => {
        this.messages = ticket.messages.map(msg => ({
          senderType: msg.senderType,
          content: msg.content,
          createdAt: msg.createdAt
        }));
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Failed to load ticket history:', error);
        this.messages = [];
      }
    });
  }

  private async connectWebSocket(): Promise<void> {
    try {
      await this.websocketService.connect();
      this.isConnected = true;

      const messagesSub = this.websocketService.subscribeToTicket(this.ticketId).subscribe({
        next: (message) => {
          this.messages.push(message);
          this.shouldScrollToBottom = true;
        },
        error: (error) => {
          console.error('Messages subscription error:', error);
          this.isConnected = false;
        }
      });

      const connectionSub = this.websocketService.connectionStatus$.subscribe(status => {
        this.isConnected = status;
      });

      this.subscriptions.push(messagesSub, connectionSub);
    } catch (error) {
      console.error('WebSocket connection failed:', error);
      this.isConnected = false;
    }
  }

  sendMessage(): void {
    if (this.messageForm.invalid || !this.isConnected || !this.currentUser) return;

    const messageText = this.messageForm.get('message')?.value.trim();
    if (!messageText) return;

    const message: ChatMessage = {
      senderType: this.currentUser.role,
      content: messageText
    };

    try {
      this.websocketService.sendMessage(this.ticketId, message);
      this.messageForm.reset();
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  goBack(): void {
    const dashboardPath = this.currentUser?.role === 'CLIENT' ? '/client/dashboard' : '/agent/dashboard';
    this.router.navigate([dashboardPath]);
  }

  isMyMessage(message: ChatMessage): boolean {
    return message.senderType === this.currentUser?.role;
  }

  formatTime(timestamp?: string): string {
    if (!timestamp) return new Date().toLocaleTimeString();
    return new Date(timestamp).toLocaleTimeString();
  }

  getSenderDisplayName(senderType: string): string {
    return senderType === 'CLIENT' ? 'Customer' : 'Support Agent';
  }
}
