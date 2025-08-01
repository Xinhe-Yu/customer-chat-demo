import { Injectable } from '@angular/core';

export interface StatusColorMapping {
  material: string;  // For Material Design components (mat-chip color)
  css: string;       // For CSS classes
}

@Injectable({
  providedIn: 'root'
})
export class TicketStatusService {

  private statusColorMap: { [key: string]: StatusColorMapping } = {
    'open': {
      material: 'primary',
      css: 'badge-open'
    },
    'in_progress': {
      material: 'accent',
      css: 'badge-in-progress'
    },
    'resolved': {
      material: 'warn',
      css: 'badge-resolved'
    },
    'closed': {
      material: 'warn',
      css: 'badge-resolved'
    }
  };

  getCssClass(status: string): string {
    const normalizedStatus = status.toLowerCase();
    return this.statusColorMap[normalizedStatus]?.css || 'primary';
  }

  /**
   * Format status text for display
   */
  formatStatus(status: string): string {
    switch (status.toLowerCase()) {
      case 'open': return 'Open';
      case 'in_progress': return 'In Progress';
      case 'resolved': return 'Resolved';
      case 'closed': return 'Closed';
      default: return status;
    }
  }

  /**
   * Get all available statuses
   */
  getAvailableStatuses(): string[] {
    return Object.keys(this.statusColorMap);
  }
}
