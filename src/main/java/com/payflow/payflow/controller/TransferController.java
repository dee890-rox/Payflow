package com.payflow.payflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.payflow.payflow.dto.TransactionResponse;
import com.payflow.payflow.dto.TransferRequest;
import com.payflow.payflow.service.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(
            Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        return transferService.transfer(authentication.getName(), idempotencyKey, request);
    }

    @GetMapping("/history")
    public List<TransactionResponse> history(Authentication authentication) {
        return transferService.history(authentication.getName());
    }
}
