package com.nasu.tienda.service;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    //Nombre del rol administrador dentro de la tabla rol
    public static final String ROL_ADMIN = "ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
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
}
