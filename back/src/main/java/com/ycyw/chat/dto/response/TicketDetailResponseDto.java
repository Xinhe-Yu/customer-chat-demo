package com.ycyw.chat.dto.response;

import java.util.List;

import com.ycyw.chat.dto.MessageDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketDetailResponseDto {
  private String ticketId;
  private String status;
  private String issueType;
  private String clientName;
  private String agentName;
  private List<MessageDto> messages;
  private String createdAt;
}
