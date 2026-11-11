# Etapa 4.1 — Idempotencia de capturas diagnósticas

> **Para agentes:** ejecutar en orden Coder → Tester → QA/Ponytail, siguiendo AGENTS.md. No comenzar la codificación hasta la confirmación del usuario sobre este plan.

**Objetivo:** guardar una captura exitosa como máximo una vez, incluso si el guardado se reintenta o concurre, preservando los datos Room existentes mediante migración no destructiva.

**Arquitectura:** la captura exitosa recibe una clave UUID al construirse y la conserva mientras siga viva en memoria. La entidad de sesión almacena esa clave bajo una restricción única; una inserción conflictiva resuelve a la sesión ya existente dentro de la transacción. La sesión, sus códigos y el avance de kilometraje siguen atómicos. No se persiste una captura pendiente ni se promete recuperarla tras la muerte del proceso.

**Tecnología:** Kotlin, UUID de Java, Room/SQLite y coroutines existentes; sin nuevas dependencias.

## Auditoría inicial y contratos observados

- DiagnosticScanResult.Success contiene vehículo, timestamp, kilometraje y códigos; no tiene identidad.
- ScanDiagnosticCodesUseCase construye un Success por lectura y deduplica códigos.
- SaveDiagnosticSessionUseCase mapea cada éxito a DiagnosticSession; recibe el mismo objeto si el consumidor reintenta esa captura.
- DiagnosticRepository.saveDiagnosticSessionAndAdvanceMileage(DiagnosticSession): Long es la API de persistencia.
- RoomDiagnosticRepository inserta una nueva sesión dentro de withTransaction, luego códigos y avance de odómetro; cada llamada usa ID autogenerado.
- DiagnosticSessionEntity usa PK autogenerada y FK a vehículo. La base declara versión 1.
- Pruebas relevantes: DiagnosticAndMaintenanceUseCasesTest, EntityMappersTest y AppDatabaseTest. La prueba instrumentada de rollback fuerza fallo al avanzar odómetro. La base instrumentada actual se crea en memoria y aún no prueba migraciones.
- El plan general aprobado prescribe v1→v2, identidad estable por captura, clave repetida con contenido diferente como error y pruebas de concurrencia, rollback y preservación histórica.

## Alcance y límites

Incluye idempotencia de captura, cambios de modelo/repositorio/esquema necesarios, migración v1→v2 y pruebas de esos comportamientos. No incluye ViewModel, Compose, navegación, Figma ni persistencia del resultado pendiente. No altera el significado de diagnóstico limpio, códigos ni odómetro.

La clave se genera al crear cada DiagnosticScanResult.Success, no al guardar. Copiar el objeto conserva la clave; una lectura posterior genera otra clave aunque sus campos coincidan. La clave será String UUID aleatorio; no se deriva del contenido, porque dos capturas distintas pueden ser idénticas en sus datos.

Un UUID de captura existente con contenido distinto se rechaza explícitamente sin sobrescribir ni considerarse reintento equivalente. Cancelación/excepción se propaga. Si el commit ocurrió pero el llamador no recibió el ID, reintentar con la misma captura obtiene el ID existente. No se promete que la captura sobreviva al proceso.

## Archivos previstos

### Producción

- Modificar app/src/main/java/cl/cristian/bitacoraobd/domain/model/DiagnosticScanResult.kt: agregar captureId String con UUID aleatorio generado como valor por defecto en Success.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/domain/model/DiagnosticSession.kt: agregar captureId y propagarlo desde el resultado.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveDiagnosticSessionUseCase.kt: transferir la clave sin regenerarla.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/domain/repository/DiagnosticRepository.kt: mantener la firma y retorno Long; documentar idempotencia.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/data/local/entity/Entities.kt: columna captureId no nula e índice único en diagnostic_sessions.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/data/local/entity/EntityMappers.kt: mapear captureId ida/vuelta.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/data/local/dao/DiagnosticDao.kt: insertar sesión con estrategia IGNORE y consultar por captureId.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/data/repository/RoomDiagnosticRepository.kt: resolver inserción nueva o existente dentro de una única transacción; verificar mismo contenido, escribir códigos y odómetro solo para sesión nueva.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/data/local/database/AppDatabase.kt: incrementar versión 1 a 2.
- Añadir app/src/main/java/cl/cristian/bitacoraobd/data/local/database/Migrations.kt: migración explícita v1→v2.
- Exportar schema Room v2 a app/schemas, ruta ya documentada en domain-contracts.md.
- Buscar todos los builders Room al iniciar. La inspección preliminar bajo app/src/main no encontró builder; si se confirma, no crear AppContainer/builder en 4.1. Si aún no hay builder de producción, no hay punto de registro que modificar: dejar MIGRATION_1_2 pública para que la etapa 4.3 la registre al construir la base.

### Pruebas

- Modificar app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt: verificar propagación de identidad.
- Modificar app/src/test/java/cl/cristian/bitacoraobd/data/local/entity/EntityMappersTest.kt: round-trip de captureId.
- Modificar app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt: reintento, clave nueva, payload contradictorio, concurrencia, rollback/reintento y unicidad.
- Añadir prueba de migración desde v1 con vehículo, sesiones, códigos y mantenimiento; comprobar IDs, datos, relaciones y claves distintas. Confirmar ruta/configuración de schema export antes de escoger fixture. Si el schema v1 no está versionado, crear fixture SQLite fiel a las entidades v1 y documentarlo; nunca desactivar validación ni usar migración destructiva.

