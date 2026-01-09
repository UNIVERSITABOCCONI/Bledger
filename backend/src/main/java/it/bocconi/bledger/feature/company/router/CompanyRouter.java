package it.bocconi.bledger.feature.company.router;

import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.auth.filter.AuthFilterAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;


@Configuration(proxyBeanMethods = false)
public class CompanyRouter {

    private final static String COMPANY_PATH = "/company";
    private final static String COMMON_PATH = BLPaths.COMMON + COMPANY_PATH;
    private final static String PUBLIC_PATH = BLPaths.PUBLIC + COMPANY_PATH;


    @Bean
    public RouterFunction<ServerResponse> companyRoute(CompanyHandler handler, AuthFilterAppender authFilterAppender) {
        return authFilterAppender.routerFunctionWithAuthFiler(RouterFunctions
                .route(GET(PUBLIC_PATH + "/get-company-names-by-type/{type}"), handler::getCompaniesByType)
                .andRoute(GET(COMMON_PATH + "/my-company"), handler::getMyCompany)
                .andRoute(GET(COMMON_PATH + "/my-profile-image"), handler::getCompanyProfileImage));
    }
}
