package it.bocconi.bledger.feature.file.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.file.entity.BcFileBinary;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface BcFileBinaryRepository extends BcAbstractRepository<BcFileBinary> {

    @Query("SELECT * FROM bc_file_binary WHERE id = (select binary_id from bc_file where id = :fileId and deleted = false) AND deleted = false")
    Mono<BcFileBinary> findByFileIdAndDeletedFalse(String fileId);
}
