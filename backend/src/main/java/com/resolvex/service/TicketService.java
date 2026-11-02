package com.resolvex.service;

import com.resolvex.dto.ApiDtos.*;
import com.resolvex.entity.*;
import com.resolvex.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final UserRepository users;
    private final TicketHistoryRepository history;
    private final MailService mail;
    private final PasswordEncoder passwordEncoder;

    public TicketService(TicketRepository t, UserRepository u, TicketHistoryRepository h, MailService m, PasswordEncoder p) {
        tickets = t; users = u; history = h; mail = m; passwordEncoder = p;
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    private Ticket get(Long id) {
        return tickets.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
    private boolean isAdmin(User u) { return u.getRole() == Role.ADMIN; }
    private boolean isAssignedEngineer(User u, Ticket t) {
        return u.getRole() == Role.ENGINEER && t.getAssignee() != null && t.getAssignee().getId().equals(u.getId());
    }

    public List<TicketResponse> list(String email) {
        User u = user(email);
        List<Ticket> all = isAdmin(u) ? tickets.findAllByOrderByCreatedAtDesc()
                : u.getRole() == Role.ENGINEER ? tickets.findByAssigneeIdOrderByCreatedAtDesc(u.getId())
                : tickets.findByRequesterIdOrderByCreatedAtDesc(u.getId());
        return all.stream().map(this::dto).toList();
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest r, String email) {
        User actor = user(email);
        Ticket t = new Ticket();
        t.setTitle(r.title().trim()); t.setDescription(r.description().trim()); t.setCategory(r.category());
        t.setPriority(r.priority() == null ? TicketPriority.MEDIUM : r.priority()); t.setRequester(actor);
        LocalDateTime createdAt = LocalDateTime.now();
        t.setDueAt(createdAt.plusHours(slaHours(t.getPriority())));
        Ticket saved = tickets.save(t);
        history.save(new TicketHistory(saved, actor, "Ticket created"));
        history.save(new TicketHistory(saved, actor, "SLA deadline set to " + t.getDueAt() + " UTC (" + slaHours(t.getPriority()) + " hour target)"));
        mail.ticketCreated(saved);
        return dto(saved);
    }

    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest r, String email) {
        User actor = user(email);
        Ticket t = get(id);
        boolean admin = isAdmin(actor);
        boolean assignedEngineer = isAssignedEngineer(actor, t);
        if (!admin && !assignedEngineer) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only an administrator or the assigned engineer can update this ticket");
        }

        if (!admin && (r.assigneeId() != null || r.priority() != null || r.dueAt() != null)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Engineers cannot change assignment, priority or SLA deadlines");
        }

        if (r.assigneeId() != null) {
            User assignee = users.findById(r.assigneeId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
            if (assignee.getRole() != Role.ENGINEER) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must have ENGINEER role");
            t.setAssignee(assignee);
            history.save(new TicketHistory(t, actor, "Assigned to " + assignee.getName()));
        }
        if (r.priority() != null && r.priority() != t.getPriority()) {
            t.setPriority(r.priority()); history.save(new TicketHistory(t, actor, "Priority changed to " + r.priority()));
        }
        if (r.dueAt() != null && !r.dueAt().equals(t.getDueAt())) {
            t.setDueAt(r.dueAt()); history.save(new TicketHistory(t, actor, "SLA due date updated"));
        }
        if (r.status() != null && r.status() != t.getStatus()) {
            if ((r.status() == TicketStatus.RESOLVED || r.status() == TicketStatus.CLOSED)
                    && (r.resolutionNotes() == null || r.resolutionNotes().isBlank())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resolution notes are required before resolving or closing a ticket");
            }
            TicketStatus previous = t.getStatus();
            t.setStatus(r.status());
            if (r.status() == TicketStatus.RESOLVED || r.status() == TicketStatus.CLOSED) {
                t.setResolvedAt(LocalDateTime.now()); t.setResolvedBy(actor);
                t.setResolutionNotes(r.resolutionNotes().trim());
                t.setRootCause(r.rootCause() == null ? null : r.rootCause().trim());
                history.save(new TicketHistory(t, actor, "Resolution recorded: " + t.getResolutionNotes()));
                if (t.getRootCause() != null && !t.getRootCause().isBlank()) history.save(new TicketHistory(t, actor, "Root cause: " + t.getRootCause()));
            } else {
                t.setResolvedAt(null); t.setResolvedBy(null); t.setResolutionNotes(null); t.setRootCause(null);
            }
            history.save(new TicketHistory(t, actor, "Status changed from " + previous + " to " + r.status()));
        } else if (r.resolutionNotes() != null || r.rootCause() != null) {
            if (t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resolution details can only be added while resolving or after resolution");
            }
            if (r.resolutionNotes() != null) t.setResolutionNotes(r.resolutionNotes().trim());
            if (r.rootCause() != null) t.setRootCause(r.rootCause().trim());
            history.save(new TicketHistory(t, actor, "Resolution details updated"));
        }

        Ticket saved = tickets.save(t);
        mail.ticketUpdated(saved);
        return dto(saved);
    }

    public List<HistoryResponse> getHistory(Long id, String email) {
        User actor = user(email); Ticket t = get(id);
        if (!isAdmin(actor) && !t.getRequester().getId().equals(actor.getId()) && !isAssignedEngineer(actor, t))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this ticket history");
        return history.findByTicketIdOrderByCreatedAtDesc(id).stream()
                .map(h -> new HistoryResponse(h.getId(), h.getActor().getName(), h.getMessage(), h.getCreatedAt())).toList();
    }

    public List<UserResponse> engineers() {
        return users.findByRole(Role.ENGINEER).stream().map(this::userDto).toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest r) {
        if (r.role() != Role.ENGINEER && r.role() != Role.ADMIN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ENGINEER or ADMIN accounts can be created here");
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        User u = new User(); u.setName(r.name().trim()); u.setEmail(email);
        u.setPassword(passwordEncoder.encode(r.password())); u.setRole(r.role());
        return userDto(users.save(u));
    }

    public DashboardResponse dashboard(String email) {
        User u = user(email);
        List<Ticket> all = isAdmin(u) ? tickets.findAll()
                : u.getRole() == Role.ENGINEER ? tickets.findByAssigneeIdOrderByCreatedAtDesc(u.getId())
                : tickets.findByRequesterIdOrderByCreatedAtDesc(u.getId());
        return new DashboardResponse(all.size(), count(all, TicketStatus.OPEN), count(all, TicketStatus.IN_PROGRESS),
                count(all, TicketStatus.RESOLVED) + count(all, TicketStatus.CLOSED),
                all.stream().filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(LocalDateTime.now())
                        && t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED).count(),
                all.stream().filter(t -> t.getPriority() == TicketPriority.CRITICAL).count());
    }


    private int slaHours(TicketPriority priority) {
        return switch (priority) {
            case CRITICAL -> 4;
            case HIGH -> 8;
            case MEDIUM -> 24;
            case LOW -> 48;
        };
    }

    private long count(List<Ticket> a, TicketStatus s) { return a.stream().filter(t -> t.getStatus() == s).count(); }
    private UserResponse userDto(User u) { return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()); }
    private TicketResponse dto(Ticket t) {
        return new TicketResponse(t.getId(), t.getTitle(), t.getDescription(), t.getCategory(), t.getPriority(), t.getStatus(),
                userDto(t.getRequester()), t.getAssignee() == null ? null : userDto(t.getAssignee()), t.getCreatedAt(),
                t.getUpdatedAt(), t.getDueAt(), t.getResolvedAt(), t.getResolutionNotes(), t.getRootCause(),
                t.getResolvedBy() == null ? null : userDto(t.getResolvedBy()));
    }
}
