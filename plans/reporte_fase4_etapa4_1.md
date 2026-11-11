# Reporte de etapa 4.1 — Idempotencia diagnóstica

**Estado:** validación instrumentada completada en Pixel_10 (AVD), Android 17/API 37. Suite instrumentada: 19 pruebas, 0 fallos, 1 omitida (integración ELM327 remota sin configuración).

## Fase A — Coder

- Añadió captureId UUID a DiagnosticScanResult.Success y DiagnosticSession; SaveDiagnosticSessionUseCase y mappers preservan la misma clave.
- Room v2 almacena captureId con índice UNIQUE. El repositorio inserta con IGNORE y dentro de withTransaction devuelve el ID existente para la misma clave/payload; payload distinto falla. Códigos y avance de odómetro ocurren solo con una inserción nueva.
- MIGRATION_1_2 añade la columna con el sentinel de esquema aprobado, asigna UUID distintos en Kotlin a sesiones existentes y luego crea el índice único. Las tablas existentes no se reconstruyen ni se eliminan.
- Schema v2 exportado en app/schemas. No hay builder Room en producción todavía; queda para registrar la migración en 4.3.

## Fase B — Tester

- Instrumentadas pasaron: idempotencia/reintento, payload contradictorio, claves distintas con contenido igual, concurrencia, rollback/reintento, metadatos SQLite, cascadas, consultas y migración desde fixture v1 con vehículo, sesión, código y mantenimiento.
- La primera corrida encontró un assert desactualizado: esperaba solo el índice de vehicleId e ignoraba el índice único nuevo captureId. Se actualizó el assert para esperar ambos índices; la segunda corrida completa pasó.
- Evidencia de la corrida final:
  - `./gradlew :app:connectedDebugAndroidTest` — BUILD SUCCESSFUL en Pixel_10 (AVD), Android 17/API 37, 31 s.
  - Informe Gradle: 19 pruebas, 0 fallos, 1 omitida; las 15 pruebas AppDatabaseTest, DataStoreAppPreferencesRepositoryTest y DiagnosticMigrationTest pasaron.
- La suite JVM, compilación de AndroidTest, lint y assemble ya estaban registradas como exitosas en la evidencia de 4.1. No se repitieron en esta validación final porque el único cambio de corrección fue el assert instrumentado.

## Fase C — QA/Ponytail

**Veredicto: APROBADO.** La captura conserva identidad estable, el índice único y la transacción protegen reintentos/concurrencia, rollback preserva integridad y la migración desde v1 preserva datos relacionados. Se revisó el assert corregido contra los metadatos reales de SQLite y la corrida final pasó.

## Límite pendiente

La integración remota ELM327 se omitió porque no se configuró `elm327Host`; no forma parte de la validación Room/migración. No se promete recuperar una captura tras muerte del proceso. El registro de MIGRATION_1_2 en un builder de producción sigue siendo parte de la composición 4.3.
