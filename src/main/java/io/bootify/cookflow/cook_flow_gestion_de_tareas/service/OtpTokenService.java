package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.OtpToken;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.model.OtpTokenDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.OtpTokenRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class OtpTokenService {

    private final OtpTokenRepository otpTokenRepository;
    private final UsuarioRepository usuarioRepository;

    public OtpTokenService(final OtpTokenRepository otpTokenRepository,
            final UsuarioRepository usuarioRepository) {
        this.otpTokenRepository = otpTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<OtpTokenDTO> findAll() {
        final List<OtpToken> otpTokens = otpTokenRepository.findAll(Sort.by("idOtpToken"));
        return otpTokens.stream()
                .map(otpToken -> mapToDTO(otpToken, new OtpTokenDTO()))
                .toList();
    }

    public OtpTokenDTO get(final Long idOtpToken) {
        return otpTokenRepository.findById(idOtpToken)
                .map(otpToken -> mapToDTO(otpToken, new OtpTokenDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final OtpTokenDTO otpTokenDTO) {
        final OtpToken otpToken = new OtpToken();
        mapToEntity(otpTokenDTO, otpToken);
        return otpTokenRepository.save(otpToken).getIdOtpToken();
    }

    public void update(final Long idOtpToken, final OtpTokenDTO otpTokenDTO) {
        final OtpToken otpToken = otpTokenRepository.findById(idOtpToken)
                .orElseThrow(NotFoundException::new);
        mapToEntity(otpTokenDTO, otpToken);
        otpTokenRepository.save(otpToken);
    }

    public void delete(final Long idOtpToken) {
        final OtpToken otpToken = otpTokenRepository.findById(idOtpToken)
                .orElseThrow(NotFoundException::new);
        otpTokenRepository.delete(otpToken);
    }

    private OtpTokenDTO mapToDTO(final OtpToken otpToken, final OtpTokenDTO otpTokenDTO) {
        otpTokenDTO.setIdOtpToken(otpToken.getIdOtpToken());
        otpTokenDTO.setCodigoOtp(otpToken.getCodigoOtp());
        otpTokenDTO.setExpiracion(otpToken.getExpiracion());
        otpTokenDTO.setUsado(otpToken.getUsado());
        otpTokenDTO.setUsuario(otpToken.getUsuario() == null ? null : otpToken.getUsuario().getIdUsuario());
        return otpTokenDTO;
    }

    private OtpToken mapToEntity(final OtpTokenDTO otpTokenDTO, final OtpToken otpToken) {
        otpToken.setCodigoOtp(otpTokenDTO.getCodigoOtp());
        otpToken.setExpiracion(otpTokenDTO.getExpiracion());
        otpToken.setUsado(otpTokenDTO.getUsado());
        final Usuario usuario = otpTokenDTO.getUsuario() == null ? null : usuarioRepository.findById(otpTokenDTO.getUsuario())
                .orElseThrow(() -> new NotFoundException("usuario not found"));
        otpToken.setUsuario(usuario);
        return otpToken;
    }

    @EventListener(BeforeDeleteUsuario.class)
    public void on(final BeforeDeleteUsuario event) {
        final ReferencedException referencedException = new ReferencedException();
        final OtpToken usuarioOtpToken = otpTokenRepository.findFirstByUsuarioIdUsuario(event.getIdUsuario());
        if (usuarioOtpToken != null) {
            referencedException.setKey("usuario.otpToken.usuario.referenced");
            referencedException.addParam(usuarioOtpToken.getIdOtpToken());
            throw referencedException;
        }
    }

}
