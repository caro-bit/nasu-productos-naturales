package com.nasu.tienda.controller;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.service.PedidoService;
import com.nasu.tienda.service.UsuarioService;
import com.nasu.tienda.util.SesionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final MessageSource messageSource;

    public UsuarioController(UsuarioService usuarioService, PedidoService pedidoService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.messageSource = messageSource;
    }

    // HU-02: muestra el formulario para iniciar sesión
    @GetMapping("/login")
    public String login() {
        return "/usuario/login";
    }

    // HU-02: valida las credenciales y guarda el cliente en sesión
    @PostMapping("/login")
    public String autenticar(@RequestParam String username, @RequestParam String password,
            HttpSession session, RedirectAttributes redirectAttributes) {

        var usuarioOpt = usuarioService.validarLogin(username, password);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.error", null, Locale.getDefault()));
            return "redirect:/login";
        }

        var usuario = usuarioOpt.get();
        var roles = usuarioService.getRoles(usuario.getIdUsuario());
        session.setAttribute(SesionUtil.USUARIO, usuario);
        session.setAttribute(SesionUtil.ID_USUARIO, usuario.getIdUsuario());
        session.setAttribute(SesionUtil.ROLES, roles);
        //Se guarda el rol en sesión para habilitar las pantallas de administración
        session.setAttribute(SesionUtil.ES_ADMIN, roles.contains(UsuarioService.ROL_ADMIN));
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.login.ok", new Object[]{usuario.getNombre()}, Locale.getDefault()));
        return "redirect:/";
    }

    // HU-02: permite al cliente ver su información y sus compras
    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute(SesionUtil.USUARIO);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.login.requerido", null, Locale.getDefault()));
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.getPedidosPorUsuario(usuario.getIdUsuario()));
        return "/usuario/perfil";
    }
    
    
    ///HU-20 administrar usuarios y sus permisos para controlar el acceso al sistema. 
    @GetMapping("/usuario/listado")
    public String inicio(Model model) {
        var usuarios = usuarioService.getUsuarios(false);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("usuario", new Usuario()); // necesario para que los fragmentos "agregar"/"editar" no fallen al parsear
        return "/usuario/listado";
    }
     @PostMapping("/usuario/guardar")
    public String guardar(@Valid Usuario usuario,
            BindingResult bindingResult,
            @RequestParam MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {

        // La contraseña solo es obligatoria al CREAR un usuario nuevo.
        // Al editar, el campo puede llegar vacío (significa "no cambiar la contraseña").
        if (usuario.getIdUsuario() == null
                && (usuario.getPassword() == null || usuario.getPassword().isBlank())) {
            bindingResult.rejectValue("password", "usuario.password.obligatoria",
                    "La contraseña es obligatoria para nuevos usuarios.");
        }

        // Si SÍ viene una contraseña (nueva o al crear), valida el largo mínimo.
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()
                && usuario.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "usuario.password.corta",
                    "La contraseña debe tener al menos 6 caracteres.");
        }

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> System.out.println(e.getDefaultMessage()));
            // Redirige al formulario de edición/creación para mostrar errores
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error04", null, Locale.getDefault()));
            // Si no hay idUsuario, redirige al listado con modal para agregar
            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }
            // Si hay idUsuario, redirige al formulario de modificación
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        }

        try {
            usuarioService.save(usuario, imagenFile, true);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("mensaje.actualizado", null, Locale.getDefault()));
        } catch (DataIntegrityViolationException e) {
            // Correo duplicado u otra violación de integridad
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        } catch (IllegalArgumentException e) {
            // Contraseña obligatoria, usuario no encontrado, etc.
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (usuario.getIdUsuario() == null) {
                return "redirect:/usuario/listado";
            }
            return "redirect:/usuario/modificar/" + usuario.getIdUsuario();
        }

        return "redirect:/usuario/listado";
    }

    @PostMapping("/usuario/eliminar")
    public String eliminar(@RequestParam Integer idUsuario,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.delete(idUsuario);
            redirectAttributes.addFlashAttribute("todoOk",
                    messageSource.getMessage("mensaje.eliminado", null,
                            Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            // Captura argumento inválido para el mensaje de "no existe"
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error01", null,
                            Locale.getDefault()));
        } catch (IllegalStateException e) {
            // Captura estado ilegal para el mensaje de "datos asociados"
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error02", null,
                            Locale.getDefault()));
        } catch (NoSuchMessageException e) {
            // Captura cualquier otra excepción inesperada
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage("usuario.error03", null,
                            Locale.getDefault()));
        }
        return "redirect:/usuario/listado";
    }

    @GetMapping("/usuario/modificar/{idUsuario}")
    public String modificar(@PathVariable("idUsuario") Integer idUsuario,
            Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioService.getUsuario(idUsuario);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "El usuario no fue encontrado.");
            return "redirect:/usuario/listado";
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setPassword("");
        model.addAttribute("usuario", usuario);
        return "/usuario/modifica";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("usuario.logout.ok", null, Locale.getDefault()));
        return "redirect:/";
    }
}
