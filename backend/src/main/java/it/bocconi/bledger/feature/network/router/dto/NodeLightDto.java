package it.bocconi.bledger.feature.network.router.dto;


import it.bocconi.bledger.feature.network.enums.NodeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class NodeLightDto extends NetworkElementDto{

    private NodeStatus nodeStatus;
    private LocalDateTime auditedAt;
    private String uploadFileTransactionId;
    private String auditFileTransactionId;
    private BigDecimal quantity;
    private BigDecimal transportationEmission;
}
