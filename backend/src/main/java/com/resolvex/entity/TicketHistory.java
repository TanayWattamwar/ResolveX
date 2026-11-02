package com.resolvex.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="ticket_history")
public class TicketHistory {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Ticket ticket;
 @ManyToOne(optional=false) private User actor;
 @Column(nullable=false,length=1000) private String message;
 private LocalDateTime createdAt=LocalDateTime.now();
 protected TicketHistory(){}
 public TicketHistory(Ticket ticket,User actor,String message){this.ticket=ticket;this.actor=actor;this.message=message;}
 public Long getId(){return id;} public Ticket getTicket(){return ticket;} public User getActor(){return actor;} public String getMessage(){return message;} public LocalDateTime getCreatedAt(){return createdAt;}
}
