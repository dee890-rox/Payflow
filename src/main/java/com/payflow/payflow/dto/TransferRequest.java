package com.payflow.payflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferRequest(
    @NotBlank @Email String receiverEmail,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    @Size(max = 250) String description
) {}
