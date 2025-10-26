package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.RegisterRequest;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.UsuarioDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        mapToEntity(usuarioDTO, usuario);
        usuarioRepository.save(usuario);
    }

    // Eliminar usuario
    public void delete(final Long idUsuario) {
        final Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteUsuario(idUsuario));
        usuarioRepository.delete(usuario);
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
}