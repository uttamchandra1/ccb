package com.ccb.ccbapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * DTO representing credit card metadata extracted from emails.
 * Contains only safe, non-sensitive information.
 */
public class CardMetadataDTO {

    private String bankName;

    @NotBlank(message = "Card type is required")
    private String cardType; // VISA, MASTERCARD, AMEX, RUPAY

    @NotBlank(message = "Last 4 digits are required")
    @Pattern(regexp = "^\\d{4}$", message = "Last 4 digits must be exactly 4 digits")
    private String last4Digits;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry must be in MM/YY format")
    private String expiryDate; // MM/YY format

    private String cardNickname; // User-friendly name (e.g., "HDFC Regalia")

    // Metadata for tracking
    private String emailSubject;
    private String senderEmail;
    private LocalDateTime detectedAt;
    private Double confidenceScore; // 0.0 to 1.0
    private boolean userConfirmed = false;

    // Constructors
    public CardMetadataDTO() {
        this.detectedAt = LocalDateTime.now();
    }

    public CardMetadataDTO(String bankName, String cardType, String last4Digits, String expiryDate) {
        this.bankName = bankName;
        this.cardType = cardType;
        this.last4Digits = last4Digits;
        this.expiryDate = expiryDate;
        this.detectedAt = LocalDateTime.now();
    }

    // Validation helper
    public boolean isValid() {
        // Valid if we have last 4 digits, OR if we have a card nickname (from statement
        // subject)
        boolean hasIdentifier = (last4Digits != null && !last4Digits.isEmpty()) ||
                (cardNickname != null && !cardNickname.isEmpty());

        // Expiry date format validation if present
        boolean validExpiry = (expiryDate == null || expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$"));

        return hasIdentifier && validExpiry;
    }

    // Getters and Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardNickname() {
        return cardNickname;
    }

    public void setCardNickname(String cardNickname) {
        this.cardNickname = cardNickname;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public boolean isUserConfirmed() {
        return userConfirmed;
    }

    public void setUserConfirmed(boolean userConfirmed) {
        this.userConfirmed = userConfirmed;
    }

    @Override
    public String toString() {
        return "CardMetadataDTO{" +
                "bankName='" + bankName + '\'' +
                ", cardType='" + cardType + '\'' +
                ", last4Digits='****" + last4Digits + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", cardNickname='" + cardNickname + '\'' +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}
