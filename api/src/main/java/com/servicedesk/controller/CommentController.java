package com.servicedesk.controller;

import com.servicedesk.dto.CommentCreateDto;
import com.servicedesk.entity.Comment;
import com.servicedesk.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/comments") @RequiredArgsConstructor
public class CommentController {
    private final CommentService service;

    // F4
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Comment add(@RequestBody CommentCreateDto dto){ return service.add(dto); }

    @GetMapping("/ticket/{ticketId}")
    public List<Comment> forTicket(@PathVariable Long ticketId){
        return service.forTicket(ticketId);
    }
}
