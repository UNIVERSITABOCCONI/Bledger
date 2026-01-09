package it.bocconi.bledger.feature.network.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.company.dao.row.CompanyLightRow;
import it.bocconi.bledger.feature.network.entity.BcNetworkAuditor;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BcNetworkAuditorRepository extends BcAbstractRepository<BcNetworkAuditor> {

    @Query("""
        select c.id, c.company_name from bc_network_auditor na
        join bc_company c on na.auditor_id = c.id
        where na.network_id = :networkId and na.deleted = false
        """)
    Flux<CompanyLightRow> findCompanyLightRowByNetworkId(String networkId);

    Mono<BcNetworkAuditor> findByNetworkIdAndAuditorIdAndDeletedFalse(String networkId, String companyId);

    @Modifying
    @Query("UPDATE bc_network_auditor SET requests_count = requests_count + 1 WHERE id = :id")
    Mono<Integer> incrementRequestsCount(String id);

    @Modifying
    @Query("UPDATE bc_network_auditor SET audited_count = audited_count + 1 WHERE id = :id")
    Mono<Integer> incrementAuditedCount(String id);

    @Query("""
            select wallet.wallet_address from bc_network_auditor na
            join bc_company company on company.id = na.auditor_id and company.deleted = false
            join bc_wallet wallet on wallet.id = company.wallet_id and wallet.deleted = false
            where network_id = :networkId and na.deleted = false
            """)

    Flux<String> findByNetworkIdAndDeletedFalse(String networkId);

    @Modifying
    @Query("""
        UPDATE bc_network_auditor na
        SET
          requests_count = COALESCE(stats.requests_count, 0),
          audited_count  = COALESCE(stats.audited_count, 0),
          updated_at     = NOW()
        FROM (
          SELECT
            na2.network_id,
            na2.auditor_id,
            COUNT(n.id)::bigint                                                  AS requests_count,
            COALESCE(SUM(CASE WHEN n.audited THEN 1 ELSE 0 END), 0)::bigint     AS audited_count
          FROM bc_network_auditor na2
          LEFT JOIN bc_node n
            ON n.network_id = na2.network_id
           AND n.auditor_id = na2.auditor_id
           AND n.deleted = false
          WHERE na2.deleted = false
            AND na2.network_id = :networkId
          GROUP BY na2.network_id, na2.auditor_id
        ) AS stats
        WHERE na.network_id = stats.network_id
          AND na.auditor_id = stats.auditor_id
          AND na.deleted = false
          AND na.network_id = :networkId
""")
    Mono<Integer> reloadCountsByNetworkId(String networkId);
}
