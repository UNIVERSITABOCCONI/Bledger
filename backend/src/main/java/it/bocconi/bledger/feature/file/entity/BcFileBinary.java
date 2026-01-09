package it.bocconi.bledger.feature.file.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


/**
 * NOTE:
 * For the Proof of Concept (PoC), files are stored directly in the database
 * as binary data for simplicity and faster development.
 *
 * In a production environment, files should be stored in an external storage
 * solution (e.g. object storage, file system, or cloud storage), and only
 * references/metadata (such as file path, URL, or identifier) should be
 * persisted in the database to improve performance, scalability, and
 * maintainability.
 */

@Table("bc_file_binary")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcFileBinary extends BcAbstractEntity {

    @Column("file_binary")
    private byte[] fileBinary;
}
