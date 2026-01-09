package it.bocconi.bledger.exception;


import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

public class UnauthorizedException extends HttpStatusCodeException {
    private static final HttpStatusCode statusCode = HttpStatusCode.valueOf(401);

    public UnauthorizedException(String statusText) {
        super(statusCode, statusText);
    }
}
