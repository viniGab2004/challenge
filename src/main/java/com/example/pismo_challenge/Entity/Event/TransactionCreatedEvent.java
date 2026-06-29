package com.example.pismo_challenge.Entity.Event;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEvent(UUID accountId, BigDecimal amount) { }
