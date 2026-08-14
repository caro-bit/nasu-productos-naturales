package com.nasu.tienda;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Comprueba que las pantallas de administración se dibujan sin errores y que
 * las reglas de acceso de la tabla ruta se están aplicando de verdad.
 *
 * Las pruebas necesitan la base nasu creada, porque las pantallas consultan
 * productos y ventas reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PantallasAdminTests {

    @Autowired
    private MockMvc mockMvc;

    //Usuarios simulados: Spring Security espera los permisos sin el prefijo ROLE_
    private RequestPostProcessor admin() {
        return user("juan").roles("ADMIN");
    }

    private RequestPostProcessor cliente() {
        return user("pedro").roles("USER");
    }

    @Test
    void elPanelSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/panel").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/panel"))
                //Comprueba que el HTML salió con los gráficos de Chart.js dentro
                .andExpect(content().string(containsString("graficoVentas")))
                .andExpect(content().string(containsString("chart.umd.js")));
    }

    @Test
    void elInventarioBajoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/inventario").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/inventario"));
    }

    @Test
    void laConsultaDeVentasSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/ventas").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/ventas"));
    }

    @Test
    void elReportePorPeriodoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/periodo").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/periodo"));
    }

    @Test
    void elListadoDeUsuariosMuestraLosRolesDeCadaCuenta() throws Exception {
        mockMvc.perform(get("/usuario/listado").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/usuario/listadoAdmin"))
                .andExpect(content().string(containsString("juan")))
                .andExpect(content().string(containsString("ADMIN")));
    }

    @Test
    void elCatalogoDeAdministracionSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/producto/listadoAdminTemp").with(admin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/producto/listadoAdminTemp"));
    }

    @Test
    void unVisitanteSinSesionEsEnviadoAlLogin() throws Exception {
        mockMvc.perform(get("/reporte/panel"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void unClienteSinRolAdminNoEntraALosReportes() throws Exception {
        mockMvc.perform(get("/reporte/ventas").with(cliente()))
                .andExpect(status().isForbidden());
    }

    @Test
    void elCatalogoPublicoSeVeSinIniciarSesion() throws Exception {
        mockMvc.perform(get("/producto/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("/producto/listado"));
    }
}
