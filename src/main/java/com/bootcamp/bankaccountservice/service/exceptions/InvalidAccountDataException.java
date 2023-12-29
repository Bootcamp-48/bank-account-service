package com.bootcamp.bankaccountservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAccountDataException extends RuntimeException {

    public InvalidAccountDataException(String message) {
        super("Invalid customer details for " + message);
    }

}