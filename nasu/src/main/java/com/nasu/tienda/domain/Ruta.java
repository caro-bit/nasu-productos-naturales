package com.nasu.tienda.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

/**
 * Ruta de la aplicación con el rol que se necesita para entrar a ella. La
 * configuración de seguridad arma sus reglas leyendo esta tabla, de modo que
 * los permisos se cambian en la base y no en el código.
 */
@Data
@Entity
@Table(name = "ruta")
public class Ruta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @Column(nullable = false)
    private String ruta;

    @Column(name = "requiere_rol", nullable = false)
    private boolean requiereRol;

    //Relación muchos a uno: varias rutas pueden exigir el mismo rol
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol rol;
}
