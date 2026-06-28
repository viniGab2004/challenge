package com.example.pismo_challenge.Controller;

import com.example.pismo_challenge.DTO.Account.Request.CreateAccountRequest;
import com.example.pismo_challenge.DTO.Account.Response.CreateAccountResponse;
import com.example.pismo_challenge.DTO.Account.Response.GetAccountResponse;
import com.example.pismo_challenge.Service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping
    public ResponseEntity<CreateAccountResponse> CreateAccount(@Valid @RequestBody CreateAccountRequest accountRequest)
    {
        CreateAccountResponse response = service.CreateAccount(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<GetAccountResponse> GetAccount(@PathVariable UUID accountId)
    {
        GetAccountResponse response = service.GetAccountById(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
