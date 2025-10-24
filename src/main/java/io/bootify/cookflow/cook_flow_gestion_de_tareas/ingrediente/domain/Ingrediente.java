package io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.domain;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.domain.TareaIngrediente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "Ingredientes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Ingrediente {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIngrediente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String unidadMedida;

    @Column(precision = 10, scale = 2)
    private BigDecimal stockActual;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "ingrediente")
    private Set<TareaIngrediente> tareasIngrediente = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
