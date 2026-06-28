package com.example.pismo_challenge.Service;

import com.example.pismo_challenge.DTO.Transaction.Request.RegisterTransactionRequest;
import com.example.pismo_challenge.DTO.Transaction.Response.RegisterTransactionResponse;
import com.example.pismo_challenge.Entity.Enum.OperationType;
import com.example.pismo_challenge.Entity.Event.TransactionCreatedEvent;
import com.example.pismo_challenge.Entity.Model.Transaction;
import com.example.pismo_challenge.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@AllArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final ApplicationEventPublisher publisher;
    private final TransactionRepository repository;

    @Transactional
    public RegisterTransactionResponse RegisterTransaction(RegisterTransactionRequest transactionRequest)
    {
        if(IsValidTransaction(transactionRequest) && accountService.IsValidAccountToTransact(transactionRequest.getAccountId()))
        {
            Transaction transactionData = RegisterTransactionObjectBuilder(transactionRequest);
            var repositoryResponse = repository.save(transactionData);
            publisher.publishEvent(new TransactionCreatedEvent(repositoryResponse.getAccountId(), repositoryResponse.getTransactAmount()));
            return RegisterTransactionResponse.builder()
                    .transactionId(repositoryResponse.getTransactionId())
                    .build();
        }

        throw new IllegalArgumentException("A operação de transação é inválida");
    }

    private Transaction RegisterTransactionObjectBuilder(RegisterTransactionRequest transactionRequest)
    {
        return Transaction.builder()
                .transactionId(UUID.randomUUID())
                .accountId(transactionRequest.getAccountId())
                .operationTypeCode(OperationTypeDefining(transactionRequest.getOperationTypeCode()))
                .transactAmount(transactionRequest.getAmount())
                .eventDate(LocalDateTime.now())
                .build();
    }

    private int OperationTypeDefining(int operationType)
    {
        return switch (operationType) {
            case 1 -> OperationType.NORMAL_PURCHASE.getOperationTypeCode();
            case 2 -> OperationType.PURCHASE_WITH_INSTALLMENTS.getOperationTypeCode();
            case 3 -> OperationType.WITHDRAWAL.getOperationTypeCode();
            case 4 -> OperationType.CREDIT_VOUCHER.getOperationTypeCode();
            default -> throw new IllegalArgumentException("O tipo de operação não está presente nos escopos permitidos");
        };
    }

    private boolean IsValidTransaction(RegisterTransactionRequest request) {
        if (request.getOperationTypeCode() == OperationType.CREDIT_VOUCHER.getOperationTypeCode()) {
            return request.getAmount() > 0;
        }
        return request.getAmount() < 0;
    }
}
