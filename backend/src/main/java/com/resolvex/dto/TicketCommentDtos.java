package com.resolvex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class TicketCommentDtos {
    public record CreateCommentRequest(@NotBlank @Size(max = 4000) String message) {}
    public record CommentResponse(Long id, Long ticketId, Long senderId, String senderName, String senderRole,
                                  String message, boolean internalNote, LocalDateTime createdAt) {}
}
