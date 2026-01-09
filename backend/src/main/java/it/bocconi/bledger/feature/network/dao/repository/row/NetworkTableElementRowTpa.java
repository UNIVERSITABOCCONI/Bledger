package it.bocconi.bledger.feature.network.dao.repository.row;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NetworkTableElementRowTpa {

    private String id;
    private String networkName;
    private Long membersCount;
    private Long requestCount;
    private Long auditedCount;
}
