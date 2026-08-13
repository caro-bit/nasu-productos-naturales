package com.nasu.tienda.util;

import jakarta.servlet.http.HttpSession;

/**
 * Centraliza la lectura de los datos que el inicio de sesión deja en la sesión
 * del usuario, para que los controladores no repitan la misma validación.
 */
public final class SesionUtil {

    public static final String USUARIO = "usuario";
    public static final String ID_USUARIO = "idUsuario";
    public static final String ES_ADMIN = "esAdmin";
    public static final String ROLES = "roles";

    private SesionUtil() {
        //Clase de utilidades, no se instancia
    }

    //Devuelve el id del usuario en sesión o null si no ha iniciado sesión
    public static Integer getIdUsuario(HttpSession session) {
        Object idUsuario = session.getAttribute(ID_USUARIO);
        if (idUsuario instanceof Integer id) {
            return id;
        }
        return null;
    }

    //Indica si el usuario en sesión tiene el rol de administrador
    public static boolean esAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(ES_ADMIN));
    }
}
