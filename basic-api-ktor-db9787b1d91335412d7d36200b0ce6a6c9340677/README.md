# API de Consolas (Ktor + Exposed)

API REST en Ktor 3 con persistencia mediante Exposed y conexión via HikariCP. Incluye datos seed y puede usar MySQL (docker-compose) o H2 embebido por defecto.

## Requisitos
- JDK 17
- Docker (opcional, para MySQL + phpMyAdmin)
- `./gradlew` (ya incluido) ejecutable en Unix/macOS

## Configuración de base de datos
`src/main/resources/application.conf` define un bloque `database`. Si no se establecen variables, se usa H2 en disco `build/db/consoles` con driver `org.h2.Driver`.

Variables de entorno soportadas (sobrescriben `application.conf`):
- `DB_URL` (p.ej. `jdbc:mysql://localhost:3306/dbEmployee`)
- `DB_DRIVER` (p.ej. `com.mysql.cj.jdbc.Driver`)
- `DB_USER`
- `DB_PASSWORD`
- `DB_MAX_POOL_SIZE`

### MySQL con Docker
```
cd backend
docker-compose up -d
```
Esto levanta MySQL con la BBDD `dbEmployee` y un volcado inicial en `backend/dump/Employee.sql`, además de phpMyAdmin en `http://localhost:8000`. Usa las credenciales del `docker-compose.yml` y define:
```
export DB_URL="jdbc:mysql://localhost:3306/dbEmployee"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_USER="santi"
export DB_PASSWORD="santi"
```

## Ejecución
```
./gradlew run
# o tests
./gradlew test
```
El servidor arranca en `http://localhost:8081` (configurable en `application.conf`).

## Endpoints
Base path: `/`

| Método | Ruta                | Descripción                               | Body / Params                                      |
| ------ | ------------------- | ----------------------------------------- | --------------------------------------------------- |
| GET    | `/`                 | Healthcheck                               | —                                                   |
| GET    | `/console`          | Lista todas las consolas                  | —                                                   |
| GET    | `/console/{name}`   | Obtiene una consola por nombre exacto     | `name` en path                                      |
| POST   | `/console`          | Crea consola                              | JSON `Console`                                      |
| PATCH  | `/console/{name}`   | Actualiza campos de una consola existente | Path `name`, body `UpdateConsole` (parcial)         |
| DELETE | `/console/{name}`   | Elimina consola                           | `name` en path                                      |

### Mensajes (JWT requerido)
- `GET /messages?with=<email>`: historial ordenado con el usuario indicado.
- `POST /messages`: envía mensaje (también lo reenvía por WebSocket si el receptor está conectado). Body:
```json
{
  "sender": "yo@correo.com",        // debe coincidir con el email del token
  "receiver": "otro@correo.com",
  "message": "Hola!",
  "timestamp": 0                    // opcional, se asigna en servidor si es 0
}
```

### Modelos
```json
// Console
{
  "name": "PlayStation 5",
  "releasedate": "2020",
  "company": "Sony",
  "description": "Consola de nueva generación.",
  "image": "https://..."
}
// UpdateConsole (todos los campos opcionales)
{
  "name": "PS5 Slim"
}
```

### Ejemplos con curl
```
curl http://localhost:8081/console

curl -X POST http://localhost:8081/console \
  -H "Content-Type: application/json" \
  -d '{"name":"PS5","releasedate":"2020","company":"Sony","description":"NG","image":"https://img"}'

curl -X PATCH http://localhost:8081/console/PS5 \
  -H "Content-Type: application/json" \
  -d '{"description":"Slim edition"}'

curl -X DELETE http://localhost:8081/console/PS5
```

## Notas técnicas
- Persistencia con Exposed (core + DAO + JDBC) y pool Hikari.
- Seed automático de consolas si la tabla `consoles` está vacía.
- Toolchain fijada a JVM 17 para evitar incompatibilidades (Gradle 8.10).
