package com.nasu.tienda.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "detcarrito")
public class DetCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detcarrito")
    private Integer idDetcarrito;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_carrito", nullable = false)
    private Integer idCarrito;

    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    @Min(value = 1, message = "La cantidad debe ser mayor a 0.")
    private Integer cantidad;

    private BigDecimal subtotal;

    private Boolean activo;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;
}
