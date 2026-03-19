package com.manulife.studentportal.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessLogicException extends BaseException {

    public BusinessLogicException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}