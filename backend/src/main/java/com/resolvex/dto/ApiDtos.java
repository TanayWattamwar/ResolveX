package com.resolvex.dto;

import com.resolvex.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class ApiDtos {
    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank @Size(min=8) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, Long id, String name, String email, Role role) {}
    public record CreateTicketRequest(@NotBlank @Size(max=140) String title, @NotBlank @Size(max=4000) String description, @NotBlank String category, TicketPriority priority) {}
    public record UpdateTicketRequest(TicketStatus status, TicketPriority priority, Long assigneeId, LocalDateTime dueAt,
                                      @Size(max=4000) String resolutionNotes, @Size(max=2000) String rootCause,
                                      Boolean unassignAssignee) {}
    public record UserResponse(Long id, String name, String email, Role role) {}
    public record TicketResponse(Long id, String title, String description, String category, TicketPriority priority,
                                 TicketStatus status, UserResponse requester, UserResponse assignee, LocalDateTime createdAt,
                                 LocalDateTime updatedAt, LocalDateTime dueAt, LocalDateTime resolvedAt,
                                 String resolutionNotes, String rootCause, UserResponse resolvedBy) {}
    public record HistoryResponse(Long id, String actor, String message, LocalDateTime createdAt) {}
    public record DashboardResponse(long total, long open, long inProgress, long resolved, long overdue, long critical) {}
    public record CreateUserRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank @Size(min=8) String password, Role role) {}
}
