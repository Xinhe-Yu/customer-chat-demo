package com.ycyw.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTicketRequestDto {
  @NotBlank
  private String issueType;
}
