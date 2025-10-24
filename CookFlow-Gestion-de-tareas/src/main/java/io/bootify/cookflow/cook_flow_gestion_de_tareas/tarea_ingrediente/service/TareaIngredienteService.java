package io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteTareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.domain.Ingrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.repos.IngredienteRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.domain.TareaIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.model.TareaIngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_ingrediente.repos.TareaIngredienteRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.domain.TareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.repos.TareaPrepRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class TareaIngredienteService {

    private final TareaIngredienteRepository tareaIngredienteRepository;
    private final TareaPrepRepository tareaPrepRepository;
    private final IngredienteRepository ingredienteRepository;

    public TareaIngredienteService(final TareaIngredienteRepository tareaIngredienteRepository,
            final TareaPrepRepository tareaPrepRepository,
            final IngredienteRepository ingredienteRepository) {
        this.tareaIngredienteRepository = tareaIngredienteRepository;
        this.tareaPrepRepository = tareaPrepRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    public List<TareaIngredienteDTO> findAll() {
        final List<TareaIngrediente> tareaIngredientes = tareaIngredienteRepository.findAll(Sort.by("idTareIngrediente"));
        return tareaIngredientes.stream()
                .map(tareaIngrediente -> mapToDTO(tareaIngrediente, new TareaIngredienteDTO()))
                .toList();
    }

    public TareaIngredienteDTO get(final Long idTareIngrediente) {
        return tareaIngredienteRepository.findById(idTareIngrediente)
                .map(tareaIngrediente -> mapToDTO(tareaIngrediente, new TareaIngredienteDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final TareaIngredienteDTO tareaIngredienteDTO) {
        final TareaIngrediente tareaIngrediente = new TareaIngrediente();
        mapToEntity(tareaIngredienteDTO, tareaIngrediente);
        return tareaIngredienteRepository.save(tareaIngrediente).getIdTareIngrediente();
    }

    public void update(final Long idTareIngrediente,
            final TareaIngredienteDTO tareaIngredienteDTO) {
        final TareaIngrediente tareaIngrediente = tareaIngredienteRepository.findById(idTareIngrediente)
                .orElseThrow(NotFoundException::new);
        mapToEntity(tareaIngredienteDTO, tareaIngrediente);
        tareaIngredienteRepository.save(tareaIngrediente);
    }

    public void delete(final Long idTareIngrediente) {
        final TareaIngrediente tareaIngrediente = tareaIngredienteRepository.findById(idTareIngrediente)
                .orElseThrow(NotFoundException::new);
        tareaIngredienteRepository.delete(tareaIngrediente);
    }

    private TareaIngredienteDTO mapToDTO(final TareaIngrediente tareaIngrediente,
            final TareaIngredienteDTO tareaIngredienteDTO) {
        tareaIngredienteDTO.setIdTareIngrediente(tareaIngrediente.getIdTareIngrediente());
        tareaIngredienteDTO.setCantidad(tareaIngrediente.getCantidad());
        tareaIngredienteDTO.setTareaPrep(tareaIngrediente.getTareaPrep() == null ? null : tareaIngrediente.getTareaPrep().getIdTareaPrep());
        tareaIngredienteDTO.setIngrediente(tareaIngrediente.getIngrediente() == null ? null : tareaIngrediente.getIngrediente().getIdIngrediente());
        return tareaIngredienteDTO;
    }

    private TareaIngrediente mapToEntity(final TareaIngredienteDTO tareaIngredienteDTO,
            final TareaIngrediente tareaIngrediente) {
        tareaIngrediente.setCantidad(tareaIngredienteDTO.getCantidad());
        final TareaPrep tareaPrep = tareaIngredienteDTO.getTareaPrep() == null ? null : tareaPrepRepository.findById(tareaIngredienteDTO.getTareaPrep())
                .orElseThrow(() -> new NotFoundException("tareaPrep not found"));
        tareaIngrediente.setTareaPrep(tareaPrep);
        final Ingrediente ingrediente = tareaIngredienteDTO.getIngrediente() == null ? null : ingredienteRepository.findById(tareaIngredienteDTO.getIngrediente())
                .orElseThrow(() -> new NotFoundException("ingrediente not found"));
        tareaIngrediente.setIngrediente(ingrediente);
        return tareaIngrediente;
    }

    @EventListener(BeforeDeleteTareaPrep.class)
    public void on(final BeforeDeleteTareaPrep event) {
        final ReferencedException referencedException = new ReferencedException();
        final TareaIngrediente tareaPrepTareaIngrediente = tareaIngredienteRepository.findFirstByTareaPrepIdTareaPrep(event.getIdTareaPrep());
        if (tareaPrepTareaIngrediente != null) {
            referencedException.setKey("tareaPrep.tareaIngrediente.tareaPrep.referenced");
            referencedException.addParam(tareaPrepTareaIngrediente.getIdTareIngrediente());
            throw referencedException;
        }
    }

    @EventListener(BeforeDeleteIngrediente.class)
    public void on(final BeforeDeleteIngrediente event) {
        final ReferencedException referencedException = new ReferencedException();
        final TareaIngrediente ingredienteTareaIngrediente = tareaIngredienteRepository.findFirstByIngredienteIdIngrediente(event.getIdIngrediente());
        if (ingredienteTareaIngrediente != null) {
            referencedException.setKey("ingrediente.tareaIngrediente.ingrediente.referenced");
            referencedException.addParam(ingredienteTareaIngrediente.getIdTareIngrediente());
            throw referencedException;
        }
    }

}
