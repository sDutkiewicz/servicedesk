package com.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketCreateDto(
        @NotBlank String title,
        String description,
        @NotNull Long reporterId,
        Long technicianId,
        Long categoryId,
        String priority // LOW/MEDIUM/HIGH/CRITICAL
) {}
