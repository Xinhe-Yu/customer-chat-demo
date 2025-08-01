import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
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
    MatChipsModule,
    MatSnackBarModule
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
  private welcomeMessageAdded = false;
  private ticketStatus: string = 'open';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private websocketService: WebsocketService,
    private ticketService: TicketService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.messageForm = this.fb.group({
      message: ['']
    });

    // Initially disable the form until connected
    this.updateFormState();
  }

  ngOnInit(): void {
    this.ticketId = this.route.snapshot.paramMap.get('ticketId') || '';
    if (!this.ticketId) {
      this.router.navigate(['/']);
      return;
    }

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      // Load ticket history after user is loaded
      if (user) {
        // If user is an agent, try to join the ticket first
        if (user.role === 'AGENT') {
          this.joinTicketAsAgent();
        } else {
          this.loadTicketHistory();
        }
      }
    });

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
    console.log('Loading ticket history for user:', this.currentUser?.role); // Debug log

    this.ticketService.getTicket(this.ticketId).subscribe({
      next: (ticket) => {
        this.messages = ticket.messages.map(msg => ({
          senderType: msg.senderType,
          senderName: msg.senderName,
          content: msg.content,
          createdAt: msg.createdAt
        }));

        this.ticketStatus = ticket.status; // Store ticket status

        console.log('Loaded messages:', this.messages.length); // Debug log

        // Add welcome message if no messages exist
        if (this.messages.length === 0) {
          this.addWelcomeMessage();
        }

        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Failed to load ticket history:', error);
        this.messages = [];
        this.addWelcomeMessage();
      }
    });
  }

  private joinTicketAsAgent(): void {
    console.log('Agent joining ticket:', this.ticketId); // Debug log

    this.ticketService.joinTicket(this.ticketId).subscribe({
      next: (ticket) => {
        console.log('Successfully joined ticket:', ticket);
        this.messages = ticket.messages.map(msg => ({
          senderType: msg.senderType,
          senderName: msg.senderName,
          content: msg.content,
          createdAt: msg.createdAt
        }));

        this.ticketStatus = ticket.status; // Store ticket status

        // Add welcome message if no messages exist
        if (this.messages.length === 0) {
          this.addWelcomeMessage();
        }

        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Failed to join ticket:', error);
        // If join fails (e.g., ticket already assigned), just load history
        this.loadTicketHistory();
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
        this.updateFormState();
      });

      this.subscriptions.push(messagesSub, connectionSub);
    } catch (error) {
      console.error('WebSocket connection failed:', error);
      this.isConnected = false;
      this.updateFormState();
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

  getSenderDisplayName(message: ChatMessage): string {
    if (message.senderType === 'SYSTEM') return 'Your Car Your Way Support Team';
    // Use the actual sender name if available, otherwise fall back to generic names
    return message.senderName || (message.senderType === 'CLIENT' ? 'Customer' : 'Support Agent');
  }

  private addWelcomeMessage(): void {
    if (this.welcomeMessageAdded || !this.currentUser) return;

    // Only show welcome messages for clients
    if (this.currentUser.role !== 'CLIENT') return;

    console.log('Adding welcome message for client'); // Debug log

    const welcomeMessages = this.getWelcomeMessages();

    welcomeMessages.forEach((messageContent, index) => {
      setTimeout(() => {
        this.messages.push({
          senderType: 'SYSTEM',
          content: messageContent,
          createdAt: new Date().toISOString()
        });
        this.shouldScrollToBottom = true;
      }, index * 1000); // Stagger messages by 1 second
    });

    this.welcomeMessageAdded = true;
  }

  private getWelcomeMessages(): string[] {
    if (this.currentUser?.role === 'CLIENT') {
      return [
        "Hello! Welcome to our support chat. 👋",
        "I'm here to help you with any questions or issues you may have.",
        "Please describe your problem and I'll assist you as soon as possible!"
      ];
    } else if (this.currentUser?.role === 'AGENT') {
      return [
        "You've joined this support ticket.",
        "Review the ticket details and start helping the customer!"
      ];
    }
    return ["Welcome to the support chat!"];
  }

  isSystemMessage(message: ChatMessage): boolean {
    return message.senderType === 'SYSTEM';
  }

  private updateFormState(): void {
    const messageControl = this.messageForm.get('message');
    if (this.isConnected) {
      messageControl?.enable();
    } else {
      messageControl?.disable();
    }
  }

  canSendMessage(): boolean {
    return this.messageForm.valid && this.isConnected && !!this.currentUser;
  }

  canResolveTicket(): boolean {
    return this.ticketStatus !== 'resolved' && this.ticketStatus !== 'closed';
  }

  resolveTicket(): void {
    if (!this.canResolveTicket()) return;

    this.ticketService.resolveTicket(this.ticketId).subscribe({
      next: (resolvedTicket) => {
        console.log('Ticket resolved:', resolvedTicket);
        this.ticketStatus = resolvedTicket.status;

        // Add a system message to indicate the ticket was resolved
        this.messages.push({
          senderType: 'SYSTEM',
          content: 'This ticket has been marked as resolved by the customer.',
          createdAt: new Date().toISOString()
        });
        this.shouldScrollToBottom = true;

        // Show success message
        this.snackBar.open('Ticket marked as resolved!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error) => {
        console.error('Error resolving ticket:', error);
        this.snackBar.open('Failed to resolve ticket. Please try again.', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
