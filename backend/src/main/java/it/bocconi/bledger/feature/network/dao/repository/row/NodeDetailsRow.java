package it.bocconi.bledger.feature.network.dao.repository.row;

import it.bocconi.bledger.feature.network.enums.NodeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeDetailsRow {

    String networkId;

    String networkName;

    String networkAdminId;

    String id;

    String companyId;

    String parentId;

    String scopeFileId;

    String scopeFileName;

    String granularScope1FileId;

    String granularScope1FileName;

    String granularScope2FileId;

    String granularScope2FileName;

    NodeStatus nodeStatus;

    boolean audited;

    LocalDateTime auditedAt;

    String scope1;

    String scope2;

    String totalScope1AndScope2;

    Integer nodeDepth;

    String uploadFileTransactionId;

    String auditFileTransactionId;

    String auditorId;

    BigInteger tokenId;

    LocalDateTime lastExport;

    BigDecimal eValue;

    BigDecimal scope3;

    BigDecimal productionVolume;

    LocalDateTime lastCompute;

}
