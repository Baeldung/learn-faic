package com.baeldung.jiralite.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 4000)
    private String body;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Comment() {}

    public Comment(Task task, User author, String body) {
        this.task = task;
        this.author = author;
        this.body = body;
    }

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public User getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
