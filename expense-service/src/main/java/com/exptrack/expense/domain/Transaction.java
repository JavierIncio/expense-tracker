package com.exptrack.expense.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "description")
    private String description;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- Default constructor for JPA ---------------------------------------------------------------------------------
    protected Transaction() {}

    // --- Constructor for creating a new category ---------------------------------------------------------------------
    public Transaction(UUID userId, TransactionType type, BigDecimal amount, UUID categoryId, LocalDate date) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
    }

    // --- Getters and setters -----------------------------------------------------------------------------------------
    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public TransactionType getType() {return type;}

    public void setType(TransactionType type) {this.type = type;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public UUID getCategoryId() {return categoryId;}

    public void setCategoryId(UUID categoryId) {this.categoryId = categoryId;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public LocalDate getDate() {return date;}

    public void setDate(LocalDate date) {this.date = date;}

    public Instant getCreatedAt() {return createdAt;}

    public void setCreatedAt(Instant createdAt) {this.createdAt = createdAt;}
}
