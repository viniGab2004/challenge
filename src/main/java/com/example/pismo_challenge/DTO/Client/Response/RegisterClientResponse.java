package com.example.pismo_challenge.DTO.Client.Response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RegisterClientResponse {
    public UUID clientId;
}
