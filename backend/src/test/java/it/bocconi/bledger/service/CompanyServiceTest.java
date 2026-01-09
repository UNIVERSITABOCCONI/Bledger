package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.auth.filter.RequestContext;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.company.service.CompanyService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
class CompanyServiceTest {

    @Autowired
    private CompanyService companyService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void getCompaniesByTypeReturnsMatches() {
        StepVerifier.create(companyService.getCompaniesByType(CompanyType.ORG).collectList())
                .expectNextMatches(list ->
                        list.stream().anyMatch(dto -> "company-001".equals(dto.companyId()))
                )
                .verifyComplete();
    }

    @Test
    void getMyCompanyUsesRequestContext() {
        StepVerifier.create(companyService.getMyCompany()
                        .contextWrite(Context.of(RequestContext.COMPANY_ID_KEY, "company-001")))
                .expectNextMatches(dto ->
                        "company-001".equals(dto.id())
                                && "0xABCDEF1234567890".equals(dto.walletAddress())
                )
                .verifyComplete();
    }

    @Test
    void getCompanyProfileImageReturnsBinaryData() {
        StepVerifier.create(companyService.getCompanyProfileImage()
                        .contextWrite(Context.of(RequestContext.COMPANY_ID_KEY, "company-001")))
                .expectNextMatches(dto ->
                        "image/png".equals(dto.mimeType())
                                && dto.size() != null
                                && dto.fileBinary() != null
                                && dto.fileBinary().length > 0
                )
                .verifyComplete();
    }
}
