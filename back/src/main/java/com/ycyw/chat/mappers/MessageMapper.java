package com.ycyw.chat.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ycyw.chat.dto.MessageDto;
import com.ycyw.chat.models.Message;

@Mapper(componentModel = "spring")
public abstract class MessageMapper implements EntityMapper<MessageDto, Message> {

  @Mapping(target = "senderType", expression = "java(message.getAgent() == null ? \"CLIENT\" : \"AGENT\")")
  @Mapping(target = "senderName", expression = "java(message.getAgent() == null ? message.getTicket().getClient().getUsername() : message.getAgent().getName())")
  @Mapping(target = "content", source = "message")
  @Mapping(target = "createdAt", expression = "java(message.getCreatedAt().toString())")
  public abstract MessageDto toDto(Message message);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "ticket", ignore = true)
  @Mapping(target = "agent", ignore = true)
  @Mapping(target = "message", source = "content")
  @Mapping(target = "createdAt", ignore = true)
  public abstract Message toEntity(MessageDto messageDto);
}
