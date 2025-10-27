package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Ingrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.TareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteTareaPrep;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaIngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.IngredienteRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.TareaIngredienteRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.TareaPrepRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.ReferencedException;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    // --- EXISTENTES --- (findAll, get, create, update, delete) ...

    // Nuevo: listar por tarea (DTO)
    public List<TareaIngredienteDTO> findByTaskId(final Long taskId) {
        final List<TareaIngrediente> list = tareaIngredienteRepository.findByTareaPrepIdTareaPrep(taskId);
        return list.stream()
                .map(ti -> mapToDTO(ti, new TareaIngredienteDTO()))
                .toList();
    }

    // Nuevo: listar por tarea, pero en formato enriquecido (útil para frontend)
    public List<Map<String, Object>> findByTaskIdEnriched(final Long taskId) {
        final List<TareaIngrediente> list = tareaIngredienteRepository.findByTareaPrepIdTareaPrep(taskId);
        List<Map<String,Object>> out = new ArrayList<>();
        for (TareaIngrediente ti : list) {
            Map<String,Object> m = new HashMap<>();
            m.put("idTareIngrediente", ti.getIdTareIngrediente());
            m.put("cantidad", ti.getCantidad());
            if (ti.getIngrediente() != null) {
                m.put("ingredienteId", ti.getIngrediente().getIdIngrediente());
                m.put("ingredienteNombre", ti.getIngrediente().getNombre());
                m.put("unidadMedida", ti.getIngrediente().getUnidadMedida());
            } else {
                m.put("ingredienteId", null);
                m.put("ingredienteNombre", null);
                m.put("unidadMedida", null);
            }
            m.put("tareaPrepId", ti.getTareaPrep() == null ? null : ti.getTareaPrep().getIdTareaPrep());
            out.add(m);
        }
        return out;
    }

    // Nuevo: upsert (replace) lista de ingredientes para una tarea
    @Transactional
    public List<TareaIngredienteDTO> upsertForTask(final Long taskId, final List<UpsertItem> items) {
        final TareaPrep tareaPrep = tareaPrepRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("tareaPrep not found"));

        // Existing by ingredienteId
        final List<TareaIngrediente> existingList =
                tareaIngredienteRepository.findByTareaPrepIdTareaPrep(taskId);
        final Map<Long, TareaIngrediente> existingByIngrediente = existingList.stream()
                .filter(ti -> ti.getIngrediente() != null)
                .collect(Collectors.toMap(ti -> ti.getIngrediente().getIdIngrediente(), ti -> ti));

        // incoming IDs
        final Set<Long> incomingIds = items.stream()
                .map(UpsertItem::getIngredienteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Upsert each incoming
        final List<TareaIngredienteDTO> resultList = new ArrayList<>();
        for (UpsertItem it : items) {
            if (it.getIngredienteId() == null) {
                throw new IllegalArgumentException("ingredienteId is required in upsert item");
            }
            final Ingrediente ingrediente = ingredienteRepository.findById(it.getIngredienteId())
                    .orElseThrow(() -> new NotFoundException("ingrediente not found"));

            final TareaIngrediente exist = existingByIngrediente.get(it.getIngredienteId());
            /* if (exist != null) {
                exist.setCantidad(it.getCantidad());
                tareaIngredienteRepository.save(exist);
                resultList.add(mapToDTO(exist, new TareaIngredienteDTO()));
            } else {
                TareaIngrediente newTI = new TareaIngrediente();
                newTI.setTareaPrep(tareaPrep);
                newTI.setIngrediente(ingrediente);
                newTI.setCantidad(it.getCantidad());
                final TareaIngrediente saved = tareaIngredienteRepository.save(newTI);
                resultList.add(mapToDTO(saved, new TareaIngredienteDTO()));
            } */
        }

        // Delete any existing associations NOT present in incomingIds (replace semantics)
        for (TareaIngrediente existing : existingList) {
            Long ingId = existing.getIngrediente() == null ? null : existing.getIngrediente().getIdIngrediente();
            if (ingId != null && !incomingIds.contains(ingId)) {
                tareaIngredienteRepository.delete(existing);
            }
        }

        return resultList;
    }

    // Helper class to parse payload (puedes mover a su propio archivo si prefieres)
    public static class UpsertItem {
        private Long ingredienteId;
        private Double cantidad;

        public Long getIngredienteId() { return ingredienteId; }
        public void setIngredienteId(Long ingredienteId) { this.ingredienteId = ingredienteId; }
        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
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
