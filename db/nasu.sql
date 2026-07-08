/*
  Script de creación de base de datos para nasu
  Este script crea el esquema, tablas, usuarios, y
  carga datos de ejemplo.
*/
-- Sección de administración (ejecutar una vez en un entorno de desarrollo)
drop database if exists nasu;
drop user if exists usuario_prueba;
drop user if exists usuario_reportes;

-- Creación del esquema
CREATE database nasu
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Creación de usuarios con contraseñas seguras (idealmente asignadas fuera del script)
create user 'usuario_prueba'@'%' identified by 'Usuar1o_Clave.';
create user 'usuario_reportes'@'%' identified by 'Usuar1o_Reportes.';

-- Asignación de permisos
-- Se otorgan permisos específicos en lugar de todos los permisos a todas las tablas futuras
grant select, insert, update, delete on nasu.* to 'usuario_prueba'@'%';
grant select on nasu.* to 'usuario_reportes'@'%';
flush privileges;

use nasu;

-- --- Sección de Creación de Tablas ---

-- Tabla de estado --potencialmente innecesaria, la sustituí por un atributo "activo" - boolean.
-- y en las tablas donde hay estados especificos como "pendiente","en camino"... lo agregue manual

create table estado (
  id_estado INT NOT NULL ,
  nombre_estado VARCHAR(50) NOT NULL,
  descripcion VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_estado),
  unique (descripcion),
  index ndx_nombre_estado (nombre_estado))
  ENGINE = InnoDB;

-- Tabla de categorías
-- (columnas alineadas al estilo del curso: descripcion es el nombre de la categoría)
create table categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(50) NOT NULL,
  ruta_imagen VARCHAR(1024),
  activo boolean,
  PRIMARY KEY (id_categoria),
  unique (descripcion))
  ENGINE = InnoDB;

-- Tabla de productos
-- (descripcion es el nombre del producto; detalle e ingredientes soportan la
--  pantalla de detalle del prototipo: descripción larga e ingredientes - HU-02)
create table producto (
  id_producto INT NOT NULL AUTO_INCREMENT,
  id_categoria INT NOT NULL,
  descripcion VARCHAR(50) NOT NULL,
  detalle TEXT,
  ingredientes TEXT,
  precio decimal(12,2) CHECK (precio >= 0),
  existencias int unsigned CHECK (existencias >= 0),
  ruta_imagen varchar(1024),
  activo boolean,
  PRIMARY KEY (id_producto),
  unique (descripcion),
  foreign key fk_producto_categoria (id_categoria) references categoria(id_categoria))
  ENGINE = InnoDB;

-- Tabla de usuarios
CREATE TABLE usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT,
  username varchar(30) NOT NULL UNIQUE,
  password varchar(512) NOT NULL,
  nombre VARCHAR(20) NOT NULL,
  apellidos VARCHAR(30) NOT NULL,
  correo VARCHAR(75) NULL UNIQUE,
  telefono VARCHAR(25) NULL,
  ruta_imagen varchar(1024),
  activo boolean,
  PRIMARY KEY (`id_usuario`),
  CHECK (correo REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
  index ndx_username (username))
  ENGINE = InnoDB;

-- Tabla de direcciones
CREATE TABLE direccion (
  id_direccion INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  nombre_direccion VARCHAR(50) NOT NULL,
  provincia VARCHAR(50) NOT NULL,
  distrito VARCHAR(50) NOT NULL,
  canton VARCHAR(50) NOT NULL,
  codigo_postal INT NOT NULL,
  dir_exacta VARCHAR(100) NOT NULL,
  detalles_adicionales VARCHAR(100) NULL,
  PRIMARY KEY (id_direccion),
  index ndx_nombre_direccion (nombre_direccion),
  foreign key fk_direccion_usuario (id_usuario) references usuario(id_usuario))
  ENGINE = InnoDB;

-- Tabla de favoritos
-- (ids como INT para que coincidan con las llaves foráneas)
create table favorito (
  id_favorito INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  id_producto INT NOT NULL,
  nombre_fav VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_favorito),
  unique (nombre_fav),
  index ndx_nombre_fav (nombre_fav),
  foreign key fk_favorito_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_favorito_producto (id_producto) references producto(id_producto))
  ENGINE = InnoDB;

-- Tabla de descuentos
create table descuento (
  id_descuento INT NOT NULL AUTO_INCREMENT,
  descripcion VARCHAR(50) NOT NULL,
  codigo VARCHAR(50) NOT NULL,
  porcentaje_descuento INT NOT NULL,
  fecha_inicio DATETIME NULL,
  fecha_fin DATETIME NULL,
  activo boolean,
  PRIMARY KEY (id_descuento),
  unique (descripcion),
  index ndx_descripcion (descripcion))
  ENGINE = InnoDB;

