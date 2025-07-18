import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateTicketRequest {
  issueType: string;
}

export interface CreateTicketResponse {
  ticketId: string;
}

export interface Message {
  id: string;
  senderType: 'CLIENT' | 'AGENT';
  content: string;
  createdAt: string;
}

export interface Ticket {
  ticketId: string;
  status: string;
  issueType: string;
  messages: Message[];
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private readonly apiUrl = '/api';

  constructor(private http: HttpClient) { }

  createTicket(request: CreateTicketRequest): Observable<CreateTicketResponse> {
    return this.http.post<CreateTicketResponse>(`${this.apiUrl}/tickets`, request);
  }

  getTicket(ticketId: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/tickets/${ticketId}`);
  }

  getAllTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/agent/tickets`);
  }

  getClientTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/tickets/client/my-tickets`);
  }
}
