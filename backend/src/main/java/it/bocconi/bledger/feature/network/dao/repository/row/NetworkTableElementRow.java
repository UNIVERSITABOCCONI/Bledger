package it.bocconi.bledger.feature.network.dao.repository.row;

import it.bocconi.bledger.feature.network.enums.NodeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NetworkTableElementRow {

    private String id;
    private String networkName;
    private Long membersCount;
    private NodeStatus nodeStatus;
    private LocalDateTime auditedAt;
    private String uploadFileTransactionId;
    private String auditFileTransactionId;
}
