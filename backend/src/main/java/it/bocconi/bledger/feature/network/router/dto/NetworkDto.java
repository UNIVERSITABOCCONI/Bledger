package it.bocconi.bledger.feature.network.router.dto;

public record NetworkDto (

        String id,
        String rootId,
        String networkName,
        String networkAdminId,
        Long membersCount,
        Long uploadedCount,
        Long auditedCount,
        NetworkTreeDto tree
) {
}
