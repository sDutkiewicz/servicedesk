package com.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateDto(
        @NotNull Long ticketId,
        @NotNull Long authorId,
        @NotBlank String content
) {}
