package it.bocconi.bledger.dao.service;


import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.dao.repository.BcFileBinaryRepository;
import it.bocconi.bledger.feature.file.dao.service.BcFileBinaryDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcFileBinaryDaoServiceTest extends BcAbstractDaoServiceTest<BcFileBinary, BcFileBinaryRepository, BcFileBinaryDaoService> {

    @Autowired
    public BcFileBinaryDaoServiceTest(BcFileBinaryDaoService service, BcFileBinaryRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcFileBinary getEToInsert() {
        return BcFileBinary
                .builder()
                .fileBinary("Hello, World!".getBytes())
                .build();
    }

    @Override
    protected BcFileBinary editE(BcFileBinary entity) {
        entity.setFileBinary("Hello, BcFileBinary!".getBytes());
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcFileBinary original, BcFileBinary edited) {
        return !new String(original.getFileBinary()).equals(new String(edited.getFileBinary()));
    }
}
