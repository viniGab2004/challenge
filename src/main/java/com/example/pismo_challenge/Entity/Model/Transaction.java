package com.example.pismo_challenge.Entity.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.SQLType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Table(name = "transactions")
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "transactionId")
    public UUID transactionId;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "accountId")
    public UUID accountId;
    @Column(name = "operationTypeCode")
    public int operationTypeCode;
    @Column(name = "transactAmount")
    public float transactAmount;
    @Column(name = "eventDate")
    public LocalDateTime eventDate;
}
