package it.bocconi.bledger.feature.network.entity;

import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;

@Table("bc_network")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcNetwork extends BcAbstractEntity {

    @Column("root_id")
    private String rootId;

    @Column("token_id")
    private BigInteger tokenId;

    @Column("network_admin_id")
    private String networkAdminId;

    @Column("network_name")
    private String networkName;

    @Column("members_count")
    @Builder.Default
    private Long membersCount = 0L;

    @Column("uploaded_count")
    @Builder.Default
    private Long uploadedCount = 0L;

    @Column("audited_count")
    @Builder.Default
    private Long auditedCount = 0L;
}
