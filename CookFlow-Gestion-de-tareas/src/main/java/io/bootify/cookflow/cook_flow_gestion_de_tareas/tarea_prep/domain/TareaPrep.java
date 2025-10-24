package io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.domain;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.domain.TareaIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "TareaPreps")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TareaPrep {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTareaPrep;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String nombreTarea;

    @Column(nullable = false)
    private String descripcion;

    @Column
    private Boolean archivada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @OneToMany(mappedBy = "tareaPrep")
    private Set<TareaIngrediente> tareaIngredientes = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
