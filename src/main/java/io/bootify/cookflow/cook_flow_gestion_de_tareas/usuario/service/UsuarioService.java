package io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.model.UsuarioDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher publisher;

    public UsuarioService(final UsuarioRepository usuarioRepository,
            final ApplicationEventPublisher publisher) {
        this.usuarioRepository = usuarioRepository;
        this.publisher = publisher;
    }

    public List<UsuarioDTO> findAll() {
        final List<Usuario> usuarios = usuarioRepository.findAll(Sort.by("idUsuario"));
        return usuarios.stream()
                .map(usuario -> mapToDTO(usuario, new UsuarioDTO()))
                .toList();
    }

    public UsuarioDTO get(final Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> mapToDTO(usuario, new UsuarioDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final UsuarioDTO usuarioDTO) {
        final Usuario usuario = new Usuario();
        mapToEntity(usuarioDTO, usuario);
        return usuarioRepository.save(usuario).getIdUsuario();
    }

    public void update(final Long idUsuario, final UsuarioDTO usuarioDTO) {
        final Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(NotFoundException::new);
        mapToEntity(usuarioDTO, usuario);
        usuarioRepository.save(usuario);
    }

    public void delete(final Long idUsuario) {
        final Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteUsuario(idUsuario));
        usuarioRepository.delete(usuario);
    }

    private UsuarioDTO mapToDTO(final Usuario usuario, final UsuarioDTO usuarioDTO) {
        usuarioDTO.setIdUsuario(usuario.getIdUsuario());
        usuarioDTO.setNombre(usuario.getNombre());
        usuarioDTO.setEmail(usuario.getEmail());
        usuarioDTO.setPassword(usuario.getPassword());
        usuarioDTO.setActivo(usuario.getActivo());
        return usuarioDTO;
    }

    private Usuario mapToEntity(final UsuarioDTO usuarioDTO, final Usuario usuario) {
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(usuarioDTO.getPassword());
        usuario.setActivo(usuarioDTO.getActivo());
        return usuario;
    }

}
