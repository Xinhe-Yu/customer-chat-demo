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
import com.ycyw.chat.dto.response.TicketDetailResponseDto;
import com.ycyw.chat.dto.response.TicketResponseDto;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.services.ClientDetails;
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
            m.getAgentId() == null ? "CLIENT" : "AGENT",
            m.getMessage(),
            m.getCreatedAt().toString()))
        .toList();

    TicketDetailResponseDto response = new TicketDetailResponseDto(
        ticket.getId().toString(),
        ticket.getStatus(),
        ticket.getIssueType(),
        messageDtos);
    return ResponseEntity.ok(response);
  }
}
