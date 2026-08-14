package com.nasu.tienda;

import com.nasu.tienda.util.SesionUtil;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba que las pantallas de administración se dibujan sin errores y que
 * un visitante sin rol no puede entrar a ellas.
 *
 * Las pruebas necesitan la base nasu creada, porque las pantallas consultan
 * productos y ventas reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PantallasAdminTests {

    @Autowired
    private MockMvc mockMvc;

    //Se arma la sesión de un administrador sin pasar por el formulario de login
    private MockHttpSession sesionAdmin() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUtil.ID_USUARIO, 1);
        sesion.setAttribute(SesionUtil.ES_ADMIN, Boolean.TRUE);
        return sesion;
    }

    @Test
    void elPanelSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/panel").session(sesionAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/panel"))
                //Comprueba que el HTML salió con los gráficos de Chart.js dentro
                .andExpect(content().string(containsString("graficoVentas")))
                .andExpect(content().string(containsString("chart.umd.js")));
    }

    @Test
    void elInventarioBajoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/inventario").session(sesionAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/inventario"));
    }

    @Test
    void laConsultaDeVentasSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/ventas").session(sesionAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/ventas"));
    }

    @Test
    void elReportePorPeriodoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/reporte/periodo").session(sesionAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/periodo"));
    }

    @Test
    void elListadoDeUsuariosMuestraLosRolesDeCadaCuenta() throws Exception {
        mockMvc.perform(get("/usuario/listado").session(sesionAdmin()))
                .andExpect(status().isOk())
                .andExpect(view().name("/usuario/listadoAdmin"))
                .andExpect(content().string(containsString("juan")))
                .andExpect(content().string(containsString("ADMIN")));
    }

    @Test
    void elCatalogoDeAdministracionSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(get("/producto/listadoAdminTemp").session(sesionAdmin()))
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
        MockHttpSession sesionCliente = new MockHttpSession();
        sesionCliente.setAttribute(SesionUtil.ID_USUARIO, 3);
        sesionCliente.setAttribute(SesionUtil.ES_ADMIN, Boolean.FALSE);

        mockMvc.perform(get("/reporte/ventas").session(sesionCliente))
                .andExpect(status().is3xxRedirection());
    }
}
