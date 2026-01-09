package it.bocconi.bledger.feature.network.dao.service;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import it.bocconi.bledger.abstraction.dao.service.BcAbstractDaoService;
import it.bocconi.bledger.feature.network.dao.repository.row.ComputeEPercentageRow;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeDetailsRow;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.feature.network.dao.repository.BcNodeRepository;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import it.bocconi.bledger.reactive.util.ReactiveUtils;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;


@Service
public class BcNodeDaoService extends BcAbstractDaoService<BcNode, BcNodeRepository> {


    private final static String SELECT_NODE_WITH_COMPANY_NAME = """
            select nd.*, c.company_name
            """;

    private final static String FROM_CLAUSE_NODE_JOIN_COMPANY =
            """
            from bc_node nd
            join bc_company c on nd.company_id = c.id
            """;

    private final static String WHERE_CONDITION_CLIENT =
            """
            WHERE nd.id = :parentId and nd.deleted = false
            """;

    private final static String WHERE_CONDITION_SUPPLIERS =
            """
            WHERE nd.parent_id = :nodeId and nd.deleted = false
                    """;

    private final static String COUNT_SELECT_CLAUSE =
            """
            select count(nd.id) as count
            """;

    private final static String GET_OTHER_MEMBERS_WHERE_CLAUSE = """
        WHERE nd.network_id = :networkId
          AND nd.deleted = false
          AND nd.id <> :id
          AND (:parentId IS NULL OR nd.id <> :parentId)
          AND (nd.parent_id IS NULL OR nd.parent_id <> :id)
        """;

    private static final String WHERE_REFUSED_IN_NETWORK = """
        WHERE nd.network_id = :networkId
          AND nd.node_status IN ('REFUSED', 'REFUSED_BY_PARENT')
    """;

    private static final String WHERE_MY_REQUESTS = """
        where nd.network_id = :networkId
          and nd.auditor_id = :auditorId
          and nd.deleted = false
        """;

    private static final String WHERE_OTHER_MEMBERS_TPA = """
        WHERE nd.network_id = :networkId
          AND nd.deleted = false
          AND (nd.auditor_id IS NULL OR nd.auditor_id <> :auditorId)
    """;


    public BcNodeDaoService(BcNodeRepository repository, R2dbcEntityTemplate template) {
        super(repository, template);
    }

    public Flux<BcNode> findByParentId(String parentId) {
        return repository.findByParentIdAndDeletedFalse(parentId);
    }

    public Mono<NodeWithCompanyNameRow> findByNetworkIdAndCompanyIdWithCompanyName(String networkId, String companyId) {
        return repository.findRowByNetworkIdAndCompanyIdAndDeletedFalse(networkId, companyId);
    }

    public Mono<BcNode> findByNetworkIdAndCompanyId(String networkId, String companyId) {
        return repository.findByNetworkIdAndCompanyIdAndDeletedFalse(networkId, companyId);
    }

    public Flux<NodeWithCompanyNameRow> findParentAndChildren(String id, String parentId) {
        return repository.findParentAndChildrenByIdAndParentId(id, parentId);
    }

    public Flux<NodeWithCompanyNameRow> findChildren(String id) {
        return repository.findChildren(id);
    }


    public Mono<NodeDetailsRow> findDetailsByNetworkIdAndCompanyName(String networkId, String companyName) {
        return repository.findDetailsByNetworkIdAndCompanyIdAndDeletedFalse(networkId, companyName);
    }

    public Mono<Double> findChildrenCompletionPercentage(String id) {
        return repository.findChildrenCompletionRatio(id)
                .flatMap(rawRatio -> Mono.just(Math.round(rawRatio * 10000) / (double) 100));
    }

    public Mono<ComputeEPercentageRow> findChildrenEComputationPercentage(String id) {
        return repository.findChildrenEComputationPercentage(id);
    }

    public Mono<Pair<Integer, Integer>> automaticAcceptAfterExpiration(LocalDate now, LocalDate childExpiry) {
//        return Mono.just(new Pair<>(0, 0));
        return repository.setApprovalExpirationDateToChildren(childExpiry, now)
                .flatMap(childCount -> repository.setAcceptedAfterExpiration(now)
                        .map(acceptedCount -> new Pair<>(acceptedCount, childCount)));
    }

    public Mono<Integer> setApprovalExpirationDateToChildrenById(String id, LocalDate childExpiry) {
        return repository.setApprovalExpirationDateToChildrenById(childExpiry, id);
    }

