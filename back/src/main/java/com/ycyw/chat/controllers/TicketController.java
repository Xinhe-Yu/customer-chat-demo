package com.ycyw.chat.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ycyw.chat.dto.MessageDto;
import com.ycyw.chat.dto.request.CreateTicketRequestDto;
import com.ycyw.chat.dto.request.UpdateTicketStatusRequestDto;
import com.ycyw.chat.dto.response.TicketDetailResponseDto;
import com.ycyw.chat.dto.response.TicketResponseDto;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.services.ClientDetails;
import com.ycyw.chat.services.AgentDetails;
import com.ycyw.chat.services.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @PostMapping
  public ResponseEntity<TicketResponseDto> createTicket(
      @Valid @RequestBody CreateTicketRequestDto request,
      @AuthenticationPrincipal ClientDetails clientDetails) {

    Ticket ticket = ticketService.createTicket(clientDetails.getId(), request.getIssueType());
    TicketResponseDto response = new TicketResponseDto(ticket.getId().toString());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{ticketId}")
  public ResponseEntity<TicketDetailResponseDto> getTicket(@PathVariable UUID ticketId) {
    Ticket ticket = ticketService.getTicket(ticketId);
    List<MessageDto> messageDtos = ticket.getMessages().stream()
        .map(m -> new MessageDto(
            m.getAgent() == null ? "CLIENT" : "AGENT",
            m.getAgent() == null ? ticket.getClient().getUsername() : m.getAgent().getName(),
            m.getMessage(),
            m.getCreatedAt().toString()))
        .toList();

    TicketDetailResponseDto response = new TicketDetailResponseDto(
        ticket.getId().toString(),
        ticket.getStatus().getValue(),
        ticket.getIssueType(),
        ticket.getClient().getUsername(),
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
        messageDtos,
        ticket.getCreatedAt().toString());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/available")
  public ResponseEntity<List<TicketDetailResponseDto>> getAvailableTickets() {
    List<TicketDetailResponseDto> tickets = ticketService.getUnassignedTicketsWithMessages().stream()
        .map(ticket -> {
          List<MessageDto> messageDtos = ticket.getMessages().stream()
              .map(m -> new MessageDto(
                  m.getAgent() == null ? "CLIENT" : "AGENT",
                  m.getAgent() == null ? ticket.getClient().getUsername() : m.getAgent().getName(),
                  m.getMessage(),
                  m.getCreatedAt().toString()))
              .toList();
          
          return new TicketDetailResponseDto(
              ticket.getId().toString(),
              ticket.getStatus().getValue(),
              ticket.getIssueType(),
              ticket.getClient().getUsername(),
              ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
              messageDtos,
              ticket.getCreatedAt().toString()
          );
        })
        .toList();
    
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/my-tickets")
  public ResponseEntity<List<TicketDetailResponseDto>> getMyTickets(
      @AuthenticationPrincipal Object principal) {
    
    List<TicketDetailResponseDto> tickets;
    
    if (principal instanceof ClientDetails clientDetails) {
      tickets = ticketService.getClientTicketsWithMessages(clientDetails.getId()).stream()
          .map(ticket -> {
            List<MessageDto> messageDtos = ticket.getMessages().stream()
                .map(m -> new MessageDto(
                    m.getAgent() == null ? "CLIENT" : "AGENT",
                    m.getAgent() == null ? ticket.getClient().getUsername() : m.getAgent().getName(),
                    m.getMessage(),
                    m.getCreatedAt().toString()))
                .toList();
            
            return new TicketDetailResponseDto(
                ticket.getId().toString(),
                ticket.getStatus().getValue(),
                ticket.getIssueType(),
                ticket.getClient().getUsername(),
                ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
                messageDtos,
                ticket.getCreatedAt().toString()
            );
          })
          .toList();
    } else if (principal instanceof AgentDetails agentDetails) {
      tickets = ticketService.getAgentTicketsWithMessages(agentDetails.getId()).stream()
          .map(ticket -> {
            List<MessageDto> messageDtos = ticket.getMessages().stream()
                .map(m -> new MessageDto(
                    m.getAgent() == null ? "CLIENT" : "AGENT",
                    m.getAgent() == null ? ticket.getClient().getUsername() : m.getAgent().getName(),
                    m.getMessage(),
                    m.getCreatedAt().toString()))
                .toList();
            
            return new TicketDetailResponseDto(
                ticket.getId().toString(),
                ticket.getStatus().getValue(),
                ticket.getIssueType(),
                ticket.getClient().getUsername(),
                ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
                messageDtos,
                ticket.getCreatedAt().toString()
            );
          })
          .toList();
    } else {
      return ResponseEntity.status(403).build();
    }
    
    return ResponseEntity.ok(tickets);
  }

  @PostMapping("/{ticketId}/status")
  public ResponseEntity<TicketDetailResponseDto> updateTicketStatus(
      @PathVariable UUID ticketId,
      @Valid @RequestBody UpdateTicketStatusRequestDto request,
      @AuthenticationPrincipal Object principal) {
    
    Ticket ticket;
    
    switch (request.getStatus().toUpperCase()) {
      case "IN_PROGRESS":
        // Only agents can set status to IN_PROGRESS (join ticket)
        if (!(principal instanceof AgentDetails agentDetails)) {
          return ResponseEntity.status(403).build();
        }
        ticket = ticketService.assignAgentToTicket(ticketId, agentDetails.getId());
        break;
        
      case "RESOLVED":
        // Only clients can set status to RESOLVED
        if (!(principal instanceof ClientDetails clientDetails)) {
          return ResponseEntity.status(403).build();
        }
        ticket = ticketService.resolveTicket(ticketId, clientDetails.getId());
        break;
        
      case "CLOSED":
        // Both agents and clients can close tickets
        if (principal instanceof ClientDetails clientDetails) {
          ticket = ticketService.closeTicket(ticketId, clientDetails.getId(), null);
        } else if (principal instanceof AgentDetails agentDetails) {
          ticket = ticketService.closeTicket(ticketId, null, agentDetails.getId());
        } else {
          return ResponseEntity.status(403).build();
        }
        break;
        
      default:
        return ResponseEntity.badRequest().build();
    }
    
    List<MessageDto> messageDtos = ticket.getMessages().stream()
        .map(m -> new MessageDto(
            m.getAgent() == null ? "CLIENT" : "AGENT",
            m.getAgent() == null ? ticket.getClient().getUsername() : m.getAgent().getName(),
            m.getMessage(),
            m.getCreatedAt().toString()))
        .toList();

    TicketDetailResponseDto response = new TicketDetailResponseDto(
        ticket.getId().toString(),
        ticket.getStatus().getValue(),
        ticket.getIssueType(),
        ticket.getClient().getUsername(),
        ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null,
        messageDtos,
        ticket.getCreatedAt().toString());
    return ResponseEntity.ok(response);
  }

}
