package com.ycyw.chat.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.ycyw.chat.dto.MessageDto;
import com.ycyw.chat.dto.response.TicketDetailResponseDto;
import com.ycyw.chat.models.Ticket;

@Component
@Mapper(componentModel = "spring", uses = {MessageMapper.class})
public abstract class TicketMapper {

  @Mapping(target = "id", expression = "java(ticket.getId().toString())")
  @Mapping(target = "status", expression = "java(ticket.getStatus().getValue())")
  @Mapping(target = "issueType", source = "issueType")
  @Mapping(target = "clientUsername", expression = "java(ticket.getClient().getUsername())")
  @Mapping(target = "agentName", expression = "java(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getName() : null)")
  @Mapping(target = "messages", source = "messages")
  @Mapping(target = "createdAt", expression = "java(ticket.getCreatedAt().toString())")
  public abstract TicketDetailResponseDto toDetailResponseDto(Ticket ticket);

  public abstract List<TicketDetailResponseDto> toDetailResponseDto(List<Ticket> tickets);

  public abstract List<MessageDto> messagesToDto(List<com.ycyw.chat.models.Message> messages);
}