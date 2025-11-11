package com.servicedesk.controller;

import com.servicedesk.dto.*;
import com.servicedesk.entity.Ticket;
import com.servicedesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tickets") @RequiredArgsConstructor
public class TicketController {
    private final TicketService service;

    // F1
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Ticket create(@RequestBody TicketCreateDto dto){ return service.create(dto); }

    // F2
    @PostMapping("/{id}/assign")
    public Ticket assign(@PathVariable Long id, @RequestBody TicketAssignDto dto){
        return service.assign(id, dto);
    }

    // F2 – zmiana statusu
    @PostMapping("/{id}/status/{status}")
    public Ticket setStatus(@PathVariable Long id, @PathVariable String status){
        return service.setStatus(id, status);
    }

    // F3 – listowanie + filtry
    @GetMapping
    public List<Ticket> search(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Long technicianId,
                               @RequestParam(required = false) Long departmentId){
        if (status==null && technicianId==null && departmentId==null) return service.all();
        return service.search(status, technicianId, departmentId);
    }

    // Endpoint dla użytkownika - tylko jego zgłoszenia
    @GetMapping("/my")
    public List<Ticket> getMyTickets(@RequestParam Long userId){
        return service.getByReporterId(userId);
    }

    // Endpoint dla technika - zgłoszenia przypisane do niego
    @GetMapping("/assigned")
    public List<Ticket> getAssignedTickets(@RequestParam Long technicianId){
        return service.getByTechnicianId(technicianId);
    }

    // Get single ticket by ID - MUSI BYĆ PO /assigned i /my!
    @GetMapping("/{id}")
    public Ticket getById(@PathVariable Long id){
        return service.getById(id);
    }
}
