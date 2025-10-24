package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaPrep;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TareaPrepRepository extends JpaRepository<TareaPrep, Long> {

    TareaPrep findFirstByResponsableIdUsuario(Long idUsuario);

}
