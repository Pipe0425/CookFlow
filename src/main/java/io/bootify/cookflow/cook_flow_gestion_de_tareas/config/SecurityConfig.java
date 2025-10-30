package io.bootify.cookflow.cook_flow_gestion_de_tareas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.bootify.cookflow.cook_flow_gestion_de_tareas.Auth.CustomAuthenticationEntryPoint;
import io.bootify.cookflow.cook_flow_gestion_de_tareas.JWT.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // inyectamos por tipo; nombre en camelCase por convención
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .formLogin(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Rutas administrativas: requiere ADMIN
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                // Rutas públicas de auth, recursos estáticos y docs
                .requestMatchers("/auth/**",
                                 "/", "/home", "/homePage",
                                 "/css/**", "/js/**", "/images/**",
                                 "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Endpoints protegidos de la app (ejemplos CookFlow)
                .requestMatchers("/miPerfil", "/api/tasks/**", "/api/ingredients/**",
                                 "/api/checklist/**", "/api/ingredients/consolidated", "/tareas", "/tareas/**", "/api/users/","/api/users/**").authenticated()

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // inyectar el provider personalizado
            .authenticationProvider(authenticationProvider)
            // nuestro filtro JWT antes del UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .deleteCookies("JSESSIONID", "token")
                .invalidateHttpSession(true)
            )
            .build();
    }
}
