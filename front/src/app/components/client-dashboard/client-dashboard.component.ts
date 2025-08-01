import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { TicketService, Ticket } from '../../services/ticket.service';
import { AuthService } from '../../services/auth.service';
import { TicketStatusService } from '../../services/ticket-status.service';

@Component({
  selector: 'app-client-dashboard',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTabsModule,
    MatChipsModule,
    MatBadgeModule
  ],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.scss'
})
export class ClientDashboardComponent implements OnInit {
  ticketForm: FormGroup;
  isLoading = false;
  historicalTickets: Ticket[] = [];

  issueTypes = [
    { value: 'payment', label: 'Payment Issues', icon: 'payment' },
    { value: 'technical', label: 'Technical Support', icon: 'build' },
    { value: 'billing', label: 'Billing Questions', icon: 'receipt' },
    { value: 'account', label: 'Account Management', icon: 'person' },
    { value: 'general', label: 'General Inquiry', icon: 'help' }
  ];

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private authService: AuthService,
    private ticketStatusService: TicketStatusService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.ticketForm = this.fb.group({
      issueType: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadHistoricalTickets();
  }

  loadHistoricalTickets(): void {
    this.ticketService.getMyTickets().subscribe({
      next: (tickets) => {
        this.historicalTickets = tickets;
      },
      error: (error) => {
        console.error('Error loading historical tickets:', error);
      }
    });
  }

  createTicket(): void {
    if (this.ticketForm.invalid) return;

    this.isLoading = true;
    const { issueType } = this.ticketForm.value;

    this.ticketService.createTicket({ issueType }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open('Ticket created successfully! Redirecting to chat...', 'Close', {
          duration: 2000
        });
        setTimeout(() => {
          this.router.navigate(['/chatroom', response.ticketId]);
        }, 2000);
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Failed to create ticket. Please try again.', 'Close', {
          duration: 3000
        });
        console.error('Create ticket error:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getSelectedIssueType() {
    const selectedValue = this.ticketForm.get('issueType')?.value;
    return this.issueTypes.find(type => type.value === selectedValue);
  }

  openTicket(ticketId: string): void {
    this.router.navigate(['/chatroom', ticketId]);
  }

  getStatusColor(status: string): string {
    return this.ticketStatusService.getCssClass(status);
  }

  formatStatus(status: string): string {
    return this.ticketStatusService.formatStatus(status);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  canCreateTicket(): boolean {
    return this.ticketForm.valid && !this.isLoading;
  }

  resolveTicket(ticketId: string): void {
    this.ticketService.resolveTicket(ticketId).subscribe({
      next: (resolvedTicket) => {
        console.log('Ticket resolved:', resolvedTicket);
        // Update the ticket in the local array
        const ticketIndex = this.historicalTickets.findIndex(t => t.ticketId === ticketId);
        if (ticketIndex !== -1) {
          this.historicalTickets[ticketIndex].status = resolvedTicket.status;
        }
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
