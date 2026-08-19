package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Ruta;
import com.nasu.tienda.repository.RolRepository;
import com.nasu.tienda.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rutas")
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("rutas", rutaRepository.findAllByOrderByRequiereRolAsc());
        model.addAttribute("totalRutas", rutaRepository.count());
        model.addAttribute("rutaForm", new Ruta());
        model.addAttribute("roles", rolRepository.findAll());
        return "/rutas/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("rutaForm") Ruta ruta) {
        rutaRepository.save(ruta);
        return "redirect:/rutas/listado";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Integer id, Model model) {
        Ruta ruta = rutaRepository.findById(id).orElse(null);
        model.addAttribute("rutaForm", ruta);
        model.addAttribute("roles", rolRepository.findAll());
        return "/rutas/modifica";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idRuta) {
        rutaRepository.deleteById(idRuta);
        return "redirect:/rutas/listado";
    }
}
