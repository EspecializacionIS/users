package com.clinic.users.domain.exception;

import ch.qos.logback.classic.Logger;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import java.util.Objects;

public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    private DomainException wrap(String op, CognitoIdentityProviderException e) {
        String msg = Objects.nonNull(e.awsErrorDetails())
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();

        Logger log = null;
        log.error("Cognito operation {} failed: {}", op, msg);
        return new DomainException("Cognito error on " + op + ": " + msg, e);
    }
}
