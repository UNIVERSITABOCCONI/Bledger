package it.bocconi.bledger.abstraction.entity;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BcAbstractEntity implements Persistable<String> {

    @Id
    @Column("id")
    private String id;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.isNew = true;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void preDelete() {
        this.deletedAt = LocalDateTime.now();
        this.deleted = true;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markAsNotNew() {
        this.isNew = false;
    }

}
