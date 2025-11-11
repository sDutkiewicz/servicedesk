package com.servicedesk.controller;

import com.servicedesk.entity.Category;
import com.servicedesk.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository repo;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Category create(@RequestBody Category c){ return repo.save(c); }

    @GetMapping
    public List<Category> all(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id){ return repo.findById(id).orElseThrow(); }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category c){
        Category existing = repo.findById(id).orElseThrow();
        existing.setName(c.getName());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
