package it.bocconi.bledger.exception.handler;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiErrorResponse {
    private String errorCode;
    private String errorMessage;
}
