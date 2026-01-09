package it.bocconi.bledger.feature.network.dao.repository.row;

import it.bocconi.bledger.feature.network.entity.IBcNode;
import it.bocconi.bledger.feature.network.enums.NodeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class NodeWithCompanyNameRow implements IBcNode {

     private String id;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
     private LocalDateTime deletedAt;
     private Boolean deleted;
     private String networkId;
     private String parentId;
     private String companyId;
     private String scopeFileId;
     private String granularScope1FileId;
     private String granularScope2FileId;
     private NodeStatus nodeStatus;
     private Boolean audited;
     private LocalDateTime auditedAt;
     private String uploadFileTransactionId;
     private String scope3TransactionId;
     private String auditFileTransactionId;
     private String scope1;
     private String scope2;
     private String totalScope1AndScope2;
     private Integer nodeDepth;
     private String companyName;
     private BigDecimal quantity;
     private BigDecimal transportationEmission;
     private BigDecimal eValue;

}

