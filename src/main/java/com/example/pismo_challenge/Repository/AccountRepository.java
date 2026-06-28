package com.example.pismo_challenge.Repository;

import com.example.pismo_challenge.Entity.Model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findAccountByAccountId(UUID accountId);
    Account findAccountByClientId(UUID clientId);
    boolean existsByClientId(UUID clientId);
}
