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

    @GetMapping
    public List<Technician> all(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public Technician getById(@PathVariable Long id){ return repo.findById(id).orElseThrow(); }

    @PutMapping("/{id}")
    public Technician update(@PathVariable Long id, @RequestBody Technician t){
        Technician existing = repo.findById(id).orElseThrow();
        existing.setFirstName(t.getFirstName());
        existing.setLastName(t.getLastName());
        existing.setEmail(t.getEmail());
        if (t.getDepartment() != null) existing.setDepartment(t.getDepartment());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
