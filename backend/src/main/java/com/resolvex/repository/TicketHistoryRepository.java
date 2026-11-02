package com.resolvex.repository;
import com.resolvex.entity.TicketHistory; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketHistoryRepository extends JpaRepository<TicketHistory,Long>{List<TicketHistory> findByTicketIdOrderByCreatedAtDesc(Long id);}
