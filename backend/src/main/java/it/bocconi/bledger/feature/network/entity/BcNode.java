package it.bocconi.bledger.feature.network.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("bc_node")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcNode extends BcAbstractEntity implements IBcNode{

    @Column("network_id")
    private String networkId;

    @Column("parent_id")
    private String parentId;

    @Column("company_id")
    private String companyId;

    @Column("scope_file_id")
    private String scopeFileId;

    @Column("granular_scope1_file_id")
    private String granularScope1FileId;

    @Column("granular_scope2_file_id")
    private String granularScope2FileId;

    @Column("node_status")
    private NodeStatus nodeStatus;

    @Column("audited")
    @Builder.Default
    private Boolean audited = false;

    @Column("audited_at")
    private LocalDateTime auditedAt;

    @Column("upload_file_transaction_id")
    private String uploadFileTransactionId;

    @Column("scope3_transaction_id")
    private String scope3TransactionId;

    @Column("audit_file_transaction_id")
    private String auditFileTransactionId;

    @Column("scope1")
    private String scope1;

    @Column("scope2")
    private String scope2;

    @Column("scope3")
    private BigDecimal scope3;

    @Column("e_value")
    private BigDecimal eValue;

    @Column("production_volume")
    private BigDecimal productionVolume;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("transportation_emission")
    private BigDecimal transportationEmission;

    @Column("total_scope1_and_scope2")
    private String totalScope1AndScope2;

    @Column("node_depth")
    private Integer nodeDepth;

    @Column("approval_expiration_date")
    private LocalDate approvalExpirationDate;

    @Column("auditor_id")
    private String auditorId;

    @Column("last_export")
    private LocalDateTime lastExport;

    @Column("last_compute")
    private LocalDateTime lastCompute;

    @Column("last_compute_change")
    @Builder.Default
    private LocalDateTime lastComputeChange = LocalDateTime.now();
}
