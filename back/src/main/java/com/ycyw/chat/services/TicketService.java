package com.ycyw.chat.services;

import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketService {
  private final TicketRepository ticketRepo;

  public TicketService(TicketRepository ticketRepo) {
    this.ticketRepo = ticketRepo;
  }

  public Ticket createTicket(UUID clientId, String issueType) {
    Ticket ticket = Ticket.builder()
        .clientId(clientId)
        .issueType(issueType)
        .status("OPEN")
        .build();
    return ticketRepo.save(ticket);
  }

  public Ticket getTicket(UUID ticketId) {
    return ticketRepo.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
  }
}
