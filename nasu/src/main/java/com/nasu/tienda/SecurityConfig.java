package com.nasu.tienda;

import com.nasu.tienda.domain.Ruta;
import com.nasu.tienda.service.RutaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de autenticación y autorización. Las reglas de acceso no están
 * escritas aquí: se arman leyendo la tabla ruta, de modo que cambiar quién
 * entra a qué pantalla es cambiar un registro de la base.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Lazy RutaService rutaService)
            throws Exception {

        var rutas = rutaService.getRutas();

        http.authorizeHttpRequests(requests -> {
            for (Ruta ruta : rutas) {
                if (ruta.isRequiereRol()) {
                    requests.requestMatchers(ruta.getRuta()).hasRole(ruta.getRol().getRol());
                } else {
                    requests.requestMatchers(ruta.getRuta()).permitAll();
                }
            }
            //Cualquier dirección que no esté en la tabla exige haber iniciado sesión
            requests.anyRequest().authenticated();
        });

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(excepciones -> excepciones
                .accessDeniedPage("/acceso-denegado")
        );

        return http.build();
    }

    //Spring Security toma este codificador y el UsuarioDetailsService del
    //contexto para validar las credenciales del formulario de login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
