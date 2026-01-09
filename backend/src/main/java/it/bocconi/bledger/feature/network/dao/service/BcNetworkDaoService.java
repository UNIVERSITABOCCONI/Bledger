package it.bocconi.bledger.feature.network.dao.service;

import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.network.dao.repository.row.NetworkTableElementRowTpa;
import it.bocconi.bledger.feature.network.entity.BcNetwork;
import it.bocconi.bledger.feature.network.dao.repository.BcNetworkRepository;
import it.bocconi.bledger.feature.network.dao.repository.row.NetworkTableElementRow;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;


@Service
public class BcNetworkDaoService extends BcAbstractDaoService<BcNetwork, BcNetworkRepository> {

    String FROM_CLAUSE_GET_MY_NETWORK = """
        FROM bc_network n
        LEFT JOIN bc_node nd ON nd.network_id = n.id AND nd.company_id = :companyId AND nd.deleted = false
        """;

    String SELECT_CLAUSE_GET_MY_NETWORKS = """
        SELECT n.id, n.network_name, n.members_count, nd.node_status, nd.audited_at, nd.upload_file_transaction_id, nd.audit_file_transaction_id
        """;

    String SELECT_CLAUSE_GET_MY_NETWORKS_TPA = """
        SELECT n.id, n.network_name, n.members_count, na.requests_count, na.audited_count
        """;

    String SELECT_CLAUSE_GET_MY_NETWORKS_COUNT_TPA = """
        SELECT COUNT(n.id) AS count
        """;
    String FROM_CLAUSE_GET_MY_NETWORKS_TPA = """
        FROM bc_network n
        JOIN bc_network_auditor na ON na.network_id = n.id AND na.auditor_id = :companyId AND na.deleted = false
        where n.deleted = false
            """;

    String SELECT_CLAUSE_GET_MY_NETWORKS_COUNT = """
        SELECT COUNT(n.id) AS count
        """;

    String FROM_CLAUSE_INVITATION_QUERY = """
            from bc_network n
            join bc_node nd ON nd.network_id = n.id AND nd.company_id = :companyId AND nd.deleted = false
            left join bc_node nd_parent on nd_parent.id = nd.parent_id
            where nd.node_status = 'INVITED' and (nd_parent.id is null or nd_parent.node_status != 'INVITED')
            """;

    String SELECT_CLAUSE_GET_MY_INVITATIONS = """
            select n.id, n.network_name
            """;

    String SELECT_CLAUSE_GET_MY_INVITATIONS_COUNT = """
            select count(n.id) as count
            """;


    public BcNetworkDaoService(BcNetworkRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Mono<Page<NetworkTableElementRow>> getMyNetworks_bna(Pageable pageable, String companyId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "n.");
        String selectQuery = SELECT_CLAUSE_GET_MY_NETWORKS + FROM_CLAUSE_GET_MY_NETWORK + " WHERE n.network_admin_id = :companyId";
        String countQuery = SELECT_CLAUSE_GET_MY_NETWORKS_COUNT + FROM_CLAUSE_GET_MY_NETWORK + " WHERE n.network_admin_id = :companyId";

        return getNetworkTableElementRowPage(pageable, companyId, selectQuery, countQuery);
    }

    public Mono<Page<NetworkTableElementRow>> getMyNetworks_bu(Pageable pageable, String companyId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "n.");
        String selectQuery = SELECT_CLAUSE_GET_MY_NETWORKS + FROM_CLAUSE_GET_MY_NETWORK + " WHERE nd.id is not null AND n.network_admin_id <> :companyId AND nd.node_status in ('ACCEPTED', 'AUDITED', 'FILE_UPLOADED')";
        String countQuery = SELECT_CLAUSE_GET_MY_NETWORKS_COUNT + FROM_CLAUSE_GET_MY_NETWORK + " WHERE nd.id is not null AND n.network_admin_id <> :companyId AND nd.node_status in ('ACCEPTED', 'AUDITED', 'FILE_UPLOADED')";

