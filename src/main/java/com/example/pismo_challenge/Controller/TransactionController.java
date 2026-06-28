package com.example.pismo_challenge.Controller;

import com.example.pismo_challenge.DTO.Transaction.Request.RegisterTransactionRequest;
import com.example.pismo_challenge.DTO.Transaction.Response.RegisterTransactionResponse;
import com.example.pismo_challenge.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
public class TransactionController {
    private final TransactionService service;

    @PostMapping
    public ResponseEntity<RegisterTransactionResponse> RegisterTransaction(@Valid @RequestBody RegisterTransactionRequest registerRequest)
    {
        RegisterTransactionResponse response = service.RegisterTransaction(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
