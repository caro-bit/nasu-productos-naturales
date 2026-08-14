package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.PedidoService;
import com.nasu.tienda.service.UsuarioService;
import com.nasu.tienda.util.UsuarioActual;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final UsuarioActual usuarioActual;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService, PedidoService pedidoService,
            UsuarioActual usuarioActual, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.usuarioActual = usuarioActual;
        this.messageSource = messageSource;
    }

    // HU-05: muestra el formulario para registrar clientes
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "/usuario/registro";
    }

    // HU-05: registra el cliente y lo deja listo para iniciar sesión
    @PostMapping("/registro/guardar")
    public String guardarRegistro(@Valid Usuario usuario, BindingResult errores,
            RedirectAttributes redirectAttributes, Model model) {

        if (usuario.getUsername() != null && !usuario.getUsername().isBlank()
                && usuarioService.existeUsername(usuario.getUsername())) {
            errores.rejectValue("username", "usuario.username.repetido",
                    messageSource.getMessage("usuario.error.username", null, Locale.getDefault()));
        }

        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()
                && usuarioService.existeCorreo(usuario.getCorreo())) {
            errores.rejectValue("correo", "usuario.correo.repetido",
                    messageSource.getMessage("usuario.error.correo", null, Locale.getDefault()));
        }

        if (usuario.getPassword() != null && usuario.getConfirmarPassword() != null
                && !usuario.getPassword().equals(usuario.getConfirmarPassword())) {
            errores.rejectValue("confirmarPassword", "usuario.password.no.coincide",
                    messageSource.getMessage("usuario.error.password", null, Locale.getDefault()));
        }

        if (errores.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "/usuario/registro";
        }

        usuarioService.registrarCliente(usuario);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.registro.ok", null, Locale.getDefault()));
        return "redirect:/login";
    }

    // HU-06: muestra el formulario de inicio de sesión.
    // La validación de las credenciales y el cierre de sesión los atiende
    // Spring Security (ver SecurityConfig), por eso aquí no hay POST /login
    // ni GET /logout.
    @GetMapping("/login")
    public String login() {
        return "/usuario/login";
    }

    // HU-06: permite al cliente ver su información y sus compras
    @GetMapping("/perfil")
    public String perfil(Model model, RedirectAttributes redirectAttributes) {
        var usuarioOpt = usuarioActual.get();
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.getPedidosPorUsuario(usuario.getIdUsuario()));
        return "/usuario/perfil";
    }
}
