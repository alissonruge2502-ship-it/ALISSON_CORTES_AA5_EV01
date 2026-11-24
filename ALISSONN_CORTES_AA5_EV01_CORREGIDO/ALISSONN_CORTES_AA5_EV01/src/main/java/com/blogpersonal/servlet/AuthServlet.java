package com.blogpersonal.servlet;

import com.blogpersonal.config.Conexion;
import com.blogpersonal.util.PasswordUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Servicio web REST sencillo para registrar usuarios e iniciar sesión.
 *
 * POST /api/auth/registro  JSON: {"nombre":"Ana","correo":"ana@correo.com","contrasena":"Clave123"}
 * POST /api/auth/login     JSON: {"correo":"ana@correo.com","contrasena":"Clave123"}
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String ruta = request.getPathInfo();
       com.google.gson.JsonElement json =
    com.google.gson.JsonParser.parseReader(request.getReader());

if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
    json = com.google.gson.JsonParser.parseString(json.getAsString());
}

Map<String, String> datos = gson.fromJson(json, Map.class);
        if (datos == null) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("mensaje", "Solicitud sin datos."));
            return;
        }

        try {
            if ("/registro".equals(ruta)) {
                registrar(datos, response);
            } else if ("/login".equals(ruta)) {
                iniciarSesion(datos, response);
            } else {
                responder(response, HttpServletResponse.SC_NOT_FOUND,
                        Map.of("mensaje", "Servicio no encontrado."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // El detalle técnico se registra en el servidor, no se expone al cliente.
            getServletContext().log("Error de base de datos en el servicio de autenticación.", e);
            responder(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of("mensaje", "No fue posible completar la operación."));
        } catch (Exception e) {
            getServletContext().log("Error procesando la solicitud de autenticación.", e);
            responder(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Map.of("mensaje", "Ocurrió un error al procesar la solicitud."));
        }
    }

    /** Registra un usuario nuevo después de validar los campos y proteger su contraseña. */
    private void registrar(Map<String, String> datos, HttpServletResponse response) throws Exception {
        String nombre = limpiar(datos.get("nombre"));
        String correo = limpiar(datos.get("correo"));
        String contrasena = datos.get("contrasena");

        if (nombre.isBlank() || correo.isBlank() || contrasena == null || contrasena.length() < 8) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("mensaje", "Ingrese nombre, correo y una contraseña de mínimo 8 caracteres."));
            return;
        }

        String consulta = "INSERT INTO usuarios (nombre, correo, contrasena_hash) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(consulta)) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, PasswordUtil.generarHash(contrasena));
            ps.executeUpdate();

            responder(response, HttpServletResponse.SC_CREATED,
                    Map.of("mensaje", "Registro exitoso."));
        } catch (SQLException e) {
            // El índice UNIQUE del correo evita registrar dos veces la misma cuenta.
            if ("23000".equals(e.getSQLState())) {
                responder(response, HttpServletResponse.SC_CONFLICT,
                        Map.of("mensaje", "El correo ya se encuentra registrado."));
            } else {
                throw e;
            }
        }
    }

    /** Comprueba el correo y verifica el hash de la contraseña recibida. */
    private void iniciarSesion(Map<String, String> datos, HttpServletResponse response) throws Exception {
        String correo = limpiar(datos.get("correo"));
        String contrasena = datos.get("contrasena");

        if (correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    Map.of("mensaje", "Debe ingresar correo y contraseña."));
            return;
        }

        String consulta = "SELECT contrasena_hash FROM usuarios WHERE correo = ?";
        try (Connection cn = Conexion.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(consulta)) {
            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verificar(contrasena, rs.getString("contrasena_hash"))) {
                    responder(response, HttpServletResponse.SC_OK,
                            Map.of("mensaje", "Autenticación satisfactoria."));
                } else {
                    responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                            Map.of("mensaje", "Error en la autenticación."));
                }
            }
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Envía una respuesta JSON con el código HTTP indicado. */
    private void responder(HttpServletResponse response, int estado, Map<String, String> cuerpo)
            throws IOException {
        response.setStatus(estado);
        gson.toJson(cuerpo, response.getWriter());
    }
}
