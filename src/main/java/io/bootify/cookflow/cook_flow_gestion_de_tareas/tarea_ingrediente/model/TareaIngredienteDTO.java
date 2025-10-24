package io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TareaIngredienteDTO {

    private Long idTareIngrediente;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(type = "string", example = "22.08")
    private BigDecimal cantidad;

    private Long tareaPrep;

    private Long ingrediente;

}
