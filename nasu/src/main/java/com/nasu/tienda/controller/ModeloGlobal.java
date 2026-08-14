package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.util.UsuarioActual;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Pone los datos del usuario autenticado en el modelo de todas las vistas, para
 * que la barra de navegación pueda saludarlo y mostrar el menú que le toca.
 */
@ControllerAdvice
public class ModeloGlobal {

    private final UsuarioActual usuarioActual;

    public ModeloGlobal(UsuarioActual usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual() {
        return usuarioActual.get().orElse(null);
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        return usuarioActual.esAdmin();
    }
}
