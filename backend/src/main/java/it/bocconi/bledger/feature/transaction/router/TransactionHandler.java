// it.bocconi.bledger.feature.transaction.router.TransactionHandler
package it.bocconi.bledger.feature.transaction.router;

import it.bocconi.bledger.feature.transaction.service.TransactionService;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class TransactionHandler {

    private final TransactionService transactionService;

    public Mono<ServerResponse> getNetworkTransactions(ServerRequest req) {
        String networkId = req.pathVariable("networkId");
        Pageable pageable = ReactiveUtils.extractPageable(req);

        return transactionService.getNetworkTransactions(networkId, pageable)
                .flatMap(page -> ServerResponse.ok().bodyValue(page))
                .doOnError(e -> log.error("Error fetching transactions for network {}", networkId, e));
    }

    public Mono<ServerResponse> getNodeTransactions(ServerRequest req) {
        String nodeId = req.pathVariable("nodeId");
        Pageable pageable = ReactiveUtils.extractPageable(req);

        return  transactionService.getNodeTransactions(nodeId, pageable)
                .flatMap(page -> ServerResponse.ok().bodyValue(page));
    }

    public Mono<ServerResponse> pollStatus(ServerRequest req) {
        String txId = req.pathVariable("transactionId");
        return transactionService.findByIdAuthorized(txId)
                .flatMap(tx -> ServerResponse.ok().bodyValue(tx));
    }


}
