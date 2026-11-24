-- Base de datos y tabla requeridas por el servicio de registro e inicio de sesión.
CREATE DATABASE IF NOT EXISTS blogpersonal
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE blogpersonal;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
