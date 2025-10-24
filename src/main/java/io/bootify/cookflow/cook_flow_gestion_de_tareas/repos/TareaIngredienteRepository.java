package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TareaIngredienteRepository extends JpaRepository<TareaIngrediente, Long> {

    TareaIngrediente findFirstByTareaPrepIdTareaPrep(Long idTareaPrep);

    TareaIngrediente findFirstByIngredienteIdIngrediente(Long idIngrediente);

}
