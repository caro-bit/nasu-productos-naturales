package com.nasu.tienda.service;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
}