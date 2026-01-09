package it.bocconi.bledger.feature.network.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.network.enums.NodeMetadataType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_node_metadata")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcNodeMetadata extends BcAbstractEntity {

    @Column("node_id")
    private String nodeId;

    @Column("type")
    private NodeMetadataType metadataType;

    @Column("metadata_key")
    private String metadataKey;

    @Column("metadata_value")
    private String metadataValue;

    @Column("category")
    private String category;

    @Column("source_of_emission")
    private String sourceOfEmission;

    @Column("unit_of_measure")
    private String unitOfMeasure;

}
