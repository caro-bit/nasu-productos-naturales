# Lógica agregada para historias de usuario de administrador

## Control de acceso

Las tres historias son de uso exclusivo del administrador, por lo que primero se
agregó la información de roles a la sesión:

1. Al iniciar sesión, `UsuarioService.getRoles()` consulta las tablas `rol` y
   `usuario_rol` y el controlador guarda en sesión `roles` y `esAdmin`.
2. `SesionUtil` centraliza la lectura de esos datos y `ControlAcceso.validarAdmin()`
   define la regla única: sin sesión redirige a `/login`, con sesión pero sin rol
   `ADMIN` redirige a la portada con un mensaje de acceso denegado.
3. El menú **Administración** de la barra de navegación solo se dibuja cuando
   `session.esAdmin` es verdadero.

La misma validación se aplicó a las pantallas de mantenimiento del catálogo
(HU-12 a HU-15), que antes quedaban accesibles para cualquier visitante.

## HU-17 Productos con bajo inventario
**Historia:** Como administrador, deseo visualizar los productos con bajo inventario para reabastecerlos oportunamente.

**Flujo implementado:**
1. El administrador entra a `/reporte/inventario`.
2. El sistema aplica un umbral de existencias (10 unidades por defecto) que se
   puede cambiar desde el formulario de la pantalla.
3. Una consulta derivada devuelve los productos **activos** cuyas existencias son
   menores o iguales al umbral, ordenados de menor a mayor.
4. Cada producto se marca como *Agotado* (0 unidades), *Crítico* (hasta la mitad
   del umbral) o *Bajo*, y se muestra cuántas unidades faltan para alcanzarlo.
5. El botón **Reabastecer** lleva a la pantalla de edición del producto (HU-15),
   donde se actualizan las existencias.

**Archivos principales:**
- `repository/ProductoRepository.java` (consulta derivada `findByActivoTrueAndExistenciasLessThanEqualOrderByExistenciasAsc`)
- `service/ReporteService.java`
- `controller/ReporteController.java`
- `templates/reporte/inventario.html`

## HU-18 Consulta de ventas realizadas
**Historia:** Como administrador, deseo consultar las ventas realizadas para analizar el desempeño del negocio.

**Flujo implementado:**
1. El administrador entra a `/reporte/ventas`.
2. Sin filtro, la pantalla muestra todo el histórico; con el filtro de fechas se
   limita el rango consultado. Si la fecha inicial es posterior a la final, el
   sistema muestra un mensaje de error y vuelve a la consulta sin filtro.
3. Una consulta SQL nativa une `venta`, `factura`, `usuario`, `producto` y
   `categoria` y devuelve cada línea de venta con su factura, pedido, cliente,
   cantidad, precio histórico y subtotal.
4. Las facturas anuladas se excluyen porque no representan ventas efectivas.
5. Encima del detalle se muestran los indicadores del rango: facturas emitidas,
   unidades vendidas y monto total.

**Archivos principales:**
- `dto/VentaDetalle.java`, `dto/ResumenVentas.java`
- `repository/VentaRepository.java` (`findDetalleVentas`, `findResumenVentas`)
- `service/ReporteService.java`
- `controller/ReporteController.java`
- `templates/reporte/ventas.html`

## HU-19 Reporte de ventas por período
**Historia:** Como administrador, deseo generar reportes de ventas por período para apoyar la toma de decisiones.

**Flujo implementado:**
1. El administrador entra a `/reporte/periodo`; por defecto el reporte abarca el
   mes en curso (del día 1 a hoy).
2. El rango se cambia con el filtro de fechas y se valida igual que en la HU-18.
3. El reporte muestra cuatro indicadores del período: facturas, unidades
   vendidas, monto total y promedio por factura.
4. Dos consultas agrupadas complementan los indicadores:
   - **Ventas por producto:** unidades y monto de cada producto, de mayor a menor.
   - **Ventas por día:** facturas, unidades y monto de cada día, con una barra
     comparativa contra el mejor día del período.
5. El día final se incluye completo (hasta las 23:59:59) para no dejar por fuera
   las ventas de esa fecha.

**Archivos principales:**
- `dto/VentaPorProducto.java`, `dto/VentaPorDia.java`, `dto/ResumenVentas.java`
- `repository/VentaRepository.java` (`findVentasPorProducto`, `findVentasPorDia`, `findResumenVentas`)
- `service/ReporteService.java`
- `controller/ReporteController.java`
- `templates/reporte/periodo.html`

## HU-14 Desactivar productos
**Historia:** Como administrador, deseo desactivar productos para evitar su venta cuando ya no estén disponibles.

**Flujo implementado:**
1. En el listado de administración cada producto tiene un botón que alterna
   entre activo e inactivo.
2. `ProductoService.cambiarEstado()` invierte el valor de `activo` y guarda.
3. Un producto inactivo desaparece del catálogo, del buscador, del filtro por
   categoría y del reporte de inventario bajo, pero conserva su historial de
   ventas, cosa que no pasaría si se borrara.
