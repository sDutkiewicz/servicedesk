package com.servicedesk.controller;

import com.servicedesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {
    private final TicketService ticketService;

    // F5
    @GetMapping("/tickets-count")
    public List<Map<String,Object>> ticketsCount(@RequestParam String groupBy){
        // groupBy = department | technician
        return ticketService.countBy(groupBy);
    }

    // F6
    @GetMapping("/avg-resolution-time")
    public Map<String,Object> avgResolution(){
        return ticketService.avgResolution();
    }
}
