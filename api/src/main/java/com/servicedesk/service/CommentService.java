package com.servicedesk.service;

import com.servicedesk.dto.CommentCreateDto;
import com.servicedesk.entity.*;
import com.servicedesk.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class CommentService {
    private final CommentRepository commentRepo;
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;

    // F4 — dodawanie komentarza
    public Comment add(CommentCreateDto dto){
        Ticket t = ticketRepo.findById(dto.ticketId()).orElseThrow();
        User a = userRepo.findById(dto.authorId()).orElseThrow();
        Comment c = Comment.builder().ticket(t).author(a).content(dto.content()).build();
        return commentRepo.save(c);
    }

    @Transactional(readOnly = true)
    public List<Comment> forTicket(Long ticketId){
        return commentRepo.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}
