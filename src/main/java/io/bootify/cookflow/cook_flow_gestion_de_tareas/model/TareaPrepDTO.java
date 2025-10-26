package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Estado;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Prioridad;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Turno;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TareaPrepDTO {

    private Long idTareaPrep;

    @NotNull
    private LocalDate fecha;

    @NotNull
    @Size(max = 255)
    private String nombreTarea;

    @NotNull
    @Size(max = 255)
    private String descripcion;

    @NotNull
    private Prioridad prioridad;

    @NotNull
    private Estado estado;

    @NotNull
    private Turno turno;

    private Boolean archivada;

    private Long responsable;

}
