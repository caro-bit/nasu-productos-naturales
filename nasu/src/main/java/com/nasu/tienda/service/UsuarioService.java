package com.nasu.tienda.service;

import com.nasu.tienda.domain.Rol;
import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.repository.RolRepository;
import com.nasu.tienda.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    //Nombre del rol administrador dentro de la tabla rol
    public static final String ROL_ADMIN = "ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
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
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameYPassword(String username,
            String password) {
        return usuarioRepository.findByUsernameAndPassword(username, password);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameOCorreo(String username,
            String correo) {
        return usuarioRepository.findByUsernameOrCorreo(username, correo);
    }

    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
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

    @Transactional
    public void save(Usuario usuario, MultipartFile imagenFile, boolean encriptaClave) {
        final Integer idUser = usuario.getIdUsuario();
        Optional<Usuario> usuarioDuplicado = usuarioRepository.findByUsernameOrCorreo(null, usuario.getCorreo());
        if (usuarioDuplicado.isPresent()) {
            Usuario encontrado = usuarioDuplicado.get();

            // Verifica si estamos en modo CREACIÓN (idUser == null) O si el ID encontrado NO es el mismo que estamos actualizando
            if (idUser == null || !encontrado.getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El correo ya está en uso por otro usuario.");
            }
        }

        //Se valida si la clave se va actualizar o si es un usuario nuevo se debe actualizar...
        var asignarRol = false;
        if (usuario.getIdUsuario() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios.");
            }

            usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            asignarRol = true;
        } else {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                // El campo de password en el formulario viene vacío (no se desea actualizar).
                // Recuperamos la contraseña HASHED existente de la base de datos.
                Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario a modificar no encontrado."));

                usuario.setRoles(usuarioExistente.getRoles());

                if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                    usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuarioExistente.getPassword()) : usuarioExistente.getPassword());
                } else {
                    usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
                }
            }
            usuario = usuarioRepository.save(usuario);
            if (asignarRol) {
                asignarRolPorUsername(usuario.getUsername(), "USER");
            }
        }
    }
    

    @Transactional
    public void delete(Integer idUsuario) {
        // Verifica si la categoría existe antes de intentar eliminarlo
        if (!usuarioRepository.existsById(idUsuario)) {
            // Lanza una excepción para indicar que el usuario no fue encontrado
            throw new IllegalArgumentException(
                    "El usuario con ID " + idUsuario + " no existe.");
        }
        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException e) {
            // Excepción para encapsular el problema de integridad de datos
            throw new IllegalStateException(
                    "No se puede eliminar el usuario. Tiene datos asociados.", e);
        }
    }

    //Sección para gestionar roles a usuarios...
    @Transactional(readOnly = true)
    public List<String> getRolesNombres() {
        // Retorna una lista de Strings con el nombre de cada rol
        return rolRepository.findAll().stream()
                .map(Rol::getRol)
                .toList();
    }

    @Transactional
    public Usuario asignarRolPorUsername(String username, String rolStr) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        Usuario usuario = usuarioOpt.get();
        Optional<Rol> rolOpt = rolRepository.findByRol(rolStr);
        if (rolOpt.isEmpty()) {
            throw new RuntimeException("Rol no encontrado.");
        }
        Rol rol = rolOpt.get();
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario eliminarRol(String username, Integer idRol) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado: " + username);
        }
        Usuario usuario = usuarioOpt.get();

        // Filtra la colección de roles del usuario para mantener solo los que NO coinciden con idRol
        usuario.getRoles().removeIf(rol -> rol.getIdRol().equals(idRol));

        // Guarda el usuario con la colección de roles modificada
        return usuarioRepository.save(usuario);
    }

}
