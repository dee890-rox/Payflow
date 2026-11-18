package com.payflow.payflow.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String email,
                           String phone, String role, LocalDateTime createdAt) {}
