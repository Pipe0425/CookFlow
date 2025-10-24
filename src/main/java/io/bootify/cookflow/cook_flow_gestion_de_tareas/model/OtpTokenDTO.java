package io.bootify.cookflow.cook_flow_gestion_de_tareas.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OtpTokenDTO {

    private Long idOtpToken;

    @NotNull
    @Size(max = 6)
    private String codigoOtp;

    @NotNull
    private LocalDateTime expiracion;

    @NotNull
    private Boolean usado;

    private Long usuario;

}