-- Tabla de metodos de pago
create table metpago (
  id_metpago INT NOT NULL AUTO_INCREMENT,
  nombre_metpago VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_metpago),
  unique (nombre_metpago),
  index ndx_nombre_metpago (nombre_metpago))
  ENGINE = InnoDB;


-- Tabla de roles
create table rol (
  id_rol INT NOT NULL AUTO_INCREMENT,
  rol varchar(100) unique,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  primary key (id_rol))
  ENGINE = InnoDB;

-- Tabla de relación entre usuarios y roles
create table usuario_rol (
  id_usuario int not null,
  id_rol INT NOT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario,id_rol),
  foreign key fk_usuarioRol_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_usuarioRol_rol (id_rol) references rol(id_rol))
  ENGINE = InnoDB;

-- Tabla de rutas
CREATE TABLE ruta (
    id_ruta INT AUTO_INCREMENT NOT NULL,
    ruta VARCHAR(255) NOT NULL,
    id_rol INT NULL,
    requiere_rol boolean NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    check (id_rol IS NOT NULL OR requiere_rol = FALSE),
    PRIMARY KEY (id_ruta),
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol))
    ENGINE = InnoDB;

-- Tabla de carritos --revisar
create table carrito (
  id_carrito INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  activo boolean,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_carrito,id_usuario),
  index ndx_id_carrito (id_carrito),
  foreign key fk_carrito_usuario (id_usuario) references usuario(id_usuario))
  ENGINE = InnoDB;

-- Tabla de detalle carrito --revisar
create table detcarrito (
  id_detcarrito INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  id_carrito INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad int unsigned CHECK (cantidad > 0),
  subtotal DECIMAL (12,2) CHECK (subtotal>0),
  activo boolean,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_detcarrito, id_carrito),
  index ndx_id_carrito (id_carrito),
  foreign key fk_detcarrito_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_detcarrito_carrito (id_carrito) references carrito(id_carrito),
  foreign key fk_detcarrito_producto (id_producto) references producto(id_producto))
  ENGINE = InnoDB;

-- Tabla de pedidos -revisar
create table pedido (
  id_pedido INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  id_direccion INT NOT NULL,
  estado ENUM('Entregado', 'En camino', 'Pendiente', 'Anulado') NOT NULL,
  total decimal(12,2) check (total>0),
  id_descuento INT NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_pedido,id_usuario),
  index ndx_id_pedido (id_pedido),
  foreign key fk_pedido_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_pedido_direccion (id_direccion) references direccion(id_direccion),
  foreign key fk_pedido_descuento (id_descuento) references descuento(id_descuento))
  ENGINE = InnoDB;

-- Tabla de detalle pedido -revisar
create table detpedido (
  id_detpedido INT NOT NULL AUTO_INCREMENT,
  id_pedido INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad int unsigned CHECK (cantidad > 0),
  precio_unitario DECIMAL (12,2) CHECK (precio_unitario>0),
  PRIMARY KEY (id_detpedido,id_pedido),
  index ndx_id_pedido (id_pedido),
  foreign key fk_detpedido_pedido (id_pedido) references pedido(id_pedido),
  foreign key fk_detpedido_producto (id_producto) references producto(id_producto))
  ENGINE = InnoDB;

-- Tabla de facturas--pago
create table factura (
  id_factura INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  id_pedido INT NOT NULL,
  id_metodo_pago INT NOT NULL,
  total decimal(12,2) check (total>0),
  estado ENUM('Activa', 'Pagada', 'Anulada') NOT NULL,
  referencia_transaccion VARCHAR(45) NULL,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_factura),
  index ndx_id_usuario (id_usuario),
  foreign key fk_factura_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_factura_pedido (id_pedido) references pedido(id_pedido),
  foreign key fk_factura_metpago (id_metodo_pago) references metpago(id_metpago))
  ENGINE = InnoDB;

-- Tabla de ventas
create table venta (
  id_venta INT NOT NULL AUTO_INCREMENT,
  id_factura INT NOT NULL,
  id_producto INT NOT NULL,
  precio_historico decimal(12,2) check (precio_historico>= 0),
  cantidad int unsigned check (cantidad> 0),
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_venta),
  index ndx_factura (id_factura),
  index ndx_producto (id_producto),
  UNIQUE (id_factura, id_producto),
  foreign key fk_venta_factura (id_factura) references factura(id_factura),
  foreign key fk_venta_producto (id_producto) references producto(id_producto))
  ENGINE = InnoDB;

-- --- Sección de Inserción de Datos ---

