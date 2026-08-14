package com.nasu.tienda.util;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.UsuarioService;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resuelve quién es el usuario autenticado a partir del contexto de Spring
 * Security, para que los controladores no tengan que leer la sesión a mano.
 */
@Component
public class UsuarioActual {

    private final UsuarioService usuarioService;

    public UsuarioActual(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Optional<Usuario> get() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioService.getUsuarioPorUsername(autenticacion.getName());
    }

    //Devuelve el id del usuario autenticado o null si es un visitante
    public Integer getIdUsuario() {
        return get().map(Usuario::getIdUsuario).orElse(null);
    }

    public boolean esAdmin() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            return false;
        }
        String permisoAdmin = "ROLE_" + UsuarioService.ROL_ADMIN;
        return autenticacion.getAuthorities().stream()
                .anyMatch(permiso -> permisoAdmin.equals(permiso.getAuthority()));
    }
}
