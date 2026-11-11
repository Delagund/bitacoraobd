# Reporte de cierre — Fase 2

**Estado:** Completada — QA global Ponytail APROBADO  
**Fecha:** 2026-11-07

## Alcance

Se completó la validación de Domain y Room descrita en el plan. No se cambiaron contratos ni el esquema de base de datos.

## Cambios y cobertura

- El escaneo exige conexión OBD `Connected`; errores no persisten sesiones y el guardado conserva los datos capturados.
- Se cubrieron recurrencia, límites de mantenimiento y kilometraje, dashboard reactivo y eliminación de vehículos.
- Room valida cascadas, transacciones y rollback, sesiones limpias y relaciones con códigos, orden e aislamiento, claves foráneas, PK compuesta e índices usando metadatos SQLite.
- Se probaron mappers en ambos sentidos, incluyendo fechas calendario, valores nulos y colecciones vacías o pobladas.
- DataStore valida persistencia y limpieza del vehículo activo.
- La eliminación activa actualiza primero la selección en DataStore. Si esa escritura falla, la fila permanece y la selección sigue apuntando a un vehículo existente.

## Hallazgo de QA corregido

La revisión global detectó que un fallo al escribir DataStore después de borrar el vehículo podía dejar un `activeVehicleId` obsoleto. Se invirtió el orden de operaciones y se añadió una regresión que falló antes del cambio y pasó después. No quedan hallazgos abiertos.

## Verificación final

Ejecutada después de la corrección, en secuencia:

1. `./gradlew :app:testDebugUnitTest` — **BUILD SUCCESSFUL**.
2. `./gradlew :app:connectedDebugAndroidTest` — **BUILD SUCCESSFUL** en SM-A066M / Android 16.
3. `./gradlew :app:lintDebug :app:assembleDebug` — **BUILD SUCCESSFUL**.

La ejecución instrumentada mostró una advertencia de deprecación de `sun.misc.Unsafe::arrayBaseOffset` en protobuf; no afectó el resultado.

## QA global

Ponytail verificó los cambios de Fase 2 frente al plan, incluyendo aserciones, aislamiento, manejo de datos y KISS/YAGNI. Veredicto: **APROBADO**.

No se validaron UI/navegación, conexión con adaptador físico ni carreras concurrentes entre conexión y borrado; corresponden a fases posteriores o fuera del alcance de esta validación.
