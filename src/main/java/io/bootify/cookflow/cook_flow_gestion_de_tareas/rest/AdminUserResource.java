package io.bootify.cookflow.cook_flow_gestion_de_tareas.rest;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioAdminCreateDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioAdminUpdateDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.service.UsuarioService;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;  
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
/* @PreAuthorize("hasRole('ADMIN')") */ // aplica a todo el controller
public class AdminUserResource {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public AdminUserResource(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listAll() {
        List<UsuarioDTO> users = usuarioService.findAll(); // o crear método adminFindAll si quieres filtro
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UsuarioAdminCreateDTO dto, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", br.getFieldErrors()));
        }
        if (usuarioService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email ya registrado"));
        }
        UsuarioDTO toCreate = new UsuarioDTO();
        toCreate.setNombre(dto.getNombre());
        toCreate.setApellido(dto.getApellido());
        toCreate.setEmail(dto.getEmail());
        toCreate.setPassword(passwordEncoder.encode(dto.getPassword()));
        toCreate.setRole(dto.getRole());
        toCreate.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        Long id = usuarioService.create(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("idUsuario", id));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<?> updateUser(@PathVariable Long idUsuario,
                                        @Valid @RequestBody UsuarioAdminUpdateDTO dto,
                                        BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", br.getFieldErrors()));
        }

        // Validar email único si cambió
        if (dto.getEmail() != null) {
            usuarioService.getByEmailOptional(dto.getEmail()).ifPresent(u -> {
                if (!u.getIdUsuario().equals(idUsuario)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya en uso");
                }
            });
        }

        try {
            usuarioService.updateAdmin(idUsuario, dto, passwordEncoder);
            return ResponseEntity.ok(Map.of("idUsuario", idUsuario));
        } catch (NotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PatchMapping("/{idUsuario}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long idUsuario) {
        usuarioService.deactivateUser(idUsuario);
        return ResponseEntity.noContent().build();
    }
}
