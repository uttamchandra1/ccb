package com.ccb.ccbapp.mapper;

import com.ccb.ccbapp.dto.UserRequestDTO;
import com.ccb.ccbapp.dto.UserResponseDTO;
import com.ccb.ccbapp.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert between Entity and DTOs.
 * This keeps the conversion logic centralized and reusable.
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserResponseDTO
     */
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPictureUrl());
    }

    /**
     * Convert UserRequestDTO to User entity
     */
    public User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPictureUrl(dto.getPictureUrl());
        return user;
    }

    /**
     * Update existing User entity from UserRequestDTO
     */
    public void updateEntityFromDTO(UserRequestDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPictureUrl(dto.getPictureUrl());
    }
}
