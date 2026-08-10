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
