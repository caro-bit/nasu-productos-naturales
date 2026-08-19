package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.RegistroService;
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
public class RegistroController {

    private final RegistroService registroService;
    private final MessageSource messageSource;

    public RegistroController(RegistroService registroService, MessageSource messageSource) {
        this.registroService = registroService;
        this.messageSource = messageSource;
    }

    // HU-01: muestra el formulario para registrar clientes
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "/registro/registro";
    }

    // HU-01: registra el cliente y lo deja listo para iniciar sesión
     @PostMapping("/registro/guardar")
    public String guardarRegistro(@Valid Usuario usuario, BindingResult errores,
            RedirectAttributes redirectAttributes, Model model) {

        if (usuario.getUsername() != null && !usuario.getUsername().isBlank()
                && registroService.existeUsername(usuario.getUsername())) {
            errores.rejectValue("username", "usuario.username.repetido",
                    messageSource.getMessage("usuario.error.username", null, Locale.getDefault()));
        }

        if (usuario.getCorreo() != null && !usuario.getCorreo().isBlank()
                && registroService.existeCorreo(usuario.getCorreo())) {
            errores.rejectValue("correo", "usuario.correo.repetido",
                    messageSource.getMessage("usuario.error.correo", null, Locale.getDefault()));
        }
        
         // Contraseña obligatoria
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            errores.rejectValue("password", "usuario.password.vacia",
                    "La contraseña no puede estar vacía.");
        } else if (usuario.getPassword().length() < 6) {
            errores.rejectValue("password", "usuario.password.corta",
                    "La contraseña debe tener al menos 6 caracteres.");
        }

        // Confirmación de contraseña obligatoria
        if (usuario.getConfirmarPassword() == null || usuario.getConfirmarPassword().isBlank()) {
            errores.rejectValue("confirmarPassword", "usuario.confirmarPassword.vacio",
                    "Debe confirmar la contraseña.");
        }
        
        // Contraseñas no coinciden (solo si ambas vienen con contenido)
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()
                && usuario.getConfirmarPassword() != null && !usuario.getConfirmarPassword().isBlank()
                && !usuario.getPassword().equals(usuario.getConfirmarPassword())) {
            errores.rejectValue("confirmarPassword", "usuario.password.no.coincide",
                    messageSource.getMessage("usuario.error.password", null, Locale.getDefault()));
        }

        if (errores.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "/registro/registro";
        }

        registroService.registrarCliente(usuario);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.registro.ok", null, Locale.getDefault()));
        return "redirect:/login";
    }
}