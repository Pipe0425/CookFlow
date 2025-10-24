package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
