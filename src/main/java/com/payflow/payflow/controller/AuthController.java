package com.payflow.payflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.payflow.payflow.dto.AuthResponse;
import com.payflow.payflow.dto.LoginRequest;
import com.payflow.payflow.dto.RegisterRequest;
import com.payflow.payflow.dto.UserResponse;
import com.payflow.payflow.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/profile")
    public UserResponse profile(
            org.springframework.security.core.Authentication authentication) {
        return userService.getProfile(authentication.getName());
    }
}
