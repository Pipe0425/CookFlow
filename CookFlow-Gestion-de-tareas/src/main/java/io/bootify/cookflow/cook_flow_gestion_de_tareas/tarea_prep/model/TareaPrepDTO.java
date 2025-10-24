package io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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

    private Boolean archivada;

    private Long responsable;

}
