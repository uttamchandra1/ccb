package com.ccb.ccbapp.controller;

import com.ccb.ccbapp.dto.CardMetadataDTO;
import com.ccb.ccbapp.service.GmailCardDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for credit card detection from Gmail.
 * Provides endpoints to scan emails and retrieve detected card metadata.
 */
@RestController
@RequestMapping("/api/card-detection")
public class CardDetectionController {

    private static final Logger logger = LoggerFactory.getLogger(CardDetectionController.class);

    private final GmailCardDetectionService gmailCardDetectionService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public CardDetectionController(GmailCardDetectionService gmailCardDetectionService,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.gmailCardDetectionService = gmailCardDetectionService;
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * Scan user's Gmail for credit card information
     * POST /api/card-detection/scan
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanGmail(
            @AuthenticationPrincipal OAuth2User principal,
            Authentication authentication) {
        try {
            logger.info("Starting Gmail scan for user: " + principal.getAttribute("email"));

            // Get access token using OAuth2AuthorizedClientService
            String accessToken = getAccessToken(authentication);

            if (accessToken == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Gmail access token not found. Please re-authenticate with Gmail scope."));
            }

            // Scan Gmail
            List<CardMetadataDTO> detectedCards = gmailCardDetectionService.scanForCreditCards(accessToken);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cardsDetected", detectedCards.size());
            response.put("cards", detectedCards);
            response.put("message", "Scan completed successfully");

            logger.info("Scan completed. Found {} cards", detectedCards.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error scanning Gmail: {}", e.getMessage(), e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Error scanning Gmail: " + e.getMessage()));
        }
    }

    /**
     * Get scan status and statistics
     * GET /api/card-detection/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScanStatus(
            @AuthenticationPrincipal OAuth2User principal,
            Authentication authentication) {
        Map<String, Object> status = new HashMap<>();

        // Get authorized client for debugging
        OAuth2AuthorizedClient authorizedClient = null;
        if (authentication != null) {
            authorizedClient = authorizedClientService.loadAuthorizedClient(
                    "google", authentication.getName());
        }

        status.put("gmailConnected", getAccessToken(authentication) != null);
        status.put("userEmail", principal.getAttribute("email"));
        status.put("scanAvailable", true);

        // Add scope information for debugging
        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            status.put("scopes", authorizedClient.getAccessToken().getScopes());
            status.put("hasGmailScope", authorizedClient.getAccessToken().getScopes()
                    .stream()
                    .anyMatch(scope -> scope.contains("gmail")));
        } else {
            status.put("scopes", "No access token available");
            status.put("hasGmailScope", false);
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Extract access token from OAuth2AuthorizedClientService
     */
    private String getAccessToken(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        try {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    "google",
                    authentication.getName());

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                return authorizedClient.getAccessToken().getTokenValue();
            }
        } catch (Exception e) {
            logger.error("Error retrieving access token: {}", e.getMessage());
        }

        return null;
    }
}
