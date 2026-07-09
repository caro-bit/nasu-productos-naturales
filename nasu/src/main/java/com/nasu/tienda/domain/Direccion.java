package com.nasu.tienda.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "direccion")
public class Direccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "nombre_direccion", nullable = false, length = 50)
    @NotBlank(message = "El nombre de la dirección no puede estar vacío.")
    @Size(max = 50)
    private String nombreDireccion;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "La provincia no puede estar vacía.")
    @Size(max = 50)
    private String provincia;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El distrito no puede estar vacío.")
    @Size(max = 50)
    private String distrito;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El cantón no puede estar vacío.")
    @Size(max = 50)
    private String canton;

    @Column(name = "codigo_postal", nullable = false)
    @NotNull(message = "El código postal no puede estar vacío.")
    private Integer codigoPostal;

    @Column(name = "dir_exacta", nullable = false, length = 100)
    @NotBlank(message = "La dirección exacta no puede estar vacía.")
    @Size(max = 100)
    private String dirExacta;

    @Column(name = "detalles_adicionales", length = 100)
    @Size(max = 100)
    private String detallesAdicionales;
}