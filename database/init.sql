-- 1. TIPO DE CAMPAÑA
CREATE TABLE Tipo_Campana (
    ID_Tipo   SERIAL PRIMARY KEY,
    Nombre    VARCHAR(50) NOT NULL UNIQUE
);

-- 2. CAMPAÑA
CREATE TABLE Campana (
    ID_Campana   SERIAL PRIMARY KEY,
    Nombre       VARCHAR(255) NOT NULL,
    ID_Tipo      INT REFERENCES Tipo_Campana(ID_Tipo) ON DELETE SET NULL,
    Fecha_inicio DATE NOT NULL,
    Fecha_fin    DATE NOT NULL,
    CHECK (Fecha_fin >= Fecha_inicio)
);

-- 3. GEOGRAFÍA
CREATE TABLE Zona_Geografica (
    ID_Zona  SERIAL PRIMARY KEY,
    Nombre   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Localidad (
    ID_Localidad SERIAL PRIMARY KEY,
    Nombre       VARCHAR(100) NOT NULL,
    ID_Zona      INT REFERENCES Zona_Geografica(ID_Zona) ON DELETE SET NULL
);

CREATE TABLE Distrito (
    ID_Distrito  SERIAL PRIMARY KEY,
    Nombre       VARCHAR(100) NOT NULL,
    ID_Localidad INT REFERENCES Localidad(ID_Localidad) ON DELETE CASCADE
);

CREATE TABLE Codigo_Postal (
    CP           VARCHAR(10) PRIMARY KEY,
    ID_Localidad INT NOT NULL REFERENCES Localidad(ID_Localidad) ON DELETE CASCADE,
    ID_Distrito  INT REFERENCES Distrito(ID_Distrito) ON DELETE SET NULL
);

-- 4. CADENA
CREATE TABLE Cadena (
    ID_Cadena SERIAL PRIMARY KEY,
    Nombre    VARCHAR(255) NOT NULL,
    Codigo    VARCHAR(50)  UNIQUE NOT NULL
);

-- 5. USUARIO
CREATE TABLE Usuario (
    ID_Usuario  SERIAL PRIMARY KEY,
    Nombre      VARCHAR(255) NOT NULL,
    Email       VARCHAR(255) UNIQUE NOT NULL,
    Telefono    VARCHAR(20),
    Contrasena  VARCHAR(255) NOT NULL,
    Domicilio   TEXT,
    CP          VARCHAR(10) REFERENCES Codigo_Postal(CP) ON DELETE SET NULL,
    Token_Recuperacion VARCHAR(255),
    Token_Recuperacion_Expira_En TIMESTAMP
);

-- 6. ENTIDAD COLABORADORA
CREATE TABLE Entidad_Colaboradora (
    ID_Entidad_Colaboradora SERIAL PRIMARY KEY,
    Nombre                  VARCHAR(255) NOT NULL,
    Domicilio               TEXT,
    Telefono                VARCHAR(20)
);

-- 7. VOLUNTARIO (independiente de Usuario)
CREATE TABLE Voluntario (
    ID_Voluntario           SERIAL PRIMARY KEY,
    Nombre                  VARCHAR(255) NOT NULL,
    Telefono                VARCHAR(20),
    Email                   VARCHAR(255),
    Domicilio               TEXT,
    ID_Entidad_Colaboradora INT REFERENCES Entidad_Colaboradora(ID_Entidad_Colaboradora) ON DELETE SET NULL
);

-- 8. TIENDA
CREATE TABLE Tienda (
    ID_Tienda       SERIAL PRIMARY KEY,
    Nombre          VARCHAR(255) NOT NULL,
    Domicilio       TEXT,
    CP              VARCHAR(10) REFERENCES Codigo_Postal(CP) ON DELETE SET NULL,
    ID_Cadena       INT REFERENCES Cadena(ID_Cadena) ON DELETE SET NULL,
    ID_Responsable  INT REFERENCES Usuario(ID_Usuario) ON DELETE SET NULL
);

-- 9. TIENDAS EN CAMPAÑA
CREATE TABLE Tiendas_en_campana (
    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE,
    ID_Tienda  INT REFERENCES Tienda(ID_Tienda)   ON DELETE CASCADE,
    PRIMARY KEY (ID_Campana, ID_Tienda)
);

-- 10. SUBCLASES DE USUARIO
CREATE TABLE Administradores (
    ID_Usuario INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE
);

CREATE TABLE Responsable_entidad_colaboradora (
    ID_Usuario              INT PRIMARY KEY REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    ID_Entidad_colaboradora INT REFERENCES Entidad_Colaboradora(ID_Entidad_Colaboradora) ON DELETE CASCADE
);

CREATE TABLE Coordinadores (
    ID_Usuario INT REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE,
    PRIMARY KEY (ID_Usuario, ID_Campana)
);

CREATE TABLE Capitanes (
    ID_Usuario INT REFERENCES Usuario(ID_Usuario) ON DELETE CASCADE,
    ID_Campana INT REFERENCES Campana(ID_Campana) ON DELETE CASCADE,
    PRIMARY KEY (ID_Usuario, ID_Campana)
);

-- 11. VOLUNTARIO_TURNO
CREATE TABLE Voluntario_Turno (
    ID_Voluntario INT,
    ID_Campana    INT,
    ID_Tienda     INT,
    Dia_Turno     DATE NOT NULL,
    Hora_inicio   TIME NOT NULL,
    Hora_fin      TIME NOT NULL,
    Asistencia    BOOLEAN DEFAULT FALSE,
    Observaciones TEXT,

    PRIMARY KEY (ID_Voluntario, ID_Campana, ID_Tienda, Dia_Turno, Hora_inicio),

    -- FKs
    FOREIGN KEY (ID_Voluntario)
        REFERENCES Voluntario(ID_Voluntario)
        ON DELETE CASCADE,

    FOREIGN KEY (ID_Campana, ID_Tienda)
        REFERENCES Tiendas_en_campana(ID_Campana, ID_Tienda)
        ON DELETE CASCADE,

    -- Restricción lógica
    CHECK (Hora_fin > Hora_inicio)
);