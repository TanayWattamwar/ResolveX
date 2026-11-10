package com.resolvex.controller;

import com.resolvex.dto.TicketCommentDtos.*;
import com.resolvex.service.TicketCommentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}")
public class TicketCommentController {
    private final TicketCommentService service;
    public TicketCommentController(TicketCommentService service) { this.service = service; }

    @GetMapping("/comments")
    public List<CommentResponse> list(@PathVariable Long ticketId, Authentication auth) {
        return service.list(ticketId, auth.getName());
    }

    @PostMapping("/comments")
    public CommentResponse add(@PathVariable Long ticketId, @Valid @RequestBody CreateCommentRequest request, Authentication auth) {
        return service.add(ticketId, request, auth.getName(), false);
    }

    @PostMapping("/notes")
    public CommentResponse note(@PathVariable Long ticketId, @Valid @RequestBody CreateCommentRequest request, Authentication auth) {
        return service.add(ticketId, request, auth.getName(), true);
    }
}
