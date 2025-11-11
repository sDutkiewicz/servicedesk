package com.servicedesk.dto;

import jakarta.validation.constraints.NotNull;

public record TicketAssignDto(
        @NotNull Long technicianId,
        String status // opcjonalna zmiana statusu przy przypisaniu
) {}
