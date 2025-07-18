package com.ycyw.chat.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ycyw.chat.models.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  @Query("SELECT t FROM Ticket t JOIN FETCH t.client WHERE t.id = :ticketId")
  Optional<Ticket> findByIdWithClient(@Param("ticketId") UUID ticketId);

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages ORDER BY t.createdAt DESC")
  List<Ticket> findAllWithMessages();

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages WHERE t.client.id = :clientId ORDER BY t.createdAt DESC")
  List<Ticket> findByClientIdWithMessages(@Param("clientId") UUID clientId);

}
