package com.ycyw.chat.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ycyw.chat.dto.response.TicketDetailResponseDto;
import com.ycyw.chat.models.Ticket;

@Mapper(componentModel = "spring", uses = { MessageMapper.class })
public abstract class TicketMapper {

  @Mapping(target = "ticketId", expression = "java(ticket.getId().toString())")
  @Mapping(target = "status", expression = "java(ticket.getStatus().getValue())")
  @Mapping(target = "issueType", source = "issueType")
  @Mapping(target = "clientName", expression = "java(ticket.getClient().getUsername())")
  @Mapping(target = "agentName", expression = "java(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null)")
  @Mapping(target = "messages", source = "messages")
  @Mapping(target = "createdAt", expression = "java(ticket.getCreatedAt().toString())")
  public abstract TicketDetailResponseDto toDetailResponseDto(Ticket ticket);

  public abstract List<TicketDetailResponseDto> toDetailResponseDto(List<Ticket> tickets);
}
