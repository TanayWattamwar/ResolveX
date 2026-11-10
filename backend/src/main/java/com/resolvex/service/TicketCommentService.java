package com.resolvex.service;

import com.resolvex.dto.TicketCommentDtos.*;
import com.resolvex.entity.*;
import com.resolvex.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class TicketCommentService {
    private final TicketRepository tickets;
    private final UserRepository users;
    private final TicketCommentRepository comments;
    private final MailService mail;

    public TicketCommentService(TicketRepository tickets, UserRepository users, TicketCommentRepository comments, MailService mail) {
        this.tickets = tickets; this.users = users; this.comments = comments; this.mail = mail;
    }

    private User actor(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
    private Ticket ticket(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
    private boolean staff(User user) { return user.getRole() == Role.ADMIN || user.getRole() == Role.ENGINEER; }
    private boolean assignedEngineer(User user, Ticket ticket) {
        return user.getRole() == Role.ENGINEER && ticket.getAssignee() != null && ticket.getAssignee().getId().equals(user.getId());
    }
    private boolean canAccess(User user, Ticket ticket) {
        return user.getRole() == Role.ADMIN || ticket.getRequester().getId().equals(user.getId()) || assignedEngineer(user, ticket);
    }
    private boolean closed(Ticket ticket) { return ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED; }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long ticketId, String email) {
        User user = actor(email); Ticket ticket = ticket(ticketId);
        if (!canAccess(user, ticket)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this conversation");
        List<TicketComment> rows = staff(user) ? comments.findByTicketIdOrderByCreatedAtAsc(ticketId)
                : comments.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId);
        return rows.stream().map(this::dto).toList();
    }

    @Transactional
    public CommentResponse add(Long ticketId, CreateCommentRequest request, String email, boolean internal) {
        User user = actor(email); Ticket ticket = ticket(ticketId);
        if (!canAccess(user, ticket)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot participate in this conversation");
        if (internal && !staff(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only support staff can add internal notes");
        if (closed(ticket)) throw new ResponseStatusException(HttpStatus.CONFLICT, "This ticket is closed. Reopen it before sending a message.");
        String message = request.message().trim();
        if (message.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        TicketComment comment = new TicketComment(); comment.setTicket(ticket); comment.setSender(user); comment.setMessage(message); comment.setInternalNote(internal);
        TicketComment saved = comments.save(comment);
        if (!internal) mail.ticketCommentCreated(ticket, user, message);
        return dto(saved);
    }

    private CommentResponse dto(TicketComment c) {
        return new CommentResponse(c.getId(), c.getTicket().getId(), c.getSender().getId(), c.getSender().getName(),
                c.getSender().getRole().name(), c.getMessage(), c.isInternalNote(), c.getCreatedAt());
    }
}
