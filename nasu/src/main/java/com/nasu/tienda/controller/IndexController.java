package com.nasu.tienda.controller;

import com.nasu.tienda.service.CategoriaService;
import com.nasu.tienda.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public IndexController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        var categorias = categoriaService.getCategorias(true);
        model.addAttribute("categorias", categorias);
        var destacados = productoService.getDestacados();
        model.addAttribute("destacados", destacados);
        return "index";
    }
}
