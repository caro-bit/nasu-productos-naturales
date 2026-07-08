package com.nasu.tienda.controller;

import com.nasu.tienda.service.CarritoService;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final MessageSource messageSource;

    public CarritoController(CarritoService carritoService, MessageSource messageSource) {
        this.carritoService = carritoService;
        this.messageSource = messageSource;
    }

    // HU-03: muestra los productos guardados en el carrito del cliente
    @GetMapping("/listado")
    public String listado(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        var detalles = carritoService.getDetalleActivo(idUsuario);
        model.addAttribute("detalles", detalles);
        model.addAttribute("total", carritoService.calcularTotal(detalles));
        model.addAttribute("cantidadArticulos", detalles.stream().mapToInt(d -> d.getCantidad()).sum());
        return "/carrito/listado";
    }

    // HU-03: agrega un producto activo al carrito para comprarlo posteriormente
    @PostMapping("/agregar/{idProducto}")
    public String agregar(@PathVariable("idProducto") Integer idProducto,
            @RequestParam(defaultValue = "1") Integer cantidad,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        try {
            carritoService.agregarProducto(idUsuario, idProducto, cantidad);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("carrito.agregar.ok", null, Locale.getDefault()));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito/listado";
    }

    @PostMapping("/actualizar/{idDetcarrito}")
    public String actualizar(@PathVariable("idDetcarrito") Integer idDetcarrito,
            @RequestParam Integer cantidad, HttpSession session, RedirectAttributes redirectAttributes) {

        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        try {
            carritoService.actualizarCantidad(idUsuario, idDetcarrito, cantidad);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("carrito.actualizar.ok", null, Locale.getDefault()));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito/listado";
    }

    @PostMapping("/eliminar/{idDetcarrito}")
    public String eliminar(@PathVariable("idDetcarrito") Integer idDetcarrito,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        try {
            carritoService.eliminarProducto(idUsuario, idDetcarrito);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("carrito.eliminar.ok", null, Locale.getDefault()));
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito/listado";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer idUsuario = getIdUsuario(session);
        if (idUsuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        carritoService.vaciarCarrito(idUsuario);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("carrito.vaciar.ok", null, Locale.getDefault()));
        return "redirect:/carrito/listado";
    }

    private Integer getIdUsuario(HttpSession session) {
        Object idUsuario = session.getAttribute("idUsuario");
        if (idUsuario instanceof Integer id) {
            return id;
        }
        return null;
    }
}
