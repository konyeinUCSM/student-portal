package com.manulife.studentportal.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends BaseException {

    public InvalidOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}