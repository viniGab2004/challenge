package com.example.pismo_challenge.DTO.Client.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterClientRequest {
    @NotBlank
    public String name;
    @NotBlank
    public String documentNumber;
    @NotBlank
    public String contactCellphone;
    @NotBlank
    public String gender;
    @NotBlank
    public String originState;
}
