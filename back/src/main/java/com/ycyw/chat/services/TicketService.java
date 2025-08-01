package com.ycyw.chat.services;

import com.ycyw.chat.dto.response.TicketStatusUpdateDto;
import com.ycyw.chat.models.Agent;
import com.ycyw.chat.models.Client;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.models.TicketStatus;
import com.ycyw.chat.repositories.AgentRepository;
import com.ycyw.chat.repositories.ClientRepository;
import com.ycyw.chat.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
  private final TicketRepository ticketRepo;
  private final ClientRepository clientRepo;
  private final AgentRepository agentRepo;
  private final NotifierService notifierService;

  public TicketService(TicketRepository ticketRepo, ClientRepository clientRepo, AgentRepository agentRepo, NotifierService notifierService) {
    this.ticketRepo = ticketRepo;
    this.clientRepo = clientRepo;
    this.agentRepo = agentRepo;
    this.notifierService = notifierService;
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
    // Check if ticket exists first
    Ticket ticket = ticketRepo.findById(ticketId)
        .orElseThrow(() -> new RuntimeException("Ticket not found"));
    
    // If ticket is already assigned, return it as-is
    if (ticket.getAssignedAgent() != null) {
      return ticket;
    }
    
    // Only assign if ticket is OPEN (unassigned)
    if (ticket.getStatus() != TicketStatus.OPEN) {
      return ticket;
    }
    
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
    notifierService.notifyTicketStatusUpdate(statusUpdate);
    
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
    notifierService.notifyTicketStatusUpdate(statusUpdate);
    
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
    notifierService.notifyTicketStatusUpdate(statusUpdate);
    
    return savedTicket;
  }

}
