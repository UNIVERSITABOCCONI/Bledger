package it.bocconi.bledger.feature.auth.router;

import it.bocconi.bledger.feature.auth.router.dto.LoginRequestDto;
import it.bocconi.bledger.feature.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static it.bocconi.bledger.reactive.util.ReactiveUtils.getBodyFromRequest;

@Component
@Slf4j
public class AuthHandler {

    private final AuthService authService;

    @Value("${it.bocconi.bledger.auth.login.cookie.max-age:864000}")
    private long cookieMaxAge;

    @Value("${it.bocconi.bledger.auth.login.cookie.path:/}")
    private String cookiePath;

    public AuthHandler(AuthService authService,
                       @Value("${it.bocconi.bledger.auth.login.cookie.max-age:864000}") long cookieMaxAge,
                       @Value("${it.bocconi.bledger.auth.login.cookie.path:/}") String cookiePath
                       ){
        this.authService = authService;
        this.cookieMaxAge = cookieMaxAge;
        this.cookiePath = cookiePath;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return getBodyFromRequest(request, LoginRequestDto.class)
                .flatMap(loginRequestDto ->
                        authService.login(loginRequestDto)
                                .flatMap(response ->
                                        responseWithCookie(response, cookieMaxAge)
                                )
                );
    }

    public Mono<ServerResponse> logout() {
        return responseWithCookie("", 0);
    }



    @NotNull
    private Mono<ServerResponse> responseWithCookie(String cookieValue, long maxAge) {
        return ServerResponse.ok()
                .cookie(ResponseCookie.from("BL_COMPANY_ID", cookieValue)
                        .httpOnly(true)
                        .secure(true)
                        .path(cookiePath)
                        .sameSite("Strict")
                        .maxAge(maxAge)
                        .build())
                .build();
    }
}
