package it.bocconi.bledger.feature.network.router.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record NetworkDetailsDto(
        String id,
        String networkName,
        boolean amIAdmin,
        Double progressPercentage,
        Double progressEcomputation,
        BigInteger tokenId,
        String smartContractAddress,
        LocalDateTime lastExport,
        LocalDateTime lastCompute,
        LocalDateTime lastVersionExport,
        LocalDateTime lastVersionCompute,
        NodeDto myNode

) {
}
