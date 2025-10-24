package io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.service;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.domain.CheckListLimpieza;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.model.CheckListLimpiezaDTO;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.repos.CheckListLimpiezaRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.events.BeforeDeleteUsuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.repos.UsuarioRepository;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.NotFoundException;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.util.ReferencedException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class CheckListLimpiezaService {

    private final CheckListLimpiezaRepository checkListLimpiezaRepository;
    private final UsuarioRepository usuarioRepository;

    public CheckListLimpiezaService(final CheckListLimpiezaRepository checkListLimpiezaRepository,
            final UsuarioRepository usuarioRepository) {
        this.checkListLimpiezaRepository = checkListLimpiezaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<CheckListLimpiezaDTO> findAll() {
        final List<CheckListLimpieza> checkListLimpiezas = checkListLimpiezaRepository.findAll(Sort.by("idCheckListLimpieza"));
        return checkListLimpiezas.stream()
                .map(checkListLimpieza -> mapToDTO(checkListLimpieza, new CheckListLimpiezaDTO()))
                .toList();
    }

    public CheckListLimpiezaDTO get(final Long idCheckListLimpieza) {
        return checkListLimpiezaRepository.findById(idCheckListLimpieza)
                .map(checkListLimpieza -> mapToDTO(checkListLimpieza, new CheckListLimpiezaDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final CheckListLimpiezaDTO checkListLimpiezaDTO) {
        final CheckListLimpieza checkListLimpieza = new CheckListLimpieza();
        mapToEntity(checkListLimpiezaDTO, checkListLimpieza);
        return checkListLimpiezaRepository.save(checkListLimpieza).getIdCheckListLimpieza();
    }

    public void update(final Long idCheckListLimpieza,
            final CheckListLimpiezaDTO checkListLimpiezaDTO) {
        final CheckListLimpieza checkListLimpieza = checkListLimpiezaRepository.findById(idCheckListLimpieza)
                .orElseThrow(NotFoundException::new);
        mapToEntity(checkListLimpiezaDTO, checkListLimpieza);
        checkListLimpiezaRepository.save(checkListLimpieza);
    }

    public void delete(final Long idCheckListLimpieza) {
        final CheckListLimpieza checkListLimpieza = checkListLimpiezaRepository.findById(idCheckListLimpieza)
                .orElseThrow(NotFoundException::new);
        checkListLimpiezaRepository.delete(checkListLimpieza);
    }

    private CheckListLimpiezaDTO mapToDTO(final CheckListLimpieza checkListLimpieza,
            final CheckListLimpiezaDTO checkListLimpiezaDTO) {
        checkListLimpiezaDTO.setIdCheckListLimpieza(checkListLimpieza.getIdCheckListLimpieza());
        checkListLimpiezaDTO.setFecha(checkListLimpieza.getFecha());
        checkListLimpiezaDTO.setCocinaLimpia(checkListLimpieza.getCocinaLimpia());
        checkListLimpiezaDTO.setUtenciliosLimpios(checkListLimpieza.getUtenciliosLimpios());
        checkListLimpiezaDTO.setResiduosDesechados(checkListLimpieza.getResiduosDesechados());
        checkListLimpiezaDTO.setResponsable(checkListLimpieza.getResponsable() == null ? null : checkListLimpieza.getResponsable().getIdUsuario());
        return checkListLimpiezaDTO;
    }

    private CheckListLimpieza mapToEntity(final CheckListLimpiezaDTO checkListLimpiezaDTO,
            final CheckListLimpieza checkListLimpieza) {
        checkListLimpieza.setFecha(checkListLimpiezaDTO.getFecha());
        checkListLimpieza.setCocinaLimpia(checkListLimpiezaDTO.getCocinaLimpia());
        checkListLimpieza.setUtenciliosLimpios(checkListLimpiezaDTO.getUtenciliosLimpios());
        checkListLimpieza.setResiduosDesechados(checkListLimpiezaDTO.getResiduosDesechados());
        final Usuario responsable = checkListLimpiezaDTO.getResponsable() == null ? null : usuarioRepository.findById(checkListLimpiezaDTO.getResponsable())
                .orElseThrow(() -> new NotFoundException("responsable not found"));
        checkListLimpieza.setResponsable(responsable);
        return checkListLimpieza;
    }

    @EventListener(BeforeDeleteUsuario.class)
    public void on(final BeforeDeleteUsuario event) {
        final ReferencedException referencedException = new ReferencedException();
        final CheckListLimpieza responsableCheckListLimpieza = checkListLimpiezaRepository.findFirstByResponsableIdUsuario(event.getIdUsuario());
        if (responsableCheckListLimpieza != null) {
            referencedException.setKey("usuario.checkListLimpieza.responsable.referenced");
            referencedException.addParam(responsableCheckListLimpieza.getIdCheckListLimpieza());
            throw referencedException;
        }
    }

}
