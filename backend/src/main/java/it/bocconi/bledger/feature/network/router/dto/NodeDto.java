package it.bocconi.bledger.feature.network.router.dto;

import it.bocconi.bledger.feature.network.enums.NodeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NodeDto(
        String id,
        String parentId,
        String companyId,
        String scopeFileId,
        String scopeFileName,
        String granularScope1FileId,
        String granularScope1FileName,
        String granularScope2FileId,
        String granularScope2FileName,
        NodeStatus nodeStatus,
        Boolean audited,
        LocalDateTime auditedAt,
        String uploadFileTransactionId,
        String auditFileTransactionId,
        String scope1,
        String scope2,
        String totalScope1AndScope2,
        Integer nodeDepth,
        String auditorId,
        BigDecimal eValue,
        BigDecimal scope3,
        BigDecimal productionVolume

) {
}
