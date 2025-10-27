package io.bootify.cookflow.cook_flow_gestion_de_tareas.repos;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Turno;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface TareaPrepRepository extends JpaRepository<TareaPrep, Long> {

    TareaPrep findFirstByResponsableIdUsuario(Long idUsuario);

    // Para filtrar por fecha y turno:
    List<TareaPrep> findAllByFechaAndTurno(LocalDate fecha, Turno turno);

    // Si quieres ignorar archivadas:
    List<TareaPrep> findAllByFechaAndTurnoAndArchivadaFalse(LocalDate fecha, Turno turno);

}
