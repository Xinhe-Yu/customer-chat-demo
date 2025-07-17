package com.ycyw.chat.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.ycyw.chat.dto.ClientDTO;
import com.ycyw.chat.models.Client;

@Component
@Mapper(componentModel = "spring")
public abstract class ClientMapper implements EntityMapper<ClientDTO, Client> {

  public abstract ClientDTO toDto(Client client);

  @Mapping(target = "password", ignore = true)
  public abstract Client toEntity(ClientDTO clientDTO);
}
