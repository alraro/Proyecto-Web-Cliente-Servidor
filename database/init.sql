-- 1. Tablas independientes (Sin claves foráneas)
CREATE TABLE Campana (    ID_Campana SERIAL PRIMARY KEY,    Nombre VARCHAR(255) NOT NULL,    Tipo VARCHAR(50) CHECK (Tipo IN ('Primavera', 'GR')),    Fecha_inicio DATE NOT NULL,    Fecha_fin DATE NOT NULL);
CREATE TABLE Cadena (    ID_Cadena SERIAL PRIMARY KEY,    Nombre VARCHAR(255) NOT NULL,    Codigo VARCHAR(50) UNIQUE NOT NULL,    Participa BOOLEAN DEFAULT TRUE);
CREATE TABLE Usuario (    ID_Usuario SERIAL PRIMARY KEY,    Nombre VARCHAR(255) NOT NULL,    Email VARCHAR(255) UNIQUE NOT NULL,    Telefono VARCHAR(20),    Contrasena VARCHAR(255) NOT NULL,    Domicilio TEXT,    Localidad VARCHAR(100),    CP VARCHAR(10));
CREATE TABLE Entidad_Colaboradora (    ID_Entidad_Colaboradora SERIAL PRIMARY KEY,    Nombre VARCHAR(255) NOT NULL,    Domicilio TEXT,    Telefono VARCHAR(20));
-- 2. Tablas con dependencias de primer nivel
CREATE TABLE Tienda (    ID_Tienda SERIAL PRIMARY KEY,    Nombre VARCHAR(255) NOT NULL,    Domicilio TEXT,    Localidad VARCHAR(100),    CP VARCHAR(10),    Zona_Geografica VARCHAR(100),    ID_Cadena INT REFERENCES Cadena(ID_Cadena) ON DELETE SET NULL);
CREATE TABLE Tiendas_en_campana (    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE,    ID_Tienda INT REFERENCES Tienda(ID_Tienda) ON DELETE CASCADE,    PRIMARY KEY (ID_Campana, ID_Tienda));
-- 3. Subclases de Usuario (Class Table Inheritance)
CREATE TABLE Administradores (    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE);
CREATE TABLE Responsable_entidad_colaboradora (    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,    ID_Entidad_colaboradora INT REFERENCES Entidad_Colaboradora(ID_Entidad_Colaboradora) ON DELETE CASCADE);
CREATE TABLE Voluntario (    ID_Voluntario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,    ID_Entidad_Colaboradora INT REFERENCES Entidad_Colaboradora(ID_Entidad_Colaboradora) ON DELETE SET NULL,    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE SET NULL);
CREATE TABLE Responsable_tienda (    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,    ID_Tienda INT REFERENCES Tienda(ID_Tienda) ON DELETE CASCADE);
CREATE TABLE Coordinadores (    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE);
CREATE TABLE Capitanes (    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE);
-- 4. Tablas con múltiples dependencias (Transaccionales)
CREATE TABLE Turnos_Recogida (    ID_Turno SERIAL PRIMARY KEY,    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE,    ID_Tienda INT REFERENCES Tienda(ID_Tienda) ON DELETE CASCADE,    ID_Voluntario INT REFERENCES Voluntario(ID_Voluntario) ON DELETE CASCADE,    Dia_Turno DATE NOT NULL,    Hora_inicio TIME NOT NULL,    Hora_fin TIME NOT NULL,    Num_Voluntarios INT DEFAULT 1,    Ubicacion_Especifica TEXT,    Observaciones_Turno TEXT);

-- INSERCIÓN DE DATOS DE PRUEBA --

-- 1. Insertar en tablas independientes (Sin dependencias)
INSERT INTO Campana (Nombre, Tipo, Fecha_inicio, Fecha_fin) VALUES 
('Recogida Primavera 2026', 'Primavera', '2026-05-01', '2026-05-31'),
('Gran Recogida Invierno', 'GR', '2026-11-20', '2026-11-22');

INSERT INTO Cadena (Nombre, Codigo, Participa) VALUES 
('Mercadona', 'MERC-01', TRUE),
('Carrefour', 'CARR-01', TRUE);

INSERT INTO Entidad_Colaboradora (Nombre, Domicilio, Telefono) VALUES 
('Banco de Alimentos Central', 'Calle Solidaridad 10', '910000000');

-- 2. Insertar Usuarios base (Se generan IDs 1, 2, 3 y 4 secuencialmente)
INSERT INTO Usuario (Nombre, Email, Telefono, Contrasena, Domicilio, Localidad, CP) VALUES 
('Admin Principal', 'admin@uni.es', '600100100', 'hash_pass_1', 'Centro', 'Madrid', '28001'),
('Juan Voluntario', 'juan@uni.es', '600200200', 'hash_pass_2', 'Sur', 'Madrid', '28012'),
('Marta Tienda', 'marta@uni.es', '600300300', 'hash_pass_3', 'Norte', 'Madrid', '28034'),
('Luis Entidad', 'luis@uni.es', '600400400', 'hash_pass_4', 'Este', 'Madrid', '28022');

-- 3. Insertar en tablas con dependencias de primer nivel
INSERT INTO Tienda (Nombre, Domicilio, Localidad, CP, Zona_Geografica, ID_Cadena) VALUES 
('Mercadona Princesa', 'Calle Princesa 40', 'Madrid', '28008', 'Centro', 1),
('Carrefour Alcobendas', 'Avenida Olímpica 9', 'Alcobendas', '28108', 'Norte', 2);

INSERT INTO Tiendas_en_campana (ID_Campana, ID_Tienda) VALUES 
(1, 1),
(1, 2),
(2, 1);

-- 4. Insertar en subclases de Usuario (Asignación de roles mediante el ID del Usuario)
INSERT INTO Administradores (ID_Usuario) VALUES (1);

-- Nota: Usando ID_Voluntario como definiste, que referencia al ID_Usuario 2
INSERT INTO Voluntario (ID_Voluntario, ID_Entidad_Colaboradora, ID_Campana) VALUES 
(2, 1, 1);

INSERT INTO Responsable_tienda (ID_Usuario, ID_Tienda) VALUES 
(3, 1);

INSERT INTO Responsable_entidad_colaboradora (ID_Usuario, ID_Entidad_colaboradora) VALUES 
(4, 1);

-- 5. Insertar en tabla transaccional final (Múltiples dependencias cruzadas)
INSERT INTO Turnos_Recogida (ID_Campana, ID_Tienda, ID_Voluntario, Dia_Turno, Hora_inicio, Hora_fin, Num_Voluntarios, Ubicacion_Especifica, Observaciones_Turno) VALUES 
(1, 1, 2, '2026-05-02', '09:00:00', '14:00:00', 3, 'Puerta Principal - Izquierda', 'Llevar chalecos azules');