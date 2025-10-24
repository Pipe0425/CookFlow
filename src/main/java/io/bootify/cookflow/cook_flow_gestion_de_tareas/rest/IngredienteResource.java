package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.IngredienteDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.IngredienteService;
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
@RequestMapping(value = "/api/ingredientes", produces = MediaType.APPLICATION_JSON_VALUE)
public class IngredienteResource {

    private final IngredienteService ingredienteService;

    public IngredienteResource(final IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    @GetMapping
    public ResponseEntity<List<IngredienteDTO>> getAllIngredientes() {
        return ResponseEntity.ok(ingredienteService.findAll());
    }

    @GetMapping("/{idIngrediente}")
    public ResponseEntity<IngredienteDTO> getIngrediente(
            @PathVariable(name = "idIngrediente") final Long idIngrediente) {
        return ResponseEntity.ok(ingredienteService.get(idIngrediente));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createIngrediente(
            @RequestBody @Valid final IngredienteDTO ingredienteDTO) {
        final Long createdIdIngrediente = ingredienteService.create(ingredienteDTO);
        return new ResponseEntity<>(createdIdIngrediente, HttpStatus.CREATED);
    }

    @PutMapping("/{idIngrediente}")
    public ResponseEntity<Long> updateIngrediente(
            @PathVariable(name = "idIngrediente") final Long idIngrediente,
            @RequestBody @Valid final IngredienteDTO ingredienteDTO) {
        ingredienteService.update(idIngrediente, ingredienteDTO);
        return ResponseEntity.ok(idIngrediente);
    }

    @DeleteMapping("/{idIngrediente}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteIngrediente(
            @PathVariable(name = "idIngrediente") final Long idIngrediente) {
        ingredienteService.delete(idIngrediente);
        return ResponseEntity.noContent().build();
    }

}