-- Inserción de usuarios
INSERT INTO usuario (username,password,nombre, apellidos, correo, telefono,ruta_imagen,activo) VALUES
('juan','$2a$10$P1.w58XvnaYQUQgZUCk4aO/RTRl8EValluCqB3S2VMLTbRt.tlre.','Juan', 'Castro Mora',    'jcastro@gmail.com',    '4556-8978', 'https://img2.rtve.es/i/?w=1600&i=1677587980597.jpg',true),
('rebeca','$2a$10$GkEj.ZzmQa/aEfDmtLIh3udIH5fMphx/35d0EYeqZL5uzgCJ0lQRi','Rebeca',  'Contreras Mora', 'acontreras@gmail.com', '5456-8789','https://media.licdn.com/dms/image/v2/C5603AQGwjJ5ht4bWXQ/profile-displayphoto-shrink_200_200/profile-displayphoto-shrink_200_200/0/1661476259292?e=2147483647&v=beta&t=9_i5zTdqHRMSXlb9H4TuWkWeRGQXmaZLjxkBlWsg2lg',true),
('pedro','$2a$10$koGR7eS22Pv5KdaVJKDcge04ZB53iMiw76.UjHPY.XyVYlYqXnPbO','Pedro', 'Mena Loria',     'lmena@gmail.com',      '7898-8936','https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Eduardo_de_Pedro_2019.jpg/480px-Eduardo_de_Pedro_2019.jpg?20200109230854',true);

-- Inserción de categorias del catálogo NaSü
INSERT INTO categoria (descripcion,activo) VALUES
('Roll-ons',    true),
('Jabón',       true),
('Bálsamo',     true),
('Desodorante', true),
('Scrub',       true),
('Bloqueador',  true);

-- Inserción de productos del catálogo NaSü
INSERT INTO producto (id_categoria,descripcion,detalle,ingredientes,precio,existencias,activo) VALUES
(1,'Focus Oil Roller',
 'Roll-on de aromaterapia elaborado con coco fraccionado como base, lo que permite una absorción suave y profunda para potenciar los beneficios de los aceites esenciales. Su fórmula está energéticamente armonizada con cuarzo citrino en su interior, que actúa como amplificador natural de la intención y el bienestar, ideal para la concentración y la claridad mental.',
 'Coco fraccionado (base), Aceite esencial de pomelo orgánico, Aceite esencial de limón, Cuarzo citrino (en el interior)',
 8500.00, 24, true),
(1,'Serenity Oil Roller',
 'Roll-on de aromaterapia diseñado para momentos de calma y descanso. La lavanda orgánica se combina con amatista en su interior para crear una experiencia relajante que equilibra cuerpo, mente y energía al final del día.',
 'Coco fraccionado (base), Aceite esencial de lavanda orgánica, Amatista (en el interior)',
 8500.00, 18, true),
(1,'Menstrual Relief Oil Roller',
 'Roll-on de aromaterapia formulado para aliviar molestias del ciclo menstrual. El geranio y el cuarzo rosa en su interior brindan una sensación de confort y bienestar durante esos días.',
 'Coco fraccionado (base), Aceite esencial de geranio, Cuarzo rosa (en el interior)',
 8500.00, 0, true),
(2,'Jabón Lavanda',
 'Jabón artesanal saponificado en frío, un proceso que conserva las propiedades de los aceites naturales. Elaborado con aceite de oliva y manteca de karité para limpiar sin resecar la piel, con el aroma relajante de la lavanda.',
 'Aceite de oliva, Manteca de karité, Aceite esencial de lavanda, Flores de lavanda',
 4000.00, 45, true),
(2,'Jabón Cúrcuma y Árbol de Té',
 'Jabón artesanal antibacterial natural. La cúrcuma ayuda a emparejar el tono de la piel mientras el árbol de té purifica y combate imperfecciones. Ideal para pieles grasas o con tendencia al acné.',
 'Aceite de coco, Cúrcuma orgánica, Aceite esencial de árbol de té',
 4000.00, 30, true),
(2,'Jabón Avena y Caléndula',
 'Jabón artesanal para pieles sensibles. La avena calma y suaviza mientras la caléndula regenera y protege. Apto para toda la familia, incluso pieles delicadas.',
 'Avena coloidal, Extracto de caléndula, Aceite de oliva, Manteca de karité',
 4000.00, 26, true),
(3,'Bálsamo Mango',
 'Bálsamo hidratante elaborado con manteca de mango orgánica y vitamina E. Nutre profundamente labios y zonas resecas, dejando una sensación suave y un aroma tropical delicioso.',
 'Manteca de mango orgánica, Cera de abeja, Vitamina E, Aceite de coco',
 3000.00, 12, true),
