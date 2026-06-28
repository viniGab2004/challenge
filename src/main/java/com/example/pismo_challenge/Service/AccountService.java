package com.example.pismo_challenge.Service;

import com.example.pismo_challenge.DTO.Account.Request.CreateAccountRequest;
import com.example.pismo_challenge.DTO.Account.Response.CreateAccountResponse;
import com.example.pismo_challenge.DTO.Account.Response.GetAccountResponse;
import com.example.pismo_challenge.Entity.Enum.AccountSituation;
import com.example.pismo_challenge.Entity.Event.ClientDiedEvent;
import com.example.pismo_challenge.Entity.Event.TransactionCreatedEvent;
import com.example.pismo_challenge.Entity.Model.Account;
import com.example.pismo_challenge.Repository.AccountRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
public class AccountService {

    private final ClientService clientService;
    private final AccountRepository repository;

    public CreateAccountResponse CreateAccount(CreateAccountRequest accountRequest)
    {
            var client = clientService.FindClientByDocumentNumber(accountRequest.getDocumentNumber());
            if(ExistsAccountByClientId(client.getClientId()))
            {
                throw new EntityExistsException("Uma conta já existe para o cliente indicado");
            }
            if(!client.getIsAlive())
            {
                throw new IllegalStateException("Não é permitido fazer a criação de contas para uma pessoa morta");
            }
            Account accountData = CreateAccountObjectBuilder(client.getClientId());
            var repositoryResponse = repository.save(accountData);
            return  CreateAccountResponse.builder()
                    .accountId(repositoryResponse.getAccountId())
                    .build();
    }

    public GetAccountResponse GetAccountById(UUID accountId)
    {
        var accountData = repository.findAccountByAccountId(accountId);
        if(accountData == null)
        {
            throw new EntityNotFoundException("Não existe nenhuma conta registrada com o identificador indicado");
        }
        var clientData = clientService.FindClientById(accountData.getClientId());
        return GetAccountResponse.builder()
                .accountId(accountData.getAccountId())
                .documentNumber(clientData.getDocumentNumber())
                .build();
    }

    public boolean IsValidAccountToTransact(UUID accountId)
    {
        var accountResponse = repository.findAccountByAccountId(accountId);
        if(accountResponse == null)
        {
            throw new EntityNotFoundException("Não existe nenhuma conta registrada com o identificador indicado");
        }
        return accountResponse.getCanTransact();
    }

    private boolean ExistsAccountByClientId(UUID clientId)
    {
        return repository.existsByClientId(clientId);
    }

    private Account CreateAccountObjectBuilder(UUID clientId)
    {
        return Account.builder()
                .accountId(UUID.randomUUID())
                .clientId(clientId)
                .totalAmount(0)
                .accountNumber(GenerateAccountNumber())
                .accountSituation(AccountSituation.ACCOUNT_WITH_NO_PENDING.getSituationCode())
                .build();
    }

    private int GenerateAccountNumber() {
        return ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
    }

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void FreezeAccount(ClientDiedEvent event)
    {
         var account = repository.findAccountByClientId(event.clientId());
         if(account != null)
         {
             account.setIsActive(false);
             account.setCanTransact(false);
             repository.save(account);
         }
    }

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void UpdateTotalAmount(TransactionCreatedEvent event)
    {
        var account = repository.findAccountByAccountId(event.accountId());
        if(account != null)
        {
            float newTotalAmount = event.amount() + account.getTotalAmount();
            account.setTotalAmount(newTotalAmount);

            if(account.getTotalAmount() < 0)
            {
                account.setAccountSituation(AccountSituation.ACCOUNT_WITH_PENDING.getSituationCode());
            }
            else
            {
                account.setAccountSituation(AccountSituation.ACCOUNT_WITH_NO_PENDING.getSituationCode());
            }

            repository.save(account);
        }
    }
}
