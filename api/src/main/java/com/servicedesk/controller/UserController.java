package com.servicedesk.controller;

import com.servicedesk.entity.User;
import com.servicedesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserRepository repo;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User u){ return repo.save(u); }

    @GetMapping
    public List<User> all(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id){ return repo.findById(id).orElseThrow(); }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User u){
        User existing = repo.findById(id).orElseThrow();
        existing.setFirstName(u.getFirstName());
        existing.setLastName(u.getLastName());
        existing.setEmail(u.getEmail());
        if (u.getDepartment() != null) existing.setDepartment(u.getDepartment());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}

