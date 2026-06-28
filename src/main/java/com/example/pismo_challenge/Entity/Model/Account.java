package com.example.pismo_challenge.Entity.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    public UUID accountId;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "clientId")
    public UUID clientId;
    @Column(name = "accountNumber")
    public int accountNumber;
    @Column(name = "totalAmount")
    public float totalAmount;
    @Builder.Default
    @Column(name = "isActive")
    public Boolean isActive = Boolean.TRUE;
    @Builder.Default
    @Column(name = "canTransact")
    public Boolean canTransact = Boolean.TRUE;
    @Column(name = "accountSituation")
    public int accountSituation;
}
