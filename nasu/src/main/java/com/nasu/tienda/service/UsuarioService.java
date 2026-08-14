package com.nasu.tienda.service;

import com.nasu.tienda.domain.Rol;
import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.dto.UsuarioRoles;
import com.nasu.tienda.repository.RolRepository;
import com.nasu.tienda.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    //Nombre del rol administrador dentro de la tabla rol
    public static final String ROL_ADMIN = "ADMIN";

    //Claves de mensaje que se devuelven cuando una acción de la HU-20 no procede
    public static final String ERROR_USUARIO = "usuario.error.noExiste";
    public static final String ERROR_ROL = "usuario.error.rolNoExiste";
    public static final String ERROR_AUTO_DESACTIVAR = "usuario.error.autoDesactivar";
    public static final String ERROR_AUTO_QUITAR_ADMIN = "usuario.error.autoQuitarAdmin";
    public static final String ERROR_ULTIMO_ADMIN = "usuario.error.ultimoAdmin";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    @Transactional
    public Usuario registrarCliente(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(Boolean.TRUE);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        var idRolUser = usuarioRepository.findIdRolByNombre("USER");
        if (idRolUser.isPresent()) {
            usuarioRepository.asignarRol(usuarioGuardado.getIdUsuario(), idRolUser.get());
        }
        return usuarioGuardado;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> validarLogin(String username, String password) {
        var usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        var usuario = usuarioOpt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return Optional.empty();
        }
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    @Transactional(readOnly = true)
    public List<String> getRoles(Integer idUsuario) {
        return usuarioRepository.findRolesByIdUsuario(idUsuario);
    }

    //Permite validar en los controladores si el usuario en sesión administra el negocio
    @Transactional(readOnly = true)
    public boolean esAdministrador(Integer idUsuario) {
        return getRoles(idUsuario).contains(ROL_ADMIN);
    }

    // --- HU-20: administración de usuarios y permisos ---

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public List<Rol> getRolesDisponibles() {
        return rolRepository.findAllByOrderByRolAsc();
    }

    //Roles de todos los usuarios, indexados por id para usarlos en el listado
    @Transactional(readOnly = true)
    public Map<Integer, String> getRolesPorUsuario() {
        return usuarioRepository.findResumenRoles().stream()
                .collect(Collectors.toMap(UsuarioRoles::getIdUsuario,
                        r -> r.getRoles() != null ? r.getRoles() : ""));
    }

    @Transactional
    public void cambiarEstado(Integer idUsuario, Integer idAdmin) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_USUARIO));

        boolean activar = !Boolean.TRUE.equals(usuario.getActivo());
        if (!activar) {
            //Al desactivar hay que cuidar que el administrador no se bloquee a sí mismo
            if (idUsuario.equals(idAdmin)) {
                throw new IllegalStateException(ERROR_AUTO_DESACTIVAR);
            }
            if (esUltimoAdministrador(idUsuario)) {
                throw new IllegalStateException(ERROR_ULTIMO_ADMIN);
            }
        }
        usuarioRepository.actualizarEstado(idUsuario, activar);
    }

    @Transactional
    public void asignarRol(Integer idUsuario, Integer idRol) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException(ERROR_USUARIO);
        }
        if (!rolRepository.existsById(idRol)) {
            throw new IllegalArgumentException(ERROR_ROL);
        }
        //La tabla usuario_rol tiene llave primaria compuesta: no se puede repetir
        if (usuarioRepository.contarAsignacion(idUsuario, idRol) == 0) {
            usuarioRepository.asignarRol(idUsuario, idRol);
        }
    }

    @Transactional
    public void quitarRol(Integer idUsuario, Integer idRol, Integer idAdmin) {
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_ROL));

        if (ROL_ADMIN.equals(rol.getRol())) {
            if (idUsuario.equals(idAdmin)) {
                throw new IllegalStateException(ERROR_AUTO_QUITAR_ADMIN);
            }
            if (esUltimoAdministrador(idUsuario)) {
                throw new IllegalStateException(ERROR_ULTIMO_ADMIN);
            }
        }
        usuarioRepository.quitarRol(idUsuario, idRol);
    }

    //El sistema siempre debe quedar con al menos un administrador activo
    private boolean esUltimoAdministrador(Integer idUsuario) {
        if (!esAdministrador(idUsuario)) {
            return false;
        }
        return usuarioRepository.contarUsuariosActivosConRol(ROL_ADMIN) <= 1;
    }
}
