package com.ycyw.chat.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ycyw.chat.models.Agent;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
  Optional<Agent> findById(UUID id);
}
