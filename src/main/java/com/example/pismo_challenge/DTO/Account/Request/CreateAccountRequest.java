package com.example.pismo_challenge.DTO.Account.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateAccountRequest {
    @NotBlank
    public String documentNumber;
}
