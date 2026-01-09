package it.bocconi.bledger.exception;

import org.springframework.web.client.HttpStatusCodeException;

public class NotFoundException extends HttpStatusCodeException {
    private static final org.springframework.http.HttpStatusCode statusCode = org.springframework.http.HttpStatus.NOT_FOUND;

    public NotFoundException(String statusText) {
        super(statusCode, statusText);
    }
}
