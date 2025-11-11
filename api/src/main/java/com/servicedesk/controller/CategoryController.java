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

    @GetMapping public List<Category> all(){ return repo.findAll(); }
}
