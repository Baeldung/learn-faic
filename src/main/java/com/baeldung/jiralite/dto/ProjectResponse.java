package com.baeldung.jiralite.dto;

import java.util.List;

public record ProjectResponse(Long id, String name, String description, List<Long> memberIds) {
}
