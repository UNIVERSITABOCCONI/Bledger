package it.bocconi.bledger.feature.network.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.network.entity.BcNetwork;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface BcNetworkRepository extends BcAbstractRepository<BcNetwork> {

    // ---- Reusable SQL fragments ----
    String TABLE = "bc_network";
    String NODE_COUNT_BASE =
            "(select count(id) from bc_node nd where nd.network_id = :id and deleted = false";
    String NODE_COUNT_END = ")";

    String STATUS_UPLOADED = " and node_status='FILE_UPLOADED'";
    String STATUS_AUDITED  = " and node_status='AUDITED'";
    String COUNT_UPLOADED = NODE_COUNT_BASE + STATUS_UPLOADED + NODE_COUNT_END;
    String COUNT_AUDITED  = NODE_COUNT_BASE + STATUS_AUDITED  + NODE_COUNT_END;
    String COUNT_MEMBERS  = NODE_COUNT_BASE + NODE_COUNT_END;
    String WHERE_ID = " where id = :id";


    // ---- Queries ----

    Mono<Boolean> existsByIdAndNetworkAdminId(String networkId, String companyId);

    Mono<Boolean> existsByNetworkNameAndNetworkAdminId(String name, String adminId);

    @Modifying
    @Query("UPDATE " + TABLE + " SET uploaded_count = uploaded_count + 1, updated_at = :now" + WHERE_ID)
    Mono<Integer> incrementUploadCount(String id, LocalDateTime now);

    @Modifying
    @Query("UPDATE " + TABLE + " SET audited_count = audited_count + 1, updated_at = :now" + WHERE_ID)
    Mono<Integer> incrementAuditedCount(String id, LocalDateTime now);


    @Modifying
    @Query(
            "UPDATE " + TABLE + " SET " +
                    "uploaded_count = " + COUNT_UPLOADED + ", " +
                    "audited_count  = " + COUNT_AUDITED  + ", " +
                    "members_count  = " + COUNT_MEMBERS  + ", " +
                    "updated_at     = :now" +
                    WHERE_ID
    )
    Mono<Integer> reloadAllCounts(String id, LocalDateTime now);

    @Query("""
SELECT
  (
    -- 1) esiste almeno un nodo con company_id = :adminId
    EXISTS (
      SELECT 1
      FROM bc_node n
      WHERE n.network_id = :id
        AND n.company_id = :adminId
        AND n.deleted = false
    )
    AND
    -- 2) esiste esattamente un root (parent_id IS NULL)
    (
      SELECT COUNT(1)
      FROM bc_node r
      WHERE r.network_id = :id
        AND r.parent_id IS NULL
        AND r.deleted = false
    ) = 1
    AND
    -- 3) nessun duplicato di company_id tra i nodi attivi del network
    NOT EXISTS (
      SELECT 1
      FROM (
        SELECT company_id
        FROM bc_node x
        WHERE x.network_id = :id
          AND x.deleted = false
        GROUP BY company_id
        HAVING COUNT(*) > 1
      ) d
    )
  )
""")
    Mono<Boolean> sanityCheckTree(String id, String adminId);


}
