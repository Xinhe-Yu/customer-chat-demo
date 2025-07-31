package com.ycyw.chat.repositories;

import com.ycyw.chat.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
  List<Message> findByTicketId(UUID ticketId);

  boolean existsByTicketIdAndAgentIdIsNull(UUID ticketId);
  
  long countByTicketIdAndAgentIdIsNull(UUID ticketId);
}
