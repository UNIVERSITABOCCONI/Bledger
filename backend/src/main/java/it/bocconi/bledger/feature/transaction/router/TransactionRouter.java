package it.bocconi.bledger.feature.transaction.router;

import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class TransactionRouter {

    private static final String TX_PATH   = "/transaction";
    private static final String COMMON    = BLPaths.COMMON + TX_PATH;

    @Bean
    public RouterFunction<ServerResponse> transactionRoute(
            TransactionHandler handler,
            AuthFilterAppender authFilterAppender
    ) {
        return authFilterAppender.routerFunctionWithAuthFiler(
                RouterFunctions
                        .route(GET(COMMON + "/get-network-transactions/{networkId}"), handler::getNetworkTransactions)
                        .andRoute(GET(COMMON + "/get-node-transactions/{nodeId}"), handler::getNodeTransactions)
                        .andRoute(GET(COMMON + "/poll-status/{transactionId}"), handler::pollStatus)
        );
    }
}
