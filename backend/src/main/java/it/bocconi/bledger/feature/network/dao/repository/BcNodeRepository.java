package it.bocconi.bledger.feature.network.dao.repository;

import it.bocconi.bledger.abstraction.dao.repository.BcAbstractRepository;
import it.bocconi.bledger.feature.network.dao.repository.row.ComputeEPercentageRow;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeDetailsRow;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BcNodeRepository extends BcAbstractRepository<BcNode> {

    String PARENTS_AND_CHILDREN_ORDER_BY = """
            order by
                case
                    when nd.id = :parentId then 0     -- Parent node first
                    when nd.parent_id = :id then 1  -- Children after
                    else 2                    -- Others, if any
                end
            """;

    String PARENTS_AND_CHILDREN_WHERE_CONDITION = """
        where ((nd.id = :parentId and :parentId is not null) or nd.parent_id = :id) and nd.deleted = false
        """;

    String CHILDREN_WHERE_CONDITION = """
            where nd.parent_id = :parentId and nd.deleted = false
            """;

    String EXPIRED_APPROVATION_WHERE_CONDITION = """
            WHERE deleted = false
            AND node_status = 'INVITED'
            AND approval_expiration_date <= :now
            """    ;

    String UPDATE_APPROVAL_EXPIRATION_DATE = """
              UPDATE bc_node
              SET approval_expiration_date = :childExpiry
            """;


    Flux<BcNode> findByParentIdAndDeletedFalse(String parentId);

    Mono<BcNode> findByNetworkIdAndCompanyIdAndDeletedFalse(String networkId, String companyId);


    @Query("""
        select nd.*, c.company_name from bc_node nd
        join bc_company c on nd.company_id = c.id
        where nd.network_id = :networkId and nd.company_id = :companyId and nd.deleted = false
        """)
    Mono<NodeWithCompanyNameRow> findRowByNetworkIdAndCompanyIdAndDeletedFalse(String networkId, String companyId);

    @Query("""
            select nd.*, c.company_name from bc_node nd
            join bc_company c on nd.company_id = c.id
            """ + PARENTS_AND_CHILDREN_WHERE_CONDITION
            + PARENTS_AND_CHILDREN_ORDER_BY
    )
    Flux<NodeWithCompanyNameRow> findParentAndChildrenByIdAndParentId(String id, String parentId);

    @Query("""
            select nd.*, c.company_name from bc_node nd
            join bc_company c on nd.company_id = c.id
            """ + CHILDREN_WHERE_CONDITION
    )
    Flux<NodeWithCompanyNameRow> findChildren(String parentId);

    @Query("""
            select nd.*, n.id as network_id, n.network_name, n.network_admin_id, f1.file_name as scope_file_name,
                   f2.file_name as granular_scope1_file_name, f3.file_name as granular_scope2_file_name,
                   nd.upload_file_transaction_id, nd.audit_file_transaction_id, nd.auditor_id, n.token_id, nd.last_export,
                   nd.production_volume, nd.scope3, nd.e_value
                    from bc_node nd
                    join bc_network n on nd.network_id = n.id and n.id = :networkId and n.deleted = false
                    left join bc_file f1 on nd.scope_file_id = f1.id and f1.deleted = false
                    left join bc_file f2 on nd.granular_scope1_file_id = f2.id and f2.deleted = false
                    left join bc_file f3 on nd.granular_scope2_file_id = f3.id and f3.deleted = false
                    where nd.company_id = :companyId and nd.deleted = false
            """)

    Mono<NodeDetailsRow> findDetailsByNetworkIdAndCompanyIdAndDeletedFalse(String networkId, String companyId);

    @Query("""
            select
                case
                    when count(*) = 0 then 1
                    else 1.0 * sum(case when nd.node_status not in ('INVITED', 'CREATED', 'ACCEPTED') then 1 else 0 end)
                         / count(*)
                end as ratio
                from bc_node nd
    """ + CHILDREN_WHERE_CONDITION)
    Mono<Double> findChildrenCompletionRatio(String id);

    @Query("""
            select
                case
                    when count(nd.id) = 0 then 1.0
                    else 1.0 * sum(case when nd.e_value is not null and nd.quantity is not null and nd.transportation_emission is not null then 1 else 0 end)
                         / count(*)
                end as ratio,
                count(nd.id) as count
                from bc_node nd
    """ + CHILDREN_WHERE_CONDITION)
    Mono<ComputeEPercentageRow> findChildrenEComputationPercentage(String id);


    @Modifying
    @Query(UPDATE_APPROVAL_EXPIRATION_DATE + """
              WHERE deleted = false AND node_status= 'INVITED' and parent_id IN (
                SELECT id
                FROM bc_node
                """
            + EXPIRED_APPROVATION_WHERE_CONDITION + " )")
    Mono<Integer> setApprovalExpirationDateToChildren(LocalDate childExpiry, LocalDate now);

    @Modifying
    @Query("""
            UPDATE bc_node
              SET node_status = 'ACCEPTED'
            """ + EXPIRED_APPROVATION_WHERE_CONDITION)
    Mono<Integer> setAcceptedAfterExpiration(LocalDate now);


    @Modifying
    @Query(UPDATE_APPROVAL_EXPIRATION_DATE + """
            WHERE id = :parentId AND deleted = false AND node_status = 'INVITED'
            """)
    Mono<Integer> setApprovalExpirationDateToChildrenById(LocalDate childExpiry, String parentId);


    @Query("""
        select nd.*, c.company_name from bc_node nd
        join bc_company c on nd.company_id = c.id
        where nd.network_id = :networkId and nd.deleted = false
        """)
    Flux<NodeWithCompanyNameRow> findByNetworkIdAndDeletedFalse(String networkId);

    @Query("""
            select max(nd.updated_at) from bc_node nd
            """ + CHILDREN_WHERE_CONDITION)
    Mono<LocalDateTime> findMaxUpdatedAtChildren(String parentId);

    @Query("""
            select max(nd.last_compute_change) from bc_node nd
            """ + CHILDREN_WHERE_CONDITION)
    Mono<LocalDateTime> findMaxLastComputeChangeChildren(String parentId);
}
