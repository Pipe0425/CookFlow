package io.bootify.cookflow.cook_flow_gestion_de_tareas.usuario.domain;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.check_list_limpieza.domain.CheckListLimpieza;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.otp_token.domain.OtpToken;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.tarea_prep.domain.TareaPrep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "Usuarios")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Usuario {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "responsable")
    private Set<TareaPrep> tareasAsignadas = new HashSet<>();

    @OneToMany(mappedBy = "responsable")
    private Set<CheckListLimpieza> checklistsLimpieza = new HashSet<>();

    @OneToMany(mappedBy = "usuario")
    private Set<OtpToken> otpTokens = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

}
