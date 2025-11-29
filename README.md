📘 Users Microservice – Gestión de Usuarios (Spring Boot + Hexagonal Architecture + AWS Cognito)

Este microservicio gestiona el ciclo de vida de los usuarios del sistema clínico, incluyendo creación, actualización, consulta y eliminación lógica (soft delete). Además, integra de manera directa con AWS Cognito para la administración de usuarios y asignación de roles mediante grupos predefinidos.

Arquitectura basada en puertos y adaptadores (Hexagonal Architecture) y desarrollada con Spring Boot 3, Java 17 y el SDK oficial de AWS.

🚀 Características principales
✔️ Gestión completa de usuarios

Crear usuarios con campos obligatorios y validaciones estrictas.

Actualizar información personal manteniendo reglas del dominio.

Inhabilitar (soft-delete) usuarios.

Consultar usuario por username.

Listar usuarios según filtro.

✔️ Integración con AWS Cognito

Creación de usuarios en Cognito.

Habilitación / deshabilitación de usuarios.

Asignación automática de roles mediante grupos:

MEDICO

ENFERMERA

ADMINISTRATIVO

RRHH

SOPORTE

✔️ Validaciones del dominio (US-HR-01 / US-HR-03)

Cédula única.

Email válido.

Teléfono de 1–10 dígitos.

Dirección ≤ 30 caracteres.

Nombre de usuario alfanumérico ≤ 15 caracteres.

Birthdate en DD/MM/YYYY y edad ≤ 150 años.

Contraseña con 8+ caracteres, 1 mayúscula, 1 número y 1 caracter especial.

🏗️ Arquitectura

El proyecto sigue una arquitectura hexagonal, dividiendo claramente:

/application
  /port/in
  /port/out
  /service

/domain
  /model
  /exception

/infrastructure
  /adapter/in/web     ← Controladores REST
  /adapter/out/cognito ← Adaptador AWS Cognito
  /config


Esto permite:

Alto desacoplamiento.

Fácil prueba con mock de los puertos.

Sustitución de proveedores (por ejemplo, cambiar Cognito por Keycloak) sin afectar el dominio.

📦 Requerimientos

Java 17

Maven 3.9+

Spring Boot 3.4+

AWS Account con:

User Pool

Grupos configurados

AWS Credentials:

En ~/.aws/credentials

O variables de entorno

O definidas en application-local.yaml

⚙️ Configuración
📍 application-local.yaml

Ejemplo:

aws:
  region: us-east-1
  cognito:
    userPoolId: us-east-1_xxxxxx
    clientId: xxxxxxxxxxxxx
  auth:
    mode: static
  accessKeyId: ${AWS_ACCESS_KEY_ID}
  secretAccessKey: ${AWS_SECRET_ACCESS_KEY}

spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration


⚠️ Importante:
No incluyas claves de AWS en el repositorio. Usa variables de entorno.

▶️ Ejecutar el proyecto

Modo local:

mvn spring-boot:run -Dspring-boot.run.profiles=local


Construir artefacto:

mvn clean package


Ejecutar tests:

mvn test

🧪 Test unitarios

El proyecto incluye pruebas sobre:

Validaciones del dominio (UserAdminServiceTest)

Operaciones sobre Cognito mockeadas

Controlador REST (UserAdminControllerTest)

Contexto de aplicación (UsersApplicationTests)

Las pruebas no requieren conexión real a AWS.

🔌 Endpoints REST
1️⃣ Crear usuario
POST /api/users

Body:
{
  "username": "medico01",
  "firstName": "Ana",
  "lastName": "Ramirez",
  "document": "123456789",
  "email": "ana@clinic.com",
  "phone": "3001234567",
  "address": "Calle 123",
  "birthdate": "10/05/1980",
  "password": "Passw0rd!",
  "role": "MEDICO",
  "sendInvite": true
}

2️⃣ Consultar usuario
GET /api/users/{username}

3️⃣ Actualizar datos
PATCH /api/users/{username}


Body parcial:

{
  "email": "new@clinic.com",
  "phone": "3000000000"
}

4️⃣ Eliminar usuario (soft-delete)
DELETE /api/users/{username}

5️⃣ Listar usuarios
GET /api/users?limit=20&filter=ana

🧱 Integración con AWS Cognito
Operaciones utilizadas

AdminCreateUser

AdminSetUserPassword

AdminDisableUser

AdminEnableUser

AdminGetUser

ListUsers

AdminAddUserToGroup

Requisitos IAM

El rol debe incluir permisos:

{
  "Effect": "Allow",
  "Action": [
    "cognito-idp:AdminCreateUser",
    "cognito-idp:AdminAddUserToGroup",
    "cognito-idp:AdminSetUserPassword",
    "cognito-idp:AdminDisableUser",
    "cognito-idp:AdminEnableUser",
    "cognito-idp:AdminGetUser",
    "cognito-idp:AdminUpdateUserAttributes",
    "cognito-idp:ListUsers"
  ],
  "Resource": "*"
}

📡 Probar en Postman

Crear colección nueva → agregar variable baseUrl:

http://localhost:8080


Agregar requests:

POST {{baseUrl}}/api/users

GET {{baseUrl}}/api/users/{{username}}

PATCH {{baseUrl}}/api/users/{{username}}

DELETE {{baseUrl}}/api/users/{{username}}

Enviar JSON con las validaciones correctas.

👥 Historias de usuario cubiertas
US-HR-01 — Crear usuario con rol y permisos

✔ Validaciones del dominio
✔ Asignación de grupo en Cognito
✔ Configuración de password
✔ Creación en IdP

US-HR-02 — Eliminar usuario

✔ Inhabilitación en Cognito
✔ Estado reflejado en consultas

US-HR-03 — Actualizar datos

✔ Mismas validaciones que creación
✔ Actualización parcial
✔ Verificación de datos en Cognito
