package com.nasu.tienda.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, unique = true, length = 30)
    @NotBlank(message = "El usuario no puede estar vacío.")
    @Size(max = 30, message = "El usuario no puede tener más de 30 caracteres.")
    private String username;

    @Column(nullable = false, length = 512)
    private String password;

    @Transient
    private String confirmarPassword;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 20, message = "El nombre no puede tener más de 20 caracteres.")
    private String nombre;

    @Column(nullable = false, length = 30)
    @NotBlank(message = "Los apellidos no pueden estar vacíos.")
    @Size(max = 30, message = "Los apellidos no pueden tener más de 30 caracteres.")
    private String apellidos;

    @Column(unique = true, length = 75)
    @NotBlank(message = "El correo no puede estar vacío.")
    @Email(message = "Digite un correo válido.")
    @Size(max = 75, message = "El correo no puede tener más de 75 caracteres.")
    private String correo;

    @Column(length = 25)
    @Size(max = 25, message = "El teléfono no puede tener más de 25 caracteres.")
    private String telefono;

    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    private Boolean activo;
    
    // Relación Many-to-Many con la entidad Rol
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<Rol> roles= new HashSet<>();
}
