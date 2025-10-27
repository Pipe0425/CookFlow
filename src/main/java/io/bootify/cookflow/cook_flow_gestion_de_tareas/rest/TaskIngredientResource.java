package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.TareaIngredienteService;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.TareaIngredienteService.UpsertItem;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaIngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/{taskId}/ingredients")
public class TaskIngredientResource {

    private final TareaIngredienteService tareaIngredienteService;

    public TaskIngredientResource(final TareaIngredienteService tareaIngredienteService) {
        this.tareaIngredienteService = tareaIngredienteService;
    }

    @GetMapping
    public ResponseEntity<?> listByTask(@PathVariable("taskId") Long taskId) {
        // Devolvemos versión enriquecida (nombre unidad)
        List<Map<String,Object>> enriched = tareaIngredienteService.findByTaskIdEnriched(taskId);
        return ResponseEntity.ok(enriched);
    }

    @PostMapping
    public ResponseEntity<?> upsertByTask(@PathVariable("taskId") Long taskId,
                                         @RequestBody @Valid List<UpsertItem> items) {
        // items: [{ "ingredienteId":1, "cantidad":2.5 }, ...]
        try {
            List<TareaIngredienteDTO> result = tareaIngredienteService.upsertForTask(taskId, items);
            return ResponseEntity.ok(result);
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{relationId}")
    public ResponseEntity<Void> deleteRelation(@PathVariable("taskId") Long taskId,
                                               @PathVariable("relationId") Long relationId) {
        // relationId es idTareIngrediente
        tareaIngredienteService.delete(relationId);
        return ResponseEntity.noContent().build();
    }
}
