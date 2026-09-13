package com.exptrack.expense.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    // --- Default constructor for JPA ---------------------------------------------------------------------------------
    protected Budget() {}

    // --- Constructor for creating a new category ---------------------------------------------------------------------
    public Budget(UUID userId, UUID categoryId, Integer year, Integer month, BigDecimal amount) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    // --- Getters and setters -----------------------------------------------------------------------------------------

    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public UUID getCategoryId() {return categoryId;}

    public void setCategoryId(UUID categoryId) {this.categoryId = categoryId;}

    public Integer getYear() {return year;}

    public void setYear(Integer year) {this.year = year;}

    public Integer getMonth() {return month;}

    public void setMonth(Integer month) {this.month = month;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}
}
