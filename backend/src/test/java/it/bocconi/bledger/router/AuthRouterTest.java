/*
package it.bocconi.bledger.router;


import it.bocconi.bledger.feature.auth.router.dto.LoginRequestDto;
import it.bocconi.bledger.feature.auth.enums.BLRole;
import it.bocconi.bledger.feature.auth.enums.BLPaths;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Base64;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class AuthRouterTest {

    @Autowired
    private RouterFunction<ServerResponse> authRoute;



    @Test
    void testLogin_shouldWork(){
        WebTestClient client = WebTestClient.bindToRouterFunction(authRoute).build();

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "company-001"
        );

        String expectedCookieValue = new String(Base64.getEncoder().encode(String.format("%s:%s", loginRequestDto.companyId(), loginRequestDto.role().name()).getBytes()));
        client.post()
                .uri(BLPaths.PUBLIC + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(
                        "Set-Cookie",
                        "SESSION_TOKEN=" + expectedCookieValue +
                                "; Path=/; Max-Age=864000; Expires=.*?; Secure; HttpOnly; SameSite=Strict"
                );

    }

    @Test
    void testLoginWithInvalidOrganization_shouldReturnUnauthorized() {
        WebTestClient client = WebTestClient.bindToRouterFunction(authRoute).build();

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "invalid-org"
        );

        client.post()
                .uri(BLPaths.PUBLIC + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequestDto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLogoutClearsCookie() {
        WebTestClient client = WebTestClient.bindToRouterFunction(authRoute).build();

        client.post()
                .uri(BLPaths.COMMON + "/auth/logout")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Set-Cookie", "SESSION_TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict");
    }
}
*/
