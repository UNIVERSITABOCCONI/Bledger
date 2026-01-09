package it.bocconi.bledger.feature.file.entity;


import it.bocconi.bledger.abstraction.entity.BcAbstractEntity;
import it.bocconi.bledger.feature.file.enums.FileStatus;
import it.bocconi.bledger.feature.file.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bc_file")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcFile extends BcAbstractEntity {

    @Column("type")
    private FileType type;

    @Column("mime_type")
    private String mimeType;

    @Column("size")
    private Long size;

    @Column("file_name")
    private String fileName;

    @Column("keccak256")
    private String keccak256;

    @Column("binary_id")
    private String binaryId;

    @Column("file_status")
    private FileStatus fileStatus;

    @Column("extracted_data")
    private String extractedData;
 }
