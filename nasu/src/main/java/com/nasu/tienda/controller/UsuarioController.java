package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.PedidoService;
import com.nasu.tienda.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService, PedidoService pedidoService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.messageSource = messageSource;
    }

    // HU-01: muestra el formulario para registrar clientes
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "/usuario/registro";
    }

    // HU-01: registra el cliente y lo deja listo para iniciar sesión
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

    // HU-02: muestra el formulario para iniciar sesión
    @GetMapping("/login")
    public String login() {
        return "/usuario/login";
    }

    // HU-02: valida las credenciales y guarda el cliente en sesión
    @PostMapping("/login")
    public String autenticar(@RequestParam String username, @RequestParam String password,
            HttpSession session, RedirectAttributes redirectAttributes) {

        var usuarioOpt = usuarioService.validarLogin(username, password);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.error", null, Locale.getDefault()));
            return "redirect:/login";
        }

        var usuario = usuarioOpt.get();
        session.setAttribute("usuario", usuario);
        session.setAttribute("idUsuario", usuario.getIdUsuario());
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.login.ok", new Object[]{usuario.getNombre()}, Locale.getDefault()));
        return "redirect:/";
    }

    // HU-02: permite al cliente ver su información y sus compras
    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.getPedidosPorUsuario(usuario.getIdUsuario()));
        return "/usuario/perfil";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.logout.ok", null, Locale.getDefault()));
        return "redirect:/";
    }
}
