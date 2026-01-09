package it.bocconi.bledger.service;

import it.bocconi.bledger.exception.UnauthorizedException;
import it.bocconi.bledger.feature.auth.router.dto.LoginRequestDto;
import it.bocconi.bledger.feature.auth.service.AuthService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void loginReturnsCompanyIdWhenCompanyExists() {
        StepVerifier.create(authService.login(new LoginRequestDto("company-001")))
                .expectNext("company-001")
                .verifyComplete();
    }

    @Test
    void loginFailsWhenCompanyMissing() {
        StepVerifier.create(authService.login(new LoginRequestDto("missing-company")))
                .expectError(UnauthorizedException.class)
                .verify();
    }
}
