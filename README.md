# Base Project

## Structure

- **frontend/**: Next.js (React + TypeScript) BASE Wealth Dashboard, Vercel-ready.
- **backend/**: Backend API en Kotlin (Spring Boot 3 + Gradle Kotlin DSL).
- **supabase/**: Migraciones y configuraciones de base de datos Supabase.

## Quick Start

### 1. Backend (Kotlin / Spring Boot)
> Requiere Java JDK 17 o 21 instalado.

```bash
cd backend
./gradlew.bat bootRun    # En Windows
# ./gradlew bootRun      # En Linux/macOS
```
Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

### 2. Frontend (Next.js)
```bash
cd frontend
npm install
npm run dev
```
Aplicación disponible en: `http://localhost:3000`

## Documentación de la API

La documentación de endpoints se genera directamente desde el código (springdoc-openapi),
nunca se escribe a mano:
- **En vivo**: con el backend corriendo, `http://localhost:8080/swagger-ui.html` (UI) y
  `http://localhost:8080/v3/api-docs` (JSON) siempre reflejan el estado actual del código.
- **Versionada en el repo**: [`backend/docs/api/openapi.json`](backend/docs/api/openapi.json)
  se regenera automáticamente en cada commit que toque `backend/src/`.

Para habilitar la regeneración automática (una sola vez por clon del repo):
```bash
git config core.hooksPath .githooks
```
Sin este paso el hook no corre y `openapi.json` puede quedar desactualizado; regenéralo a mano
con `cd backend && ./gradlew generateOpenApiDocs` si hace falta.