        return getNetworkTableElementRowPage(pageable, companyId, selectQuery, countQuery);
    }

    public Mono<Page<NetworkTableElementRowTpa>> getMyNetworks_tpa(Pageable pageable, String companyId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "n.");
        String selectQuery = SELECT_CLAUSE_GET_MY_NETWORKS_TPA + FROM_CLAUSE_GET_MY_NETWORKS_TPA;
        String countQuery = SELECT_CLAUSE_GET_MY_NETWORKS_COUNT_TPA + FROM_CLAUSE_GET_MY_NETWORKS_TPA;

        return getNetworkTableElementRowPageTpa(pageable, companyId, selectQuery, countQuery);
    }

    public Mono<Page<NetworkTableElementRow>> getInvitations(Pageable pageable, String companyId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "n.");
        String selectQuery = SELECT_CLAUSE_GET_MY_INVITATIONS + FROM_CLAUSE_INVITATION_QUERY;
        String countQuery = SELECT_CLAUSE_GET_MY_INVITATIONS_COUNT + FROM_CLAUSE_INVITATION_QUERY;
        return getNetworkTableElementRowPageInvitations(pageable, companyId, selectQuery, countQuery);
    }

    private Mono<Page<NetworkTableElementRow>> getNetworkTableElementRowPage(Pageable pageable, String companyId, String selectQuery, String countQuery) {
        return findPage(selectQuery, countQuery,
                Map.of(
                        "companyId", companyId
                ),
                pageable,
                (row, rowMetadata) -> new NetworkTableElementRow(
                        row.get("id", String.class),
                        row.get("network_name", String.class),
                        row.get("members_count", Long.class),
                        row.get("node_status", String.class) != null ? NodeStatus.valueOf(row.get("node_status", String.class)) : null,
                        row.get("audited_at", java.time.LocalDateTime.class),
                        row.get("upload_file_transaction_id", String.class),
                        row.get("audit_file_transaction_id", String.class)
                )
        );
    }

    private Mono<Page<NetworkTableElementRowTpa>> getNetworkTableElementRowPageTpa(Pageable pageable, String companyId, String selectQuery, String countQuery) {
        return findPage(selectQuery, countQuery,
                Map.of(
                        "companyId", companyId
                ),
                pageable,
                (row, rowMetadata) -> new NetworkTableElementRowTpa(
                        row.get("id", String.class),
                        row.get("network_name", String.class),
                        row.get("members_count", Long.class),
                        row.get("requests_count", Long.class),
                        row.get("audited_count", Long.class)
                )
        );
    }


    private Mono<Page<NetworkTableElementRow>> getNetworkTableElementRowPageInvitations(Pageable pageable, String companyId, String selectQuery, String countQuery) {
        return findPage(selectQuery, countQuery,
                Map.of(
                        "companyId", companyId
                ),
                pageable,
                (row, rowMetadata) -> new NetworkTableElementRow(
                        row.get("id", String.class),
                        row.get("network_name", String.class),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    public Mono<Boolean> isCompanyNetworkAdmin(String networkId, String companyId) {
        return repository.existsByIdAndNetworkAdminId(networkId, companyId);
    }


    public Mono<Integer> incrementUploadCount(String networkId) {
        return repository.incrementUploadCount(networkId, LocalDateTime.now());
    }

    public Mono<Integer> incrementAuditedCount(String networkId) {
        return repository.incrementAuditedCount(networkId, LocalDateTime.now());
    }

    public Mono<Integer> reloadAllCounts(String networkId) {
        return repository.reloadAllCounts(networkId, LocalDateTime.now());
    }

    public Mono<Boolean> sanityCheckTree(String networkId, String adminId) {
        return repository.sanityCheckTree(networkId, adminId);
    }

    public Mono<Boolean> existsByNameAndNetworkAdminId(String name, String companyId){
        return repository.existsByNetworkNameAndNetworkAdminId(name, companyId);
    }
}
