package it.bocconi.bledger.feature.network.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_network_tree_cache")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcNetworkTreeCache extends BcAbstractEntity {

    @Column("network_id")
    private String networkId;
    @Column("tree_json")
    private String treeJson;
}
