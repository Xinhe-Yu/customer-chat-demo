package com.ycyw.chat.controllers;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.ycyw.chat.dto.MessageDto;
import com.ycyw.chat.dto.request.CreateMessageRequestDto;
import com.ycyw.chat.models.Message;
import com.ycyw.chat.models.Ticket;
import com.ycyw.chat.services.MessageService;
import com.ycyw.chat.services.NotifierService;
import com.ycyw.chat.services.TicketService;

@Controller
public class MessageController {
  private final NotifierService notifierService;
  private final MessageService messageService;
  private final TicketService ticketService;

  public MessageController(NotifierService notifierService,
      MessageService messageService,
      TicketService ticketService) {
    this.notifierService = notifierService;
    this.messageService = messageService;
    this.ticketService = ticketService;
  }

  @MessageMapping("/tickets/{ticketId}/messages")
  public void handleMessage(@DestinationVariable UUID ticketId, CreateMessageRequestDto inDto,
      Principal principal) {
    Message saved = messageService.addMessage(ticketId, inDto, principal);

    MessageDto outDto = new MessageDto(
        inDto.getSenderType(),
        saved.getAgent() == null ? saved.getTicket().getClient().getUsername() : saved.getAgent().getName(),
        inDto.getContent(),
        saved.getCreatedAt().toString());

    notifierService.notifyTicketMessage(ticketId, outDto);

    if ("CLIENT".equals(inDto.getSenderType()) && messageService.isFirstClientMessage(ticketId)) {
      Ticket ticket = ticketService.getTicketWithClient(ticketId);
      String clientUsername = ticket.getClient() != null ? ticket.getClient().getUsername() : "Unknown";
      notifierService.notifyNewTicketToAgents(ticketId, ticket.getIssueType(), clientUsername);
    }
  }
}
