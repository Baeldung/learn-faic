package com.baeldung.jiralite.project;

import java.util.List;

public record ProjectResponse(Long id, String name, String description, List<Long> memberIds) { }
