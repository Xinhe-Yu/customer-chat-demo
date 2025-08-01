package com.ycyw.chat.services;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.ycyw.chat.dto.MessageDto;
import com.ycyw.chat.dto.response.OpenTicketNotification;
import com.ycyw.chat.dto.response.TicketStatusUpdateDto;

@Service
public class NotifierService {
  private final SimpMessagingTemplate messagingTemplate;

  public NotifierService(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void notifyTicketMessage(UUID ticketId, MessageDto message) {
    messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, message);
  }

  public void notifyNewTicketToAgents(UUID ticketId, String issueType, String clientUsername) {
    OpenTicketNotification notification = new OpenTicketNotification(ticketId, issueType, clientUsername);
    messagingTemplate.convertAndSend("/topic/agent/open-tickets", notification);
  }

  public void notifyTicketStatusUpdate(TicketStatusUpdateDto statusUpdate) {
    messagingTemplate.convertAndSend("/topic/agent/ticket-status-updates", statusUpdate);
  }
}