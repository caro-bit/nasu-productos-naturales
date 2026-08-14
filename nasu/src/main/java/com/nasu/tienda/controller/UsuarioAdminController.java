package com.nasu.tienda.controller;

import com.nasu.tienda.service.UsuarioService;
import com.nasu.tienda.util.ControlAcceso;
import com.nasu.tienda.util.SesionUtil;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Administración de usuarios y de sus permisos (HU-20). Permite activar o
 * desactivar cuentas y asignar o quitar roles sin tocar la base a mano.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public UsuarioAdminController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        var usuarios = usuarioService.getUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("rolesPorUsuario", usuarioService.getRolesPorUsuario());
        model.addAttribute("roles", usuarioService.getRolesDisponibles());
        //Permite marcar la fila del administrador que está usando la pantalla
        model.addAttribute("idUsuarioActual", SesionUtil.getIdUsuario(session));
        return "/usuario/listadoAdmin";
    }

    @PostMapping("/estado")
    public String cambiarEstado(@RequestParam Integer idUsuario, HttpSession session,
            RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        try {
            usuarioService.cambiarEstado(idUsuario, SesionUtil.getIdUsuario(session));
            mensaje(redirectAttributes, "todoOk", "mensaje.actualizado");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mensaje(redirectAttributes, "error", ex.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    @PostMapping("/rol/asignar")
    public String asignarRol(@RequestParam Integer idUsuario, @RequestParam Integer idRol,
            HttpSession session, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        try {
            usuarioService.asignarRol(idUsuario, idRol);
            mensaje(redirectAttributes, "todoOk", "usuario.rol.asignado");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mensaje(redirectAttributes, "error", ex.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    @PostMapping("/rol/quitar")
    public String quitarRol(@RequestParam Integer idUsuario, @RequestParam Integer idRol,
            HttpSession session, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        try {
            usuarioService.quitarRol(idUsuario, idRol, SesionUtil.getIdUsuario(session));
            mensaje(redirectAttributes, "todoOk", "usuario.rol.quitado");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mensaje(redirectAttributes, "error", ex.getMessage());
        }
        return "redirect:/usuario/listado";
    }

    //Los errores del servicio viajan como clave de mensaje para poder traducirlos
    private void mensaje(RedirectAttributes redirectAttributes, String tipo, String clave) {
        redirectAttributes.addFlashAttribute(tipo,
                messageSource.getMessage(clave, null, Locale.getDefault()));
    }

    private String validarAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        return ControlAcceso.validarAdmin(session, redirectAttributes, messageSource);
    }
}
