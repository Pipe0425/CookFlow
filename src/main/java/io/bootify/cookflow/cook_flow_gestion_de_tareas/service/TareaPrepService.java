package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Estado;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Turno;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteTareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaPrepDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.TareaPrepRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.ReferencedException;

import java.time.LocalDate;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class TareaPrepService {

    private final TareaPrepRepository tareaPrepRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher publisher;

    public TareaPrepService(final TareaPrepRepository tareaPrepRepository,
            final UsuarioRepository usuarioRepository, final ApplicationEventPublisher publisher) {
        this.tareaPrepRepository = tareaPrepRepository;
        this.usuarioRepository = usuarioRepository;
        this.publisher = publisher;
    }

    public List<TareaPrepDTO> findAll() {
        final List<TareaPrep> tareaPreps = tareaPrepRepository.findAll(Sort.by("idTareaPrep"));
        return tareaPreps.stream()
                .map(tareaPrep -> mapToDTO(tareaPrep, new TareaPrepDTO()))
                .toList();
    }

    public TareaPrepDTO get(final Long idTareaPrep) {
        return tareaPrepRepository.findById(idTareaPrep)
                .map(tareaPrep -> mapToDTO(tareaPrep, new TareaPrepDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final TareaPrepDTO tareaPrepDTO) {
        final TareaPrep tareaPrep = new TareaPrep();
        mapToEntity(tareaPrepDTO, tareaPrep);
        return tareaPrepRepository.save(tareaPrep).getIdTareaPrep();
    }

    public void update(final Long idTareaPrep, final TareaPrepDTO tareaPrepDTO) {
        final TareaPrep tareaPrep = tareaPrepRepository.findById(idTareaPrep)
                .orElseThrow(NotFoundException::new);
        mapToEntity(tareaPrepDTO, tareaPrep);
        tareaPrepRepository.save(tareaPrep);
    }

    public void delete(final Long idTareaPrep) {
        final TareaPrep tareaPrep = tareaPrepRepository.findById(idTareaPrep)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteTareaPrep(idTareaPrep));
        tareaPrepRepository.delete(tareaPrep);
    }

    private TareaPrepDTO mapToDTO(final TareaPrep tareaPrep, final TareaPrepDTO tareaPrepDTO) {
        tareaPrepDTO.setIdTareaPrep(tareaPrep.getIdTareaPrep());
        tareaPrepDTO.setFecha(tareaPrep.getFecha());
        tareaPrepDTO.setNombreTarea(tareaPrep.getNombreTarea());
        tareaPrepDTO.setDescripcion(tareaPrep.getDescripcion());
        tareaPrepDTO.setPrioridad(tareaPrep.getPrioridad());
        tareaPrepDTO.setEstado(tareaPrep.getEstado());
        tareaPrepDTO.setTurno(tareaPrep.getTurno());
        tareaPrepDTO.setArchivada(tareaPrep.getArchivada());
        tareaPrepDTO.setResponsable(tareaPrep.getResponsable() == null ? null : tareaPrep.getResponsable().getIdUsuario());
        return tareaPrepDTO;
    }

    private TareaPrep mapToEntity(final TareaPrepDTO tareaPrepDTO, final TareaPrep tareaPrep) {
        tareaPrep.setFecha(tareaPrepDTO.getFecha());
        tareaPrep.setNombreTarea(tareaPrepDTO.getNombreTarea());
        tareaPrep.setDescripcion(tareaPrepDTO.getDescripcion());
        tareaPrep.setPrioridad(tareaPrepDTO.getPrioridad());
        tareaPrep.setEstado(tareaPrepDTO.getEstado());
        tareaPrep.setTurno(tareaPrepDTO.getTurno());
        tareaPrep.setArchivada(tareaPrepDTO.getArchivada());
        final Usuario responsable = tareaPrepDTO.getResponsable() == null ? null : usuarioRepository.findById(tareaPrepDTO.getResponsable())
                .orElseThrow(() -> new NotFoundException("responsable not found"));
        tareaPrep.setResponsable(responsable);
        return tareaPrep;
    }

    @EventListener(BeforeDeleteUsuario.class)
    public void on(final BeforeDeleteUsuario event) {
        final ReferencedException referencedException = new ReferencedException();
        final TareaPrep responsableTareaPrep = tareaPrepRepository.findFirstByResponsableIdUsuario(event.getIdUsuario());
        if (responsableTareaPrep != null) {
            referencedException.setKey("usuario.tareaPrep.responsable.referenced");
            referencedException.addParam(responsableTareaPrep.getIdTareaPrep());
            throw referencedException;
        }
    }

    // Filtrar por fecha + turno (no archivadas por defecto)
    public List<TareaPrepDTO> getTasksByDateAndTurn(final LocalDate fecha, final Turno turno, final boolean includeArchived) {
        final List<TareaPrep> tareas;
        if (includeArchived) {
            // devuelve todas (archivadas y no)
            tareas = tareaPrepRepository.findAllByFechaAndTurno(fecha, turno);
        } else {
            // comportamiento previo: sólo no archivadas
            tareas = tareaPrepRepository.findAllByFechaAndTurnoAndArchivadaFalse(fecha, turno);
        }
        return tareas.stream()
                .map(t -> mapToDTO(t, new TareaPrepDTO()))
                .toList();
    }

    // Cambiar estado (PATCH /state)
    public void setState(final Long idTareaPrep, final Estado nuevoEstado) {
        final TareaPrep tarea = tareaPrepRepository.findById(idTareaPrep)
                .orElseThrow(NotFoundException::new);
        tarea.setEstado(nuevoEstado);
        tareaPrepRepository.save(tarea);
    }

    // Archivar tarea (marcar archivada = true)
    public void archiveTask(final Long idTareaPrep) {
        final TareaPrep tarea = tareaPrepRepository.findById(idTareaPrep)
                .orElseThrow(NotFoundException::new);
        tarea.setArchivada(true);
        tareaPrepRepository.save(tarea);
    }

}
