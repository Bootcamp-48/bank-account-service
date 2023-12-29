package com.bootcamp.bankaccountservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MaxTransactionLimitExceededException extends RuntimeException {
    public MaxTransactionLimitExceededException(String message) {
        super(message);
    }
}