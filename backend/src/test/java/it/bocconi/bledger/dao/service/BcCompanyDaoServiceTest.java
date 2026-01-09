package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.company.entity.BcCompany;
import it.bocconi.bledger.feature.company.enums.CompanyType;
import it.bocconi.bledger.feature.company.dao.repository.BcCompanyRepository;
import it.bocconi.bledger.feature.company.dao.service.BcCompanyDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcCompanyDaoServiceTest extends BcAbstractDaoServiceTest<BcCompany, BcCompanyRepository, BcCompanyDaoService> {

    @Autowired
    public BcCompanyDaoServiceTest(BcCompanyDaoService service, BcCompanyRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcCompany getEToInsert() {
        return BcCompany
                .builder()
                .email("company@comp.it")
                .nation("ITALY")
                .companyName("Company Srl")
                .representativeName("Giovanni")
                .representativeSurname("Rossi")
                .idType("ID_CARD")
                .idNumber("AB1234567")
                .walletId("wallet-001")
                .profileImageId("file-001")
                .companyType(CompanyType.ORG)
                .build();
    }

    @Override
    protected BcCompany editE(BcCompany entity) {
        entity.setCompanyName("Company 2 Srl");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcCompany original, BcCompany edited) {
        return !original.getCompanyName().equals(edited.getCompanyName());
    }
}
