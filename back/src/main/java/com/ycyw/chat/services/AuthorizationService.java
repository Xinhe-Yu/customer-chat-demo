package com.ycyw.chat.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.repositories.TicketRepository;

@Service
public class AuthorizationService {
  private final TicketRepository ticketRepository;

  public AuthorizationService(TicketRepository ticketRepository) {
    this.ticketRepository = ticketRepository;
  }

  public boolean isUserAuthorizedForTicket(String userEmail, String ticketId) {
    try {
      UUID ticketUuid = UUID.fromString(ticketId);
      Ticket ticket = ticketRepository.findByIdWithClient(ticketUuid).orElse(null);
      return ticket != null && ticket.getClient().getEmail().equals(userEmail);
    } catch (Exception e) {
      return false;
    }
  }
}