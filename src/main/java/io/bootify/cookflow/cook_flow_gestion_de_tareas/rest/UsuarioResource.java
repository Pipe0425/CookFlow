package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.UsuarioService;
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
@RequestMapping(value = "/api/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsuarioResource {

    private final UsuarioService usuarioService;

    public UsuarioResource(final UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioDTO> getUsuario(
            @PathVariable(name = "idUsuario") final Long idUsuario) {
        return ResponseEntity.ok(usuarioService.get(idUsuario));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUsuario(@RequestBody @Valid final UsuarioDTO usuarioDTO) {
        final Long createdIdUsuario = usuarioService.create(usuarioDTO);
        return new ResponseEntity<>(createdIdUsuario, HttpStatus.CREATED);
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<Long> updateUsuario(
            @PathVariable(name = "idUsuario") final Long idUsuario,
            @RequestBody @Valid final UsuarioDTO usuarioDTO) {
        usuarioService.update(idUsuario, usuarioDTO);
        return ResponseEntity.ok(idUsuario);
    }

    @DeleteMapping("/{idUsuario}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteUsuario(
            @PathVariable(name = "idUsuario") final Long idUsuario) {
        usuarioService.delete(idUsuario);
        return ResponseEntity.noContent().build();
    }

}
