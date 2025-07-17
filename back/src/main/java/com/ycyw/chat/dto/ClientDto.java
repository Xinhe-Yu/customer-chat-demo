package com.ycyw.chat.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
  private UUID id;
  private String email;
  private String username;
  private Map<String, Object> clientData;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
