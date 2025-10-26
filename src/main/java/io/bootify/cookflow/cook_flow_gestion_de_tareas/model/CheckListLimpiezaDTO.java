package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CheckListLimpiezaDTO {

    private Long idCheckListLimpieza;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private String turno;

    private Boolean cocinaLimpia;

    private Boolean utenciliosLimpios;

    private Boolean residuosDesechados;

    @NotNull
    private String estadoGeneral;

    private Long responsable;

}
