package com.ycyw.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
  private String senderType;
  private String senderName;
  private String content;
  private String createdAt;
}
