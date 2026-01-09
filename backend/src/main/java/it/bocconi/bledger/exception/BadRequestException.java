package it.bocconi.bledger.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends HttpStatusCodeException {
    private static final HttpStatusCode statusCode = HttpStatusCode.valueOf(400);

    public BadRequestException(String statusText) {
        super(statusCode, statusText);
    }
}
