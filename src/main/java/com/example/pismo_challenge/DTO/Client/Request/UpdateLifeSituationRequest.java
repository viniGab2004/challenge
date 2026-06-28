package com.example.pismo_challenge.DTO.Client.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateLifeSituationRequest {
    @NotBlank
    public String documentNumber;
    @NotNull
    public Boolean isAlive;
}
