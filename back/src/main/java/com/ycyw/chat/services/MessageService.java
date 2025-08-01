package com.ycyw.chat.services;

import com.ycyw.chat.dto.request.CreateMessageRequestDto;
import com.ycyw.chat.models.Agent;
import com.ycyw.chat.models.Message;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.repositories.AgentRepository;
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
  private final AgentRepository agentRepository;

  public MessageService(MessageRepository messageRepository, TicketRepository ticketRepository,
      AgentRepository agentRepository) {
    this.messageRepository = messageRepository;
    this.ticketRepository = ticketRepository;
    this.agentRepository = agentRepository;
  }

  /**
   * Save a new message into the database.
   */
  public Message addMessage(UUID ticketId, CreateMessageRequestDto messageDto, Principal principal) {
    // Ensure ticket exists with client loaded
    Ticket ticket = ticketRepository.findByIdWithClient(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));

    // Determine if this is a client or agent
    Agent agent = null;
    if ("AGENT".equalsIgnoreCase(messageDto.getSenderType())) {
      // principal.getName() could be agentId or email depending on JWT
      UUID agentId = UUID.fromString(principal.getName());
      agent = agentRepository.findById(agentId)
          .orElseThrow(() -> new RuntimeException("Agent not found"));
    }

    Message message = Message.builder()
        .ticket(ticket)
        .agent(agent)
        .message(messageDto.getContent())
        .createdAt(LocalDateTime.now())
        .build();

    Message savedMessage = messageRepository.save(message);
    
    // Ensure the saved message has the ticket with client loaded
    savedMessage.setTicket(ticket);
    
    return savedMessage;
  }

  /**
   * Check if this is the first client message in a ticket.
   */
  public boolean isFirstClientMessage(UUID ticketId) {
    // If no client message exists yet, then the next one will be the first
    return messageRepository.countByTicketIdAndAgentIdIsNull(ticketId) == 1;
  }
}
