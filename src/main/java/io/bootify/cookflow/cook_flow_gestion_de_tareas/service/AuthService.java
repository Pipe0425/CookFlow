package io.bootify.cookflow.cook_flow_gestion_de_tareas.service;

import org.springframework.stereotype.Service;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.AuthResponse;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.LoginRequest;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.RegisterRequest;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Role;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.domain.Usuario;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.repos.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        Usuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos."));

        if (!user.getActivo()) {
            throw new RuntimeException("El usuario está inactivo.");
        }

        // Autenticar credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Generar token JWT
        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse register(RegisterRequest request) {
        Usuario user = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .activo(true)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }
}
