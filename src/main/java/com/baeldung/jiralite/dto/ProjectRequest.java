package com.baeldung.jiralite.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
    @NotBlank String name,
    String description
) {}
