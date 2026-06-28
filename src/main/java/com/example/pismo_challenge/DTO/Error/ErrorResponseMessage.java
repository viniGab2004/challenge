package com.example.pismo_challenge.DTO.Error;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseMessage {
    private int statusCode;
    public String errorMessage;
    private LocalDateTime timeStamp;
}
