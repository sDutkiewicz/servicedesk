package com.servicedesk.controller;

import com.servicedesk.entity.Department;
import com.servicedesk.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/departments") @RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentRepository repo;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Department create(@RequestBody Department d){ return repo.save(d); }

    @GetMapping
    public List<Department> all(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public Department getById(@PathVariable Long id){ return repo.findById(id).orElseThrow(); }

    @PutMapping("/{id}")
    public Department update(@PathVariable Long id, @RequestBody Department d){
        Department existing = repo.findById(id).orElseThrow();
        existing.setName(d.getName());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
