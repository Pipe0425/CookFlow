package io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.domain.TareaPrep;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TareaPrepRepository extends JpaRepository<TareaPrep, Long> {

    TareaPrep findFirstByResponsableIdUsuario(Long idUsuario);

}
