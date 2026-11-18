package com.payflow.payflow.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.payflow.dto.*;
import com.payflow.payflow.entity.*;
import com.payflow.payflow.exception.BusinessException;
import com.payflow.payflow.repository.UserRepository;
import com.payflow.payflow.repository.WalletRepository;
import com.payflow.payflow.security.JwtService;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.USER);
        user = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse("Bearer", token, jwtService.getExpirationMs(),
            toUserResponse(user), new WalletResponse(wallet.getId(),
                wallet.getBalance(), wallet.getCurrency(), wallet.getStatus()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED,
                "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,
                "Invalid email or password.");
        }

        Wallet wallet = walletRepository.findByUser_Email(user.getEmail())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found."));
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse("Bearer", token, jwtService.getExpirationMs(),
            toUserResponse(user), new WalletResponse(wallet.getId(),
                wallet.getBalance(), wallet.getCurrency(), wallet.getStatus()));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found."));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
            user.getPhone(), user.getRole().name(), user.getCreatedAt());
    }
}
