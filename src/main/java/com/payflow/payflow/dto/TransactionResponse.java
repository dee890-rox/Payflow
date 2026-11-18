package com.payflow.payflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    String reference,
    String senderEmail,
    String receiverEmail,
    BigDecimal amount,
    String currency,
    String status,
    String description,
    LocalDateTime createdAt
) {}
