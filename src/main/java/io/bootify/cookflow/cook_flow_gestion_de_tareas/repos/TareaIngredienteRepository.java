package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaIngrediente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TareaIngredienteRepository extends JpaRepository<TareaIngrediente, Long> {

    // Todas las asociaciones de una tarea
    List<TareaIngrediente> findByTareaPrepIdTareaPrep(Long idTareaPrep);

    // Buscar asociación por tarea + ingrediente (para upsert)
    Optional<TareaIngrediente> findByTareaPrepIdTareaPrepAndIngredienteIdIngrediente(Long idTareaPrep, Long idIngrediente);

    // Buscar primera asociación (ya tienes similares en el service)
    TareaIngrediente findFirstByTareaPrepIdTareaPrep(Long idTareaPrep);

    TareaIngrediente findFirstByIngredienteIdIngrediente(Long idIngrediente);
    

}
