package it.bocconi.bledger.service;

import it.bocconi.bledger.feature.file.dao.repository.BcFileBinaryRepository;
import it.bocconi.bledger.feature.file.dao.repository.BcFileRepository;
import it.bocconi.bledger.feature.file.service.FileService;
import it.bocconi.bledger.feature.network.dao.service.BcNodeDaoService;
import it.bocconi.bledger.feature.wallet.service.AdminWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles({"unit-test", "unit-test-local"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private BcFileRepository bcFileRepository;

    @Autowired
    private BcFileBinaryRepository bcFileBinaryRepository;

    @Autowired
    private BcNodeDaoService bcNodeDaoService;

    @MockBean
    private AdminWalletService adminWalletService;

    @Test
    void deleteFileDataSoftDeletesFilesAndClearsNodeLinks() {
        StepVerifier.create(fileService.deleteFileData("network-001", "company-001")
                        .then(Mono.zip(
                                bcFileRepository.findById("file-001"),
                                bcFileRepository.findById("file-002"),
                                bcNodeDaoService.findById("node-001")
                        ))
                        .flatMap(tuple -> Mono.zip(
                                bcFileBinaryRepository.findById(tuple.getT1().getBinaryId()),
                                bcFileBinaryRepository.findById(tuple.getT2().getBinaryId()),
                                Mono.just(tuple)
                        )))
                .expectNextMatches(tuple -> {
                    var fileTuple = tuple.getT3();
                    var file1 = fileTuple.getT1();
                    var file2 = fileTuple.getT2();
                    var node = fileTuple.getT3();
                    var bin1 = tuple.getT1();
                    var bin2 = tuple.getT2();

                    return Boolean.TRUE.equals(file1.getDeleted())
                            && Boolean.TRUE.equals(file2.getDeleted())
                            && Boolean.TRUE.equals(bin1.getDeleted())
                            && Boolean.TRUE.equals(bin2.getDeleted())
                            && node.getScopeFileId() == null
                            && node.getGranularScope1FileId() == null
                            && node.getGranularScope2FileId() == null;
                })
                .verifyComplete();
    }
}
