package com.nasu.tienda.controller;

import com.nasu.tienda.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;
    private final MessageSource messageSource;

    public PedidoController(PedidoService pedidoService, MessageSource messageSource) {
        this.pedidoService = pedidoService;
        this.messageSource = messageSource;
    }

    @PostMapping("/confirmar")
    public String confirmar(@RequestParam Integer idDireccion, @RequestParam Integer idMetodoPago,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        try {
            pedidoService.confirmarPedido(idUsuario, idDireccion, idMetodoPago);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("pedido.confirmar.ok", null, Locale.getDefault()));
            return "redirect:/carrito/listado";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/carrito/checkout";
        }
    }

    private Integer getIdUsuario(HttpSession session) {
        Object idUsuario = session.getAttribute("idUsuario");
        if (idUsuario instanceof Integer id) {
            return id;
        }
        return null;
    }
}