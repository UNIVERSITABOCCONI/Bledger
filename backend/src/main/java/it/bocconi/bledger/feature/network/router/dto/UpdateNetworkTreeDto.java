package it.bocconi.bledger.feature.network.router.dto;

import java.util.List;

public record UpdateNetworkTreeDto(
        NetworkTreeDto updatedTree,
        List<String>            removed
) {}
