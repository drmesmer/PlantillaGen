CREATE TABLE operarios (
    id          SERIAL PRIMARY KEY,
    codigo      VARCHAR(20)  NOT NULL UNIQUE,
    nombre      VARCHAR(200) NOT NULL,
    activo      BOOLEAN      DEFAULT TRUE,
    efi         INTEGER      DEFAULT 50 CHECK (efi BETWEEN 1 AND 99),
    cal         INTEGER      DEFAULT 50 CHECK (cal BETWEEN 1 AND 99),
    seg         INTEGER      DEFAULT 50 CHECK (seg BETWEEN 1 AND 99),
    ini         INTEGER      DEFAULT 50 CHECK (ini BETWEEN 1 AND 99),
    pol         INTEGER      DEFAULT 50 CHECK (pol BETWEEN 1 AND 99),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lineas_produccion (
    id          SERIAL PRIMARY KEY,
    numero      INTEGER      NOT NULL UNIQUE CHECK (numero BETWEEN 1 AND 20),
    nombre      VARCHAR(100) DEFAULT '',
    activo      BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plantilla (
    id               SERIAL PRIMARY KEY,
    linea_id         INTEGER  NOT NULL REFERENCES lineas_produccion(id) ON DELETE CASCADE,
    operario_id      INTEGER  NOT NULL REFERENCES operarios(id) ON DELETE CASCADE,
    es_lider         BOOLEAN  DEFAULT FALSE,
    tiene_formacion  BOOLEAN  DEFAULT FALSE,
    orden            INTEGER  DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (linea_id, operario_id)
);

CREATE TABLE turnos (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,
    hora_inicio TIME,
    hora_fin    TIME
);

CREATE TABLE calendario (
    id           SERIAL PRIMARY KEY,
    plantilla_id INTEGER  NOT NULL REFERENCES plantillas(id) ON DELETE CASCADE,
    fecha        DATE     NOT NULL,
    activo       BOOLEAN  DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (plantilla_id, fecha)
);

CREATE INDEX idx_plantilla_linea ON plantilla(linea_id);
CREATE INDEX idx_plantilla_operario ON plantilla(operario_id);
CREATE INDEX idx_calendario_fecha ON calendario(fecha);
CREATE INDEX idx_calendario_plantilla ON calendario(plantilla_id);

INSERT INTO lineas_produccion (numero, nombre) VALUES
    (1, 'Línea 1'),  (2, 'Línea 2'),  (3, 'Línea 3'),
    (4, 'Línea 4'),  (5, 'Línea 5'),  (6, 'Línea 6'),
    (7, 'Línea 7'),  (8, 'Línea 8'),  (9, 'Línea 9'),
    (10, 'Línea 10');

INSERT INTO turnos (nombre, hora_inicio, hora_fin) VALUES
    ('Mañana',    '06:00', '14:00'),
    ('Tarde',     '14:00', '22:00'),
    ('Noche',     '22:00', '06:00');

CREATE TABLE plantillas (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(200) NOT NULL,
    estado      VARCHAR(50)  DEFAULT 'BORRADOR',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE plantilla_detalle (
    id              SERIAL PRIMARY KEY,
    plantilla_id    INTEGER  NOT NULL REFERENCES plantillas(id) ON DELETE CASCADE,
    linea_id        INTEGER  NOT NULL REFERENCES lineas_produccion(id) ON DELETE CASCADE,
    operario_id     INTEGER  NOT NULL REFERENCES operarios(id) ON DELETE CASCADE,
    es_lider        BOOLEAN  DEFAULT FALSE,
    tiene_formacion BOOLEAN  DEFAULT FALSE,
    orden           INTEGER  DEFAULT 0,
    turno_id        INTEGER  DEFAULT 1 REFERENCES turnos(id),
    UNIQUE (plantilla_id, operario_id)
);

CREATE TABLE plantilla_detalle_tmp (
    id              SERIAL PRIMARY KEY,
    plantilla_id    INTEGER  NOT NULL REFERENCES plantillas(id) ON DELETE CASCADE,
    linea_id        INTEGER  NOT NULL REFERENCES lineas_produccion(id) ON DELETE CASCADE,
    operario_id     INTEGER  NOT NULL REFERENCES operarios(id) ON DELETE CASCADE,
    es_lider        BOOLEAN  DEFAULT FALSE,
    tiene_formacion BOOLEAN  DEFAULT FALSE,
    orden           INTEGER  DEFAULT 0,
    turno_id        INTEGER  DEFAULT 1 REFERENCES turnos(id),
    UNIQUE (plantilla_id, operario_id)
);

CREATE INDEX idx_plantilla_detalle_plantilla ON plantilla_detalle(plantilla_id);
CREATE INDEX idx_plantilla_detalle_linea ON plantilla_detalle(linea_id);
CREATE INDEX idx_plantilla_detalle_turno ON plantilla_detalle(plantilla_id, turno_id);
CREATE INDEX idx_plantilla_detalle_tmp_plantilla ON plantilla_detalle_tmp(plantilla_id);
CREATE INDEX idx_plantilla_detalle_tmp_turno ON plantilla_detalle_tmp(plantilla_id, turno_id);

CREATE TABLE usuarios (
    id             SERIAL PRIMARY KEY,
    codigo         VARCHAR(50)  NOT NULL UNIQUE,
    password       VARCHAR(100) NOT NULL,
    ultima_sesion  TIMESTAMP
);

INSERT INTO usuarios (codigo, password) VALUES ('admin', 'admin');
