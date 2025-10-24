package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.CheckListLimpieza;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CheckListLimpiezaRepository extends JpaRepository<CheckListLimpieza, Long> {

    CheckListLimpieza findFirstByResponsableIdUsuario(Long idUsuario);

}
