package com.ccb.ccbapp.dto;

/**
 * Data Transfer Object for User responses.
 * This separates the API contract from the database entity.
 */
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String pictureUrl;

    // Default constructor
    public UserResponseDTO() {
    }

    // All-args constructor
    public UserResponseDTO(Long id, String email, String name, String pictureUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.pictureUrl = pictureUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
