package it.bocconi.bledger.feature.network.router.dto;

import java.util.List;

public record CreateNetworkDto(String name,
                               List<String> auditorIds,
                               NetworkTreeDto tree
                               ) {
}
