package it.bocconi.bledger.dao.service;


import it.bocconi.bledger.abstraction.BcAbstractDaoServiceTest;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.enums.FileStatus;
import it.bocconi.bledger.feature.file.enums.FileType;
import it.bocconi.bledger.feature.file.dao.repository.BcFileRepository;
import it.bocconi.bledger.feature.file.dao.service.BcFileDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
public class BcFileDaoServiceTest extends BcAbstractDaoServiceTest<BcFile, BcFileRepository, BcFileDaoService> {

    @Autowired
    public BcFileDaoServiceTest(BcFileDaoService service, BcFileRepository repository) {
        super(service, repository);
    }

    @Override
    protected BcFile getEToInsert() {
        return BcFile.builder()
                .type(FileType.PROFILE_IMAGE)
                .mimeType("image/png")
                .size(500L)
                .fileName("profile-image.png")
                .keccak256("sha256-hash-value")
                .binaryId("binary-001")
                .fileStatus(FileStatus.CONFIRMED)
                .extractedData("{\"width\":64}")
                .build();
    }

    @Override
    protected BcFile editE(BcFile entity) {
        entity.setMimeType("image/jpeg");
        return entity;
    }

    @Override
    protected boolean isEditEffective(BcFile original, BcFile edited) {
        return original.getMimeType() != null && !original.getMimeType().equals(edited.getMimeType());
    }
}
