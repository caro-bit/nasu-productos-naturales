package com.nasu.tienda.dto;

/**
 * Proyección con los roles que tiene asignados cada usuario, para poder
 * mostrarlos en el listado de administración sin consultar uno por uno (HU-20).
 */
public interface UsuarioRoles {

    Integer getIdUsuario();

    String getRoles();
}
