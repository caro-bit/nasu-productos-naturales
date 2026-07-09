package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    public Optional<Usuario> findByUsername(String username);

    public boolean existsByUsername(String username);

    public boolean existsByCorreo(String correo);

    @Query(value = "SELECT id_rol FROM rol WHERE rol = :rol LIMIT 1", nativeQuery = true)
    public Optional<Integer> findIdRolByNombre(@Param("rol") String rol);

    @Modifying
    @Query(value = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (:idUsuario, :idRol)", nativeQuery = true)
    public void asignarRol(@Param("idUsuario") Integer idUsuario, @Param("idRol") Integer idRol);
}
