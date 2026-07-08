-- ============================================================
-- NaSü Productos Naturales
-- Script de creación de la base de datos, usuario y tablas
-- con los datos iniciales del catálogo (Avance 2)
-- ============================================================

DROP DATABASE IF EXISTS nasu;
CREATE DATABASE nasu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario utilizado por la aplicación (application.properties)
CREATE USER IF NOT EXISTS 'usuario_prueba'@'localhost' IDENTIFIED BY 'Usuar1o_Clave.';
GRANT ALL PRIVILEGES ON nasu.* TO 'usuario_prueba'@'localhost';
FLUSH PRIVILEGES;

USE nasu;

-- ============================================================
-- Tabla categoria
-- ============================================================
CREATE TABLE categoria (
    id_categoria INT NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(50) NOT NULL UNIQUE,
    ruta_imagen VARCHAR(1024),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_categoria)
);

-- ============================================================
-- Tabla producto
-- ============================================================
CREATE TABLE producto (
    id_producto INT NOT NULL AUTO_INCREMENT,
    id_categoria INT NOT NULL,
    descripcion VARCHAR(50) NOT NULL,
    detalle TEXT,
    ingredientes TEXT,
    precio DECIMAL(12,2),
    existencias INT,
    ruta_imagen VARCHAR(1024),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_producto),
    FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
);

-- ============================================================
-- Datos iniciales: categorías
-- ============================================================
INSERT INTO categoria (descripcion, activo) VALUES
('Roll-ons', TRUE),
('Jabón', TRUE),
('Bálsamo', TRUE),
('Desodorante', TRUE),
('Scrub', TRUE),
('Bloqueador', TRUE);

-- ============================================================
-- Datos iniciales: productos del catálogo NaSü
-- ============================================================
INSERT INTO producto (id_categoria, descripcion, detalle, ingredientes, precio, existencias, activo) VALUES
(1, 'Focus Oil Roller',
 'Roll-on de aromaterapia elaborado con coco fraccionado como base, lo que permite una absorción suave y profunda para potenciar los beneficios de los aceites esenciales. Su fórmula está energéticamente armonizada con cuarzo citrino en su interior, que actúa como amplificador natural de la intención y el bienestar, ideal para la concentración y la claridad mental.',
 'Coco fraccionado (base), Aceite esencial de pomelo orgánico, Aceite esencial de limón, Cuarzo citrino (en el interior)',
 8500.00, 24, TRUE),
(1, 'Serenity Oil Roller',
 'Roll-on de aromaterapia diseñado para momentos de calma y descanso. La lavanda orgánica se combina con amatista en su interior para crear una experiencia relajante que equilibra cuerpo, mente y energía al final del día.',
 'Coco fraccionado (base), Aceite esencial de lavanda orgánica, Amatista (en el interior)',
 8500.00, 18, TRUE),
(1, 'Menstrual Relief Oil Roller',
 'Roll-on de aromaterapia formulado para aliviar molestias del ciclo menstrual. El geranio y el cuarzo rosa en su interior brindan una sensación de confort y bienestar durante esos días.',
 'Coco fraccionado (base), Aceite esencial de geranio, Cuarzo rosa (en el interior)',
 8500.00, 0, TRUE),
(2, 'Jabón Lavanda',
 'Jabón artesanal saponificado en frío, un proceso que conserva las propiedades de los aceites naturales. Elaborado con aceite de oliva y manteca de karité para limpiar sin resecar la piel, con el aroma relajante de la lavanda.',
 'Aceite de oliva, Manteca de karité, Aceite esencial de lavanda, Flores de lavanda',
 4000.00, 45, TRUE),
(2, 'Jabón Cúrcuma y Árbol de Té',
 'Jabón artesanal antibacterial natural. La cúrcuma ayuda a emparejar el tono de la piel mientras el árbol de té purifica y combate imperfecciones. Ideal para pieles grasas o con tendencia al acné.',
 'Aceite de coco, Cúrcuma orgánica, Aceite esencial de árbol de té',
 4000.00, 30, TRUE),
(2, 'Jabón Avena y Caléndula',
 'Jabón artesanal para pieles sensibles. La avena calma y suaviza mientras la caléndula regenera y protege. Apto para toda la familia, incluso pieles delicadas.',
 'Avena coloidal, Extracto de caléndula, Aceite de oliva, Manteca de karité',
 4000.00, 26, TRUE),
(3, 'Bálsamo Mango',
 'Bálsamo hidratante elaborado con manteca de mango orgánica y vitamina E. Nutre profundamente labios y zonas resecas, dejando una sensación suave y un aroma tropical delicioso.',
 'Manteca de mango orgánica, Cera de abeja, Vitamina E, Aceite de coco',
 3000.00, 12, TRUE),
(3, 'Bálsamo Karité y Menta',
 'Bálsamo hidratante con efecto refrescante. La manteca de karité repara la piel mientras la menta brinda una sensación de frescura inmediata. Perfecto para labios y pies cansados.',
 'Manteca de karité, Aceite esencial de menta, Cera de abeja, Vitamina E',
 3000.00, 15, TRUE),
(4, 'Desodorante Ylang Naranja',
 'Desodorante natural en barra con propiedades antibacteriales. Libre de aluminio y parabenos, permite que tu piel respire mientras te mantiene fresco con un aroma floral y cítrico.',
 'Bicarbonato de sodio, Aceite de coco, Aceite esencial de ylang ylang, Aceite esencial de naranja',
 6500.00, 8, TRUE),
(4, 'Desodorante Lavanda y Árbol de Té',
 'Desodorante natural en barra con efecto antimanchas. La combinación de lavanda y árbol de té protege y cuida la piel de las axilas sin ingredientes agresivos.',
 'Bicarbonato de sodio, Aceite de coco, Aceite esencial de lavanda, Aceite esencial de árbol de té',
 6500.00, 10, TRUE),
(5, 'Lavanda Herb Scrub',
 'Exfoliante herbal natural que remueve células muertas y renueva la piel. Las flores de lavanda y el azúcar orgánica dejan la piel suave, hidratada y con un aroma relajante.',
 'Azúcar orgánica, Flores de lavanda, Aceite de almendras, Aceite esencial de lavanda',
 10500.00, 6, TRUE),
(5, 'Caléndula Herb Scrub',
 'Exfoliante herbal regenerador. La caléndula estimula la renovación celular mientras exfolia suavemente, ideal para pieles que necesitan un extra de cuidado y luminosidad.',
 'Azúcar orgánica, Pétalos de caléndula, Aceite de almendras, Extracto de caléndula',
 10500.00, 7, TRUE),
(6, 'Bloqueador Solar Natural FPS 30',
 'Bloqueador solar mineral elaborado con óxido de zinc no nano. Protege la piel del sol de forma natural, resistente al agua y amigable con los arrecifes de coral.',
 'Óxido de zinc no nano, Manteca de karité, Aceite de coco, Cera de abeja',
 12000.00, 9, TRUE),
(6, 'Bloqueador Solar Infantil FPS 50',
 'Bloqueador solar mineral de fórmula suave especialmente pensado para la piel delicada de los más pequeños. Protección alta, natural y sin químicos agresivos.',
 'Óxido de zinc no nano, Manteca de karité, Aceite de almendras, Manzanilla',
 13500.00, 5, TRUE);
