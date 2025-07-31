package com.ycyw.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequestDto {
    @NotBlank(message = "Status is required")
    private String status; // "IN_PROGRESS", "RESOLVED", "CLOSED"
}