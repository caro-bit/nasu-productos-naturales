package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Categoria;
import com.nasu.tienda.service.CategoriaService;
import com.nasu.tienda.service.ReporteService;
import com.nasu.tienda.util.ControlAcceso;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de administración del negocio: inventario bajo (HU-17), consulta de
 * ventas (HU-18) y reporte de ventas por período (HU-19).
 */
@Controller
@RequestMapping("/reporte")
public class ReporteController {

    private final ReporteService reporteService;
    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    public ReporteController(ReporteService reporteService, CategoriaService categoriaService,
            MessageSource messageSource) {
        this.reporteService = reporteService;
        this.categoriaService = categoriaService;
        this.messageSource = messageSource;
    }

    // HU-17: lista los productos activos cuyo inventario llegó al umbral definido
    @GetMapping("/inventario")
    public String inventario(@RequestParam(required = false) Integer umbral,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        int umbralAplicado = reporteService.resolverUmbral(umbral);
        var productos = reporteService.getProductosBajoInventario(umbralAplicado);

        model.addAttribute("productos", productos);
        model.addAttribute("umbral", umbralAplicado);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalAgotados", reporteService.contarAgotados(productos));
        model.addAttribute("categoriasMap", getCategoriasMap());
        return "/reporte/inventario";
    }

    // HU-18: detalle de las ventas realizadas, con filtro opcional por fechas
    @GetMapping("/ventas")
    public String ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        LocalDate desdeAplicado = reporteService.resolverDesde(desde);
        LocalDate hastaAplicado = reporteService.resolverHasta(hasta);
        if (desdeAplicado.isAfter(hastaAplicado)) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("reporte.error.rango", null, Locale.getDefault()));
            return "redirect:/reporte/ventas";
        }

        model.addAttribute("ventas", reporteService.getVentas(desdeAplicado, hastaAplicado));
        model.addAttribute("resumen", reporteService.getResumen(desdeAplicado, hastaAplicado));
        //Solo se devuelven al formulario las fechas que el administrador digitó
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "/reporte/ventas";
    }

    // HU-19: reporte de ventas por período, agrupado por producto y por día
    @GetMapping("/periodo")
    public String periodo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        String redireccion = validarAdmin(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        //Por defecto el reporte muestra el mes en curso
        LocalDate hastaAplicado = hasta != null ? hasta : LocalDate.now();
        LocalDate desdeAplicado = desde != null ? desde : hastaAplicado.withDayOfMonth(1);
        if (desdeAplicado.isAfter(hastaAplicado)) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("reporte.error.rango", null, Locale.getDefault()));
            return "redirect:/reporte/periodo";
        }

        var resumen = reporteService.getResumen(desdeAplicado, hastaAplicado);
        var ventasPorDia = reporteService.getVentasPorDia(desdeAplicado, hastaAplicado);

        model.addAttribute("resumen", resumen);
        model.addAttribute("ticketPromedio", reporteService.calcularTicketPromedio(resumen));
        model.addAttribute("ventasPorProducto", reporteService.getVentasPorProducto(desdeAplicado, hastaAplicado));
        model.addAttribute("ventasPorDia", ventasPorDia);
        //Sirve para dibujar la barra comparativa de cada día del período
        model.addAttribute("montoMaximoDia", reporteService.calcularMontoMaximo(ventasPorDia));
        model.addAttribute("desde", desdeAplicado);
        model.addAttribute("hasta", hastaAplicado);
        return "/reporte/periodo";
    }

    //Los reportes son de uso exclusivo del administrador del negocio
    private String validarAdmin(HttpSession session, RedirectAttributes redirectAttributes) {
        return ControlAcceso.validarAdmin(session, redirectAttributes, messageSource);
    }

    //Permite mostrar el nombre de la categoría a partir del id guardado en el producto
    private Map<Integer, Categoria> getCategoriasMap() {
        return categoriaService.getCategorias(false).stream()
                .collect(Collectors.toMap(Categoria::getIdCategoria, c -> c));
    }
}
