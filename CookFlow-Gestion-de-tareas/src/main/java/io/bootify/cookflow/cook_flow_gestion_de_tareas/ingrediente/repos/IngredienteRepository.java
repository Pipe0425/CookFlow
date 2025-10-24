package io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.domain.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
}
