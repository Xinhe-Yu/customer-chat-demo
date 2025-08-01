package com.ycyw.chat.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ycyw.chat.dto.ClientDto;
import com.ycyw.chat.models.Client;

@Mapper(componentModel = "spring")
public abstract class ClientMapper implements EntityMapper<ClientDto, Client> {

  public abstract ClientDto toDto(Client client);

  @Mapping(target = "password", ignore = true)
  public abstract Client toEntity(ClientDto clientDTO);
}
