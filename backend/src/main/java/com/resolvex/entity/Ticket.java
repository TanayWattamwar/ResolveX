package com.resolvex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(optional = false) @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne @JoinColumn(name = "assignee_id")
    private User assignee;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime dueAt;
    private LocalDateTime resolvedAt;

    @Column(length = 4000)
    private String resolutionNotes;

    @Column(length = 2000)
    private String rootCause;

    @ManyToOne @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String v) { title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category = v; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority v) { priority = v; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus v) { status = v; }
    public User getRequester() { return requester; }
    public void setRequester(User v) { requester = v; }
    public User getAssignee() { return assignee; }
    public void setAssignee(User v) { assignee = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime v) { dueAt = v; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v) { resolvedAt = v; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String v) { resolutionNotes = v; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String v) { rootCause = v; }
    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User v) { resolvedBy = v; }
}
