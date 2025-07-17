package com.ycyw.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDto {
  private String senderType;
  private String content;
  private String createdAt;
}
