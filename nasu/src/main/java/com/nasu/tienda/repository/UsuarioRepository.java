package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Usuario;
import com.nasu.tienda.dto.UsuarioRoles;
import java.util.List;
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

    //Roles asignados al usuario, para distinguir al administrador de los clientes
    @Query(value = "SELECT r.rol FROM rol r "
            + "JOIN usuario_rol ur ON ur.id_rol = r.id_rol "
            + "WHERE ur.id_usuario = :idUsuario", nativeQuery = true)
    public List<String> findRolesByIdUsuario(@Param("idUsuario") Integer idUsuario);

    //HU-20: consulta derivada para listar los usuarios en un orden estable
    public List<Usuario> findAllByOrderByUsernameAsc();

    //HU-20: activa o desactiva la cuenta. Se hace con UPDATE y no con save()
    //porque la entidad valida confirmarPassword, que solo existe en el registro.
    @Modifying
    @Query(value = "UPDATE usuario SET activo = :activo WHERE id_usuario = :idUsuario", nativeQuery = true)
    public void actualizarEstado(@Param("idUsuario") Integer idUsuario, @Param("activo") boolean activo);

    //HU-20: quita un rol al usuario
    @Modifying
    @Query(value = "DELETE FROM usuario_rol WHERE id_usuario = :idUsuario AND id_rol = :idRol", nativeQuery = true)
    public void quitarRol(@Param("idUsuario") Integer idUsuario, @Param("idRol") Integer idRol);

    //HU-20: evita insertar dos veces la misma asignación
    @Query(value = "SELECT COUNT(*) FROM usuario_rol WHERE id_usuario = :idUsuario AND id_rol = :idRol",
            nativeQuery = true)
    public long contarAsignacion(@Param("idUsuario") Integer idUsuario, @Param("idRol") Integer idRol);

    //HU-20: cuenta los administradores activos, para no dejar el sistema sin ninguno
    @Query(value = "SELECT COUNT(DISTINCT u.id_usuario) FROM usuario u "
            + "JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario "
            + "JOIN rol r ON r.id_rol = ur.id_rol "
            + "WHERE r.rol = :rol AND u.activo = TRUE", nativeQuery = true)
    public long contarUsuariosActivosConRol(@Param("rol") String rol);

    //HU-20: roles de todos los usuarios en una sola consulta, para el listado
    @Query(value = "SELECT u.id_usuario AS idUsuario, "
            + "GROUP_CONCAT(r.rol ORDER BY r.rol SEPARATOR ', ') AS roles "
            + "FROM usuario u "
            + "LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario "
            + "LEFT JOIN rol r ON r.id_rol = ur.id_rol "
            + "GROUP BY u.id_usuario", nativeQuery = true)
    public List<UsuarioRoles> findResumenRoles();
}
