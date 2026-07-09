package com.nasu.tienda.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "metpago")
public class MetPago implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metpago")
    private Integer idMetpago;

    @Column(name = "nombre_metpago", nullable = false, length = 50)
    private String nombreMetpago;
}