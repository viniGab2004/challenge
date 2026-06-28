package com.example.pismo_challenge.DTO.Transaction.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RegisterTransactionRequest {
    @NotNull
    public UUID accountId;
    @NotNull
    public int operationTypeCode;
    @NotNull
    public float amount;
}
