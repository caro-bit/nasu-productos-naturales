# Lógica agregada para historias de usuario de cliente

## HU-01 Registro de cliente
**Historia:** Como cliente, deseo registrarme en el sistema para realizar compras y dar seguimiento a mis pedidos.

**Flujo implementado:**
1. El cliente entra a `/registro`.
2. Completa nombre, apellidos, usuario, correo, teléfono y contraseña.
3. El sistema valida campos vacíos, formato de correo, usuario repetido, correo repetido y confirmación de contraseña.
4. La contraseña se guarda cifrada con BCrypt.
5. El usuario queda activo y se le asigna el rol `USER`.
6. El sistema redirige a `/login` con mensaje de éxito.

**Archivos principales:**
- `domain/Usuario.java`
- `repository/UsuarioRepository.java`
- `service/UsuarioService.java`
- `controller/UsuarioController.java`
- `templates/usuario/registro.html`

## HU-02 Inicio de sesión y acceso a compras
**Historia:** Como cliente, deseo iniciar sesión para acceder a mi información personal y mis compras.

**Flujo implementado:**
1. El cliente entra a `/login`.
2. Digita usuario y contraseña.
3. El sistema busca el usuario activo y valida la contraseña con BCrypt.
4. Si es correcto, guarda `usuario` e `idUsuario` en sesión.
5. El cliente puede entrar a `/perfil` para ver datos personales y pedidos asociados.
6. Puede cerrar sesión desde `/logout`.

**Archivos principales:**
- `service/UsuarioService.java`
- `controller/UsuarioController.java`
- `templates/usuario/login.html`
- `templates/usuario/perfil.html`
- `domain/Pedido.java`
- `repository/PedidoRepository.java`
- `service/PedidoService.java`

## HU-03 Agregar productos al carrito
**Historia:** Como cliente, deseo agregar productos al carrito para comprarlos posteriormente.

**Flujo implementado:**
1. El cliente presiona “Agregar al carrito” desde el catálogo, destacados o detalle del producto.
2. Si no inició sesión, el sistema lo redirige a `/login`.
3. Si inició sesión, el sistema busca o crea un carrito activo.
4. Si el producto ya existe en el carrito, aumenta la cantidad.
5. Valida que exista stock suficiente.
6. Calcula subtotal por producto y total del carrito.
7. El cliente puede ver, actualizar cantidad, eliminar productos o vaciar el carrito en `/carrito/listado`.

**Archivos principales:**
- `domain/Carrito.java`
- `domain/DetCarrito.java`
- `repository/CarritoRepository.java`
- `repository/DetCarritoRepository.java`
- `service/CarritoService.java`
- `controller/CarritoController.java`
- `templates/carrito/listado.html`
- `templates/producto/fragmentos.html`
- `templates/producto/detalle.html`

## Cambios visuales
Se respetó la paleta existente del documento/proyecto:
- verde NaSü: `--nasu-verde`
- verde oscuro: `--nasu-verde-oscuro`
- crema: `--nasu-crema`
- arena: `--nasu-arena`
- terracota: `--nasu-terracota`

Se agregó `.btn-outline-nasu` y estilos de formulario usando los mismos colores.

## Cambios en base de datos
El script `db/nasu.sql` ya tenía las tablas necesarias. Solo se ajustaron rutas para incluir:
- `/perfil` con rol `USER`
- `/carrito/**` con rol `USER`
- `/login` y `/logout` como rutas públicas
