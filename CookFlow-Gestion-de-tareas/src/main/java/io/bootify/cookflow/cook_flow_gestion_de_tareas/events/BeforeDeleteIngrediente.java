package io.bootify.cookflow.cook_flow_gestion_de_tareas.events;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BeforeDeleteIngrediente {

    private Long idIngrediente;

}
