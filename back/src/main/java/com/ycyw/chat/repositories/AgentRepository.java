package com.ycyw.chat.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ycyw.chat.models.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, UUID> {
  Optional<Agent> findById(UUID id);
}
