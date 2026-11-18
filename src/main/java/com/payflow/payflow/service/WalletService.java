package com.payflow.payflow.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.payflow.dto.WalletResponse;
import com.payflow.payflow.exception.BusinessException;
import com.payflow.payflow.repository.WalletRepository;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String email) {
        var wallet = walletRepository.findByUser_Email(email)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found."));
        return new WalletResponse(wallet.getId(), wallet.getBalance(),
            wallet.getCurrency(), wallet.getStatus());
    }
}
