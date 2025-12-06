package com.ccb.ccbapp.controller;

import com.ccb.ccbapp.dto.UserResponseDTO;
import com.ccb.ccbapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin controller for managing users.
 * Returns DTOs instead of entities for proper separation of concerns.
 */
@RestController
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}
