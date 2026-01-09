package it.bocconi.bledger.feature.am.router;


import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class AmRouter {

    private final static String AM_PATH = BLPaths.AM;

    @Bean
    public RouterFunction<ServerResponse> amRoute(AmHandler handler, AuthFilterAppender authFilterAppender) {
        return authFilterAppender.routerFunctionWithAuthFiler(RouterFunctions
                .route(POST(AM_PATH + "/add-companies"), handler::handleUploadCompanies));
    }
}
