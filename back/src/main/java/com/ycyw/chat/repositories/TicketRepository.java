package com.ycyw.chat.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ycyw.chat.models.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

}
