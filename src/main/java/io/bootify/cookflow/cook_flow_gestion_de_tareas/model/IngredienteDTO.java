package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class IngredienteDTO {

    private Long idIngrediente;

    @NotNull
    @Size(max = 100)
    private String nombre;

    @NotNull
    @Size(max = 20)
    private String unidadMedida;

    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(type = "string", example = "68.08")
    private BigDecimal stockActual;

    @NotNull
    private Boolean activo;

}
