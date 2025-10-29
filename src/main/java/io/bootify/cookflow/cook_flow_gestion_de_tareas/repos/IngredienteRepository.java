package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Ingrediente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    Optional<Ingrediente> findByNombre(String nombre);
}
