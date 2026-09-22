
-- 1) Base de datos
IF DB_ID('bloom') IS NULL
    CREATE DATABASE bloom;
GO

USE bloom;
GO

-- 2) Tabla cliente
IF OBJECT_ID('cliente', 'U') IS NULL
CREATE TABLE cliente (
    idcliente         INT IDENTITY(1,1) PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    telefono          VARCHAR(20)  NOT NULL,
    direccion         VARCHAR(200) NULL,
    correoelectronico VARCHAR(100) NULL,
    tipodocumento     VARCHAR(20)  NOT NULL,
    numerodocumento   VARCHAR(30)  NOT NULL UNIQUE,
    activo            BIT          NOT NULL DEFAULT 1
);
GO

-- 3) Tabla pedido
IF OBJECT_ID('pedido', 'U') IS NULL
CREATE TABLE pedido (
    idpedido         INT IDENTITY(1,1) PRIMARY KEY,
    idcliente        INT           NOT NULL REFERENCES cliente(idcliente),
    fecharegistro    DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    fechahoraentrega DATETIME2     NOT NULL,
    direccionentrega VARCHAR(200)  NOT NULL,
    ocasion          VARCHAR(100)  NULL,
    estado           VARCHAR(20)   NOT NULL DEFAULT 'REGISTRADO',
    valortotal       DECIMAL(12,2) NOT NULL DEFAULT 0,
    fechaentregareal DATETIME2     NULL,
    CONSTRAINT ck_pedido_estado CHECK (estado IN
        ('REGISTRADO', 'EN_ELABORACION', 'LISTO', 'ENTREGADO', 'CANCELADO'))
);
GO

-- 4) Tabla arreglo (cada pedido tiene uno o varios arreglos)
IF OBJECT_ID('arreglo', 'U') IS NULL
CREATE TABLE arreglo (
    idarreglo     INT IDENTITY(1,1) PRIMARY KEY,
    idpedido      INT           NOT NULL REFERENCES pedido(idpedido),
    tipoarreglo   VARCHAR(50)   NOT NULL,
    tamano        VARCHAR(30)   NULL,
    colores       VARCHAR(100)  NULL,
    descripcion   VARCHAR(300)  NULL,
    valorunitario DECIMAL(12,2) NOT NULL
);
GO

-- 5) Usuario con el que se conecta el proyecto
USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'bloom_app')
    CREATE LOGIN bloom_app WITH PASSWORD = 'Bloom2026*', CHECK_POLICY = OFF;
GO

USE bloom;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'bloom_app')
    CREATE USER bloom_app FOR LOGIN bloom_app;
GO
ALTER ROLE db_owner ADD MEMBER bloom_app;
GO

-- 6) Comprobacion: debe mostrar las 3 tablas
SELECT name AS tablas_creadas FROM sys.tables ORDER BY name;
GO