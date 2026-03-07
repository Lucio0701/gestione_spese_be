package com.gestionespese.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double amount;
    private String currency = "EUR";
    private LocalDate date;
    private String description;
    private String categoryId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ElementCollection
    private List<String> tags;

    @Enumerated(EnumType.STRING)
    private Recurrence recurrence = Recurrence.NONE;

    private Boolean isRecurrenceProcessed = false;

    public Expense() {
    }

    public Expense(User user, Double amount, LocalDate date, String description, String categoryId,
            TransactionType type, List<String> tags, Recurrence recurrence) {
        this.user = user;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
        this.type = type;
        this.tags = tags;
        this.recurrence = recurrence != null ? recurrence : Recurrence.NONE;
        this.isRecurrenceProcessed = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public Boolean getIsRecurrenceProcessed() {
        return isRecurrenceProcessed;
    }

    public void setIsRecurrenceProcessed(Boolean isRecurrenceProcessed) {
        this.isRecurrenceProcessed = isRecurrenceProcessed;
    }
}
