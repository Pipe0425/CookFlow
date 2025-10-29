package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Estado;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Turno;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.TareaPrepDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.TareaPrepService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
public class TareaPrepResource {

    private final TareaPrepService tareaPrepService;

    public TareaPrepResource(TareaPrepService tareaPrepService) {
        this.tareaPrepService = tareaPrepService;
    }

    // Listar por fecha y turno: /api/tasks?date=2025-10-27&turno=MANANA
    @GetMapping
    public ResponseEntity<List<TareaPrepDTO>> listByDateAndTurn(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String turno) {

        if (date != null && turno != null) {
            LocalDate localDate = LocalDate.parse(date); // yyyy-MM-dd
            Turno t = Turno.valueOf(turno.toUpperCase());
            return ResponseEntity.ok(tareaPrepService.getTasksByDateAndTurn(localDate, t));
        }
        // si no vienen filtros, devolver todo (o vacío)
        return ResponseEntity.ok(tareaPrepService.findAll());
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid TareaPrepDTO dto) {
        Long id = tareaPrepService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaPrepDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(tareaPrepService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id, @RequestBody @Valid TareaPrepDTO dto) {
        tareaPrepService.update(id, dto);
        return ResponseEntity.ok(id);
    }

    // PATCH estado: { "estado": "EN_PROGRESO" }
    static class StateUpdateRequest {
        public String estado;
        public String getEstado(){return estado;}
        public void setEstado(String e){this.estado=e;}
    }

    @PatchMapping("/{id}/state")
    public ResponseEntity<Void> setState(@PathVariable Long id, @RequestBody StateUpdateRequest body) {
        try {
            Estado nuevo = Estado.valueOf(body.getEstado().toUpperCase()); // adapta nombre de enum
            tareaPrepService.setState(id, nuevo);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable Long id) {
        tareaPrepService.archiveTask(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tareaPrepService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
