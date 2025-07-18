package com.ycyw.chat.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ycyw.chat.models.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

  Optional<Client> findByEmail(String email);

  Optional<Client> findByEmailOrUsername(String email, String username);

  Boolean existsByEmail(String email);

  Boolean existsByUsername(String username);

  Boolean existsByEmailAndIdNot(String email, UUID id);

  Boolean existsByUsernameAndIdNot(String username, UUID id);
}
