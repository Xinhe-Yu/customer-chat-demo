package com.ycyw.chat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ycyw.chat.models.Client;
import com.ycyw.chat.repositories.ClientRepository;

import jakarta.transaction.Transactional;

@Service
public class ClientDetailsService implements UserDetailsService {
  @Autowired
  private ClientRepository clientRepository;

  @Override
  @Transactional
  public ClientDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
    return clientRepository.findByEmailOrUsername(loginIdentifier, loginIdentifier)
        .map(client -> ClientDetails.builder()
            .id(client.getId())
            .username(client.getUsername())
            .password(client.getPassword())
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
  }

  public Client getCurrentUser(String loginIdentifier) {
    return clientRepository.findByEmailOrUsername(loginIdentifier, loginIdentifier)
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
  }
}
