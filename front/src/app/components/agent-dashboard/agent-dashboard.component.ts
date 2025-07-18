import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { WebsocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { TicketService } from '../../services/ticket.service';
import { Subscription } from 'rxjs';

interface TicketInfo {
  ticketId: string;
  issueType: string;
  status: string;
  clientId: string;
  createdAt: string;
}

@Component({
  selector: 'app-agent-dashboard',
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatToolbarModule,
    MatChipsModule,
    MatIconModule,
    MatBadgeModule
  ],
  templateUrl: './agent-dashboard.component.html',
  styleUrl: './agent-dashboard.component.scss'
})
export class AgentDashboardComponent implements OnInit, OnDestroy {
  tickets: TicketInfo[] = [];
  private subscriptions: Subscription[] = [];

  constructor(
    private websocketService: WebsocketService,
    private authService: AuthService,
    private ticketService: TicketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTickets();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.websocketService.disconnect();
  }

  private loadTickets(): void {
    this.ticketService.getAllTickets().subscribe({
      next: (tickets) => {
        this.tickets = tickets.map(ticket => ({
          ticketId: ticket.ticketId,
          issueType: ticket.issueType,
          status: ticket.status,
          clientId: 'client', // You can enhance this later
          createdAt: ticket.createdAt
        }));
      },
      error: (error) => {
        console.error('Error loading tickets:', error);
      }
    });
  }

  private async connectWebSocket(): Promise<void> {
    try {
      await this.websocketService.connect();
      
      const agentTicketsSub = this.websocketService.subscribeToAgentTickets().subscribe({
        next: (ticketUpdate) => {
          console.log('New ticket update:', ticketUpdate);
          // Add the new ticket to the list
          this.tickets.push({
            ticketId: ticketUpdate.ticketId,
            issueType: ticketUpdate.issueType,
            status: 'open',
            clientId: ticketUpdate.clientUsername || 'client',
            createdAt: new Date().toISOString()
          });
        },
        error: (error) => console.error('Agent tickets subscription error:', error)
      });
      
      this.subscriptions.push(agentTicketsSub);
    } catch (error) {
      console.error('WebSocket connection failed:', error);
    }
  }

  joinTicket(ticketId: string): void {
    this.router.navigate(['/chatroom', ticketId]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'open': return 'primary';
      case 'in-progress': return 'accent';
      case 'closed': return 'warn';
      default: return 'primary';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
