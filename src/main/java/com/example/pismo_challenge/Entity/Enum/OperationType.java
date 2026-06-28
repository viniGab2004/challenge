package com.example.pismo_challenge.Entity.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationType {
    NORMAL_PURCHASE(1),
    PURCHASE_WITH_INSTALLMENTS(2),
    WITHDRAWAL(3),
    CREDIT_VOUCHER(4);

    private final int operationTypeCode;
}
