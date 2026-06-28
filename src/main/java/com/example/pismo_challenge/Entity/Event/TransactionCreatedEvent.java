package com.example.pismo_challenge.Entity.Event;

import java.util.UUID;

public record TransactionCreatedEvent(UUID accountId, float amount) { }
