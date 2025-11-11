package com.servicedesk.service;

import com.servicedesk.dto.*;
import com.servicedesk.entity.*;
import com.servicedesk.entity.Ticket.*;
import com.servicedesk.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class TicketService {

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final TechnicianRepository techRepo;
    private final CategoryRepository categoryRepo;

    // F1 — utworzenie zgłoszenia
    public Ticket create(TicketCreateDto dto){
        User reporter = userRepo.findById(dto.reporterId()).orElseThrow();
        Technician tech = dto.technicianId()==null ? null : techRepo.findById(dto.technicianId()).orElseThrow();
        Category cat = dto.categoryId()==null ? null : categoryRepo.findById(dto.categoryId()).orElseThrow();

        Ticket t = Ticket.builder()
                .title(dto.title())
                .description(dto.description())
                .reporter(reporter)
                .technician(tech)
                .category(cat)
                .priority(dto.priority()==null? Priority.MEDIUM : Priority.valueOf(dto.priority()))
                .status(Status.OPEN)
                .build();
        return ticketRepo.save(t);
    }

    // F2 — przypisanie + zmiana statusu
    public Ticket assign(Long ticketId, TicketAssignDto dto){
        Ticket t = ticketRepo.findById(ticketId).orElseThrow();
        t.setTechnician(techRepo.findById(dto.technicianId()).orElseThrow());
        if (dto.status()!=null) {
            Status newStatus = Status.valueOf(dto.status());
            t.setStatus(newStatus);
            if (newStatus==Status.CLOSED || newStatus==Status.RESOLVED) {
                t.setClosedAt(LocalDateTime.now());
            }
        }
        return t;
    }

    // F2 — szybka zmiana statusu
    public Ticket setStatus(Long id, String status){
        Ticket t = ticketRepo.findById(id).orElseThrow();
        Status s = Status.valueOf(status);
        t.setStatus(s);
        if (s==Status.CLOSED || s==Status.RESOLVED) t.setClosedAt(LocalDateTime.now());
        return t;
    }

    // F3 — filtrowanie po status/technician/department
    @Transactional(readOnly = true)
    public List<Ticket> search(String status, Long technicianId, Long departmentId){
        Specification<Ticket> spec = Specification.where(null);

        if (status!=null) {
            Status st = Status.valueOf(status);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), st));
        }
        if (technicianId!=null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("technician").get("id"), technicianId));
        }
        if (departmentId!=null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("reporter").get("department").get("id"), departmentId));
        }
        return ticketRepo.findAll(spec);
    }

    @Transactional(readOnly = true)
    public List<Ticket> all(){ return ticketRepo.findAll(); }

    @Transactional(readOnly = true)
    public Ticket getById(Long id){ return ticketRepo.findById(id).orElseThrow(); }

    // Pobierz zgłoszenia dla konkretnego użytkownika (tylko jego zgłoszenia)
    @Transactional(readOnly = true)
    public List<Ticket> getByReporterId(Long reporterId) {
        return ticketRepo.findAll().stream()
                .filter(t -> t.getReporter() != null && t.getReporter().getId().equals(reporterId))
                .toList();
    }

    // Pobierz zgłoszenia dla konkretnego technika (przypisane do niego)
    @Transactional(readOnly = true)
    public List<Ticket> getByTechnicianId(Long technicianId) {
        return ticketRepo.findAll().stream()
                .filter(t -> t.getTechnician() != null && t.getTechnician().getId().equals(technicianId))
                .toList();
    }

    // F5 — raport: liczba otwartych/zamkniętych wg działu/technika
    @Transactional(readOnly = true)
    public List<Map<String,Object>> countBy(String groupBy){
        // prosto w pamięci, czytelnie
        List<Ticket> tickets = ticketRepo.findAll();
        Map<String,int[]> agg = new LinkedHashMap<>();
        for (Ticket t : tickets){
            String key = switch (groupBy){
                case "department" -> Optional.ofNullable(t.getReporter().getDepartment()).map(Department::getName).orElse("—");
                case "technician" -> Optional.ofNullable(t.getTechnician()).map(x -> x.getFirstName()+" "+x.getLastName()).orElse("—");
                default -> "—";
            };
            agg.putIfAbsent(key, new int[]{0,0});
            if (t.getStatus()==Status.CLOSED || t.getStatus()==Status.RESOLVED) agg.get(key)[1]++; else agg.get(key)[0]++;
        }
        List<Map<String,Object>> out = new ArrayList<>();
        agg.forEach((k,v) -> out.add(Map.of("group",k,"open",v[0],"closed",v[1])));
        return out;
    }

    // F6 — średni czas rozwiązania
    @Transactional(readOnly = true)
    public Map<String,Object> avgResolution(){
        List<Ticket> closed = ticketRepo.findAll().stream()
                .filter(t -> t.getClosedAt()!=null && t.getCreatedAt()!=null).toList();
        if (closed.isEmpty()) return Map.of("avgHours", 0, "count", 0);
        double avgHours = closed.stream()
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getClosedAt()).toHours())
                .average().orElse(0);
        return Map.of("avgHours", avgHours, "count", closed.size());
    }
}
