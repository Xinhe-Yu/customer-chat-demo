package com.ycyw.chat.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ycyw.chat.models.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  @Query("SELECT t FROM Ticket t JOIN FETCH t.client WHERE t.id = :ticketId")
  Optional<Ticket> findByIdWithClient(@Param("ticketId") UUID ticketId);

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages ORDER BY t.createdAt DESC")
  List<Ticket> findAllWithMessages();

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages WHERE t.client.id = :clientId ORDER BY t.createdAt DESC")
  List<Ticket> findByClientIdWithMessages(@Param("clientId") UUID clientId);

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages WHERE t.status = 'IN_PROGRESS' AND t.assignedAgent.id = :agentId ORDER BY t.createdAt DESC")
  List<Ticket> findByAssignedAgentIdWithMessages(@Param("agentId") UUID agentId);

  @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.assignedAgent IS NULL")
  Optional<Ticket> findUnassignedTicketById(@Param("ticketId") UUID ticketId);

  @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.messages WHERE t.assignedAgent IS NULL ORDER BY t.createdAt DESC")
  List<Ticket> findUnassignedTicketsWithMessages();

}
