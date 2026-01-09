package it.bocconi.bledger.feature.file.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.dao.repository.BcFileRepository;
import it.bocconi.bledger.feature.file.dao.row.CompanyProfileImageRow;
import it.bocconi.bledger.feature.smartcontract.entity.DocumentMetadataDto;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class BcFileDaoService extends BcAbstractDaoService<BcFile, BcFileRepository> {

    public BcFileDaoService(BcFileRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    @Transactional(readOnly = true)
    public Mono<? extends CompanyProfileImageRow> companyProfileImageRow(String companyId) {
        return repository.getCompanyProfileImageRow(companyId);
    }

    @Transactional(readOnly = true)
    public Flux<DocumentMetadataDto> findAllNetworkFiles(String networkId) {
        return repository.findAllNetworkFiles(networkId);
    }


}
