package it.bocconi.bledger.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class ForbiddenException extends HttpStatusCodeException {
    private static final HttpStatusCode statusCode = org.springframework.http.HttpStatus.FORBIDDEN;

    public ForbiddenException(String statusText) {
        super(statusCode, statusText);
    }
}
