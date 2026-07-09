package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Direccion;
import com.nasu.tienda.service.DireccionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/direccion")
public class DireccionController {

    private final DireccionService direccionService;
    private final MessageSource messageSource;

    public DireccionController(DireccionService direccionService, MessageSource messageSource) {
        this.direccionService = direccionService;
        this.messageSource = messageSource;
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Direccion direccion, BindingResult errores,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        if (errores.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("direccion.error.validacion", null, Locale.getDefault()));
            return "redirect:/carrito/checkout";
        }

        direccion.setIdUsuario(idUsuario);
        direccionService.guardar(direccion);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("direccion.guardar.ok", null, Locale.getDefault()));
        return "redirect:/carrito/checkout";
    }

    private Integer getIdUsuario(HttpSession session) {
        Object idUsuario = session.getAttribute("idUsuario");
        if (idUsuario instanceof Integer id) {
            return id;
        }
        return null;
    }
}