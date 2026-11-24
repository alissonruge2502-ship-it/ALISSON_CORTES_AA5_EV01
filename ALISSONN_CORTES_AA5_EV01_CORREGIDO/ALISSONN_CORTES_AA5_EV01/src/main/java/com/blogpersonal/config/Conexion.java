
package com.blogpersonal.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la conexión JDBC con la base de datos blogpersonal.
 * La contraseña se obtiene de una variable de entorno.
 */
public final class Conexion {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3307/blogpersonal?useSSL=false&serverTimezone=America/Bogota"
    );

    private static final String USUARIO =
            System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "");

    private Conexion() {
        // Evita crear instancias de esta clase de utilidad.
    }

    public static Connection obtenerConexion() throws SQLException {
        try {
            // Cargar el controlador JDBC de MySQL.
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establecer la conexión con la base de datos.
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontró el controlador de MySQL.", e
            );
        }
    }
}