4. El borrado definitivo sigue disponible aparte, para productos cargados por
   error que todavía no tienen movimientos.

**Archivos principales:**
- `service/ProductoService.java` (`cambiarEstado`)
- `controller/ProductoController.java` (`/producto/estado`)
- `templates/producto/listadoAdminTemp.html`

## HU-20 Administrar usuarios y permisos
**Historia:** Como administrador, deseo administrar usuarios y sus permisos para controlar el acceso al sistema.

**Flujo implementado:**
1. El administrador entra a `/usuario/listado` y ve todas las cuentas con su
   estado y los roles que tienen asignados.
2. Los roles se traen en una sola consulta con `GROUP_CONCAT`, para no consultar
   la tabla `usuario_rol` una vez por usuario.
3. Desde la misma fila se puede asignar o quitar cualquiera de los roles de la
   tabla `rol`, y activar o desactivar la cuenta.
4. Tres reglas protegen al sistema de quedar sin administración:
   - nadie puede desactivar su propia cuenta;
   - nadie puede quitarse a sí mismo el rol `ADMIN`;
   - siempre debe quedar al menos un administrador activo.
5. El estado se cambia con un `UPDATE` y no con `save()`, porque la entidad
   `Usuario` valida `confirmarPassword`, un campo que solo existe en el registro.

**Archivos principales:**
- `domain/Rol.java`, `repository/RolRepository.java`, `dto/UsuarioRoles.java`
- `repository/UsuarioRepository.java`, `service/UsuarioService.java`
- `controller/UsuarioAdminController.java`
- `templates/usuario/listadoAdmin.html`

## HU-22 Panel de estadísticas
**Historia:** Como administrador, deseo visualizar un panel con estadísticas de ventas e inventario para monitorear el negocio.

**Flujo implementado:**
1. El administrador entra a `/reporte/panel`, que es la primera pantalla del
   menú de administración.
2. Cuatro indicadores resumen el estado del negocio: ventas del mes, facturas
   del mes, valor del inventario y productos en alerta.
3. El valor del inventario se calcula con una **consulta JPQL** de agregación
   (`SUM(p.precio * p.existencias)`), distinta de las consultas nativas de los
   otros reportes.
4. Dos gráficos hechos con **Chart.js**: la evolución de las ventas de los
   últimos tres meses y los cinco productos que más ingresos han generado.
5. Abajo, un atajo con los cinco productos más urgentes por reponer, que enlaza
   al reporte completo de la HU-17.

**Archivos principales:**
- `controller/ReporteController.java` (`/reporte/panel`)
- `service/ReporteService.java` (`getTopProductos`), `service/ProductoService.java` (`getValorInventario`)
- `repository/ProductoRepository.java` (`calcularValorInventario`, consulta JPQL)
- `templates/reporte/panel.html`

## Tecnología investigada por el equipo
**Chart.js 4.4.3**, servida como WebJar desde el propio proyecto. No se vio en
clase y no depende de ningún CDN externo: el archivo viaja dentro del `.jar` de
la aplicación. Se usa únicamente en el panel de la HU-22, que recibe las series
ya calculadas desde el controlador mediante `th:inline="javascript"`.

## Pruebas automatizadas
`PantallasAdminTests` levanta el contexto de Spring y comprueba con MockMvc que
las seis pantallas de administración se dibujan sin errores, que el panel
incluye los gráficos y que un visitante o un cliente sin rol `ADMIN` es
redirigido. La sesión de la prueba se arma directamente, sin pasar por el
formulario de login.

## Cambios visuales
Se reutilizó la paleta existente (`--nasu-verde`, `--nasu-crema`, `--nasu-arena`)
y las clases `btn-nasu`, `bg-nasu` y `text-nasu`. Se agregó
`templates/reporte/fragmentos.html` con cuatro fragmentos reutilizables: el menú
de administración, la tarjeta de indicador, el filtro de fechas y el aviso de
"sin datos".

## Cambios en base de datos
No se crearon tablas nuevas; las historias se resuelven con las tablas `venta`,
`factura`, `producto` y `categoria` que ya existían. En `db/nasu.sql` se agregó:

- Rutas de administración (`/reporte/**`, `/producto/listadoAdminTemp`,
  `/producto/editar/**`, `/producto/eliminar`) con el rol `ADMIN`.
- Datos de ejemplo de direcciones, pedidos, detalle de pedidos, facturas y
  ventas repartidos en tres meses, con una factura anulada para comprobar que
  los reportes la excluyen.
- Un `UPDATE` que descuenta del inventario las unidades vendidas, igual que lo
  hace la aplicación al confirmar un pedido (HU-16).
- Corrección en el borrado de usuarios de conexión: ahora se eliminan las
  variantes `@'%'` y `@'localhost'`. Si quedaba un `usuario_prueba@localhost`
  de una instalación anterior, MySQL lo prefería al conectarse desde la misma
  máquina y la aplicación fallaba con `SELECT command denied`.
