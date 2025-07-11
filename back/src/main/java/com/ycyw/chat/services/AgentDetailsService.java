package com.ycyw.chat.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ycyw.chat.repositories.AgentRepository;

import jakarta.transaction.Transactional;

@Service
public class AgentDetailsService implements UserDetailsService {
  @Autowired
  private AgentRepository agentRepository;

  @Override
  @Transactional
  public AgentDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    return agentRepository.findById(UUID.fromString(id))
        .map(agent -> AgentDetails.builder()
            .id(agent.getId())
            .username(agent.getId().toString())
            .password(agent.getSecret())
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("Agent not found"));
  }
}
