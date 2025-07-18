package com.ycyw.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusUpdateDto {
    private UUID ticketId;
    private String status;
    private UUID agentId;
    private String agentName;
}