package com.example.pismo_challenge.Entity.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountSituation {
    ACCOUNT_WITH_NO_PENDING(1),
    ACCOUNT_WITH_PENDING(2);

    private final int situationCode;
}
