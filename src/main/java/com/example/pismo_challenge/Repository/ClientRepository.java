package com.example.pismo_challenge.Repository;

import com.example.pismo_challenge.DTO.Client.Response.RegisterClientResponse;
import com.example.pismo_challenge.Entity.Model.Client;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByDocumentNumber(Long documentNumber);
    Client findByDocumentNumber(Long documentNumber);
    Client findClientByClientId(UUID clientId);
}
