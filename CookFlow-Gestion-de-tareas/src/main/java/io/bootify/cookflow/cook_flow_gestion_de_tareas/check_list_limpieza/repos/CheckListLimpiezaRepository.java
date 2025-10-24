package io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.domain.CheckListLimpieza;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CheckListLimpiezaRepository extends JpaRepository<CheckListLimpieza, Long> {

    CheckListLimpieza findFirstByResponsableIdUsuario(Long idUsuario);

}
