-- Migración: añadir campo Participacion a la tabla Cadena

ALTER TABLE Cadena
    ADD COLUMN Participacion BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN Cadena.Participacion IS 'Indica si la cadena participa activamente en campañas';

-- docker exec -i <nombre_contenedor_postgres> psql -U <usuario> -d <bbdd> < database/V2__add_participacion_cadena.sql