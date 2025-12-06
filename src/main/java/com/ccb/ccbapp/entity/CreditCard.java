package com.ccb.ccbapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a credit card.
 * Each credit card belongs to one user (ManyToOne relationship).
 */
@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String cardNumber; // Should be encrypted in production

    @Column(nullable = false, length = 255)
    private String cardHolderName;

    @Column(nullable = false)
    private String expiryDate; // Format: MM/YY

    // WARNING: Never store CVV in production! Only use for validation
    @Transient // This field won't be persisted to database
    private String cvv;

    @Column(nullable = false, length = 50)
    private String cardType; // VISA, MASTERCARD, AMEX, etc.

    @Column(length = 100)
    private String cardName; // Optional: User-defined card nickname

    // ManyToOne relationship: Many credit cards belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_credit_card_user"))
    private User user;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public CreditCard() {
    }

    public CreditCard(String cardNumber, String cardHolderName, String expiryDate, String cardType, User user) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cardType = cardType;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CreditCard{" +
                "id=" + id +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardType='" + cardType + '\'' +
                ", cardName='" + cardName + '\'' +
                ", maskedCardNumber='****"
                + (cardNumber != null && cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4)
                        : "****")
                + '\'' +
                '}';
    }
}
