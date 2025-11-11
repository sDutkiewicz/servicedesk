package com.servicedesk.controller;

import com.servicedesk.entity.Technician;
import com.servicedesk.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/technicians") @RequiredArgsConstructor
public class TechnicianController {
    private final TechnicianRepository repo;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Technician create(@RequestBody Technician t){ return repo.save(t); }

    @GetMapping public List<Technician> all(){ return repo.findAll(); }
}
