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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ClientService service;

    private Client client;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .clientId(UUID.randomUUID())
                .Name("João da Silva")
                .documentNumber(12345678900L)
                .isAlive(true)
                .contactCellphone("11999999999")
                .Gender("Masculino")
                .originState("SP")
                .build();
    }

    @Test
    void registerClient_ShouldSaveAndReturnResponse_WhenClientDoesNotExist() {
        RegisterClientRequest request = new RegisterClientRequest();
        request.name = "João da Silva";
        request.documentNumber = "123.456.789-00";
        request.contactCellphone = "11999999999";
        request.gender = "Masculino";
        request.originState = "SP";

        when(repository.existsByDocumentNumber(12345678900L)).thenReturn(false);
        when(repository.save(any(Client.class))).thenReturn(client);

        RegisterClientResponse response = service.RegisterClient(request);

        assertThat(response).isNotNull();
        assertThat(response.getClientId()).isEqualTo(client.getClientId());
        verify(repository).save(any(Client.class));
    }

    @Test
    void registerClient_ShouldThrowEntityExistsException_WhenClientAlreadyExists() {
        RegisterClientRequest request = new RegisterClientRequest();
        request.documentNumber = "123.456.789-00";

        when(repository.existsByDocumentNumber(12345678900L)).thenReturn(true);

        assertThatThrownBy(() -> service.RegisterClient(request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("O cliente já foi registrado na base de dados");

        verify(repository, never()).save(any(Client.class));
    }

    @Test
    void updateLifeSituation_ShouldUpdateToFalseAndPublishEvent_WhenClientDied() {
        UpdateLifeSituationRequest request = new UpdateLifeSituationRequest();
        request.documentNumber = "123.456.789-00";
        request.isAlive = false;

        when(repository.findByDocumentNumber(12345678900L)).thenReturn(client);
        
        Client deadClient = Client.builder()
                .clientId(client.getClientId())
                .isAlive(false)
                .build();
        when(repository.save(any(Client.class))).thenReturn(deadClient);

        UpdateLifeSituationResponse response = service.UpdateLifeSituation(request);

        assertThat(response).isNotNull();
        assertThat(response.getIsAlive()).isFalse();

        ArgumentCaptor<ClientDiedEvent> eventCaptor = ArgumentCaptor.forClass(ClientDiedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().clientId()).isEqualTo(client.getClientId());
    }

    @Test
    void updateLifeSituation_ShouldUpdateToTrueAndNotPublishEvent_WhenClientIsAlive() {
        UpdateLifeSituationRequest request = new UpdateLifeSituationRequest();
        request.documentNumber = "123.456.789-00";
        request.isAlive = true;

        when(repository.findByDocumentNumber(12345678900L)).thenReturn(client);
        when(repository.save(any(Client.class))).thenReturn(client);

        UpdateLifeSituationResponse response = service.UpdateLifeSituation(request);

        assertThat(response).isNotNull();
        assertThat(response.getIsAlive()).isTrue();
        verify(publisher, never()).publishEvent(any(ClientDiedEvent.class));
    }

    @Test
    void updateLifeSituation_ShouldThrowIllegalArgumentException_WhenIsAliveIsNull() {
        UpdateLifeSituationRequest request = new UpdateLifeSituationRequest();
        request.documentNumber = "123.456.789-00";
        request.isAlive = null;

        when(repository.findByDocumentNumber(12345678900L)).thenReturn(client);

        assertThatThrownBy(() -> service.UpdateLifeSituation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Situação de vida inválida");

        verify(repository, never()).save(any(Client.class));
    }

    @Test
    void findClientByDocumentNumber_ShouldReturnClient_WhenFound() {
        when(repository.findByDocumentNumber(12345678900L)).thenReturn(client);

        Client result = service.FindClientByDocumentNumber("12345678900");

        assertThat(result).isEqualTo(client);
    }

    @Test
    void findClientByDocumentNumber_ShouldThrowEntityNotFoundException_WhenNotFound() {
        when(repository.findByDocumentNumber(12345678900L)).thenReturn(null);

        assertThatThrownBy(() -> service.FindClientByDocumentNumber("12345678900"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Não existe cliente com esse número de documento");
    }

    @Test
    void findClientById_ShouldReturnClient_WhenFound() {
        UUID id = client.getClientId();
        when(repository.findClientByClientId(id)).thenReturn(client);

        Client result = service.FindClientById(id);

        assertThat(result).isEqualTo(client);
    }

    @Test
    void findClientById_ShouldThrowEntityNotFoundException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findClientByClientId(id)).thenReturn(null);

        assertThatThrownBy(() -> service.FindClientById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Nenhum cliente encontrado");
    }
}
