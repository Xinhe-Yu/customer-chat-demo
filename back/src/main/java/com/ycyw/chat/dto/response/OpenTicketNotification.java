package com.ycyw.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenTicketNotification {
  private UUID ticketId;
  private String issueType;
  private String clientUsername;
}
