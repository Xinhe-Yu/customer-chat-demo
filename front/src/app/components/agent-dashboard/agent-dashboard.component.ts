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
import { TicketService, Ticket } from '../../services/ticket.service';
import { TicketStatusService } from '../../services/ticket-status.service';
import { Subscription } from 'rxjs';

interface TicketInfo {
  ticketId: string;
  issueType: string;
  status: string;
  clientName: string;
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
  tickets: Ticket[] = [];
  assignedTickets: Ticket[] = [];
  private subscriptions: Subscription[] = [];
  private currentUserId: string | null = null;

  constructor(
    private websocketService: WebsocketService,
    private authService: AuthService,
    private ticketService: TicketService,
    private ticketStatusService: TicketStatusService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Subscribe to current user to get the user ID
    const userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUserId = user?.id || null;
    });
    this.subscriptions.push(userSub);

    this.loadTickets();
    this.loadAssignedTickets();
    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.websocketService.disconnect();
  }

  private loadTickets(): void {
    // Load only unassigned tickets (available for agents to claim)
    this.ticketService.getAllTickets().subscribe({
      next: (tickets) => {
        this.tickets = tickets;
      },
      error: (error) => {
        console.error('Error loading available tickets:', error);
      }
    });
  }

  private loadAssignedTickets(): void {
    // Load tickets assigned to the current agent
    this.ticketService.getMyTickets().subscribe({
      next: (tickets) => {
        this.assignedTickets = tickets;
      },
      error: (error) => {
        console.error('Error loading assigned tickets:', error);
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
            clientName: ticketUpdate.clientUsername || 'Unknown Client',
            agentName: undefined,
            messages: [],
            createdAt: new Date().toISOString()
          });
        },
        error: (error) => console.error('Agent tickets subscription error:', error)
      });

      const ticketStatusSub = this.websocketService.subscribeToTicketStatusUpdates().subscribe({
        next: (statusUpdate) => {
          console.log('Ticket status update received:', statusUpdate);
          console.log('Current user ID:', this.currentUserId);

          // Convert ticket ID to string for comparison
          const ticketIdStr = statusUpdate.ticketId.toString();

          // Get current user info to check if this agent is the one being assigned
          const isCurrentAgent = this.currentUserId && statusUpdate.agentId &&
            this.currentUserId === statusUpdate.agentId.toString();

          console.log('Is current agent:', isCurrentAgent, 'Agent ID from update:', statusUpdate.agentId);

          // Update the ticket status or remove it if assigned to another agent
          const ticketIndex = this.tickets.findIndex(t => t.ticketId === ticketIdStr);
          if (ticketIndex !== -1) {
            if (statusUpdate.status === 'in_progress') {
              const ticket = this.tickets[ticketIndex];
              // Remove ticket from available list as it's now assigned
              this.tickets.splice(ticketIndex, 1);
              console.log('Removed ticket from available list:', ticketIdStr);

              // If this agent is the one who got assigned, add it to assigned tickets
              if (isCurrentAgent) {
                ticket.status = statusUpdate.status;
                this.assignedTickets.push(ticket);
                console.log('Added ticket to current agent assigned list:', ticketIdStr);
              }
            } else {
              // Update ticket status
              this.tickets[ticketIndex].status = statusUpdate.status;
              console.log('Updated ticket status in available list:', ticketIdStr, statusUpdate.status);
            }
          }

          // Handle assigned tickets updates
          const assignedTicketIndex = this.assignedTickets.findIndex(t => t.ticketId === ticketIdStr);
          if (assignedTicketIndex !== -1) {
            if (statusUpdate.status === 'resolved' || statusUpdate.status === 'closed') {
              // Remove resolved/closed tickets from assigned list
              this.assignedTickets.splice(assignedTicketIndex, 1);
              console.log('Removed resolved ticket from assigned list:', ticketIdStr);
            } else {
              // Update status for other status changes
              this.assignedTickets[assignedTicketIndex].status = statusUpdate.status;
              console.log('Updated assigned ticket status:', ticketIdStr, statusUpdate.status);
            }
          }
        },
        error: (error) => console.error('Ticket status subscription error:', error)
      });

      this.subscriptions.push(agentTicketsSub, ticketStatusSub);
    } catch (error) {
      console.error('WebSocket connection failed:', error);
    }
  }

  joinTicket(ticketId: string): void {
    this.router.navigate(['/chatroom', ticketId]);
  }

  logout(): void {
    // Clean up WebSocket connections before logout
    this.websocketService.disconnect();
    
    // Unsubscribe from all subscriptions to prevent memory leaks
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];
    
    // Clear auth and navigate
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getStatusColor(status: string): string {
    return this.ticketStatusService.getCssClass(status);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
