package it.bocconi.bledger.feature.transaction.router.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TransactionOperationWrapper<T> {

    private String transactionId;
    private T content;
}
