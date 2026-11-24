# BlogPersonalWeb – Servicio de registro e inicio de sesión
Evidencia GA7-220501096-AA5-EV01

## Descripción
Proyecto Maven empaquetado como WAR. Implementa dos operaciones HTTP POST:
- `POST /BlogPersonalWeb/api/auth/registro`
- `POST /BlogPersonalWeb/api/auth/login`

El servicio recibe los datos en JSON y devuelve respuestas JSON. La contraseña se almacena como hash PBKDF2 con salt aleatorio; no se guarda en texto plano. Las consultas SQL utilizan `PreparedStatement`.

## Requisitos
- JDK 17.
- Maven 3.10 o compatible.
- MySQL Server.
- Apache Tomcat 9 (compatible con `javax.servlet` 4.0.1).

## 1. Crear la tabla
Abrir MySQL Workbench y ejecutar el contenido de `src/main/resources/db/schema.sql`.
La configuración predeterminada de este proyecto usa `localhost:3307`, porque ese es el puerto configurado en el entorno de desarrollo registrado para BlogPersonalWeb.

## 2. Configurar la conexión
Antes de iniciar Tomcat, configurar estas variables de entorno:
- `DB_URL` (opcional; predeterminado: `jdbc:mysql://localhost:3307/blogpersonal?useSSL=false&serverTimezone=America/Bogota`)
- `DB_USER` (opcional; predeterminado: `root`)
- `DB_PASSWORD` (definir con la contraseña local de MySQL; no escribirla en este archivo ni subirla a Git).

Si tu servidor MySQL usa otro puerto, cambia `DB_URL`. No compartas la contraseña en capturas ni en el repositorio.

## 3. Compilar
Desde la carpeta raíz del proyecto ejecutar:
```bash
mvn clean package
```
El archivo generado será `target/BlogPersonalWeb.war`. Copiarlo a la carpeta `webapps` de Tomcat 9 e iniciar el servidor.

## 4. Probar el servicio
Usar Postman, Insomnia o una herramienta equivalente. Sustituir `8080` por el puerto HTTP de tu Tomcat si es diferente.

### Registro
`POST http://localhost:8080/BlogPersonalWeb/api/auth/registro`
Encabezado: `Content-Type: application/json`
```json
{
  "nombre": "Usuario de prueba",
  "correo": "prueba@correo.com",
  "contrasena": "ClaveSegura123"
}
```
Respuesta esperada: HTTP 201
```json
{"mensaje":"Registro exitoso."}
```

### Inicio de sesión correcto
`POST http://localhost:8080/BlogPersonalWeb/api/auth/login`
```json
{
  "correo": "prueba@correo.com",
  "contrasena": "ClaveSegura123"
}
```
Respuesta esperada: HTTP 200
```json
{"mensaje":"Autenticación satisfactoria."}
```

### Inicio de sesión incorrecto
Enviar el mismo correo con una contraseña equivocada.
Respuesta esperada: HTTP 401
```json
{"mensaje":"Error en la autenticación."}
```

## 5. Control de versiones con Git
El proyecto incluye `.gitignore`. Desde la carpeta raíz ejecutar:
```bash
git init
git add .
git commit -m "Evidencia AA5 EV01: servicio de registro e inicio de sesión"
```
Para conectarlo con un repositorio remoto creado en GitHub:
```bash
git branch -M main
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
```
No inventes ni dejes una URL de ejemplo como si fuera la real: pega en `enlace_repositorio.txt` el enlace del repositorio que creaste.

## Estructura
- `src/main/java`: clases Java del servicio.
- `src/main/resources/db/schema.sql`: creación de la base de datos y tabla.
- `pom.xml`: dependencias y configuración Maven.
- `enlace_repositorio.txt`: espacio para el enlace real del repositorio.
