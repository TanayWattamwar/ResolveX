package com.resolvex.repository;

import com.resolvex.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(Long ticketId);
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
