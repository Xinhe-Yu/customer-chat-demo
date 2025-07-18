package com.ycyw.chat.services;

import com.ycyw.chat.dto.response.TicketStatusUpdateDto;
import com.ycyw.chat.models.Agent;
import com.ycyw.chat.models.Client;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.models.TicketStatus;
import com.ycyw.chat.repositories.AgentRepository;
import com.ycyw.chat.repositories.ClientRepository;
import com.ycyw.chat.repositories.TicketRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
  private final TicketRepository ticketRepo;
  private final ClientRepository clientRepo;
  private final AgentRepository agentRepo;
  private final SimpMessagingTemplate messagingTemplate;

  public TicketService(TicketRepository ticketRepo, ClientRepository clientRepo, AgentRepository agentRepo, SimpMessagingTemplate messagingTemplate) {
    this.ticketRepo = ticketRepo;
    this.clientRepo = clientRepo;
    this.agentRepo = agentRepo;
    this.messagingTemplate = messagingTemplate;
  }

  public Ticket createTicket(UUID clientId, String issueType) {
    Client client = clientRepo.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found"));
    
    Ticket ticket = Ticket.builder()
        .client(client)
        .issueType(issueType)
        .status(TicketStatus.OPEN)
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

  public List<Ticket> getUnassignedTicketsWithMessages() {
    return ticketRepo.findUnassignedTicketsWithMessages();
  }

  public List<Ticket> getClientTicketsWithMessages(UUID clientId) {
    return ticketRepo.findByClientIdWithMessages(clientId);
  }

  public List<Ticket> getAgentTicketsWithMessages(UUID agentId) {
    return ticketRepo.findByAssignedAgentIdWithMessages(agentId);
  }

  public Ticket assignAgentToTicket(UUID ticketId, UUID agentId) {
    // Check if ticket exists and is unassigned
    Ticket ticket = ticketRepo.findUnassignedTicketById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found or already assigned"));
    
    // Find the agent
    Agent agent = agentRepo.findById(agentId)
        .orElseThrow(() -> new RuntimeException("Agent not found"));
    
    // Assign agent and update status
    ticket.setAssignedAgent(agent);
    ticket.setStatus(TicketStatus.IN_PROGRESS);
    
    Ticket savedTicket = ticketRepo.save(ticket);
    
    // Broadcast status update to all agents
    TicketStatusUpdateDto statusUpdate = new TicketStatusUpdateDto(
        ticketId, 
        TicketStatus.IN_PROGRESS.getValue(), 
        agentId, 
        agent.getName()
    );
    messagingTemplate.convertAndSend("/topic/agent/ticket-status-updates", statusUpdate);
    
    return savedTicket;
  }

  public Ticket resolveTicket(UUID ticketId, UUID clientId) {
    // Find the ticket and verify it belongs to the client
    Ticket ticket = ticketRepo.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
    
    if (!ticket.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Ticket does not belong to this client");
    }
    
    // Only allow resolution if ticket is not already resolved or closed
    if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
      throw new RuntimeException("Ticket is already resolved or closed");
    }
    
    // Update ticket status to resolved
    ticket.setStatus(TicketStatus.RESOLVED);
    
    Ticket savedTicket = ticketRepo.save(ticket);
    
    // Broadcast status update to all agents
    TicketStatusUpdateDto statusUpdate = new TicketStatusUpdateDto(
        ticketId, 
        TicketStatus.RESOLVED.getValue(), 
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null,
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null
    );
    messagingTemplate.convertAndSend("/topic/agent/ticket-status-updates", statusUpdate);
    
    return savedTicket;
  }

  public Ticket closeTicket(UUID ticketId, UUID clientId, UUID agentId) {
    // Find the ticket
    Ticket ticket = ticketRepo.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
    
    // Verify permissions
    if (clientId != null && !ticket.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Ticket does not belong to this client");
    }
    
    if (agentId != null && (ticket.getAssignedAgent() == null || 
        !ticket.getAssignedAgent().getId().equals(agentId))) {
      throw new RuntimeException("Ticket is not assigned to this agent");
    }
    
    // Update ticket status to closed
    ticket.setStatus(TicketStatus.CLOSED);
    
    Ticket savedTicket = ticketRepo.save(ticket);
    
    // Broadcast status update to all agents
    TicketStatusUpdateDto statusUpdate = new TicketStatusUpdateDto(
        ticketId, 
        TicketStatus.CLOSED.getValue(), 
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null,
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null
    );
    messagingTemplate.convertAndSend("/topic/agent/ticket-status-updates", statusUpdate);
    
    return savedTicket;
  }
}
