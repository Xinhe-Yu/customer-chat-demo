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

import com.ycyw.chat.dto.request.CreateTicketRequestDto;
import com.ycyw.chat.dto.request.UpdateTicketStatusRequestDto;
import com.ycyw.chat.dto.response.TicketDetailResponseDto;
import com.ycyw.chat.dto.response.TicketResponseDto;
import com.ycyw.chat.mappers.TicketMapper;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.services.ClientDetails;
import com.ycyw.chat.services.AgentDetails;
import com.ycyw.chat.services.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  private final TicketService ticketService;
  private final TicketMapper ticketMapper;

  public TicketController(TicketService ticketService, TicketMapper ticketMapper) {
    this.ticketService = ticketService;
    this.ticketMapper = ticketMapper;
  }

  @PostMapping
  public ResponseEntity<TicketResponseDto> createTicket(
      @Valid @RequestBody CreateTicketRequestDto request,
      @AuthenticationPrincipal ClientDetails clientDetails) {

    Ticket ticket = ticketService.createTicket(clientDetails.getId(), request.getIssueType());
    TicketResponseDto response = new TicketResponseDto(ticket.getId().toString());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<TicketDetailResponseDto>> getMyTickets(
      @AuthenticationPrincipal Object principal) {

    List<TicketDetailResponseDto> tickets;

    if (principal instanceof ClientDetails clientDetails) {
      List<Ticket> clientTickets = ticketService.getClientTicketsWithMessages(clientDetails.getId());
      tickets = ticketMapper.toDetailResponseDto(clientTickets);
    } else if (principal instanceof AgentDetails agentDetails) {
      List<Ticket> agentTickets = ticketService.getAgentTicketsWithMessages(agentDetails.getId());
      tickets = ticketMapper.toDetailResponseDto(agentTickets);
    } else {
      return ResponseEntity.status(403).build();
    }

    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/{ticketId}")
  public ResponseEntity<TicketDetailResponseDto> getTicket(@PathVariable UUID ticketId) {
    Ticket ticket = ticketService.getTicket(ticketId);
    TicketDetailResponseDto response = ticketMapper.toDetailResponseDto(ticket);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/available")
  public ResponseEntity<List<TicketDetailResponseDto>> getAvailableTickets() {
    List<Ticket> unassignedTickets = ticketService.getUnassignedTicketsWithMessages();
    List<TicketDetailResponseDto> tickets = ticketMapper.toDetailResponseDto(unassignedTickets);
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

    TicketDetailResponseDto response = ticketMapper.toDetailResponseDto(ticket);
    return ResponseEntity.ok(response);
  }

}
