package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaIngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.TareaIngredienteService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/tareaIngredientes", produces = MediaType.APPLICATION_JSON_VALUE)
public class TareaIngredienteResource {

    private final TareaIngredienteService tareaIngredienteService;

    public TareaIngredienteResource(final TareaIngredienteService tareaIngredienteService) {
        this.tareaIngredienteService = tareaIngredienteService;
    }

    @GetMapping
    public ResponseEntity<List<TareaIngredienteDTO>> getAllTareaIngredientes() {
        return ResponseEntity.ok(tareaIngredienteService.findAll());
    }

    @GetMapping("/{idTareIngrediente}")
    public ResponseEntity<TareaIngredienteDTO> getTareaIngrediente(
            @PathVariable(name = "idTareIngrediente") final Long idTareIngrediente) {
        return ResponseEntity.ok(tareaIngredienteService.get(idTareIngrediente));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createTareaIngrediente(
            @RequestBody @Valid final TareaIngredienteDTO tareaIngredienteDTO) {
        final Long createdIdTareIngrediente = tareaIngredienteService.create(tareaIngredienteDTO);
        return new ResponseEntity<>(createdIdTareIngrediente, HttpStatus.CREATED);
    }

    @PutMapping("/{idTareIngrediente}")
    public ResponseEntity<Long> updateTareaIngrediente(
            @PathVariable(name = "idTareIngrediente") final Long idTareIngrediente,
            @RequestBody @Valid final TareaIngredienteDTO tareaIngredienteDTO) {
        tareaIngredienteService.update(idTareIngrediente, tareaIngredienteDTO);
        return ResponseEntity.ok(idTareIngrediente);
    }

    @DeleteMapping("/{idTareIngrediente}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteTareaIngrediente(
            @PathVariable(name = "idTareIngrediente") final Long idTareIngrediente) {
        tareaIngredienteService.delete(idTareIngrediente);
        return ResponseEntity.noContent().build();
    }

}
