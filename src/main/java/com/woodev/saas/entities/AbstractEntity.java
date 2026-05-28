package com.woodev.saas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractEntity {

    @Id
    @GeneratedValue(strategy = UUID)
    //updatable = false ça veut dire que je ne peux pas modifier l'identifiant, même si j'exécute une requête.
    //Je parle toujours dans le contexte de Spring et Hibernate.
    //Mais vous pouvez le modifier directement dans la base
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    //updatable = false, cela veut dire qu'on ne doit pas modifier la valeur de CreatedAt une fois insérée.
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    //InsertableFalse : veut dire que lorsqu'on exécute une requête d'insertion,
    //la valeur de UpdatedAt doit être nulle pour dire que cet attribut, cette ligne n'a pas été modifiée
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    private String updatedBy;

    //Gérer la suppression logique au lieu de supprimer réellement les données
    //à partir de la base de données, ou bien à partir de la table.
    //nullable = false : je veux que la valeur par défaut de cet attribut Deleted, soit automatiquement égal à False.
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;


    @PrePersist
    protected void onCreate() {
        if(this.deleted == null) {
            this.deleted = Boolean.FALSE;
        }
        //TODO : this has to be deleted once security is implemented
        if(this.createdBy == null) {
            this.createdBy = "SYSTEM";
        }

    }
}
