package it.bocconi.bledger.feature.file.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import it.bocconi.bledger.feature.file.dao.repository.BcFileBinaryRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BcFileBinaryDaoService extends BcAbstractDaoService<BcFileBinary, BcFileBinaryRepository> {
    public BcFileBinaryDaoService(BcFileBinaryRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Mono<BcFileBinary> findByFileId(String fileId) {
        return repository.findByFileIdAndDeletedFalse(fileId);
    }
}
