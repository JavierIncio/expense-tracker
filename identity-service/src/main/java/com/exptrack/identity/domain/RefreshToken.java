package com.exptrack.identity.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Default constructor for JPA ---------------------------------------------------------------------------------
    public RefreshToken() {}

    // --- Getters and setters -----------------------------------------------------------------------------------------

    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public String getTokenHash() {return tokenHash;}

    public void setTokenHash(String tokenHash) {this.tokenHash = tokenHash;}

    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public Instant getExpiresAt() {return expiresAt;}

    public void setExpiresAt(Instant expiresAt) {this.expiresAt = expiresAt;}

    public boolean isRevoked() {return revoked;}

    public void setRevoked(boolean revoked) {this.revoked = revoked;}

    public Instant getCreatedAt() {return createdAt;}

    public void setCreatedAt(Instant createdAt) {this.createdAt = createdAt;}
}
