package io.bootify.cookflow.cook_flow_gestion_de_tareas.otp_token.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.otp_token.domain.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    OtpToken findFirstByUsuarioIdUsuario(Long idUsuario);

}
