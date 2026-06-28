package com.example.pismo_challenge.DTO.Transaction.Response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RegisterTransactionResponse {
    public UUID transactionId;
}
