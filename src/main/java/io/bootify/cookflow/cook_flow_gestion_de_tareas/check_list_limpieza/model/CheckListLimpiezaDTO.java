package io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.model;

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

    private Boolean cocinaLimpia;

    private Boolean utenciliosLimpios;

    private Boolean residuosDesechados;

    private Long responsable;

}
