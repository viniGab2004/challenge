package com.example.pismo_challenge.Entity.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "client")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @Id
    @Column(name = "clientId")
    @JdbcTypeCode(SqlTypes.CHAR)
    public UUID clientId;
    @Column(name = "name")
    public String Name;
    @Column(name = "documentNumber")
    public Long documentNumber;
    @Builder.Default
    @Column(name = "isAlive")
    public Boolean isAlive = Boolean.TRUE;
    @Column(name = "contactCellphone")
    public String contactCellphone;
    @Column(name = "gender")
    public String Gender;
    @Column(name = "originState")
    public String originState;
}
