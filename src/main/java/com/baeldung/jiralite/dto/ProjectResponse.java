package com.baeldung.jiralite.dto;

import java.util.Set;

public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private Set<Long> memberIds;

    public ProjectResponse(Long id, String name, String description, Set<Long> memberIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberIds = memberIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
