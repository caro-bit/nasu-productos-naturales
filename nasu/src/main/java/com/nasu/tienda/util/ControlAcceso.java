package com.nasu.tienda.util;

import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Valida el acceso a las pantallas de administración del negocio para que la
 * misma regla se aplique en todos los controladores.
 */
public final class ControlAcceso {

    private ControlAcceso() {
        //Clase de utilidades, no se instancia
    }

    /**
     * Devuelve la redirección que corresponde cuando el usuario no puede entrar
     * a una pantalla de administración, o {@code null} si el acceso es válido.
     */
    public static String validarAdmin(HttpSession session, RedirectAttributes redirectAttributes,
            MessageSource messageSource) {

        if (SesionUtil.getIdUsuario(session) == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }
        if (!SesionUtil.esAdmin(session)) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.acceso.denegado", null, Locale.getDefault()));
            return "redirect:/";
        }
        return null;
    }
}
