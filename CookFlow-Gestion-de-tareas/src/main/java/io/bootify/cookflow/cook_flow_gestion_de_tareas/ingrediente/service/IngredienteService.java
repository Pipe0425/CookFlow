package io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteIngrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.domain.Ingrediente;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.model.IngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.ingrediente.repos.IngredienteRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final ApplicationEventPublisher publisher;

    public IngredienteService(final IngredienteRepository ingredienteRepository,
            final ApplicationEventPublisher publisher) {
        this.ingredienteRepository = ingredienteRepository;
        this.publisher = publisher;
    }

    public List<IngredienteDTO> findAll() {
        final List<Ingrediente> ingredientes = ingredienteRepository.findAll(Sort.by("idIngrediente"));
        return ingredientes.stream()
                .map(ingrediente -> mapToDTO(ingrediente, new IngredienteDTO()))
                .toList();
    }

    public IngredienteDTO get(final Long idIngrediente) {
        return ingredienteRepository.findById(idIngrediente)
                .map(ingrediente -> mapToDTO(ingrediente, new IngredienteDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final IngredienteDTO ingredienteDTO) {
        final Ingrediente ingrediente = new Ingrediente();
        mapToEntity(ingredienteDTO, ingrediente);
        return ingredienteRepository.save(ingrediente).getIdIngrediente();
    }

    public void update(final Long idIngrediente, final IngredienteDTO ingredienteDTO) {
        final Ingrediente ingrediente = ingredienteRepository.findById(idIngrediente)
                .orElseThrow(NotFoundException::new);
        mapToEntity(ingredienteDTO, ingrediente);
        ingredienteRepository.save(ingrediente);
    }

    public void delete(final Long idIngrediente) {
        final Ingrediente ingrediente = ingredienteRepository.findById(idIngrediente)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteIngrediente(idIngrediente));
        ingredienteRepository.delete(ingrediente);
    }

    private IngredienteDTO mapToDTO(final Ingrediente ingrediente,
            final IngredienteDTO ingredienteDTO) {
        ingredienteDTO.setIdIngrediente(ingrediente.getIdIngrediente());
        ingredienteDTO.setNombre(ingrediente.getNombre());
        ingredienteDTO.setUnidadMedida(ingrediente.getUnidadMedida());
        ingredienteDTO.setStockActual(ingrediente.getStockActual());
        ingredienteDTO.setActivo(ingrediente.getActivo());
        return ingredienteDTO;
    }

    private Ingrediente mapToEntity(final IngredienteDTO ingredienteDTO,
            final Ingrediente ingrediente) {
        ingrediente.setNombre(ingredienteDTO.getNombre());
        ingrediente.setUnidadMedida(ingredienteDTO.getUnidadMedida());
        ingrediente.setStockActual(ingredienteDTO.getStockActual());
        ingrediente.setActivo(ingredienteDTO.getActivo());
        return ingrediente;
    }

}