    public Mono<Page<NodeWithCompanyNameRow>> getClients(Pageable pageable, String parentId) {

        if(parentId == null){
            return Mono.just(new PageImpl<>(List.of(), pageable, 0));
        }

        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        // your base queries
        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_CONDITION_CLIENT;
        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_CONDITION_CLIENT;

        return findPage(
                query,
                countQuery,
                Map.of("parentId", parentId),
                pageable,
                mapToNodeWithCompanyName()
        );
    }

    public Mono<Page<NodeWithCompanyNameRow>> getSuppliers(Pageable pageable, String id) {

        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        // your base queries
        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_CONDITION_SUPPLIERS;
        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_CONDITION_SUPPLIERS;

        return findPage(
                query,
                countQuery,
                Map.of("nodeId", id),
                pageable,
                mapToNodeWithCompanyName()
        );
    }

    public Mono<Page<NodeWithCompanyNameRow>> getOtherMembers_bna(Pageable pageable, String networkId, String id, String parentId) {

        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + GET_OTHER_MEMBERS_WHERE_CLAUSE;

        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + GET_OTHER_MEMBERS_WHERE_CLAUSE;

        return findPage(
                query,
                countQuery,
                new HashMap<>() {{
                        put("networkId", networkId);
                        put("id", id);
                        put("parentId", parentId);
                }},
                pageable,
                mapToNodeWithCompanyName()
        );
    }


    public Mono<Page<NodeWithCompanyNameRow>> getRefused(Pageable pageable, String networkId) {

        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_REFUSED_IN_NETWORK;

        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_REFUSED_IN_NETWORK;

        return findPage(
                query,
                countQuery,
                Map.of("networkId", networkId),
                pageable,
                mapToNodeWithCompanyName()
        );
    }


    @NotNull
    private static BiFunction<Row, RowMetadata, NodeWithCompanyNameRow> mapToNodeWithCompanyName() {
        return (row, row_metadata) -> {
            NodeStatus node_status = Optional
                    .ofNullable(row.get("node_status", String.class))
                    .map(NodeStatus::valueOf)
                    .orElse(null);

            return new NodeWithCompanyNameRow(
                    row.get("id", String.class),
                    row.get("created_at", LocalDateTime.class),
                    row.get("updated_at", LocalDateTime.class),
                    row.get("deleted_at", LocalDateTime.class),
                    row.get("deleted", Boolean.class),
                    row.get("network_id", String.class),
                    row.get("parent_id", String.class),
                    row.get("company_id", String.class),
                    row.get("scope_file_id", String.class),
                    row.get("granular_scope1_file_id", String.class),
                    row.get("granular_scope2_file_id", String.class),
                    node_status,
                    row.get("audited", Boolean.class),
                    row.get("audited_at", LocalDateTime.class),
                    row.get("upload_file_transaction_id", String.class),
                    row.get("scope3_transaction_id", String.class),
                    row.get("audit_file_transaction_id", String.class),
                    row.get("scope1", String.class),
                    row.get("scope2", String.class),
                    row.get("total_scope1_and_scope2", String.class),
                    row.get("node_depth", Integer.class),
                    row.get("company_name", String.class),
                    row.get("quantity", BigDecimal.class),
                    row.get("transportation_emission", BigDecimal.class),
                    row.get("e_value", BigDecimal.class)
            );
        };
    }


    public Flux<NodeWithCompanyNameRow> findAllByNetworkIdWithCompanyName(String networkId) {
        return repository.findByNetworkIdAndDeletedFalse(networkId);
    }

    public Mono<Page<NodeWithCompanyNameRow>> getMyRequests(Pageable pageable, String networkId, String auditorId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_MY_REQUESTS;

        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_MY_REQUESTS;

        return findPage(
                query,
                countQuery,
                Map.of("networkId", networkId, "auditorId", auditorId),
                pageable,
                mapToNodeWithCompanyName()
        );
    }

    public Mono<Page<NodeWithCompanyNameRow>> getOtherMembers_tpa(Pageable pageable, String networkId, String auditorId) {
        pageable = ReactiveUtils.addPrefixToPageable(pageable, "nd.");

        String query = SELECT_NODE_WITH_COMPANY_NAME
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_OTHER_MEMBERS_TPA;

        String countQuery = COUNT_SELECT_CLAUSE
                + FROM_CLAUSE_NODE_JOIN_COMPANY
                + WHERE_OTHER_MEMBERS_TPA;

        return findPage(
                query,
                countQuery,
                Map.of(
                        "networkId", networkId,
                        "auditorId", auditorId
                ),
                pageable,
                mapToNodeWithCompanyName()
        );
    }

    public Mono<LocalDateTime> getChildrenLastComputeChange(String nodeId){
        return repository.findMaxLastComputeChangeChildren(nodeId);
    }
}
