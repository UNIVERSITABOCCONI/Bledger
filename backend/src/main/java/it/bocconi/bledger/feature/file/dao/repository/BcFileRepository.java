package it.bocconi.bledger.feature.file.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.file.entity.BcFile;
import it.bocconi.bledger.feature.file.dao.row.impl.CompanyProfileImageRowImpl;
import it.bocconi.bledger.feature.smartcontract.entity.DocumentMetadataDto;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BcFileRepository extends BcAbstractRepository<BcFile> {

    @Query("""
        SELECT
            f.file_name AS file_name,
            f.mime_type AS mime_type,
            f.size AS size,
            b.file_binary AS file_binary
        FROM bc_file f
        JOIN bc_company c ON c.profile_image_id = f.id
        JOIN bc_file_binary b ON f.binary_id = b.id
        WHERE c.id = :companyId
          AND c.deleted = false
          AND f.deleted = false
          AND b.deleted = false
    """)
    Mono<CompanyProfileImageRowImpl> getCompanyProfileImageRow(String companyId);

    @Query(
            """
                SELECT
                  doc.id,
                  doc.keccak256 as document_hash,
                  doc.type,
                  node.id AS node_id,
                  node.audited
                FROM bc_file AS doc
                JOIN bc_node AS node
                  ON doc.id IN (
                       node.scope_file_id,
                       node.granular_scope1_file_id,
                       node.granular_scope2_file_id
                     )
                WHERE node.deleted = FALSE
                  AND doc.deleted = FALSE and node.network_id = :networkId
            """
    )
    Flux<DocumentMetadataDto> findAllNetworkFiles(String networkId);
}
