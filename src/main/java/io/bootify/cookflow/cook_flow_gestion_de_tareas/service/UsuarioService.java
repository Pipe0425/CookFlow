package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.RegisterRequest;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.ChangePasswordRequest;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.PerfilDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioAdminCreateDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioAdminUpdateDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;

    // Obtener todos los usuarios
    public List<UsuarioDTO> findAll() {
        final List<Usuario> usuarios = usuarioRepository.findAll(Sort.by("idUsuario"));
        return usuarios.stream()
                .map(usuario -> mapToDTO(usuario, new UsuarioDTO()))
                .toList();
    }

    // Obtener un usuario por ID
    public UsuarioDTO get(final Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> mapToDTO(usuario, new UsuarioDTO()))
                .orElseThrow(NotFoundException::new);
    }

    // Crear usuario a partir de un DTO
    public Long create(final UsuarioDTO usuarioDTO) {
        final Usuario usuario = new Usuario();
        mapToEntity(usuarioDTO, usuario);
        return usuarioRepository.save(usuario).getIdUsuario();
    }

    // Crear usuario a partir del RegisterRequest (registro de Auth)
    public Long create(final RegisterRequest request) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setNombre(request.getNombre());
        usuarioDTO.setApellido(request.getApellido());
        usuarioDTO.setEmail(request.getEmail());
        usuarioDTO.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar contraseña
        usuarioDTO.setActivo(true); // Valor por defecto
        usuarioDTO.setRole(Role.STAFF); // Puedes cambiarlo a Role.USER si quieres
        return create(usuarioDTO);
    }

    // Actualizar usuario
    public void update(final Long idUsuario, final UsuarioDTO usuarioDTO) {
        final Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(NotFoundException::new);

        // mapear campos "seguros"
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());

        // si el email puede cambiar, revisa unicidad
        if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
            usuario.setEmail(usuarioDTO.getEmail());
        }

        // contraseña: solo actualizar si viene no-nula y no vacía (y ya has cifrado cuando corresponde)
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isBlank()) {
            usuario.setPassword(usuarioDTO.getPassword());
        }

        // resto de campos
        usuario.setActivo(usuarioDTO.getActivo() != null ? usuarioDTO.getActivo() : usuario.getActivo());
        usuario.setRole(usuarioDTO.getRole() != null ? usuarioDTO.getRole() : usuario.getRole());

        usuarioRepository.save(usuario);
    }

    // Eliminar usuario
    public void delete(final Long idUsuario) {
        final Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteUsuario(idUsuario));
        usuarioRepository.delete(usuario);
    }

    /*
     * 
     */
    public PerfilDTO getPerfilByEmail(final String email){
        final Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);
        return mapToPerfilDTO(usuario);
    }

    private PerfilDTO mapToPerfilDTO(final Usuario usuario) {
        PerfilDTO perfilDTO = new PerfilDTO();
        perfilDTO.setIdUsuario(usuario.getIdUsuario());
        perfilDTO.setNombre(usuario.getNombre());
        perfilDTO.setApellido(usuario.getApellido());
        perfilDTO.setEmail(usuario.getEmail());
        perfilDTO.setRole(usuario.getRole());
        perfilDTO.setActivo(usuario.getActivo());
        return perfilDTO;
    }

    // Mapeo entidad → DTO
    private UsuarioDTO mapToDTO(final Usuario usuario, final UsuarioDTO usuarioDTO) {
        usuarioDTO.setIdUsuario(usuario.getIdUsuario());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setApellido(usuario.getApellido());
        usuarioDTO.setEmail(usuario.getEmail());
        usuarioDTO.setPassword(usuario.getPassword());
        usuarioDTO.setActivo(usuario.getActivo());
        usuarioDTO.setRole(usuario.getRole());
        return usuarioDTO;
    }

    // Mapeo DTO → entidad
    private Usuario mapToEntity(final UsuarioDTO usuarioDTO, final Usuario usuario) {
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(usuarioDTO.getPassword());
        usuario.setActivo(usuarioDTO.getActivo() != null ? usuarioDTO.getActivo() : true);
        usuario.setRole(usuarioDTO.getRole() != null ? usuarioDTO.getRole() : Role.STAFF);
        return usuario;
    }

    // Validar si un correo ya está registrado
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /* public PerfilDTO getCurrentUser(UserDetails userDetails) {
            return usuarioRepository.findByEmail(userDetails.getEmail())
                    .map(user -> mapToPerfilDTO(user, new mapToPerfilDTO()))
                    .orElseThrow(NotFoundException::new);
    } */

    // ========== DESACTIVAR USUARIO (ADMIN) ==========
    // Dejar claro que esto activa/desactiva a false; el endpoint REST debe estar protegido con
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateUser(final Long id) {
        final Usuario user = usuarioRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        user.setActivo(false);
        usuarioRepository.save(user);
    }

    public void changePassword(final String email, final ChangePasswordRequest request) {
        final Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException());

        // validar current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // validar nueva y confirmación
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }

        // reglas básicas de seguridad
        String newPassword = request.getNewPassword();
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos una letra mayúscula.");
        }
        if (!newPassword.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos una letra minúscula.");
        }
        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos un número.");
        }
        if (!newPassword.matches(".*[!@#$%^&*()_+\\-={}:;\"'<>.,?/].*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos un carácter especial.");
        }

        // cifrar y guardar
        user.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(user);
    }

    public Optional<UsuarioDTO> getByEmailOptional(String email) {
        return usuarioRepository.findByEmail(email).map(u -> mapToDTO(u, new UsuarioDTO()));
    }

    
    public Long createAdmin(UsuarioAdminCreateDTO dto, PasswordEncoder encoder) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(encoder.encode(dto.getPassword()));
        usuario.setRole(dto.getRole());
        usuario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        usuarioRepository.save(usuario);
        return usuario.getIdUsuario();
    }
    
    // admin update: recibe DTO admin y PasswordEncoder para cifrar si viene password
    public void updateAdmin(Long idUsuario, UsuarioAdminUpdateDTO dto, PasswordEncoder encoder) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(NotFoundException::new);

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());

        if (dto.getEmail() != null && !dto.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
            usuario.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(encoder.encode(dto.getPassword()));
        }

        if (dto.getRole() != null) {
            usuario.setRole(dto.getRole());
        }

        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }

        usuarioRepository.save(usuario);
    }


}