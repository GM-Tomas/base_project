# BASE Wealth Management & Tasks - Backend API (Kotlin)

API REST moderna, reactiva y testeable construida con **Kotlin 1.9+**, **Spring Boot 3.3**, y **Gradle Kotlin DSL**.

El frontend (Next.js) vive en un repo aparte: [GM-Tomas/base_project_fe](https://github.com/GM-Tomas/base_project_fe).

---

## 📋 Requisitos Previos

Para compilar, ejecutar y ejecutar los tests necesitas tener instalado **Java JDK 21** (la toolchain
del proyecto lo exige exactamente, ver `build.gradle.kts`).

El perfil `dev` (el que usa `bootRun`) además necesita **Docker** corriendo: levanta Postgres
automáticamente vía `compose.yaml` (plugin `spring-boot-docker-compose`), no hay que
arrancarlo a mano.

### 📥 Instalación del JDK en Windows:
1. **Eclipse Temurin OpenJDK 21** (Recomendado):
   - Descarga el instalador `.msi` desde [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21).
   - Durante la instalación, asegúrate de marcar la opción **"Set JAVA_HOME variable"** y **"Add to PATH"**.
2. **Verificación**:
   Abre una terminal (PowerShell o CMD) y ejecuta:
   ```bash
   java -version
   javac -version
   ```

*Nota: No necesitas instalar Kotlin ni Gradle de forma manual; el script `gradlew.bat` incluido en este repositorio gestiona y descarga automáticamente todo lo necesario.*

Para que el hook de pre-commit regenere `docs/api/openapi.json` automáticamente (una sola vez por clon del repo):
```bash
git config core.hooksPath .githooks
```
Sin este paso el hook no corre y `openapi.json` puede quedar desactualizado; regenéralo a mano
con `./gradlew generateOpenApiDocs` si hace falta.

---

## 🚀 Comandos Rápidos

### 1. Ejecutar la API en modo Desarrollo
```powershell
# En Windows:
.\gradlew.bat bootRun

# En Linux/macOS:
./gradlew bootRun
```
La API estará disponible en `http://localhost:8080`.

### 2. Ejecutar la Suite de Tests Automatizados
```powershell
# En Windows:
.\gradlew.bat test

# En Linux/macOS:
./gradlew test
```

### 3. Compilar el archivo ejecutable (JAR)
```powershell
.\gradlew.bat bootJar
```
El archivo JAR ejecutable se generará en `build/libs/base-wealth-backend-0.0.1-SNAPSHOT.jar`.

---

## 📖 Documentación Interactiva (Swagger / OpenAPI)

Una vez que la aplicación esté corriendo, accede a:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🌐 Catálogo de Endpoints REST

La lista de endpoints, con headers y bodies exactos, se genera desde el código y nunca se
escribe a mano (así nunca queda desactualizada):
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (con la app corriendo)
- **OpenAPI JSON en vivo**: `http://localhost:8080/v3/api-docs`
- **OpenAPI JSON versionado en el repo**: [`docs/api/openapi.json`](docs/api/openapi.json),
  regenerado automáticamente en cada commit que toque `src/` (ver `.githooks/pre-commit`).

El **contrato de diseño** (fuente de verdad, escrito a mano antes que el código) vive en
[`specs/001-backend-para-frontend/contracts/openapi.yaml`](specs/001-backend-para-frontend/contracts/openapi.yaml).
CI compara ambos en cada push (`./gradlew contractDriftCheck`) y falla si divergen.

---

## 🔐 Autenticación

Todo endpoint bajo `/api/v1/**` excepto `/api/v1/health` exige un JWT de Supabase Auth:

```
Authorization: Bearer <session.access_token>
```

El token se valida contra el JWKS del proyecto de Supabase (`SUPABASE_URL`, que ya trae un
default público apuntando al proyecto propio — ver `application.yml`); sin token, con firma
inválida, expirado o con `iss`/`aud` incorrectos, la respuesta es `401` en `application/problem+json`.
La identidad del usuario (`sub` del JWT) es la única fuente del `userId` — ningún endpoint lo
acepta en body, path ni query (ver `specs/001-backend-para-frontend/spec.md` CA-01.5).

## 💾 Persistencia (Postgres + Flyway)

El esquema vive versionado en [`src/main/resources/db/migration`](src/main/resources/db/migration)
(Flyway) y todos los repositorios (`Jdbc*Repository`) hablan `JdbcClient` contra Postgres —
no hay almacenamiento en archivo ni en memoria. Según el perfil de Spring:

- **`dev`** (default de `bootRun`): Postgres real vía `compose.yaml`, auto-levantado por
  `spring-boot-docker-compose` (requiere Docker; ver arriba). Flyway migra el esquema al arrancar.
- **`test`** (`./gradlew test`): Postgres real vía Testcontainers, un container efímero por
  suite (ver `src/test/kotlin/com/base/wealth/support/PostgresTestBase.kt`). Los `@WebMvcTest`
  con servicios mockeados no activan este perfil y no tocan ninguna base.
- **`prod`**: el Postgres real de Supabase, con el `DataSource`/`HikariCP` armados a mano en
  `infrastructure/config/DataSourceConfig.kt`. Requiere las variables de entorno
  `SUPABASE_DB_URL` (el host del *Session Pooler*, no `db.<ref>.supabase.co` — la mayoría de los
  PaaS son IPv4-only y ese host es IPv6-only), `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD` y
  `FRONTEND_ORIGIN` (CORS).

Para correr localmente contra el Postgres de Supabase en vez del de `compose.yaml`, exportá esas
variables y arrancá con `SPRING_PROFILES_ACTIVE=prod`.

Un usuario nuevo arranca sin holdings ni plataformas — no hay semillas en las migraciones. Para
tener datos de ejemplo en dev, corré [`scripts/seed-dev.sql`](scripts/seed-dev.sql) a mano (ver el
comentario del archivo).

## 🔗 Integración con el Frontend

El backend tiene configurado CORS para permitir peticiones directas desde `http://localhost:3000`.
El cliente de referencia es [`lib/api.ts`](https://github.com/GM-Tomas/base_project_fe/blob/main/src/lib/api.ts)
en [base_project_fe](https://github.com/GM-Tomas/base_project_fe), que envuelve exactamente esto:

```typescript
const { data: { session } } = await supabase.auth.getSession();
const response = await fetch('http://localhost:8080/api/v1/wealth/summary', {
  headers: { Authorization: `Bearer ${session?.access_token}` },
});
const summary = await response.json();
```
