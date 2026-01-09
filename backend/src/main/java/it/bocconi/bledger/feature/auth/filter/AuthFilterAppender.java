package it.bocconi.bledger.feature.auth.filter;


import it.bocconi.bledger.exception.ForbiddenException;
import it.bocconi.bledger.exception.UnauthorizedException;
import it.bocconi.bledger.feature.auth.enums.BLPaths;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.network.dao.service.BcNetworkDaoService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * This filter implements a **very simple authentication and authorization model**
 * designed exclusively for a **Proof of Concept (PoC)**.
 *
 * <p>
 * Access to application APIs is granted through the presence of a
 * {@code BL_COMPANY_ID} cookie and a set of logical checks based on:
 * <ul>
 *   <li>the company type</li>
 *   <li>the requested path</li>
 *   <li>network-level permissions (e.g. network admin role)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Administrative (AM) endpoints are protected using **Basic Authentication**
 * with credentials provided via application configuration properties.
 * </p>
 *
 * <p>
 * This approach allows fast validation of business flows and authorization rules
 * while keeping the system lightweight.
 * </p>
 *
 * <b>IMPORTANT:</b>
 * <p>
 * This authentication model is <b>NOT suitable for production environments</b>.
 * In real-world scenarios, the application must rely on <b>Spring Security</b>,
 * using:
 * <ul>
 *   <li>secure password handling (hashing and salting)</li>
 *   <li>session-based authentication or <b>JWT tokens</b></li>
 *   <li>centralized role and permission management</li>
 *   <li>protection against common security vulnerabilities (e.g. CSRF, credential leakage)</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class should therefore be considered a temporary solution for the PoC phase.
 * </p>
 */


@Component
public class AuthFilterAppender {
    private final BcCompanyDaoService bcCompanyDaoService;
    private final BcNetworkDaoService bcNetworkDaoService;
    private final String amUsername;
    private final String amPassword;

    public AuthFilterAppender(BcCompanyDaoService bcCompanyDaoService, BcNetworkDaoService bcNetworkDaoService,
                              @Value("${it.bocconi.bledger.am.username}") String amUsername,
                              @Value("${it.bocconi.bledger.am.password}") String amPassword) {
        this.bcCompanyDaoService = bcCompanyDaoService;
        this.bcNetworkDaoService = bcNetworkDaoService;
        this.amUsername = amUsername;
        this.amPassword = amPassword;
    }

    public RouterFunction<ServerResponse> routerFunctionWithAuthFiler(RouterFunction<ServerResponse> routerFunction) {
        return routerFunction
                .filter((request, next) -> {
                    String path = request.path();

                    if(path.startsWith(BLPaths.AM)) {
                        return manageAmPath(request, next);
                    }

                    if(path.startsWith(BLPaths.PUBLIC)) {
                        return nextWithNoContext(request, next);
                    }

                    Optional<HttpCookie> companyIdCookie = Optional.ofNullable(request.cookies().getFirst("BL_COMPANY_ID"));

                    if (companyIdCookie.isEmpty()) {
                        return unauthorized("Missing BL_COMPANY_ID cookie");
                    }

                    String companyId = companyIdCookie.get().getValue();

                    if (StringUtils.isBlank(companyId)) {
                        return unauthorized("Empty BL_COMPANY_ID cookie");
                    }

                    return bcCompanyDaoService.findById(companyId)
                           .switchIfEmpty(Mono.error(new UnauthorizedException("Company not found for id: " + companyId)))
                            .flatMap(company -> manageApplicationPath(request, next, company, path, companyId));
                });
    }



    @NotNull
    private Mono<ServerResponse> manageApplicationPath(ServerRequest request, HandlerFunction<ServerResponse> next, BcCompany company, String path, String companyId) {
        if((path.startsWith(BLPaths.BNA) || path.startsWith(BLPaths.BU)) && company.getCompanyType() == CompanyType.TPA) {
            return forbidden("TPA company cannot access BNA or BU paths");
        } else if(path.startsWith(BLPaths.TPA) && company.getCompanyType() != CompanyType.TPA) {
            return forbidden("Non-TPA company cannot access TPA paths");
        }


        if(path.startsWith(BLPaths.BNA+"/network") && request.pathVariables().containsKey("networkId")){
            String networkId = request.pathVariable("networkId");

            return bcNetworkDaoService.isCompanyNetworkAdmin(networkId, companyId)
                    .flatMap(isAdmin -> {
                        if (!isAdmin) {
                            return forbidden("Company is not admin of the network: " + networkId);
                        }
                        return nextWithContext(request, next, companyId);
                    });

        }

        return nextWithContext(request, next, companyId);
    }

    @NotNull
    private Mono<ServerResponse> manageAmPath(ServerRequest request, HandlerFunction<ServerResponse> next) {
        String authHeader = request.headers().firstHeader("Authorization");
        if(StringUtils.isBlank(authHeader)) {
            return unauthorized("Missing AM credentials");
        }
        String base64Credentials = authHeader.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if(!amUsername.equals(values[0]) || !amPassword.equals(values[1])){
            return unauthorized("Invalid AM credentials");
        } else {
            return nextWithNoContext(request, next);
        }
    }


    @NotNull
    private static Mono<ServerResponse> nextWithContext(ServerRequest request, HandlerFunction<ServerResponse> next, String companyId) {
        return next.handle(request)
                .contextWrite(ctx -> ctx.put(RequestContext.COMPANY_ID_KEY, companyId));
    }

    @NotNull
    private static Mono<ServerResponse> nextWithNoContext(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request);
    }


    private static Mono<ServerResponse> unauthorized(String message) {
        return Mono.error(new UnauthorizedException(message));
    }

    private static Mono<ServerResponse> forbidden(String message) {
        return Mono.error(new ForbiddenException(message));
    }

}