(3,'Bálsamo Karité y Menta',
 'Bálsamo hidratante con efecto refrescante. La manteca de karité repara la piel mientras la menta brinda una sensación de frescura inmediata. Perfecto para labios y pies cansados.',
 'Manteca de karité, Aceite esencial de menta, Cera de abeja, Vitamina E',
 3000.00, 15, true),
(4,'Desodorante Ylang Naranja',
 'Desodorante natural en barra con propiedades antibacteriales. Libre de aluminio y parabenos, permite que tu piel respire mientras te mantiene fresco con un aroma floral y cítrico.',
 'Bicarbonato de sodio, Aceite de coco, Aceite esencial de ylang ylang, Aceite esencial de naranja',
 6500.00, 8, true),
(4,'Desodorante Lavanda y Árbol de Té',
 'Desodorante natural en barra con efecto antimanchas. La combinación de lavanda y árbol de té protege y cuida la piel de las axilas sin ingredientes agresivos.',
 'Bicarbonato de sodio, Aceite de coco, Aceite esencial de lavanda, Aceite esencial de árbol de té',
 6500.00, 10, true),
(5,'Lavanda Herb Scrub',
 'Exfoliante herbal natural que remueve células muertas y renueva la piel. Las flores de lavanda y el azúcar orgánica dejan la piel suave, hidratada y con un aroma relajante.',
 'Azúcar orgánica, Flores de lavanda, Aceite de almendras, Aceite esencial de lavanda',
 10500.00, 6, true),
(5,'Caléndula Herb Scrub',
 'Exfoliante herbal regenerador. La caléndula estimula la renovación celular mientras exfolia suavemente, ideal para pieles que necesitan un extra de cuidado y luminosidad.',
 'Azúcar orgánica, Pétalos de caléndula, Aceite de almendras, Extracto de caléndula',
 10500.00, 7, true),
(6,'Bloqueador Solar Natural FPS 30',
 'Bloqueador solar mineral elaborado con óxido de zinc no nano. Protege la piel del sol de forma natural, resistente al agua y amigable con los arrecifes de coral.',
 'Óxido de zinc no nano, Manteca de karité, Aceite de coco, Cera de abeja',
 12000.00, 9, true),
(6,'Bloqueador Solar Infantil FPS 50',
 'Bloqueador solar mineral de fórmula suave especialmente pensado para la piel delicada de los más pequeños. Protección alta, natural y sin químicos agresivos.',
 'Óxido de zinc no nano, Manteca de karité, Aceite de almendras, Manzanilla',
 13500.00, 5, true);

-- Inserción de métodos de pago (opciones del prototipo de checkout)
INSERT INTO metpago (nombre_metpago) VALUES
('Tarjeta de crédito / débito'),
('SINPE Móvil'),
('Efectivo al entregar');

-- Inserción de roles
insert into rol (rol) values ('ADMIN'), ('ENCARGADO DE PRODUCCION'), ('USER');

-- ASignación de roles a usuarios
insert into usuario_rol (id_usuario, id_rol) values
 (1,1), (1,2), (1,3),(2,2),(2,3),(3,3);

-- Inserción de rutas con roles específicos
INSERT INTO ruta (ruta, id_rol) VALUES
('/producto/nuevo', 1),
('/producto/guardar', 1),
('/producto/modificar/**', 1),
('/producto/eliminar/**', 1),
('/categoria/nuevo', 1),
('/categoria/guardar', 1),
('/categoria/modificar/**', 1),
('/categoria/eliminar/**', 1),
('/usuario/**', 1),
('/constante/**', 1),
('/role/**', 1),
('/usuario_role/**', 1),
('/ruta/**', 1),
('/categoria/listado', 2),
('/pruebas/**', 2),
('/reportes/**', 2),
('/paypal/**', 3),
('/facturar/carrito', 3);

-- Inserción de rutas que no requieren rol
-- (el catálogo público de las HU-01 a HU-04 debe ser accesible para visitantes)
INSERT INTO ruta (ruta,requiere_rol) VALUES
('/',false),
('/index',false),
('/producto/listado',false),
('/producto/detalle/**',false),
('/producto/categoria/**',false),
('/producto/buscar',false),
('/nosotros',false),
('/contacto',false),
('/errores/**',false),
('/carrito/**',false),
('/registro/**',false),
('/403',false),
('/fav/**',false),
('/js/**',false),
('/css/**',false),
('/img/**',false),
('/webjars/**',false);

-- Inserciones de facturas y ventas: pendientes de definir por el equipo
-- cuando se implementen los pedidos (HU-10) y las ventas/reportes (HU-18),
-- ya que requieren pedidos, direcciones y métodos de pago asociados.
