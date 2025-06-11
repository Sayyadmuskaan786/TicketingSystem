package com.example.formbackend.repository;

import com.example.formbackend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByAssignedAgentId(Long agentId);

    @Query("SELECT t FROM Ticket t WHERE t.createdBy.id = :customerId")
    List<Ticket> findByCreatedById(@Param("customerId") Long customerId);

    List<Ticket> findByState(Ticket.State state);

    @Query("SELECT COUNT(t) FROM Ticket t")
    long countAllTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent.id = :agentId")
    long countTicketsByAgentId(@Param("agentId") Long agentId);

    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.assignedAgent LEFT JOIN FETCH t.createdBy")
    List<Ticket> findAllWithAgents();

    @Query("SELECT t FROM Ticket t")
    List<Ticket> getAllTickets();
}
