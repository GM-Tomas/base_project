# BASE Wealth Management & Tasks - Backend API (Kotlin)

API REST moderna, reactiva y testeable construida con **Kotlin 1.9+**, **Spring Boot 3.3**, y **Gradle Kotlin DSL**.

---

## 📋 Requisitos Previos

Para compilar, ejecutar y ejecutar los tests necesitas tener instalado **Java JDK (versión 17 o 21)**.

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

---

## 🚀 Comandos Rápidos

Dentro del directorio `backend/`:

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
  regenerado automáticamente en cada commit — ver sección de documentación en el
  [README raíz](../README.md#documentación-de-la-api).

---

## 🔗 Integración con el Frontend Next.js

El backend tiene configurado CORS para permitir peticiones directas desde `http://localhost:3000`.

En tu frontend Next.js, puedes consumir los endpoints directamente:
```typescript
const response = await fetch('http://localhost:8080/api/v1/wealth/summary');
const summary = await response.json();
```
