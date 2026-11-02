package com.resolvex.repository;
import com.resolvex.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketRepository extends JpaRepository<Ticket,Long>{List<Ticket> findByRequesterIdOrderByCreatedAtDesc(Long id); List<Ticket> findByAssigneeIdOrderByCreatedAtDesc(Long id); List<Ticket> findAllByOrderByCreatedAtDesc(); long countByStatus(TicketStatus status); long countByPriority(TicketPriority priority);}
