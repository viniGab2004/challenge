package com.example.pismo_challenge.Service;

import com.example.pismo_challenge.DTO.Transaction.Request.RegisterTransactionRequest;
import com.example.pismo_challenge.DTO.Transaction.Response.RegisterTransactionResponse;
import com.example.pismo_challenge.Entity.Event.TransactionCreatedEvent;
import com.example.pismo_challenge.Entity.Model.Transaction;
import com.example.pismo_challenge.Repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    private UUID accountId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        transaction = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .accountId(accountId)
                .operationTypeCode(1)
                .transactAmount(new BigDecimal("-50.00"))
                .build();
    }

    @Test
    void registerTransaction_ShouldSaveAndPublishEvent_WhenValidPurchaseTransaction() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 1; // NORMAL_PURCHASE
        request.amount = new BigDecimal("-50.00");

        when(accountService.IsValidAccountToTransact(accountId)).thenReturn(true);
        when(repository.save(any(Transaction.class))).thenReturn(transaction);

        RegisterTransactionResponse response = service.RegisterTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(transaction.getTransactionId());

        verify(repository).save(any(Transaction.class));
        ArgumentCaptor<TransactionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().accountId()).isEqualTo(accountId);
        assertThat(eventCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void registerTransaction_ShouldSaveAndPublishEvent_WhenValidCreditVoucherTransaction() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 4; // CREDIT_VOUCHER
        request.amount = new BigDecimal("150.00");

        Transaction voucherTx = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .accountId(accountId)
                .operationTypeCode(4)
                .transactAmount(new BigDecimal("150.00"))
                .build();

        when(accountService.IsValidAccountToTransact(accountId)).thenReturn(true);
        when(repository.save(any(Transaction.class))).thenReturn(voucherTx);

        RegisterTransactionResponse response = service.RegisterTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo(voucherTx.getTransactionId());

        verify(repository).save(any(Transaction.class));
        ArgumentCaptor<TransactionCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().accountId()).isEqualTo(accountId);
        assertThat(eventCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void registerTransaction_ShouldThrowIllegalArgumentException_WhenPurchaseHasPositiveAmount() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 1; // NORMAL_PURCHASE
        request.amount = new BigDecimal("50.00"); // Invalid: Positive for purchase

        assertThatThrownBy(() -> service.RegisterTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A operação de transação é inválida");

        verify(repository, never()).save(any(Transaction.class));
        verify(publisher, never()).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void registerTransaction_ShouldThrowIllegalArgumentException_WhenCreditVoucherHasNegativeAmount() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 4; // CREDIT_VOUCHER
        request.amount = new BigDecimal("-50.00"); // Invalid: Negative for credit voucher

        assertThatThrownBy(() -> service.RegisterTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A operação de transação é inválida");

        verify(repository, never()).save(any(Transaction.class));
        verify(publisher, never()).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void registerTransaction_ShouldThrowIllegalArgumentException_WhenAccountCannotTransact() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 1; // NORMAL_PURCHASE
        request.amount = new BigDecimal("-50.00");

        when(accountService.IsValidAccountToTransact(accountId)).thenReturn(false);

        assertThatThrownBy(() -> service.RegisterTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A operação de transação é inválida");

        verify(repository, never()).save(any(Transaction.class));
        verify(publisher, never()).publishEvent(any(TransactionCreatedEvent.class));
    }

    @Test
    void registerTransaction_ShouldThrowIllegalArgumentException_WhenOperationTypeIsInvalid() {
        RegisterTransactionRequest request = new RegisterTransactionRequest();
        request.accountId = accountId;
        request.operationTypeCode = 5; // Invalid Code
        request.amount = new BigDecimal("-50.00");

        when(accountService.IsValidAccountToTransact(accountId)).thenReturn(true);

        assertThatThrownBy(() -> service.RegisterTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O tipo de operação não está presente nos escopos permitidos");

        verify(repository, never()).save(any(Transaction.class));
        verify(publisher, never()).publishEvent(any(TransactionCreatedEvent.class));
    }
}
