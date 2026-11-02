package com.resolvex.controller;
import com.resolvex.dto.ApiDtos.*;import com.resolvex.service.TicketService;import jakarta.validation.Valid;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api") public class TicketController{
 private final TicketService service;public TicketController(TicketService s){service=s;}
 @GetMapping("/tickets") public List<TicketResponse> list(Authentication a){return service.list(a.getName());}
 @PostMapping("/tickets") public TicketResponse create(@Valid @RequestBody CreateTicketRequest r,Authentication a){return service.create(r,a.getName());}
 @PatchMapping("/tickets/{id}") public TicketResponse update(@PathVariable Long id,@Valid @RequestBody UpdateTicketRequest r,Authentication a){return service.update(id,r,a.getName());}
 @GetMapping("/tickets/{id}/history") public List<HistoryResponse> history(@PathVariable Long id,Authentication a){return service.getHistory(id,a.getName());}
 @GetMapping("/dashboard") public DashboardResponse dashboard(Authentication a){return service.dashboard(a.getName());}
 @GetMapping("/engineers") @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','ENGINEER')") public List<UserResponse> engineers(){return service.engineers();}
 @PostMapping("/admin/users") @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public UserResponse createUser(@Valid @RequestBody CreateUserRequest r){return service.createUser(r);}
}
