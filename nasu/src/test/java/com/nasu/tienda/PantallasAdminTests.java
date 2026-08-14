package com.nasu.tienda;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.UsuarioService;
import com.nasu.tienda.util.SesionUtil;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Comprueba que las pantallas de administración se dibujan sin errores y que
 * las reglas de acceso se aplican de verdad.
 *
 * El acceso se controla en dos capas: Spring Security filtra por rol usando la
 * tabla ruta, y los controladores vuelven a validar con lo que el login deja en
 * la sesión. Por eso una petición de administrador necesita las dos cosas.
 *
 * Las pruebas necesitan la base nasu creada, porque las pantallas consultan
 * productos y ventas reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PantallasAdminTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    //Petición de un administrador: rol para Spring Security y sesión para
    //ControlAcceso, armada igual que la deja el successHandler del login.
    private MockHttpServletRequestBuilder comoAdmin(String direccion) {
        Usuario admin = usuarioService.getUsuarioPorUsername("juan").orElseThrow();

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUtil.USUARIO, admin);
        sesion.setAttribute(SesionUtil.ID_USUARIO, admin.getIdUsuario());
        sesion.setAttribute(SesionUtil.ROLES, usuarioService.getRoles(admin.getIdUsuario()));
        sesion.setAttribute(SesionUtil.ES_ADMIN, Boolean.TRUE);
        return get(direccion).session(sesion).with(user("juan").roles("ADMIN"));
    }

    @Test
    void elPanelSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(comoAdmin("/reporte/panel"))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/panel"))
                //Comprueba que el HTML salió con los gráficos de Chart.js dentro
                .andExpect(content().string(containsString("graficoVentas")))
                .andExpect(content().string(containsString("chart.umd.js")));
    }

    @Test
    void elInventarioBajoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(comoAdmin("/reporte/inventario"))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/inventario"));
    }

    @Test
    void laConsultaDeVentasSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(comoAdmin("/reporte/ventas"))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/ventas"));
    }

    @Test
    void elReportePorPeriodoSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(comoAdmin("/reporte/periodo"))
                .andExpect(status().isOk())
                .andExpect(view().name("/reporte/periodo"));
    }

    @Test
    void elListadoDeUsuariosMuestraLosRolesDeCadaCuenta() throws Exception {
        mockMvc.perform(comoAdmin("/usuario/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("/usuario/listadoAdmin"))
                .andExpect(content().string(containsString("juan")))
                .andExpect(content().string(containsString("ADMIN")));
    }

    @Test
    void elCatalogoDeAdministracionSeDibujaParaElAdministrador() throws Exception {
        mockMvc.perform(comoAdmin("/producto/listadoAdmin"))
                .andExpect(status().isOk())
                .andExpect(view().name("/producto/listadoAdmin"));
    }

    @Test
    void unVisitanteSinSesionEsEnviadoAlLogin() throws Exception {
        mockMvc.perform(get("/reporte/panel"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void unClienteSinRolAdminNoEntraALosReportes() throws Exception {
        mockMvc.perform(get("/reporte/ventas").with(user("pedro").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void elCatalogoPublicoSeVeSinIniciarSesion() throws Exception {
        mockMvc.perform(get("/producto/listado"))
                .andExpect(status().isOk())
                .andExpect(view().name("/producto/listado"));
    }
}
