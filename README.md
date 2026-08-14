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
- **Chart.js** (WebJar) — librería investigada por el equipo, no vista en clase;
  se usa en el panel de estadísticas (HU-22)
- Maven

## Pruebas

`nasu/src/test/java/com/nasu/tienda/PantallasAdminTests.java` comprueba con
MockMvc que las seis pantallas de administración se dibujan sin errores y que
un visitante o un cliente sin rol `ADMIN` no puede entrar. Requieren la base
`nasu` creada:

```bash
mvnw.cmd test
```

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

### Problemas comunes en macOS

**Error: `Permission denied` al ejecutar `./mvnw`**
Cannot run program "./mvnw" (in directory "/Users/username/NetBeansProjects/nasu-productos-naturales/nasu"): Exec failed, error: 13 (Permission denied)

Esto ocurre porque el script `mvnw` perdió su permiso de ejecución (algo común al clonar el repositorio, extraer un zip o copiar archivos en macOS). Se soluciona desde una terminal:

```bash
cd /Users/username/NetBeansProjects/nasu-productos-naturales/nasu
chmod +x mvnw
```

Luego vuelve a ejecutar el proyecto con normalidad.


## Estado del avance (Avance 2)

Historias de usuario implementadas y funcionales:

- ✅ **HU-01** — Catálogo de productos visible para visitantes
- ✅ **HU-02** — Detalle de producto (descripción, ingredientes y precio)
- ✅ **HU-03** — Búsqueda de productos por nombre
- ✅ **HU-04** — Filtro de productos por categoría
- ✅ **HU-05** — Registro de cliente en el sistema para realizar compras y dar seguimiento a pedidos
- ✅ **HU-06** — Inicio de sesión del cliente para acceder a su información personal y compras
- ✅ **HU-07** — Agregar productos al carrito para comprarlos posteriormente
- ✅ **HU-08** — Modificar las cantidades del carrito para ajustar la compra
- ✅ **HU-09** — Eliminar productos del carrito para actualizar el pedido
- ✅ **HU-10** — Confirmar una compra para realizar un pedido de productos
- ✅ **HU-11** — Consultar el historial de pedidos para dar seguimiento a compras anteriores
- ✅ **HU-12** — Administrador, registra nuevos productos para mantener actualizado el catálogo.
- ✅ **HU-13** — Administrador, edita productos para actualizar precios, imágenes o descripciones.
- ✅ **HU-14** — Administrador, desactiva productos para evitar su venta cuando ya no estén disponibles.
- ✅ **HU-15** — Administrador, registra la cantidad disponible de cada producto para controlar el inventario.
- ✅ **HU-16** — El inventario disminuye automáticamente al confirmar una compra.
- ✅ **HU-17** — Administrador, visualiza los productos con bajo inventario para reabastecerlos oportunamente.
- ✅ **HU-18** — Administrador, consulta las ventas realizadas para analizar el desempeño del negocio.
- ✅ **HU-19** — Administrador, genera reportes de ventas por período para apoyar la toma de decisiones.
- ✅ **HU-20** — Administrador, administra usuarios y sus permisos para controlar el acceso al sistema.
- ✅ **HU-21** — Confirmación de compra para verificar que el pedido fue registrado
- ✅ **HU-22** — Administrador, visualiza un panel con estadísticas de ventas e inventario.

Las 22 historias del documento quedan implementadas.

> HU-19 estaba clasificada como *No tendrá (v1)* en la priorización MoSCoW inicial;
> se adelantó porque comparte las mismas consultas de la HU-18.

## Pantallas de administración

Las historias HU-12 a HU-19 son de uso exclusivo del administrador. Al iniciar
sesión, el sistema carga los roles del usuario y habilita el menú
**Administración** de la barra de navegación:

| Pantalla | Ruta | Historia |
|---|---|---|
| Panel del negocio | `/reporte/panel` | HU-22 |
| Listado de productos | `/producto/listadoAdminTemp` | HU-12 a HU-15 |
| Usuarios y permisos | `/usuario/listado` | HU-20 |
| Inventario bajo | `/reporte/inventario` | HU-17 |
| Ventas realizadas | `/reporte/ventas` | HU-18 |
| Reporte por período | `/reporte/periodo` | HU-19 |

El usuario de prueba con rol `ADMIN` es **juan** (ver `db/nasu.sql`). Un cliente
registrado desde la página de registro obtiene el rol `USER` y no puede entrar a
estas pantallas.

El detalle de la lógica implementada está en
[LOGICA_HU_ADMIN.md](LOGICA_HU_ADMIN.md) y [LOGICA_HU_CLIENTE.md](LOGICA_HU_CLIENTE.md).


## Equipo
Ballkiria Monge Espinoza  
María Paula Salas Soto  
Axel Segura Abarca  
Carolina Solano Chinchilla 

## LINK GRABACION:
https://ufidelitas-my.sharepoint.com/:v:/g/personal/csolano30601_ufide_ac_cr/IQDHvLbagfbqQorzAWFlcpc4AeqgqyP1cZr_Q7TgN9e7mFM?e=dFNykU&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZy1MaW5rIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXcifX0%3D

## LINK REPOSITORIO:
https://github.com/caro-bit/nasu-productos-naturales
