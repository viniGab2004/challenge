package com.example.pismo_challenge.Controller;

import com.example.pismo_challenge.DTO.Client.Request.UpdateLifeSituationRequest;
import com.example.pismo_challenge.DTO.Client.Response.RegisterClientResponse;
import com.example.pismo_challenge.DTO.Client.Request.RegisterClientRequest;
import com.example.pismo_challenge.DTO.Client.Response.UpdateLifeSituationResponse;
import com.example.pismo_challenge.Service.ClientService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/client")
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/register-client")
    public ResponseEntity<RegisterClientResponse> RegisterClient(@Valid @RequestBody RegisterClientRequest clientData)
    {
        RegisterClientResponse response = clientService.RegisterClient(clientData);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/client-life-situation")
    public ResponseEntity<UpdateLifeSituationResponse> UpdateLifeSituation(@Valid @RequestBody UpdateLifeSituationRequest updateLifeRequest)
    {
        UpdateLifeSituationResponse response = clientService.UpdateLifeSituation(updateLifeRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
