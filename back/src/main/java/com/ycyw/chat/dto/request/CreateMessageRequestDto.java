package com.ycyw.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequestDto {
  private String content; // message text
  private String senderType; // "CLIENT" or "AGENT"
}
