package io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.model.CheckListLimpiezaDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.service.CheckListLimpiezaService;
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
@RequestMapping(value = "/api/checkListLimpiezas", produces = MediaType.APPLICATION_JSON_VALUE)
public class CheckListLimpiezaResource {

    private final CheckListLimpiezaService checkListLimpiezaService;

    public CheckListLimpiezaResource(final CheckListLimpiezaService checkListLimpiezaService) {
        this.checkListLimpiezaService = checkListLimpiezaService;
    }

    @GetMapping
    public ResponseEntity<List<CheckListLimpiezaDTO>> getAllCheckListLimpiezas() {
        return ResponseEntity.ok(checkListLimpiezaService.findAll());
    }

    @GetMapping("/{idCheckListLimpieza}")
    public ResponseEntity<CheckListLimpiezaDTO> getCheckListLimpieza(
            @PathVariable(name = "idCheckListLimpieza") final Long idCheckListLimpieza) {
        return ResponseEntity.ok(checkListLimpiezaService.get(idCheckListLimpieza));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createCheckListLimpieza(
            @RequestBody @Valid final CheckListLimpiezaDTO checkListLimpiezaDTO) {
        final Long createdIdCheckListLimpieza = checkListLimpiezaService.create(checkListLimpiezaDTO);
        return new ResponseEntity<>(createdIdCheckListLimpieza, HttpStatus.CREATED);
    }

    @PutMapping("/{idCheckListLimpieza}")
    public ResponseEntity<Long> updateCheckListLimpieza(
            @PathVariable(name = "idCheckListLimpieza") final Long idCheckListLimpieza,
            @RequestBody @Valid final CheckListLimpiezaDTO checkListLimpiezaDTO) {
        checkListLimpiezaService.update(idCheckListLimpieza, checkListLimpiezaDTO);
        return ResponseEntity.ok(idCheckListLimpieza);
    }

    @DeleteMapping("/{idCheckListLimpieza}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteCheckListLimpieza(
            @PathVariable(name = "idCheckListLimpieza") final Long idCheckListLimpieza) {
        checkListLimpiezaService.delete(idCheckListLimpieza);
        return ResponseEntity.noContent().build();
    }

}
