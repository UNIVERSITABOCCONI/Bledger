package it.bocconi.bledger.dao.service;

import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.smartcontract.dao.repository.BcSmartContractRepository;
import it.bocconi.bledger.feature.smartcontract.dao.service.BcSmartContractDaoService;
import it.bocconi.bledger.feature.smartcontract.entity.BcSmartContract;
import it.bocconi.bledger.feature.smartcontract.enums.SmartContractType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcSmartContractDaoServiceTest extends BcAbstractDaoServiceTest<BcSmartContract, BcSmartContractRepository, BcSmartContractDaoService> {

    @Autowired
    public BcSmartContractDaoServiceTest(BcSmartContractDaoService service, BcSmartContractRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcSmartContract getEToInsert() {
        return BcSmartContract.builder()
                .contractAddress("0xSC_TEST")
                .type(SmartContractType.ERC721)
                .build();
    }

    @Override
    protected BcSmartContract editE(BcSmartContract entity) {
        entity.setContractAddress("0xSC_EDIT");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcSmartContract original, BcSmartContract edited) {
        return !original.getContractAddress().equals(edited.getContractAddress());
    }
}
