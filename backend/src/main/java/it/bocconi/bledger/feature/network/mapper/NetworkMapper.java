package it.bocconi.bledger.feature.network.mapper;


import it.bocconi.bledger.feature.network.dao.repository.row.NodeDetailsRow;
import it.bocconi.bledger.feature.network.dao.repository.row.NodeWithCompanyNameRow;
import it.bocconi.bledger.feature.network.entity.BcNetwork;
import it.bocconi.bledger.feature.network.entity.BcNode;
import it.bocconi.bledger.feature.network.router.dto.*;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, componentModel = MappingConstants.ComponentModel.SPRING)
public interface NetworkMapper {

    @Mapping(target = "tree", source = "tree")
    @Mapping(target = "id", source = "bcNetwork.id")
    NetworkDto toNetworkDto(BcNetwork bcNetwork, NetworkTreeDto tree);

    BcNode toEntity(NodeWithCompanyNameRow node);

    NodeLightDto toNodeLightDto(NodeWithCompanyNameRow node);

    NetworkTreeDto toNetworkElementDto(NodeWithCompanyNameRow nodeWithCompanyNameRow);


    @Named("nodeDtoFromNodeDetailsRow")
    NodeDto toNodeDto(NodeDetailsRow nodeDetailsRow);

    NodeDto toNodeDto(BcNode node);

    GetAuditorRequestsDto toGetAuditorRequestsDto(NodeWithCompanyNameRow nodeWithCompanyNameRow);
}
