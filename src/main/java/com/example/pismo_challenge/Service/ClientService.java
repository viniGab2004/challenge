package com.example.pismo_challenge.Service;

import com.example.pismo_challenge.DTO.Client.Request.RegisterClientRequest;
import com.example.pismo_challenge.DTO.Client.Request.UpdateLifeSituationRequest;
import com.example.pismo_challenge.DTO.Client.Response.RegisterClientResponse;
import com.example.pismo_challenge.DTO.Client.Response.UpdateLifeSituationResponse;
import com.example.pismo_challenge.Entity.Event.ClientDiedEvent;
import com.example.pismo_challenge.Entity.Model.Client;
import com.example.pismo_challenge.Repository.ClientRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ClientService
{
    private final ClientRepository repository;
    private final ApplicationEventPublisher publisher;

    public RegisterClientResponse RegisterClient(RegisterClientRequest client) {
        if(ExistsClient(client.documentNumber)) {
            throw new EntityExistsException("O cliente já foi registrado na base de dados");
        }

        Client clientData = ClientRegisterObjectBuilder(client);
        var repositoryResponse = repository.save(clientData);
        return RegisterClientResponse.builder()
                .clientId(repositoryResponse.getClientId())
                .build();
    }

    @Transactional
    public UpdateLifeSituationResponse UpdateLifeSituation(UpdateLifeSituationRequest updateLifeRequest) {
            var client = FindClientByDocumentNumber(updateLifeRequest.getDocumentNumber());
            if(updateLifeRequest.getIsAlive() == null)
            {
                throw new IllegalArgumentException("Situação de vida inválida");
            }
            client.setIsAlive(updateLifeRequest.getIsAlive());
            var repositoryResponse = repository.save(client);

            if(repositoryResponse.getIsAlive().equals(false))
            {
                publisher.publishEvent(new ClientDiedEvent(repositoryResponse.getClientId()));
            }

            return UpdateLifeSituationResponse.builder()
                    .clientId(repositoryResponse.getClientId())
                    .isAlive(repositoryResponse.getIsAlive())
                    .build();
    }

    public Client FindClientByDocumentNumber(String documentNumber)
    {
        var client = repository.findByDocumentNumber(unmaskNormalizeCpf(documentNumber));
        if(client == null)
        {
            throw new EntityNotFoundException("Não existe cliente com esse número de documento");
        }
        return client;
    }

    public Client FindClientById(UUID clientId)
    {
        var client = repository.findClientByClientId(clientId);
        if(client == null)
        {
            throw new EntityNotFoundException("Nenhum cliente encontrado");
        }
        return client;
    }

    private boolean ExistsClient(String documentNumber)
    {
        Long documentNumberNormalized = unmaskNormalizeCpf(documentNumber);
        return repository.existsByDocumentNumber(documentNumberNormalized);
    }

    private Client ClientRegisterObjectBuilder(RegisterClientRequest client) {
         return Client.builder()
                .clientId(UUID.randomUUID())
                .Name(client.getName())
                 .documentNumber(unmaskNormalizeCpf(client.getDocumentNumber()))
                .contactCellphone(client.getContactCellphone())
                .Gender(client.getGender())
                .originState(client.getOriginState())
                .build();
    }

    private Long unmaskNormalizeCpf(String cpf) {
        if(cpf == null || cpf.isBlank())
        {
            throw new IllegalArgumentException("CPF do cliente não indicado");
        }
        return Long.valueOf(cpf.replaceAll("\\D", ""));
    }
}
