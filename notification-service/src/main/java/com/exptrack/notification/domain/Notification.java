package com.exptrack.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "read", nullable = false)
    private boolean read;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Default constructor for JPA ---------------------------------------------------------------------------------
    protected Notification() {}

    // --- Constructor for creating a new category ---------------------------------------------------------------------
    public Notification(UUID userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    // --- Getters and setters -----------------------------------------------------------------------------------------
    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public boolean getRead() {return read;}

    public void setRead(boolean read) {this.read = read;}

    public Instant getCreatedAt() {return createdAt;}

    public void setCreatedAt(Instant createdAt) {this.createdAt = createdAt;}
}