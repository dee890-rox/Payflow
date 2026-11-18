package com.payflow.payflow.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.payflow.payflow.dto.WalletResponse;
import com.payflow.payflow.service.WalletService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse myWallet(Authentication authentication) {
        return walletService.getMyWallet(authentication.getName());
    }
}
