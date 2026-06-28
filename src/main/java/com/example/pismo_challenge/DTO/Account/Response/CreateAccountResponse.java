package com.example.pismo_challenge.DTO.Account.Response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CreateAccountResponse {
    public UUID accountId;
}
