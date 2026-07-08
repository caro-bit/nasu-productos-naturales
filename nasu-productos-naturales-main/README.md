# nasu-productos-naturales
Sistema web para centralizar la gestión de inventario, producción y ventas del emprendimiento NaSü, dedicado a productos naturales para el cuidado personal.

## Descripción

NaSü produce jabones artesanales, desodorantes naturales, labiales, 
rolones de aromaterapia y bloqueadores solares. Este sistema centraliza 
la gestión de inventario, materias primas, producción y ventas, 
sustituyendo los registros manuales y hojas de cálculo actuales.

## Usuarios del sistema

- **Administrador** — gestión de productos, inventario, ventas y usuarios
- **Encargado de producción** — registro de procesos productivos y lotes
- **Vendedor** — registro de ventas y consulta de disponibilidad

## Historias de usuario

22 historias de usuario definidas y priorizadas mediante la técnica 
MoSCoW en cuatro categorías:

- **Debe tener** — HU-01, HU-05, HU-06, HU-07, HU-10, HU-12, HU-13, HU-15, HU-16, HU-20
- **Debería tener** — HU-02, HU-08, HU-09, HU-11, HU-14, HU-17, HU-18, HU-21
- **Podría tener** — HU-03, HU-04, HU-22
- **No tendrá (v1)** — HU-19

## Criterios de aceptación

Redactados en formato Gherkin con escenarios de éxito y error 
para las historias priorizadas como Debe tener.

## Tecnologías

- Java 21
- Spring Boot (Web, Thymeleaf, Data JPA, Validation, DevTools)
- Hibernate/JPA con MySQL
- Bootstrap 5 y Font Awesome (WebJars)
- Maven

## Estructura del proyecto

```
nasu/                    Proyecto Spring Boot
├── src/main/java/com/nasu/tienda/
│   ├── controller/      Controladores MVC
│   ├── domain/          Entidades JPA
│   ├── repository/      Repositorios Spring Data
│   └── service/         Lógica de negocio
└── src/main/resources/
    ├── templates/       Vistas Thymeleaf
    └── static/          CSS e imágenes
db/nasu.sql              Script de creación de la base de datos
```

## Requisitos de ejecución

1. **JDK 21 o superior** y **MySQL 8** instalados.
2. Ejecutar el script [db/nasu.sql](db/nasu.sql) en MySQL
   (crea la base de datos `nasu`, los usuarios de conexión, todas las tablas del sistema
   y los datos iniciales del catálogo).
3. Desde la carpeta `nasu/`, ejecutar la aplicación:
   - Con NetBeans: abrir el proyecto y presionar **Run**.
   - Por consola: `mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. Abrir el navegador en `http://localhost` (la aplicación escucha en el puerto 80).

## Estado del avance (Avance 2)

Historias de usuario implementadas y funcionales:

- ✅ **HU-01** — Catálogo de productos visible para visitantes
- ✅ **HU-02** — Detalle de producto (descripción, ingredientes y precio)
- ✅ **HU-03** — Búsqueda de productos por nombre
- ✅ **HU-04** — Filtro de productos por categoría

En desarrollo por el resto del equipo: registro e inicio de sesión (HU-05, HU-06),
carrito de compras (HU-07 a HU-09) y confirmación de pedidos (HU-10).

## Equipo
Ballkiria Monge Espinoza  
María Paula Salas Soto  
Axel Segura Abarca  
Carolina Solano Chinchilla 
