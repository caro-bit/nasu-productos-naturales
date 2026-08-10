package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Categoria;
import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.service.CategoriaService;
import com.nasu.tienda.service.ProductoService;
import com.nasu.tienda.util.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService, MessageSource messageSource) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.messageSource = messageSource;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        var productos = productoService.getProductos(true);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/producto/listado";
    }

    @PostMapping("/buscar")
    public String buscar(@RequestParam String descripcion, Model model) {
        var productos = productoService.buscarProductos(descripcion);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("busquedaActual", descripcion);
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/producto/listado";
    }

    @GetMapping("/categoria/{idCategoria}")
    public String categoria(@PathVariable("idCategoria") Integer idCategoria, Model model, RedirectAttributes redirectAttributes) {
        Optional<Categoria> categoriaOpt = categoriaService.getCategoria(idCategoria);
        if (categoriaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("categoria.error01", null, Locale.getDefault()));
            return "redirect:/producto/listado";
        }
        model.addAttribute("categoriaActual", categoriaOpt.get());
        model.addAttribute("idCategoriaActual", idCategoria);
        var productos = productoService.getProductosPorCategoria(idCategoria);
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/producto/listado";
    }

    @GetMapping("/detalle/{idProducto}")
    public String detalle(@PathVariable("idProducto") Integer idProducto, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> productoOpt = productoService.getProducto(idProducto);
        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("producto.error01", null, Locale.getDefault()));
            return "redirect:/producto/listado";
        }
        var producto = productoOpt.get();
        model.addAttribute("producto", producto);
        Optional<Categoria> categoriaOpt = categoriaService.getCategoria(producto.getIdCategoria());
        if (categoriaOpt.isPresent()) {
            model.addAttribute("categoria", categoriaOpt.get());
        }
        return "/producto/detalle";
    }
    
    @GetMapping("/listadoAdminTemp")
    public String listadoAdminTemp(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        var productos = productoService.getProductos(false); // incluye inactivos
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        Map<Integer, Categoria> categoriasMap = categorias.stream()
                .collect(Collectors.toMap(Categoria::getIdCategoria, c -> c));
        model.addAttribute("categoriasMap", categoriasMap);
        model.addAttribute("producto", new Producto());
        return "/producto/listadoAdminTemp";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Producto producto, BindingResult errores,
            HttpSession session, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        //Sin BindingResult, un dato inválido terminaba en la página de error del servidor
        if (errores.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errores.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/producto/listadoAdminTemp";
        }

        productoService.save(producto);
        redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault()));
        return "redirect:/producto/listadoAdminTemp";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProducto, HttpSession session, RedirectAttributes redirectAttributes) {
        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        String titulo = "todoOk";
        String detalle = "mensaje.eliminado";
        try {
            productoService.delete(idProducto);
        } catch (IllegalArgumentException e) {
            titulo = "error"; // el producto no existe
            detalle = "producto.error01";
        } catch (IllegalStateException e) {
            titulo = "error"; // tiene datos asociados (ej. detalles de pedido)
            detalle = "producto.error02";
        } catch (Exception e) {
            titulo = "error"; // cualquier otro error inesperado
            detalle = "producto.error03";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(detalle, null, Locale.getDefault()));
        return "redirect:/producto/listadoAdminTemp";
    }

    @GetMapping("/editar/{idProducto}")
    public String editar(@PathVariable("idProducto") Integer idProducto, HttpSession session,
            Model model, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        Optional<Producto> productoOpt = productoService.getProducto(idProducto);
        if (productoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("producto.error01", null, Locale.getDefault()));
            return "redirect:/producto/listadoAdminTemp";
        }
        model.addAttribute("producto", productoOpt.get());
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        return "/producto/modificaAdminTemp";
    }

    //El mantenimiento del catálogo es de uso exclusivo del administrador
    private String validarAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        return ControlAcceso.validarAdmin(session, redirectAttributes, messageSource);
    }

}
