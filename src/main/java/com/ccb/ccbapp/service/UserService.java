package com.ccb.ccbapp.service;

import com.ccb.ccbapp.dto.UserRequestDTO;
import com.ccb.ccbapp.dto.UserResponseDTO;
import com.ccb.ccbapp.entity.User;
import com.ccb.ccbapp.mapper.UserMapper;
import com.ccb.ccbapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for User business logic.
 * This is where all business rules and data manipulation happens.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Create or update user (upsert logic for OAuth2)
     */
    public UserResponseDTO createOrUpdateUser(String email, String name, String pictureUrl) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    return newUser;
                });

        user.setName(name);
        user.setPictureUrl(pictureUrl);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * Create a new user
     */
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        User user = userMapper.toEntity(userRequestDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * Update an existing user
     */
    public Optional<UserResponseDTO> updateUser(Long id, UserRequestDTO userRequestDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    userMapper.updateEntityFromDTO(userRequestDTO, user);
                    User updatedUser = userRepository.save(user);
                    return userMapper.toResponseDTO(updatedUser);
                });
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
