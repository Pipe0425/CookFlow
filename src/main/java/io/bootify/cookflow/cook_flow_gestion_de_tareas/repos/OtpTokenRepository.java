package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    OtpToken findFirstByUsuarioIdUsuario(Long idUsuario);

}
