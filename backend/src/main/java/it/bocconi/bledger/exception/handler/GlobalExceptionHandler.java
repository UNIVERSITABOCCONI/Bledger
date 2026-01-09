package it.bocconi.bledger.exception.handler;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import reactor.core.publisher.Mono;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleHttpException(HttpStatusCodeException ex) {
        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiErrorResponse(
                        ex.getStatusCode().toString(),
                        ex.getStatusText()
                )));
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            R2dbcDataIntegrityViolationException.class,
            SQLIntegrityConstraintViolationException.class
    })
    public Mono<ResponseEntity<ApiErrorResponse>> handleDbConstraintViolation(Exception ex) {
        log.warn("Database constraint violation: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "BAD_REQUEST",
                        "Invalid data"
                )));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleGeneralError(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "Unexpected error, please retry" //do not expose unmanaged errors to clients
                )));
    }
}