## Fase A — Coder (no ejecuta pruebas)

1. Revisar usos de Success, DiagnosticSession, mappers, DAO y constructores Room; confirmar configuración de exportación de schema y buscar builders Room en todo el código fuente.
2. Agregar captureId UUID a Success, generado al construir cada resultado; copiar el objeto mantiene identidad. Pasar la misma clave al crear DiagnosticSession.
3. Añadir captureId a entidad, mapper y restricción UNIQUE de SQLite; agregar inserción IGNORE y consulta DAO por clave.
4. Implementar la operación transaccional:
   - Intentar insertar la sesión con IGNORE.
   - Si se insertó, insertar códigos deduplicados y avanzar odómetro dentro de la transacción; retornar el ID nuevo.
   - Si hubo conflicto, consultar la fila por captureId. Comparar vehicleId, timestampMillis, mileageAtScanKm y el conjunto normalizado de códigos.
   - Si difiere, lanzar IllegalArgumentException sin actualizar fila ni efectos asociados.
   - Si coincide, retornar el ID existente sin volver a insertar códigos ni avanzar odómetro.
   La restricción única es la autoridad ante carreras; no separar comprobación y escritura fuera de transacción.
5. Incrementar Room a v2 con una migración aditiva: ALTER TABLE diagnostic_sessions ADD COLUMN captureId TEXT NOT NULL DEFAULT 'legacy-migration-sentinel'; recorrer los IDs existentes y reemplazar el sentinel por UUID.randomUUID().toString() desde Kotlin; después crear el índice UNIQUE de captureId. El usuario autorizó este default de migración y aclaró que los datos presentes son pruebas. Se conservan tablas y claves foráneas sin DROP, evitando cascada accidental. Declarar el mismo default en @ColumnInfo para que Room valide el schema; todo DAO de producción escribe captureId explícito. Room ejecuta Migration dentro de su transacción.
6. Registrar migración en un builder Room existente, si lo hay; si aún no existe uno en producción, no crear composición nueva en esta etapa.
7. Entregar al Tester la lista de cambios. No ejecutar pruebas.

## Fase B — Tester

1. Añadir pruebas para misma captura dos veces: mismo ID, una sesión y códigos intactos.
2. Verificar captureId distinto con campos iguales: crea dos sesiones distintas.
3. Verificar misma clave con contenido distinto: falla sin modificar sesión, códigos ni kilometraje.
4. Ejecutar múltiples coroutines concurrentes para el mismo resultado, sin sleeps: todas obtienen el mismo ID, una sesión y un conjunto de códigos.
5. Forzar fallo del avance del odómetro: sesión/códigos ausentes y odómetro intacto. Retirar el trigger y reintentar la misma captura: una sesión guardada.
6. Migrar base v1 con vehículo, sesiones, códigos y mantenimiento; comprobar PKs/datos/relaciones, consultas existentes y claves históricas no nulas y únicas.
7. Ejecutar secuencialmente:
   - ./gradlew :app:testDebugUnitTest
   - ./gradlew :app:connectedDebugAndroidTest si hay dispositivo disponible
   - ./gradlew :app:lintDebug
   - ./gradlew :app:assembleDebug
8. Registrar salidas exactas y dispositivo. Marcar como no ejecutada cualquier validación omitida; no afirmar 100% PASS con suite requerida pendiente.

## Fase C — QA/Ponytail Reviewer

1. Revisar cambios conectados en modelos, caso de uso, contrato, Room, DAO, mappers, migración y pruebas.
2. Confirmar que la clave nace una vez por captura y no se deriva del payload.
3. Confirmar carrera protegida por índice único y transacción, contenido contradictorio sin sobrescritura y ausencia de efectos duplicados.
4. Confirmar preservación de PK/FK/códigos/mantenimiento y apertura Room validada con migración.
5. Revisar propagación de cancelación/errores, rollback y kilometraje no regresivo.
6. Aplicar KISS/YAGNI: sin dependencias, wrappers, tabla de capturas ni persistencia de pendientes.
7. Ante hallazgos, devolverlos al Coder y repetir Tester/QA secuencialmente. Aprobar solo con checklist completo y evidencia.

## Criterios de aceptación

- Reintento de la misma captura retorna el mismo ID y deja exactamente una sesión y sus códigos.
- Claves distintas crean sesiones distintas, incluso con payload igual.
- Misma clave con payload diferente falla sin sobrescribir.
- Guardados concurrentes de la misma clave convergen a un ID y una sesión.
- El avance de kilometraje ocurre solo junto a una inserción nueva; rollback revierte toda la operación.
- Migración v1 preserva IDs, vehículos, sesiones, códigos, mantenimientos y consultas; claves históricas son únicas.
- Cancelación y error no se convierten en éxito.
- No se promete recuperación tras muerte del proceso; no se inicia Compose ni se modifica Figma.

## Pendientes de inspección al ejecutar

La búsqueda completa de builders Room y configuración de schema export debe cerrarse antes de tocar esa integración. Esa inspección puede ajustar ruta/fixture, pero no cambia el contrato funcional descrito.
