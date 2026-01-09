package it.bocconi.bledger.feature.network.router.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NetworkElementDto {

    private String id;
    private String networkId;
    private String parentId;
    private String parentCompanyId;
    private String companyId;
    private String companyName;
    private Integer nodeDepth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
