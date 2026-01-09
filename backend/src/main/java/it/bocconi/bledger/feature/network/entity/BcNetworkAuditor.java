package it.bocconi.bledger.feature.network.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_network_auditor")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcNetworkAuditor extends BcAbstractEntity {

    @Column("auditor_id")
    private String auditorId;

    @Column("network_id")
    private String networkId;

    @Column("requests_count")
    @Builder.Default
    private Long requestsCount = 0L;

    @Column("audited_count")
    @Builder.Default
    private Long auditedCount = 0L;
}
