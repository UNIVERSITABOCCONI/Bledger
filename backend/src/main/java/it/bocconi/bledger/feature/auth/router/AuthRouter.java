package it.bocconi.bledger.feature.auth.router;

import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration(proxyBeanMethods = false)
public class AuthRouter {

    private final static String AUTH_PATH = "/auth";
    private final static String COMMON_PATH = BLPaths.COMMON + AUTH_PATH;
    private final static String PUBLIC_PATH = BLPaths.PUBLIC + AUTH_PATH;

    @Bean
    public RouterFunction<ServerResponse> authRoute(AuthHandler handler, AuthFilterAppender authFilterAppender) {
        return authFilterAppender.routerFunctionWithAuthFiler(RouterFunctions
                .route(POST(PUBLIC_PATH + "/login"), handler::login)
                .andRoute(POST(COMMON_PATH + "/logout"), request -> handler.logout()));
    }
}
