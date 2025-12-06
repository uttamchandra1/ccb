package com.ccb.ccbapp.service;

import com.ccb.ccbapp.config.TrustedBankConfig;
import com.ccb.ccbapp.dto.CardMetadataDTO;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for integrating with Gmail API to detect credit cards from emails.
 * Implements security best practices and filters for trusted senders only.
 */
@Service
public class GmailCardDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(GmailCardDetectionService.class);

    private final TrustedBankConfig trustedBankConfig;
    private final CardMetadataExtractor metadataExtractor;

    public GmailCardDetectionService(TrustedBankConfig trustedBankConfig,
            CardMetadataExtractor metadataExtractor) {
        this.trustedBankConfig = trustedBankConfig;
        this.metadataExtractor = metadataExtractor;
    }

    /**
     * Scan Gmail for credit card information
     * 
     * @param accessToken OAuth2 access token with Gmail read scope
     * @return List of detected card metadata
     */
    public List<CardMetadataDTO> scanForCreditCards(String accessToken) {
        try {
            Gmail gmailService = createGmailService(accessToken);

            // Build search query for card-related emails
            String query = buildSearchQuery();

            logger.info("Scanning Gmail with query: {}", query);

            // Fetch messages
            ListMessagesResponse response = gmailService.users()
                    .messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults(100L)
                    .execute();

            if (response.getMessages() == null || response.getMessages().isEmpty()) {
                logger.info("No card-related emails found");
                return Collections.emptyList();
            }

            logger.info("Found {} potential card emails", response.getMessages().size());

            List<CardMetadataDTO> detectedCards = new ArrayList<>();

            for (Message message : response.getMessages()) {
                try {
                    CardMetadataDTO metadata = processMessage(gmailService, message.getId());
                    if (metadata != null && metadata.isValid()) {
                        detectedCards.add(metadata);
                    }
                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", message.getId(), e.getMessage());
                }
            }

            // Deduplicate and sort by confidence
            List<CardMetadataDTO> uniqueCards = deduplicateCards(detectedCards);

            logger.info("Detected {} unique cards", uniqueCards.size());

            return uniqueCards;

        } catch (Exception e) {
            logger.error("Error scanning Gmail: {}", e.getMessage(), e);
            if (e.getMessage().contains("403")) {
                logger.error(
                        "Access Forbidden. Please ensure the Gmail API is enabled in Google Cloud Console and the user has granted 'https://www.googleapis.com/auth/gmail.readonly' scope.");
            } else if (e.getMessage().contains("401")) {
                logger.error("Unauthorized. Access token may be invalid or expired.");
            }
            throw new RuntimeException("Failed to scan Gmail for credit cards: " + e.getMessage(), e);
        }
    }

    /**
     * Process a single email message
     */
    private CardMetadataDTO processMessage(Gmail gmailService, String messageId) throws Exception {
        // Fetch full message
        Message fullMessage = gmailService.users()
                .messages()
                .get("me", messageId)
                .setFormat("full")
                .execute();

        // Extract headers
        String from = getHeader(fullMessage, "From");
        String subject = getHeader(fullMessage, "Subject");

        logger.info("Processing email - From: {}, Subject: {}", from, subject);

        // Verify sender is trusted
        if (!trustedBankConfig.isTrustedDomain(from)) {
            logger.warn("Skipping untrusted sender: {}", from);
            return null;
        }

        // Verify subject contains card keywords
        if (!trustedBankConfig.hasCardKeyword(subject)) {
            logger.warn("Skipping email without card keywords - Subject: {}", subject);
            return null;
        }

        logger.info("Email passed filters, extracting body...");

        // Extract email body
        String body = getEmailBody(fullMessage);

        if (body == null || body.isEmpty()) {
            logger.warn("Empty email body for message: {}", messageId);
            return null;
        }

        logger.debug("Email body length: {} characters", body.length());

        // Extract metadata (pass subject for better card name extraction)
        CardMetadataDTO metadata = metadataExtractor.extractMetadata(body, subject);

        if (metadata != null) {
            // Enrich with email metadata
            metadata.setBankName(trustedBankConfig.extractBankName(from));
            metadata.setEmailSubject(subject);
            metadata.setSenderEmail(from);

            logger.info("✅ Successfully extracted card metadata: Last4={}, Type={}, Bank={}",
                    metadata.getLast4Digits(), metadata.getCardType(), metadata.getBankName());
        } else {
            logger.warn("❌ Failed to extract card metadata from email body (no card details found)");
        }

        return metadata;
    }

    /**
     * Build Gmail search query for card-related emails
     */
    private String buildSearchQuery() {
        return "subject:(credit card OR debit card OR card issued OR card activated OR new card OR statement) " +
                "newer_than:2y"; // Last 2 years only
    }

    /**
     * Extract header value from message
     */
    private String getHeader(Message message, String headerName) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return null;
        }

        return message.getPayload().getHeaders().stream()
                .filter(header -> headerName.equalsIgnoreCase(header.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extract email body from message
     */
    private String getEmailBody(Message message) {
        if (message.getPayload() == null) {
            return null;
        }

        StringBuilder body = new StringBuilder();

        // Try to get body from payload
        if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
            body.append(decodeBase64(message.getPayload().getBody().getData()));
        }

        // Check parts for multipart messages
        if (message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                body.append(extractPartBody(part));
            }
        }

        return body.toString();
    }

    /**
     * Recursively extract body from message parts
     */
    private String extractPartBody(MessagePart part) {
        StringBuilder body = new StringBuilder();

        if (part.getBody() != null && part.getBody().getData() != null) {
            body.append(decodeBase64(part.getBody().getData()));
        }

        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                body.append(extractPartBody(subPart));
            }
        }

        return body.toString();
    }

    /**
     * Decode base64 URL-safe encoded string
     */
    private String decodeBase64(String encodedData) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error decoding base64: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Remove duplicate cards based on last 4 digits and expiry date
     */
    private List<CardMetadataDTO> deduplicateCards(List<CardMetadataDTO> cards) {
        Map<String, CardMetadataDTO> uniqueCards = new LinkedHashMap<>();

        for (CardMetadataDTO card : cards) {
            String key = card.getLast4Digits() + "_" +
                    (card.getExpiryDate() != null ? card.getExpiryDate() : "");

            // Keep the card with highest confidence score
            if (!uniqueCards.containsKey(key) ||
                    card.getConfidenceScore() > uniqueCards.get(key).getConfidenceScore()) {
                uniqueCards.put(key, card);
            }
        }

        // Sort by confidence score (descending)
        return uniqueCards.values().stream()
                .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
                .collect(Collectors.toList());
    }

    /**
     * Create Gmail service with access token
     */
    private Gmail createGmailService(String accessToken) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.create(
                new AccessToken(accessToken, null));

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("CCB App")
                .build();
    }
}
