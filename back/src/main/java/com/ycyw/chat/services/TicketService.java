package com.ycyw.chat.services;

import com.ycyw.chat.models.Client;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.repositories.ClientRepository;
import com.ycyw.chat.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
  private final TicketRepository ticketRepo;
  private final ClientRepository clientRepo;

  public TicketService(TicketRepository ticketRepo, ClientRepository clientRepo) {
    this.ticketRepo = ticketRepo;
    this.clientRepo = clientRepo;
  }

  public Ticket createTicket(UUID clientId, String issueType) {
    Client client = clientRepo.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found"));
    
    Ticket ticket = Ticket.builder()
        .client(client)
        .issueType(issueType)
        .status("OPEN")
        .build();
    return ticketRepo.save(ticket);
  }

  public Ticket getTicket(UUID ticketId) {
    return ticketRepo.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
  }

  public List<Ticket> getAllTickets() {
    return ticketRepo.findAll();
  }

  public Ticket getTicketWithClient(UUID ticketId) {
    return ticketRepo.findByIdWithClient(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
  }

  public List<Ticket> getAllTicketsWithMessages() {
    return ticketRepo.findAllWithMessages();
  }

  public List<Ticket> getClientTicketsWithMessages(UUID clientId) {
    return ticketRepo.findByClientIdWithMessages(clientId);
  }
}
