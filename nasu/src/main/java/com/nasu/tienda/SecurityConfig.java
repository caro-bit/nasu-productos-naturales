/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nasu.tienda;

import com.nasu.tienda.domain.Ruta;
import com.nasu.tienda.service.RutaService;
import com.nasu.tienda.service.UsuarioService;
import com.nasu.tienda.util.SesionUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Lazy RutaService rutaService, @Lazy UsuarioService usuarioService) throws Exception {

        var rutas = rutaService.getRutas();

        http.authorizeHttpRequests(requests -> {
            for (Ruta ruta : rutas) {
                if (ruta.isRequiereRol()) {
                    requests.requestMatchers(ruta.getRuta()).hasRole(ruta.getRol().getRol());
                } else {
                    requests.requestMatchers(ruta.getRuta()).permitAll();
                }
            }
            requests.anyRequest().authenticated();
        });

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    var usuarioOpt = usuarioService.getUsuarioPorUsername(authentication.getName());
                    if (usuarioOpt.isPresent()) {
                        var usuario = usuarioOpt.get();
                        var roles = usuarioService.getRoles(usuario.getIdUsuario());
                        var session = request.getSession();
                        session.setAttribute(SesionUtil.USUARIO, usuario);
                        session.setAttribute(SesionUtil.ID_USUARIO, usuario.getIdUsuario());
                        session.setAttribute(SesionUtil.ROLES, roles);
                        session.setAttribute(SesionUtil.ES_ADMIN, roles.contains(UsuarioService.ROL_ADMIN));
                    }
                    response.sendRedirect(request.getContextPath() + "/");
                })
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/")
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}