package com.ccb.ccbapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for trusted bank email domains.
 * Only emails from these domains will be processed for card detection.
 */
@Configuration
@ConfigurationProperties(prefix = "card-detection")
public class TrustedBankConfig {

    private Set<String> trustedDomains = new HashSet<>();
    private Set<String> trustedKeywords = new HashSet<>();

    public TrustedBankConfig() {
        // Initialize with default trusted domains
        initializeDefaultDomains();
        initializeDefaultKeywords();
    }

    private void initializeDefaultDomains() {
        // Indian Banks - HDFC Bank
        trustedDomains.add("hdfcbank.com");
        trustedDomains.add("hdfcbank.net"); // HDFC uses .net for statements and notifications
        trustedDomains.add("applications.hdfcbank.net"); // HDFC credit card applications

        // Other Indian Banks
        trustedDomains.add("icicibank.com");
        trustedDomains.add("sbi.co.in");
        trustedDomains.add("axisbank.com");
        trustedDomains.add("kotak.com");
        trustedDomains.add("yesbank.in");
        trustedDomains.add("indusind.com");
        trustedDomains.add("sc.com"); // Standard Chartered
        trustedDomains.add("hsbc.co.in");
        trustedDomains.add("citibank.com");

        // International Banks
        trustedDomains.add("chase.com");
        trustedDomains.add("bankofamerica.com");
        trustedDomains.add("wellsfargo.com");
        trustedDomains.add("citi.com");

        // Card Networks
        trustedDomains.add("visa.com");
        trustedDomains.add("mastercard.com");
        trustedDomains.add("americanexpress.com");
    }

    private void initializeDefaultKeywords() {
        // Email subject keywords that indicate card-related emails
        trustedKeywords.add("credit card");
        trustedKeywords.add("debit card");
        trustedKeywords.add("card issued");
        trustedKeywords.add("card activated");
        trustedKeywords.add("card details");
        trustedKeywords.add("new card");
        trustedKeywords.add("card delivery");
        trustedKeywords.add("card statement");
        trustedKeywords.add("statement");
    }

    /**
     * Check if the email domain is trusted
     */
    public boolean isTrustedDomain(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String lowerEmail = email.toLowerCase();
        return trustedDomains.stream()
                .anyMatch(domain -> lowerEmail.endsWith("@" + domain) ||
                        lowerEmail.endsWith("." + domain));
    }

    /**
     * Check if the email subject contains card-related keywords
     */
    public boolean hasCardKeyword(String subject) {
        if (subject == null || subject.isEmpty()) {
            return false;
        }

        String lowerSubject = subject.toLowerCase();
        return trustedKeywords.stream()
                .anyMatch(lowerSubject::contains);
    }

    /**
     * Extract bank name from email domain
     */
    public String extractBankName(String email) {
        if (email == null || !email.contains("@")) {
            return "Unknown Bank";
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();

        // Map domains to bank names
        if (domain.contains("hdfc"))
            return "HDFC Bank";
        if (domain.contains("icici"))
            return "ICICI Bank";
        if (domain.contains("sbi"))
            return "State Bank of India";
        if (domain.contains("axis"))
            return "Axis Bank";
        if (domain.contains("kotak"))
            return "Kotak Mahindra Bank";
        if (domain.contains("yes"))
            return "Yes Bank";
        if (domain.contains("indusind"))
            return "IndusInd Bank";
        if (domain.contains("sc.com"))
            return "Standard Chartered";
        if (domain.contains("hsbc"))
            return "HSBC";
        if (domain.contains("citi"))
            return "Citibank";
        if (domain.contains("chase"))
            return "Chase";
        if (domain.contains("bankofamerica"))
            return "Bank of America";
        if (domain.contains("wells"))
            return "Wells Fargo";
        if (domain.contains("visa"))
            return "Visa";
        if (domain.contains("mastercard"))
            return "Mastercard";
        if (domain.contains("americanexpress"))
            return "American Express";

        return "Unknown Bank";
    }

    // Getters and Setters
    public Set<String> getTrustedDomains() {
        return trustedDomains;
    }

    public void setTrustedDomains(Set<String> trustedDomains) {
        this.trustedDomains = trustedDomains;
    }

    public Set<String> getTrustedKeywords() {
        return trustedKeywords;
    }

    public void setTrustedKeywords(Set<String> trustedKeywords) {
        this.trustedKeywords = trustedKeywords;
    }
}
