package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaPrepDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.TareaPrepService;
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
@RequestMapping(value = "/api/tareaPreps", produces = MediaType.APPLICATION_JSON_VALUE)
public class TareaPrepResource {

    private final TareaPrepService tareaPrepService;

    public TareaPrepResource(final TareaPrepService tareaPrepService) {
        this.tareaPrepService = tareaPrepService;
    }

    @GetMapping
    public ResponseEntity<List<TareaPrepDTO>> getAllTareaPreps() {
        return ResponseEntity.ok(tareaPrepService.findAll());
    }

    @GetMapping("/{idTareaPrep}")
    public ResponseEntity<TareaPrepDTO> getTareaPrep(
            @PathVariable(name = "idTareaPrep") final Long idTareaPrep) {
        return ResponseEntity.ok(tareaPrepService.get(idTareaPrep));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createTareaPrep(
            @RequestBody @Valid final TareaPrepDTO tareaPrepDTO) {
        final Long createdIdTareaPrep = tareaPrepService.create(tareaPrepDTO);
        return new ResponseEntity<>(createdIdTareaPrep, HttpStatus.CREATED);
    }

    @PutMapping("/{idTareaPrep}")
    public ResponseEntity<Long> updateTareaPrep(
            @PathVariable(name = "idTareaPrep") final Long idTareaPrep,
            @RequestBody @Valid final TareaPrepDTO tareaPrepDTO) {
        tareaPrepService.update(idTareaPrep, tareaPrepDTO);
        return ResponseEntity.ok(idTareaPrep);
    }

    @DeleteMapping("/{idTareaPrep}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteTareaPrep(
            @PathVariable(name = "idTareaPrep") final Long idTareaPrep) {
        tareaPrepService.delete(idTareaPrep);
        return ResponseEntity.noContent().build();
    }

}
