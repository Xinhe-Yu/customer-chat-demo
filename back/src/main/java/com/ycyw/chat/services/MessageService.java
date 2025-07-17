package com.ycyw.chat.services;

import com.ycyw.chat.dto.request.CreateMessageRequestDto;
import com.ycyw.chat.models.Message;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.repositories.MessageRepository;
import com.ycyw.chat.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MessageService {

  private final MessageRepository messageRepository;
  private final TicketRepository ticketRepository;

  public MessageService(MessageRepository messageRepository, TicketRepository ticketRepository) {
    this.messageRepository = messageRepository;
    this.ticketRepository = ticketRepository;
  }

  /**
   * Save a new message into the database.
   */
  public Message addMessage(UUID ticketId, CreateMessageRequestDto messageDto, Principal principal) {
    // Ensure ticket exists
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Determine if this is a client or agent
    UUID agentId = null;
    if ("AGENT".equalsIgnoreCase(messageDto.getSenderType())) {
      // principal.getName() could be agentId or email depending on JWT
      agentId = UUID.fromString(principal.getName());
    }

    Message message = Message.builder()
        .ticket(ticket)
        .agentId(agentId)
        .message(messageDto.getContent())
        .createdAt(LocalDateTime.now())
        .build();

    return messageRepository.save(message);
  }

  /**
   * Check if this is the first client message in a ticket.
   */
  public boolean isFirstClientMessage(UUID ticketId) {
    // If no client message exists yet, then the next one will be the first
    return !messageRepository.existsByTicketIdAndAgentIdIsNull(ticketId);
  }
}
