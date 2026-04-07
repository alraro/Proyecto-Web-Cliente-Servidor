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