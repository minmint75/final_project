package com.example.final_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountNotApprovedException extends RuntimeException {

    public AccountNotApprovedException(String message) {
        super(message);
    }

    public AccountNotApprovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
