package com.payflow.payflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.payflow.payflow.dto.TransactionResponse;
import com.payflow.payflow.dto.UserResponse;
import com.payflow.payflow.service.TransferService;
import com.payflow.payflow.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final TransferService transferService;

    public AdminController(UserService userService, TransferService transferService) {
        this.userService = userService;
        this.transferService = transferService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userService.getAllUsers();
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions() {
        return transferService.allTransactions();
    }
}
