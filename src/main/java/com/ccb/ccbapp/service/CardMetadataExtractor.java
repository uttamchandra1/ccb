package com.ccb.ccbapp.service;

import com.ccb.ccbapp.dto.CardMetadataDTO;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for extracting credit card metadata from email content.
 * Uses regex patterns and NLP techniques to identify card information.
 */
@Service
public class CardMetadataExtractor {

    // Regex patterns for card information extraction
    private static final Pattern LAST_4_DIGITS = Pattern.compile(
            "(?:ending in|last 4 digits|XXXX|\\*{4,}|ending with|card number ending|card no\\.?\\s*ending|\\*{12})(\\s*[:\\-]?\\s*|\\s+)(\\d{4})",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EXPIRY_DATE = Pattern.compile(
            "(?:expiry|expires|valid till|valid thru|valid until|expiration|exp\\.?\\s*date)\\s*[:\\-]?\\s*(\\d{2})[/\\-](\\d{2,4})",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CARD_TYPE = Pattern.compile(
            "\\b(Visa|Mastercard|Master Card|American Express|Amex|Discover|RuPay|Diners|Diners Club)\\b",
            Pattern.CASE_INSENSITIVE);

    // Pattern to extract card name from subject line (e.g., "HDFC Bank - Diners
    // Privilege Credit Card Statement")
    private static final Pattern CARD_NAME_FROM_SUBJECT = Pattern.compile(
            "(?:HDFC Bank|ICICI Bank|SBI|Axis Bank|Kotak)\\s*-\\s*([A-Za-z\\s]+(?:Credit|Debit)\\s*Card)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CARD_NICKNAME = Pattern.compile(
            "(?:card name|card type|product|card variant)\\s*[:\\-]?\\s*([A-Za-z\\s]+(?:Credit|Debit|Platinum|Gold|Silver|Signature|Regalia|Titanium|Rewards|Cashback|Privilege)[A-Za-z\\s]*)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Extract card metadata from email body
     */
    public CardMetadataDTO extractMetadata(String emailBody) {
        return extractMetadata(emailBody, null);
    }

    /**
     * Extract card metadata from email body and subject
     */
    public CardMetadataDTO extractMetadata(String emailBody, String emailSubject) {
        if (emailBody == null || emailBody.isEmpty()) {
            return null;
        }

        CardMetadataDTO metadata = new CardMetadataDTO();
        double confidenceScore = 0.0;

        // Extract last 4 digits (group 3 because we have 2 groups before the digits)
        Matcher last4Matcher = LAST_4_DIGITS.matcher(emailBody);
        if (last4Matcher.find()) {
            String last4 = last4Matcher.group(3);
            metadata.setLast4Digits(last4);
            confidenceScore += 0.4;
            System.out.println("✅ Extracted last 4 digits: " + last4);
        } else {
            System.out.println("❌ Could not find last 4 digits in email body");
        }

        // Extract expiry date
        Matcher expiryMatcher = EXPIRY_DATE.matcher(emailBody);
        if (expiryMatcher.find()) {
            String month = expiryMatcher.group(1);
            String year = expiryMatcher.group(2);

            // Normalize year to YY format
            if (year.length() == 4) {
                year = year.substring(2);
            }

            metadata.setExpiryDate(month + "/" + year);
            confidenceScore += 0.3;
            System.out.println("✅ Extracted expiry date: " + month + "/" + year);
        } else {
            System.out.println("❌ Could not find expiry date in email body");
        }

        // Extract card type
        Matcher cardTypeMatcher = CARD_TYPE.matcher(emailBody);
        if (cardTypeMatcher.find()) {
            String type = normalizeCardType(cardTypeMatcher.group(1));
            metadata.setCardType(type);
            confidenceScore += 0.2;
            System.out.println("✅ Extracted card type: " + type);
        } else {
            System.out.println("❌ Could not find card type in email body");
        }

        // Try to extract card name from subject line first
        if (emailSubject != null && !emailSubject.isEmpty()) {
            System.out.println("📧 Email subject: " + emailSubject);
            Matcher subjectMatcher = CARD_NAME_FROM_SUBJECT.matcher(emailSubject);
            if (subjectMatcher.find()) {
                String cardName = subjectMatcher.group(1).trim();
                metadata.setCardNickname(cleanNickname(cardName));
                confidenceScore += 0.15;
                System.out.println("✅ Extracted card name from subject: " + cardName);
            } else {
                System.out.println("❌ Could not extract card name from subject");
            }
        }

        // Extract card nickname from body if not found in subject
        if (metadata.getCardNickname() == null) {
            Matcher nicknameMatcher = CARD_NICKNAME.matcher(emailBody);
            if (nicknameMatcher.find()) {
                String nickname = nicknameMatcher.group(1).trim();
                metadata.setCardNickname(cleanNickname(nickname));
                confidenceScore += 0.1;
                System.out.println("✅ Extracted card nickname from body: " + nickname);
            }
        }

        metadata.setConfidenceScore(Math.min(confidenceScore, 1.0));

        System.out.println("📊 Final metadata - Valid: " + metadata.isValid() +
                ", Last4: " + metadata.getLast4Digits() +
                ", Type: " + metadata.getCardType() +
                ", Nickname: " + metadata.getCardNickname() +
                ", Confidence: " + metadata.getConfidenceScore());

        // Only return if we have minimum required data
        return metadata.isValid() ? metadata : null;
    }

    /**
     * Normalize card type to standard format
     */
    private String normalizeCardType(String cardType) {
        if (cardType == null)
            return "UNKNOWN";

        String normalized = cardType.toLowerCase().replaceAll("\\s+", "");

        if (normalized.contains("visa"))
            return "VISA";
        if (normalized.contains("master"))
            return "MASTERCARD";
        if (normalized.contains("amex") || normalized.contains("american"))
            return "AMEX";
        if (normalized.contains("discover"))
            return "DISCOVER";
        if (normalized.contains("rupay"))
            return "RUPAY";
        if (normalized.contains("diners"))
            return "DINERS";

        return "UNKNOWN";
    }

    /**
     * Clean and format card nickname
     */
    private String cleanNickname(String nickname) {
        if (nickname == null)
            return null;

        // Remove extra whitespace and limit length
        String cleaned = nickname.replaceAll("\\s+", " ").trim();

        // Capitalize first letter of each word
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Calculate confidence score based on extracted fields
     */
    public double calculateConfidence(CardMetadataDTO metadata) {
        if (metadata == null)
            return 0.0;

        double score = 0.0;

        if (metadata.getLast4Digits() != null)
            score += 0.4;
        if (metadata.getExpiryDate() != null)
            score += 0.3;
        if (metadata.getCardType() != null && !"UNKNOWN".equals(metadata.getCardType()))
            score += 0.2;
        if (metadata.getCardNickname() != null)
            score += 0.1;

        return Math.min(score, 1.0);
    }
}
