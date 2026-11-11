# Pre-Fase 4 — Auditoría técnica parcial

Estado: investigación documental parcial histórica; no constituye cierre de QA. El texto inicial y sus hallazgos originales se conservan como línea base y quedan actualizados por la conciliación siguiente. No se ejecutaron pruebas durante esta conciliación.

## Conciliación técnica posterior — alcance Pre-Fase 4

- **Mantenimiento y Dashboard:** el saneamiento posterior implementó edición de mantenimiento del mismo ID y validaciones; en alta se puede avanzar kilometraje, al editar no. Hay siete tipos alertables y OTHER. Dashboard entrega todos los códigos analizados en `codesInLastSession` y mantiene separadas las alertas de mantenimiento. La antigua descripción de inserción exclusiva, cuatro tipos y `recurrentCodesInLastSession` queda superada. Fuentes: [contratos](designplans/domain-contracts.md), [saneamiento](reporte_saneamiento_prefase4.md), [RoomMaintenanceRepository](../app/src/main/java/cl/cristian/bitacoraobd/data/repository/RoomMaintenanceRepository.kt) y [Dashboard](../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetDashboardDataUseCase.kt).
- **Decisiones funcionales:** DD-12 conserva `ObdResult`; DD-24 usa año 0 como ausencia; DD-33–37 resuelven salida durante lectura, recuperación de permiso LAN, salida con configuración modificada, guardado explícito de Ajustes y redondeo de millas. La lista de decisiones abiertas de la línea base es histórica. Sus implementaciones y validaciones siguen pendientes según [decisiones](designplans/design-decisions.md) y [contratos](designplans/domain-contracts.md).
- **Selección y borrado:** el bloqueo actual solo comprueba Connected; el cambio de preferencia y borrado son operaciones DataStore/Room separadas. Guardias transitorias, captura pendiente y recuperación de fallo parcial quedan por planificar e implementar. Fallback del Dashboard no persiste selección. La política de migración no destructiva se mantiene; una migración futura no ha sido ejecutada.
- **Diagnóstico y preferencias:** Room no garantiza idempotencia al reintentar un guardado de resultado incierto. El usuario aceptó un identificador único estable por captura; su diseño técnico, migración y pruebas son prioritarios en fase 4. Nombre, unidades y visibilidad aún no existen en el contrato actual; los defaults y errores propuestos se registran en [contratos](designplans/domain-contracts.md). Todo backend necesario para la UI se aborda al inicio de fase 4 conforme a [seguimiento](task.md#entrada-prioritaria-para-fase-4--backend-requerido-por-ui).
- **Validación histórica:** [saneamiento Domain/Data](reporte_saneamiento_prefase4.md) registra QA aprobado y suites ejecutadas; la integración remota omitida en esa suite se revalidó por separado en [guía ELM327](emuladorEML327.md#revalidación-remota-de-pre-fase-4). La ejecución en API 36 no demuestra permiso runtime en API 37 ni compatibilidad con adaptador físico. No se afirma ejecución nueva ni QA global de Pre-Fase 4.

## Evidencia revisada

- `AppDatabase.kt` declara versión 1 y `exportSchema=true`; `app/build.gradle.kts` configura `room.schemaLocation`, minSdk 26 y compileSdk/targetSdk 37. Existe `app/schemas/cl.cristian.bitacoraobd.data.local.database.AppDatabase/1.json`; `git ls-files app/schemas` confirma que está versionado.
- Política prevista: conservar el esquema v1; ante cambios de esquema incrementar versión, definir migración explícita y probar datos representativos sin borrado destructivo. No se encontró constructor de base en producción; no se ha ejecutado ni verificado una migración.
- `MaintenanceDao` ofrece consulta e inserción, sin actualización. `RoomMaintenanceRepository.saveMaintenanceRecord` fuerza id=0, inserta y avanza odómetro en transacción. La edición DD-16 requiere una propuesta técnica separada para actualizar el mismo ID, conservar datos y recalcular alertas; cubrir fechas, kilometrajes y registro inexistente según DD-30. No se ha decidido su contrato.
- `AppPreferencesRepository` y `DataStoreAppPreferencesRepository` solo guardan ID activo e IP/puerto. Faltan preferencias DD-22/23/29; definir defaults y compatibilidad con datos previos y probarlos. La precisión y redondeo de entrada en millas y el momento de persistencia requieren acuerdo antes de implementar.
- `DeleteVehicleUseCase` bloquea únicamente estado Connected, escribe el reemplazo en DataStore antes de borrar en Room y no tiene transacción común entre ambos; si el borrado falla tras la escritura puede cambiar la selección sin borrar el vehículo. La selección y estados transitorios requieren coordinación compartida en Presentation, con reglas acordadas; no reparar preferencias desde un Flow de consulta.
- `GetDashboardDataUseCase` utiliza el primer vehículo como fallback sin persistirlo. Calcula `recurrent` para alertas pero entrega `analyzed` completo a `recurrentCodesInLastSession`; aclarar contrato antes de consumirlo. El documento `designplans/domain-contracts.md` ya identifica esta discrepancia.
- La referencia de pruebas existente en `domain-contracts.md` es histórica y contiene saldos de cobertura anteriores a la fase 2; contrastar con `reporte_fase2.md` y pruebas actuales antes de presentar una carencia como vigente.

## Decisiones abiertas registradas en screen-flows.md

1. Persistencia y errores en ajustes.
2. Precisión y redondeo al introducir millas.
3. Salida/desconexión durante una lectura OBD.
4. Destino al descartar y coordinación de escaneo, cambio de vehículo y desconexión con diagnóstico pendiente.
5. Guardar configuración IP/puerto frente a conectar o salir con cambios.
6. Permiso de red local: definir interacción de solicitud, denegación, revocación y recuperación; la documentación oficial fue consultada abajo, y las pruebas API 37/API 36 permanecen pendientes.

## Contratos y pruebas contrastados

- DD-12: ObdGateway expone ObdResult y consumidores deben manejar Success/Failure con mensaje; el cambio respecto al contexto sigue pendiente de ratificación individual, no se propone modificarlo en esta etapa.
- DD-15: enum existente de cuatro tipos; otros servicios del brief siguen pendientes de decisión, sin ampliarlos por inferencia.
- DD-17: DeleteVehicleUseCase comprueba Connected; Connecting/Disconnecting y coordinación con captura pendiente requieren guardias Presentation y definición de prioridad.
- DD-18: Dashboard entrega todos los códigos analizados al campo llamado recurrentCodesInLastSession; alertas filtran recurrentes. Aclarar semántica y prueba de código no recurrente antes de consumir o modificar contrato.
- DD-24: Vehicle usa year Int y campos no nulos, mientras la UX conserva campos opcionales; representación de ausencia numérica sin valor ficticio requiere decisión técnica/funcional y eventualmente plan aprobado.
- Búsqueda de constructores Room: solo se encontró Room.inMemoryDatabaseBuilder en AppDatabaseTest; no se encontró creación de base en producción. La composición real permanece pendiente de fase 4. No se ha comprobado migración.
- Consumidores encontrados: SaveMaintenanceUseCase llama saveMaintenanceRecord; DeleteVehicleUseCase llama setActiveVehicleId; no se encontró consumidor de recurrentCodesInLastSession en producción fuera del modelo. AppDatabaseTest cubre cascada, rollback, orden, aislamiento, últimos por tipo y kilometraje histórico; DataStoreAppPreferencesRepositoryTest cubre selección/configuración. Se inspeccionaron nombres y usos; resultados históricos no constituyen ejecución actual.

## Permiso de red local

[Android Developers — Local network permission](https://developer.android.com/privacy-and-security/local-network-permission), consultado 2026-11-08: en Android 17/API 37 para target 37 o superior el acceso LAN directo queda bloqueado por defecto y requiere declarar y solicitar ACCESS_LOCAL_NETWORK antes de acceder, salvo alternativas mediadas por sistema aplicables. Este proyecto usa socket TCP con IP configurada; la declaración ya existe en manifiesto y falta verificación/solicitud runtime. En API 36 la protección es opt-in de pruebas, no exigencia general; comprobar concesión, denegación, revocación y recuperación en API 37 y regresión API 36. El detalle UX de solicitud y ajustes permanece abierto.

## Pendientes de auditoría

Completar revisión de documentos, consumidores y aserciones; la raíz FigJam y metadatos Design ya se inspeccionaron en lectura, pero falta revisión visual e interactiva por variante. Se prepararon un handoff y plan técnico preliminares, pendientes de ampliar y validar. Producción aún no construye Room; falta verificar migración y ejecutar pruebas cuando corresponda. Ningún paquete se declara DESIGN_READY sin evidencia de aprobación visual. No se realizaron cambios de código ni de Figma.
