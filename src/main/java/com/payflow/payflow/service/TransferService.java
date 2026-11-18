package com.payflow.payflow.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.payflow.dto.TransactionResponse;
import com.payflow.payflow.dto.TransferRequest;
import com.payflow.payflow.entity.Wallet;
import com.payflow.payflow.entity.WalletTransaction;
import com.payflow.payflow.exception.BusinessException;
import com.payflow.payflow.repository.WalletRepository;
import com.payflow.payflow.repository.WalletTransactionRepository;

@Service
public class TransferService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public TransferService(WalletRepository walletRepository,
                           WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse transfer(String senderEmail, String idempotencyKey,
                                        TransferRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 80) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                "Supply a unique Idempotency-Key header (maximum 80 characters).");
        }

        var previous = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (previous.isPresent()) {
            WalletTransaction old = previous.get();
            if (!old.getSenderWallet().getUser().getEmail().equals(senderEmail)) {
                throw new BusinessException(HttpStatus.CONFLICT,
                    "Idempotency key has already been used.");
            }
            return toResponse(old);
        }

        Long senderId = walletRepository.findWalletIdByEmail(senderEmail)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Sender wallet not found."));
        Long receiverId = walletRepository.findWalletIdByEmail(
            request.receiverEmail().trim().toLowerCase())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Receiver wallet not found."));

        if (senderId.equals(receiverId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                "You cannot transfer money to your own wallet.");
        }

        // Lock in ascending ID order to reduce the chance of deadlocks.
        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);
        Wallet first = walletRepository.findByIdForUpdate(firstId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found."));
        Wallet second = walletRepository.findByIdForUpdate(secondId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Wallet not found."));

        Wallet sender = senderId.equals(firstId) ? first : second;
        Wallet receiver = receiverId.equals(firstId) ? first : second;

        BigDecimal amount = request.amount().setScale(2, java.math.RoundingMode.UNNECESSARY);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
        if (!"ACTIVE".equals(sender.getStatus()) || !"ACTIVE".equals(receiver.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Both wallets must be active.");
        }
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Insufficient wallet balance.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        walletRepository.save(sender);
        walletRepository.save(receiver);

        WalletTransaction tx = new WalletTransaction();
        tx.setReference("PF-" + UUID.randomUUID());
        tx.setSenderWallet(sender);
        tx.setReceiverWallet(receiver);
        tx.setAmount(amount);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setDescription(request.description());
        tx = transactionRepository.save(tx);

        return toResponse(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String email) {
        return transactionRepository.findHistoryForEmail(email).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> allTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse).toList();
    }

    private TransactionResponse toResponse(WalletTransaction tx) {
        return new TransactionResponse(tx.getReference(),
            tx.getSenderWallet().getUser().getEmail(),
            tx.getReceiverWallet().getUser().getEmail(),
            tx.getAmount(), tx.getSenderWallet().getCurrency(),
            tx.getStatus(), tx.getDescription(), tx.getCreatedAt());
    }
}
