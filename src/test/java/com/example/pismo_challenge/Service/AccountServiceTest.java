package com.example.pismo_challenge.Service;

import com.example.pismo_challenge.DTO.Account.Request.CreateAccountRequest;
import com.example.pismo_challenge.DTO.Account.Response.CreateAccountResponse;
import com.example.pismo_challenge.DTO.Account.Response.GetAccountResponse;
import com.example.pismo_challenge.Entity.Enum.AccountSituation;
import com.example.pismo_challenge.Entity.Event.ClientDiedEvent;
import com.example.pismo_challenge.Entity.Event.TransactionCreatedEvent;
import com.example.pismo_challenge.Entity.Model.Account;
import com.example.pismo_challenge.Entity.Model.Client;
import com.example.pismo_challenge.Repository.AccountRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountService service;

    private Client client;
    private Account account;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .clientId(UUID.randomUUID())
                .Name("João da Silva")
                .documentNumber(12345678900L)
                .isAlive(true)
                .build();

        account = Account.builder()
                .accountId(UUID.randomUUID())
                .clientId(client.getClientId())
                .accountNumber(123456)
                .totalAmount(BigDecimal.ZERO)
                .isActive(true)
                .canTransact(true)
                .accountSituation(AccountSituation.ACCOUNT_WITH_NO_PENDING.getSituationCode())
                .build();
    }

    @Test
    void createAccount_ShouldSaveAndReturnResponse_WhenValidRequest() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.documentNumber = "123.456.789-00";

        when(clientService.FindClientByDocumentNumber(request.getDocumentNumber())).thenReturn(client);
        when(repository.existsByClientId(client.getClientId())).thenReturn(false);
        when(repository.save(any(Account.class))).thenReturn(account);

        CreateAccountResponse response = service.CreateAccount(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountId()).isEqualTo(account.getAccountId());
        verify(repository).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowEntityExistsException_WhenAccountAlreadyExists() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.documentNumber = "123.456.789-00";

        when(clientService.FindClientByDocumentNumber(request.getDocumentNumber())).thenReturn(client);
        when(repository.existsByClientId(client.getClientId())).thenReturn(true);

        assertThatThrownBy(() -> service.CreateAccount(request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("Uma conta já existe para o cliente indicado");

        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_ShouldThrowIllegalStateException_WhenClientIsDeceased() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.documentNumber = "123.456.789-00";
        client.setIsAlive(false);

        when(clientService.FindClientByDocumentNumber(request.getDocumentNumber())).thenReturn(client);
        when(repository.existsByClientId(client.getClientId())).thenReturn(false);

        assertThatThrownBy(() -> service.CreateAccount(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é permitido fazer a criação de contas para uma pessoa morta");

        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void getAccountById_ShouldReturnResponse_WhenFound() {
        UUID accountId = account.getAccountId();
        when(repository.findAccountByAccountId(accountId)).thenReturn(account);
        when(clientService.FindClientById(client.getClientId())).thenReturn(client);

        GetAccountResponse response = service.GetAccountById(accountId);

        assertThat(response).isNotNull();
        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getDocumentNumber()).isEqualTo(client.getDocumentNumber());
    }

    @Test
    void getAccountById_ShouldThrowEntityNotFoundException_WhenNotFound() {
        UUID accountId = UUID.randomUUID();
        when(repository.findAccountByAccountId(accountId)).thenReturn(null);

        assertThatThrownBy(() -> service.GetAccountById(accountId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Não existe nenhuma conta registrada com o identificador indicado");
    }

    @Test
    void isValidAccountToTransact_ShouldReturnCanTransactValue_WhenFound() {
        UUID accountId = account.getAccountId();
        when(repository.findAccountByAccountId(accountId)).thenReturn(account);

        boolean result = service.IsValidAccountToTransact(accountId);

        assertThat(result).isTrue();
    }

    @Test
    void isValidAccountToTransact_ShouldThrowEntityNotFoundException_WhenNotFound() {
        UUID accountId = UUID.randomUUID();
        when(repository.findAccountByAccountId(accountId)).thenReturn(null);

        assertThatThrownBy(() -> service.IsValidAccountToTransact(accountId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Não existe nenhuma conta registrada com o identificador indicado");
    }

    @Test
    void freezeAccount_ShouldSetStatusToInactiveAndSave_WhenAccountExists() {
        ClientDiedEvent event = new ClientDiedEvent(client.getClientId());
        when(repository.findAccountByClientId(event.clientId())).thenReturn(account);

        service.FreezeAccount(event);

        assertThat(account.getIsActive()).isFalse();
        assertThat(account.getCanTransact()).isFalse();
        verify(repository).save(account);
    }

    @Test
    void freezeAccount_ShouldDoNothing_WhenAccountDoesNotExist() {
        ClientDiedEvent event = new ClientDiedEvent(UUID.randomUUID());
        when(repository.findAccountByClientId(event.clientId())).thenReturn(null);

        service.FreezeAccount(event);

        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void updateTotalAmount_ShouldAddPositiveAmountAndKeepNoPendingSituation() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(account.getAccountId(), new BigDecimal("150.00"));
        when(repository.findAccountByAccountId(event.accountId())).thenReturn(account);

        service.UpdateTotalAmount(event);

        assertThat(account.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(account.getAccountSituation()).isEqualTo(AccountSituation.ACCOUNT_WITH_NO_PENDING.getSituationCode());
        verify(repository).save(account);
    }

    @Test
    void updateTotalAmount_ShouldSubtractNegativeAmountAndKeepNoPendingSituation_WhenResultIsPositive() {
        account.setTotalAmount(new BigDecimal("200.00"));
        TransactionCreatedEvent event = new TransactionCreatedEvent(account.getAccountId(), new BigDecimal("-150.00"));
        when(repository.findAccountByAccountId(event.accountId())).thenReturn(account);

        service.UpdateTotalAmount(event);

        assertThat(account.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(account.getAccountSituation()).isEqualTo(AccountSituation.ACCOUNT_WITH_NO_PENDING.getSituationCode());
        verify(repository).save(account);
    }

    @Test
    void updateTotalAmount_ShouldSubtractNegativeAmountAndSetPendingSituation_WhenResultIsNegative() {
        account.setTotalAmount(new BigDecimal("100.00"));
        TransactionCreatedEvent event = new TransactionCreatedEvent(account.getAccountId(), new BigDecimal("-150.00"));
        when(repository.findAccountByAccountId(event.accountId())).thenReturn(account);

        service.UpdateTotalAmount(event);

        assertThat(account.getTotalAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
        assertThat(account.getAccountSituation()).isEqualTo(AccountSituation.ACCOUNT_WITH_PENDING.getSituationCode());
        verify(repository).save(account);
    }

    @Test
    void updateTotalAmount_ShouldDoNothing_WhenAccountNotFound() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(UUID.randomUUID(), new BigDecimal("100.00"));
        when(repository.findAccountByAccountId(event.accountId())).thenReturn(null);

        service.UpdateTotalAmount(event);

        verify(repository, never()).save(any(Account.class));
    }
}
