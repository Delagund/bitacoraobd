# Contratos Domain — línea base auditada

## Contrato vigente B-02 — Elegibilidad de alertas APROBADA

B-02 APROBADA (2026-11-12): conservar MaintenanceStatus; alertas derivadas en Domain: OTHER excluido; OVERDUE si fecha ≤ hoy o km actual ≥ límite; UPCOMING como alerta solo a 1–29 días calendario y sin límite vencido; a 30 días o más y km aún no alcanzado no hay alerta preventiva. Mantener programación visible aunque no genere alerta; excluir históricos no vigentes. Sin persistir alertas, cambiar Room/DataStore ni añadir polling.

Entradas: tipo, registro vigente, fecha local actual (Clock), kilometraje actual y límites opcionales. Salida derivada: OVERDUE, UPCOMING o ausencia de alerta. Los límites ausentes no participan; no crear otro estado MaintenanceStatus. GetMaintenanceStatusUseCase conserva su semántica. El ensamblado actual de GetDashboardDataUseCase convierte todo estado no UNSCHEDULED en alerta: comportamiento previo que debe adaptarse, no evidencia de B-02 implementada.

Pruebas: 31/30/29/1/0 días y fechas pasadas; km bajo/igual/superado; solo fecha/km; límites combinados; OTHER; sin límites; históricos no vigentes; reactivación y coherencia de consumidores. Esta sección prevalece sobre referencias históricas que equiparen programación UPCOMING y alerta.


## Cierre de composición 4.3 — 2026-11-10

La etapa 4.3 está cerrada con QA APROBADO. El reporte de cierre confirma `AppSession` como propietario de Presentation, composición compartida por proceso, builder Room con `MIGRATION_1_2`, coordinación OBD/vehículo con Mutex y recolección de Inicio al activarse. Room y DataStore no forman una transacción; el estado real se refresca tras fallo parcial. La captura pendiente se conserva solo durante la vida del proceso. Ver [reporte_fase4_etapa4_3.md](../reporte_fase4_etapa4_3.md).

Navigation Compose `2.10.2` fue autorizada, resuelta y compilada. El NavHost técnico contiene cuatro rutas principales, Ajustes/Historial/detalle y F03 anidado bajo Diagnóstico. Las pruebas conectadas de 4.3 pasaron en Pixel_10 AVD / Android 17 (API 37): 23 casos, 0 fallos, 1 omitido por falta de `elm327Host`. Las rutas aún muestran placeholders `Text`; no implementan pantallas de producto ni acreditan validación visual o DESIGN_READY funcional.

`MainActivity` obtiene ViewModels desde `BitacoraApplication`; la sesión y dependencias viven en Application. No se serializan sockets, Jobs ni captura pendiente, y no se promete recuperar una captura tras muerte del proceso. Estos puntos de composición, navegación y recreación están verificados según el reporte; la integración de producto por UI-01–07 queda para 4.5–4.8.

**DEP-011:** el identificador estable por captura, índice UNIQUE, migración aditiva v1→v2 y guardado idempotente se implementaron y probaron en 4.1. Se preservan los límites: no hay recuperación de captura tras muerte del proceso. Ver [reporte_fase4_etapa4_1.md](../reporte_fase4_etapa4_1.md).

## Paso 3 — contraste estático previo al cierre 4.3, 2026-11-10

Esta tabla conserva la inspección de código realizada antes del cierre de composición 4.3; describe aquella línea base, no los consumidores actuales de Presentation. En esa inspección aún no se habían integrado `AppSession`, ViewModels ni Navigation Compose. El estado vigente se resume arriba y se detalla en el [reporte de 4.3](../reporte_fase4_etapa4_3.md). Los contratos de Domain siguientes permanecen como referencia; las brechas de integración de cada UI se mantienen pendientes.

| UI / DEP | Soporte actual y consumidor | Contrato/coordinación pendiente y cierre |
|---|---|---|
| UI-02 / 001–003 | [VehicleRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/VehicleRepository.kt): observación Flow, `saveVehicle(Vehicle): Long` suspend; [preferencias](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/AppPreferencesRepository.kt): `setActiveVehicleId(Long?)` suspend, sin guardia de conexión. [DeleteVehicleUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/DeleteVehicleUseCase.kt) devuelve Deleted/Connected/NotFound/Failed, propaga cancelación y puede cambiar activo antes de fallo Room. | Presentation recibe ID creado, conserva contexto y reintenta solo selección; ante fallo de borrado consulta estado real. Responsable Android; cerrar con prueba de fallo parcial, guardias y cascada. Room y DataStore no comparten transacción. |
| UI-03 / 006–009 | [ObdConnectionConfig](../../app/src/main/java/cl/cristian/bitacoraobd/domain/obd/ObdConnectionConfig.kt) valida IPv4 y puerto 1..65535 al construir; defaults 192.168.0.10:35000. [ObdGateway](../../app/src/main/java/cl/cristian/bitacoraobd/domain/obd/ObdGateway.kt): StateFlow de conexión; connect/config y lecturas devuelven ObdResult, disconnect suspend sin resultado. | Presentation valida borrador antes de construir y conectar, gestiona permiso y estados; tras handshake persiste config y, si falla, reintenta solo persistencia con conexión real. Android implementa y prueba no apertura de socket en entrada inválida, cancelación, permiso y fallo de guardado; hardware y API pertinentes requieren validación. |
| UI-04/05 / 010–014 | [ScanDiagnosticCodesUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/ScanDiagnosticCodesUseCase.kt) recibe ID, exige Connected y devuelve Success o Failure; [SaveDiagnosticSessionUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveDiagnosticSessionUseCase.kt) devuelve Long? (null para Failure), sin convertir errores de persistencia. [DiagnosticRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/DiagnosticRepository.kt) observa sesiones y busca por ID nullable. | Presentation conserva captura íntegra y vehículo original hasta guardar o descartar; error de escritura conserva captura y reintento no relee. Android verifica éxito vacío, error y retorno; no se promete recuperación tras muerte de proceso. |
| UI-06 / 015–017 | [SaveMaintenanceUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveMaintenanceUseCase.kt) recibe record y confirmHistoricalMileage=false; devuelve Saved, Failed o RequiresHistoricalMileageConfirmation. Captura IllegalArgumentException en guardado; otras excepciones no se adaptan. [GetMaintenanceStatusUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetMaintenanceStatusUseCase.kt) usa fecha y km actuales, vehículo ausente devuelve lista vacía. | Android coordina errores, confirmación y edición del mismo ID; selector ofrece siete tipos alertables y OTHER. Cierre: prueba de validaciones, confirmación, edición y selector final; «Aceite» es ejemplo. |
| UI-07 / 018–019 | AppPreferencesRepository expone activeVehicleId, obdConnectionConfig y settings: Flow<AppSettings>; saveSettings guarda nombre nullable, unidades y visibilidad. DataStore mantiene las claves antiguas y aplica defaults por clave ausente: null, km, °C y tres métricas visibles. Errores/cancelación se propagan. | Presentation mantiene borrador hasta Guardar y conserva borrador ante fallo. DD-37 conversión/redondeo corresponde a Presentation; cambiar unidad no modifica kilometrajes. Pruebas DataStore y errores pasan; ver reporte 4.2. |
| UI-01 / 004–005, 023 | [GetDashboardDataUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetDashboardDataUseCase.kt) devuelve Flow de NoVehicles o Ready y usa primer vehículo si ID no coincide, sin persistir fallback; no emite por cambio de día. GetMaintenanceStatusUseCase toma fecha al evaluarse. | Al activar Inicio, Presentation reinicia/recolecta el Flow para reevaluar con fecha vigente; sin temporizador ni alertas persistidas. Probar cambio de fecha al reactivar en etapa UI. |
| Transversal / 020–022 | Los contratos actuales no describen navegación, borradores, errores UI ni continuidad de operaciones. | Presentation recibe eventos y expone estado observable; conservar contexto en recreación con proceso vivo, evitar operación duplicada y distinguir fallo de carga de lista vacía. Android define propietario y prueba recuperación; diseño/QA valida navegación, accesibilidad y prototipo. |

### Ampliación del contraste por operación — paso 3

La tabla superior cubre DEP-001–023 agrupadas por pantalla; los renglones siguientes precisan límites que afectan los casos de aceptación. «Propuesta» describe el comportamiento acordado y no una firma disponible.

| UI / DEP | Entrada → salida actual; error y consumidor | Brecha, efecto y condición de cierre |
|---|---|---|
| UI-02 / 001–003, 019 | `observeVehicles(): Flow<List<Vehicle>>`, `observeVehicleById(Long): Flow<Vehicle?>`, `getVehicleById(Long): Vehicle?`, `saveVehicle(Vehicle): Long`, `deleteVehicle(Long): Unit`. El repositorio Room inserta con ID 0 y actualiza un ID existente; una edición de ID inexistente falla. `setActiveVehicleId(Long?)` valida null o ID positivo y escribe DataStore por separado. `DeleteVehicleUseCase(Long)` bloquea solo Connected y puede cambiar DataStore antes de borrar en Room; otros fallos devuelven Failed, cancelación se propaga. Consumidor de la operación de borrado en Presentation: no encontrado. | **Implementación:** Android debe coordinar estados transitorios y captura pendiente, conservar ID tras alta para reintentar únicamente selección y consultar estado real tras fallo de borrado. Cierre: probar fallos entre operaciones y cascada, sin asumir atomicidad Room/DataStore. |
| UI-03 / 006–009 | `ObdConnectionConfig(String, Int)` rechaza IPv4/puerto inválidos antes del socket; si faltan valores DataStore usa 192.168.0.10 y 35000. `connect(config): ObdResult<Unit>`, `readLiveMetrics(): ObdResult<ObdLiveMetrics>`, `readDiagnosticCodes(): ObdResult<List<String>>`, `disconnect(): Unit` son suspend; `connectionState` es StateFlow. Gateway y socket concretos usan esa configuración, pero no solicitan permiso ni guardan preferencias. Una respuesta PID no interpretable se presenta como null; un fallo de comunicación devuelve Failure. | **Implementación:** Android debe validar borrador, comprobar permiso en cada intento y conservar conexión y borrador si falla guardar tras handshake; reintento exclusivo de DataStore. Cierre F03: probar entrada inválida sin apertura de socket, corregir/revalidar, cancelar sin guardar a Diagnóstico, fallo TCP/inicialización con reintento y fallo de persistencia sin reconectar. Diseño/QA debe comprobar interacciones y conectores; evidencia estructural previa no prueba ejecución. |
| UI-04/05 / 010–014 | `ScanDiagnosticCodesUseCase(Long): DiagnosticScanResult` devuelve Failure si falta vehículo o conexión, o Success con fecha, km local y códigos distintos. `SaveDiagnosticSessionUseCase(DiagnosticScanResult): Long?` devuelve null para Failure; Success invoca escritura, cuyas excepciones no traduce. Room v2 guarda captureId con índice UNIQUE; el repositorio resuelve reintento equivalente dentro de transacción y rechaza payload distinto para la misma clave. MIGRATION_1_2 añade UUID únicos preservando datos. AnalyzeDiagnosticHistoryUseCase consulta sesiones del mismo vehículo y catálogo. | **Estado actualizado:** idempotencia/migración implementadas; reintento, payload contradictorio, concurrencia, rollback y migración pasaron instrumentadas Pixel_10 AVD/API 37. Presentation conserva captura en memoria y reintenta sin escanear; no se promete recuperación tras muerte de proceso. Builder Room de producción y registro de migración quedan en 4.3. Ver reporte 4.1. |
| UI-06 / 015–017 | `observeMaintenanceByVehicle(Long): Flow<List<MaintenanceRecord>>`, `getMaintenanceById(Long): MaintenanceRecord?`, `saveMaintenanceRecord(MaintenanceRecord): Long` y `getLatestRecordsPerAlertableType(Long): List<MaintenanceRecord>`. `SaveMaintenanceUseCase(record, confirmHistoricalMileage=false)` devuelve Saved/Failed/RequiresHistoricalMileageConfirmation; captura IllegalArgumentException del guardado, no toda excepción. Room guarda registro y avance de km del alta en transacción; edición conserva ID y no avanza km. | **Implementación y visual:** Android maneja fallos restantes y confirmación; diseño debe representar selector completo de siete tipos alertables y OTHER. Cierre: verificar reglas de fechas/km, confirmación, edición e integridad transaccional y aprobar selector. |
| UI-01 / 004–005, 009, 023 | `GetDashboardDataUseCase(): Flow<DashboardState>` combina vehículos, activo, sesiones, mantenimiento y conexión; usa primer vehículo si selección ausente/inválida sin persistir fallback y no recibe reloj como fuente reactiva. `GetMaintenanceStatusUseCase(Long): List<MaintenanceStatusItem>` usa fecha del reloj al invocarse y retorna vacío si vehículo falta. No hay captura de métricas en DashboardSummary. | **Implementación:** Android distingue vacío de error, evita datos del vehículo anterior y comparte solo lectura explícita; reinvoca cálculo al activar Inicio con fecha actual. Cierre: probar sin vehículos también tras borrar último, cambio de vehículo, null/0 y vencimiento al día siguiente. |
| UI-07 y transversal / 018–022 | AppPreferencesRepository ahora expone AppSettings y guardado completo por DataStore (ver fila UI-07 anterior). Repositorios/casos de uso todavía no definen estados visuales de carga, guardias de navegación ni persistencia de borrador. Los Flow y operaciones suspend pueden fallar; Presentation debe representar el estado real. | **Pendiente de Presentation:** definir propietario de eventos/estado, borrador y mapeo de errores; conservar contexto en recreación con proceso vivo y evitar repetir operaciones. Coordinación de conexión, selección y borrado usa el Mutex compartido aprobado para 4.3. No se promete restaurar captura tras muerte de proceso. Probar estados, cancelación y recuperación y validar diseño/accesibilidad. |

**Lectura de cobertura:** DEP-001–023 se asignan a las filas anteriores y a [dependencias](screen-dependencies.md); DEP-019 se comparte entre formularios y Ajustes, DEP-020–022 son transversales. Backend/idempotencia 4.1 y preferencias 4.2 están implementados y verificados; Presentation, composición y pantallas siguen pendientes. Esta matriz no acredita DESIGN_READY ni validación visual. Los contratos propuestos requieren planificación técnica y aprobación antes de cambiar Kotlin.

**Propuesta funcional para Ajustes (DEP-018/019; no implementada):** entrada de carga sin parámetros de usuario; salida con nombre para mostrar opcional, unidades y tres opciones de visibilidad, o estado de carga/error recuperable. Ante ausencia de datos, nombre sin informar; su representación técnica exacta (null o cadena vacía) queda por definir, y se aplican km, °C y las tres métricas visibles. Entrada de guardado: borrador completo de esos valores; salida observable: confirmación de guardado o error. Ante error o resultado parcial incierto se conserva borrador y se consulta el estado real antes de reintentar; no se presupone atomicidad ni se afirma éxito completo. La conversión de distancia y su redondeo siguen DD-37; guardar sin editar una distancia no reescribe su valor interno. Responsable Android: definir firmas, tipo de resultado y persistencia en plan técnico, probar carga, ausencia, fallos y recuperación. La naturaleza y granularidad del fallo parcial dependen del diseño técnico aún no definido.

No se define una firma nueva ni una estrategia de persistencia para preferencias en este documento. La temperatura de admisión queda fuera del MVP y el odómetro ECU de DD-21 es futuro.


Auditoría documental: 2026-11-07. Implementado significa código inspeccionado, no ejecución validada en esta auditoría. PLANIFICADO conserva requisitos todavía no implementados; NO VERIFICADO identifica evidencia ausente; PENDIENTE DE DEFINICIÓN identifica decisiones abiertas.

## Fuentes y arquitectura

Fuentes: [contexto maestro](../Contexto_Maestro_App_OBDII_v2.md), [plan previo](../implementation_plan.md), [cierre fase 0](../reporte_fase0.md), [cierre fase 1](../reporte_fase1.md), [brief UX](../ux_fase1.md).
Las rutas Kotlin siguientes son relativas a `app/src/main/java/cl/cristian/bitacoraobd/`, desde la raíz del repositorio.

Un módulo `:app`; paquetes `domain/model`, `domain/repository`, `domain/obd`, `domain/usecase`, `data/local`, `data/repository` y `ui/theme`. Domain usa Kotlin, java.time y Flow/StateFlow sin Room, Compose ni SDK Android. Dependencias previstas: Presentation → Domain ← Data. Los repositorios y casos de uso reciben dependencias por constructor. AppContainer, ViewModels, UiState de Presentation y conexión con Compose: PLANIFICADO. MainActivity solo contiene el saludo de plantilla.

## Modelos existentes

Cada fila corresponde al archivo homónimo en `domain/model/`, salvo indicación.

| Modelo | Propiedades y reglas verificadas |
|---|---|
| Vehicle | id Long (0 nuevo), brand, model, year Int, fuelType, engineDescription, currentMileageKm Int. id y kilometraje ≥ 0. No valida textos vacíos ni rango de año. |
| DiagnosticSession | id ≥ 0, vehicleId > 0, timestampMillis Long, mileageAtScanKm ≥ 0, codes List<String> (vacía válida). No tiene estado de fallo. |
| MaintenanceRecord | id ≥ 0, vehicleId > 0, serviceType MaintenanceType, performedDate LocalDate, mileageAtServiceKm ≥ 0, notes (vacío por defecto), nextDueDate LocalDate?, nextDueMileageKm Int? (si existe ≥ 0). No impone orden entre fechas ni entre kilometrajes realizado/próximo. |
| MaintenanceType | OIL_AND_FILTER, OIL_ONLY, FILTER_ONLY, BRAKES, TIRES, BATTERY y COOLANT con supportsAutoAlerts=true; OTHER=false. Tipos independientes. |
| DiagnosticScanResult | Success(vehicleId > 0, timestampMillis, mileageAtScanKm ≥ 0, codes); Failure(message). Temporal, sin tabla. |
| DtcGuidance | code, description, relatedSystem, possibleCauses, orientativeActions, isKnownInCatalog. |
| DiagnosedDtc | code, guidance, isRecurrent. |
| MaintenanceStatus / MaintenanceStatusItem | UNSCHEDULED, UPCOMING, OVERDUE; item asocia record y status. |
| VehicleAlert | RecurrentDtcAlert permanece declarado pero Dashboard ya no lo emite; MaintenanceAlert(serviceType, status, dueDate, dueMileageKm), clave MAINT_tipo, se calcula sin tabla. |
| DashboardState | NoVehicles o Ready(summary). Es Domain, no UiState de Presentation; no define carga/error. |
| DashboardSummary | activeVehicle, connectionState, lastDiagnosticSession?, codesInLastSession (todos los códigos analizados de la última sesión), activeMaintenanceSchedules, alerts. |

Relaciones: Vehicle 1:N DiagnosticSession y MaintenanceRecord; sesión 1:N códigos. El código persistido pertenece al vehículo mediante su sesión. No existen usuario, perfiles, tabla de alertas ni catálogo persistido.

## Contratos OBD existentes

Archivos `domain/obd/*.kt`.

- ObdConnectionConfig: IPv4 de cuatro segmentos numéricos 0..255; puerto 1..65535. Valores iniciales 192.168.0.10:35000, editables, sin garantía de corresponder al equipo.
- ObdConnectionState: Disconnected, Connecting, Connected(config), Disconnecting, Error(message).
- ObdLiveMetrics: rpm Int?, coolantTempCelsius Int?, speedKmh Int?, timestampMillis Long; null significa no disponible, distinto de cero.
- ObdResult<T>: Success(value) o Failure(message), sin Throwable expuesto.
- ObdGateway: connectionState StateFlow; suspend connect(config): ObdResult<Unit>, disconnect(): Unit, readLiveMetrics(): ObdResult<ObdLiveMetrics>, readDiagnosticCodes(): ObdResult<List<String>>.
- Implementados Elm327ObdGateway, Elm327SocketClient, ObdResponseParser y ObdPidInterpreter; MainActivity aún no los integra. El manifiesto declara INTERNET y ACCESS_LOCAL_NETWORK; compileSdk/targetSdk=37. En app/src/main no se encontró solicitud/comprobación del permiso runtime de red local. Validación física del hardware y flujo de permisos pendientes; ver H-12.

## Repositorios y operaciones reales

Archivos `domain/repository/*.kt`; implementaciones en `data/repository/` y `data/local/datastore/`.

| Contrato | Operaciones |
|---|---|
| VehicleRepository | observeVehicles(): Flow<List<Vehicle>>, observeVehicleById(id): Flow<Vehicle?>, getVehicleById(id): Vehicle?, saveVehicle(vehicle): Long, deleteVehicle(id). Las últimas tres suspend. |
| AppPreferencesRepository | activeVehicleId: Flow<Long?>, obdConnectionConfig: Flow<ObdConnectionConfig>; suspend setActiveVehicleId(Long?), saveObdConnectionConfig(config). No bloquea selección durante conexión. |
| DiagnosticRepository | observeSessionsByVehicle(id): Flow<List<DiagnosticSession>>; suspend getLatestSession(id), getSessionById(id) (nullable); saveDiagnosticSessionAndAdvanceMileage(session): Long; getSuccessfulCodesBefore(vehicleId, timestampMillis, sessionId): Set<String>; getSuccessfulCodeOccurrenceCounts(vehicleId): Map<String,Int>; getDtcGuidance(code): DtcGuidance (no suspend). |
| MaintenanceRepository | observeMaintenanceByVehicle(id): Flow<List<MaintenanceRecord>>; suspend getMaintenanceById(id): MaintenanceRecord?, saveMaintenanceRecord(record): Long, getLatestRecordsPerAlertableType(id): List<MaintenanceRecord>. La misma operación inserta con id=0 o actualiza ID existente del mismo vehículo. |

RoomVehicleRepository inserta id=0; para actualizar comprueba existencia, usa UPDATE y devuelve ID existente. Admite corrección manual descendente no negativa. DataStore conserva active_vehicle_id, obd_ip, obd_port; ID null elimina preferencia, ID no null debe ser positivo. No valida existencia ni conexión; errores de almacenamiento no se transforman en estados UI.

## Casos de uso y límites

Archivos `domain/usecase/*.kt`.

| Caso de uso | Comportamiento inspeccionado |
|---|---|
| ScanDiagnosticCodesUseCase(vehicleId) | Obtiene vehículo; si falta Failure. Exige gateway.connectionState Connected antes de leer; deduplica códigos, timestamp tras respuesta y kilometraje local leído antes. No persiste. La captura no es lectura de odómetro ECU. |
| SaveDiagnosticSessionUseCase(result): Long? | Failure devuelve null sin escribir. Success conserva captura y delega transacción; existencia del vehículo se verifica en repositorio Room. No administra confirmación ni resultado pendiente UI. |
| AnalyzeDiagnosticHistoryUseCase(session) | Códigos distintos + catálogo + recurrencia por antecedentes del mismo vehículo. No verifica por sí mismo que la sesión recibida esté guardada. |
| SaveMaintenanceUseCase(record, confirmHistoricalMileage=false) | Valida fecha realizada no futura, próxima fecha no anterior y próximo kilometraje no inferior al realizado; vehículo inexistente → Failed; kilometraje inferior no confirmado → RequiresHistoricalMileageConfirmation(enteredMileageKm,currentMileageKm), sin escritura. Guardado → Saved(recordId). Convierte IllegalArgumentException en Failed; otros fallos no tienen adaptación general a UI. |
| GetMaintenanceStatusUseCase(vehicleId) | Usa Clock, fecha calendario y kilometraje actual; vehículo inexistente → lista vacía. Límite fecha O kilometraje alcanzado → OVERDUE; ninguno configurado → UNSCHEDULED; resto UPCOMING. Selección de último tipo alertable delegada al repositorio. |
| GetDashboardDataUseCase(): Flow<DashboardState> | Combina vehículos y preferencias; selecciona ID válido o primer vehículo disponible. Observa historial, mantenimiento y conexión con flatMapLatest. NoVehicles si vacío. Expone todos los códigos analizados de la última sesión con isRecurrent; alerts contiene mantenimiento programado, deduplicado por conditionKey. Fallback no repara DataStore. |
| DeleteVehicleUseCase(vehicleId) | Connected bloquea cualquier borrado; inexistente → NotFound. Si era el activo persistido, escribe primero el ID del primer restante o null en preferencias y luego borra en Room; ante fallo posterior el activo podría haber cambiado. Resultado Deleted/Connected/NotFound/Failed definido en el mismo archivo; propaga CancellationException. Room y DataStore no son una transacción común. |

La fecha actual se recalcula cuando se ejecuta GetMaintenanceStatusUseCase: no hay temporizador que provoque una emisión de Dashboard al cambiar el día sin otro cambio observado.

## Persistencia verificada

`data/local/entity/Entities.kt`: cuatro tablas vehicles, diagnostic_sessions, diagnostic_codes, maintenance_records. Columnas camelCase. FK CASCADE vehículo→sesiones→códigos y vehículo→mantenimientos. PK códigos (sessionId, code). Índices: sesiones(vehicleId), códigos(sessionId), mantenimiento(vehicleId) y (vehicleId,serviceType,performedDateEpochDay,id).

`EntityMappers.kt` transforma LocalDate con toEpochDay/ofEpochDay. `AppDatabase.kt`: versión 1, exportSchema=true; DatabaseConverters convierte MaintenanceType a/desde nombre. `app/build.gradle.kts` configura exportación en app/schemas. Política de migraciones explícitas figura en plan aprobado; migración futura ejecutada NO VERIFICADA.

Repositorios Room guardan diagnóstico/códigos/avance de odómetro o mantenimiento dentro de withTransaction. Diagnóstico inserta con id=0. Mantenimiento inserta con id=0 y avanza odómetro; con id existente actualiza el mismo registro y no modifica odómetro, comprobando que pertenezca al mismo vehículo. DAO solo avanza kilometraje cuando aumenta. Sesión vacía es admisible. Historial diagnóstico ordenado timestamp DESC, id DESC; antecedentes con timestamp menor o igualdad e ID menor; conteo COUNT(DISTINCT sessionId). Mantenimiento vigente por fecha e ID descendentes, por tipo, excluyendo OTHER.

DtcCatalog contiene únicamente P0300 y P0420. Fallback conserva código, sistema no identificado, causas vacías y consulta a técnico. No es diagnóstico definitivo.

## Actualización de trazabilidad — revisión de aserciones

La revisión de `AppDatabaseTest` confirma que `diagnosticQueriesKeepSessionsCodesOrderingAndVehicleIsolation` comprueba sesiones vacías, desempates, aislamiento y conteo por sesiones; `maintenanceQueriesChooseLatestPerTypeByDateThenIdAndExcludeOther` comprueba la selección por fecha/ID. Las afirmaciones contrarias en la matriz y H-11 de abajo son históricas y quedan superadas para esos casos. No se ejecutaron pruebas en esta revisión.

La prueba de cascada ahora consulta directamente `diagnostic_codes` después del borrado y comprueba cero filas, además de la FK CASCADE y eliminación del vehículo, sesión y mantenimiento. La validación en dispositivo y su alcance se registran en [reporte de saneamiento](../reporte_saneamiento_prefase4.md).

El usuario confirmó conservar `ObdResult`. El resumen expone todos los DTC de la última sesión y los tipos nuevos admiten programación opcional. No se clasifica gravedad. El enlace visual y coordinación de captura pendiente corresponden a Presentation en fase 4.

## Matriz funcional y pruebas inspeccionadas

Rutas de tests relativas a `app/src/test/java/cl/cristian/bitacoraobd/`; instrumentadas a `app/src/androidTest/java/cl/cristian/bitacoraobd/`.

| Requisito | Contrato / pantalla | Evidencia de pruebas y pendiente |
|---|---|---|
| RF-01 vehículos | VehicleRepository, preferencias, DeleteVehicleUseCase / UI-02 | MaintenanceDashboardDeleteUseCasesTest: borrar activo, bloqueo Connected, selección obsoleta. AppDatabaseTest: update conserva sesión y cascada. Selector UI pendiente. |
| RF-02 OBD | ObdGateway / UI-03 | ObdContractsTest: IPv4, puertos, null vs cero, éxito limpio vs fallo. Transporte implementado; validación de ejecución/hardware y permiso runtime pendientes. |
| RF-03 diagnóstico | Scan/Save/Analyze / UI-04, UI-05 | DiagnosticAndMaintenanceUseCasesTest: éxito vacío, fallo, no guardar fallo, vehículo original, recurrencia con fake. SQL temporal y empates no probados por esos fakes. |
| RF-04 mantenimiento | SaveMaintenance/GetMaintenanceStatus / UI-06 | Tests cubren límites, edición y desempates SQL; evidencia de ejecución en reporte de saneamiento. UI pendiente. |
| RF-05 Dashboard | GetDashboardData / UI-01 | Pruebas de códigos nuevos y recurrentes; `codesInLastSession` incluye ambos y las alertas derivadas son de mantenimiento. UI pendiente. |
| RF-06 persistencia | Room/DataStore / transversal | AppDatabaseTest: cascada, conservación de hijos al actualizar, rollback mediante trigger. DataStoreAppPreferencesRepositoryTest: defaults y lectura tras escritura en misma instancia; no prueba reinicio de proceso. |

DomainModelsTest cubre captura limpia, negativos, fechas, tipos y estados; VehicleAlertTest verifica claves. La prueba instrumentada de cascada consulta directamente códigos huérfanos. No se encontraron tests funcionales Compose ni navegación. El reporte fase 1 declara 27 JVM y 5 instrumentadas aprobadas, lint/assemble correctos: evidencia histórica documental, NO VERIFICADO en esta auditoría (no ejecutada suite).

## Hallazgos y decisiones pendientes

| ID | Diferencia verificable / implicación |
|---|---|
| H-01 | Contexto §8 usa Result; código usa ObdResult. Reporte fase 0 registra el resultado tipado. Consumidores futuros deben usar contrato real. |
| H-02 | Contexto §9 llama saveDiagnosticSession; código usa saveDiagnosticSessionAndAdvanceMileage. Transacción explícita; no son dos operaciones disponibles. |
| H-03 | Resuelto en Domain: frenos, neumáticos, batería y refrigerante añadidos; OTHER usa notes y no genera alertas. |
| H-04 | Resuelto en Domain: codesInLastSession expone todos los analizados con recurrencia; no se asigna gravedad. Falta línea y navegación en UI. |
| H-05 | Edición del mismo ID y validaciones DD-30 implementadas; edición no modifica odómetro. Falta formulario UI. |
| H-06 | ScanDiagnosticCodesUseCase ya exige Connected y existe gateway ELM327 concreto. Bloqueo general de cambio de vehículo aún no está implementado en preferencias/Presentation; borrado solo comprueba Connected. Apertura, salidas transitorias y selección aprobadas en DD-20/DD-26/DD-28; faltan guardias y coordinación con diagnóstico pendiente. |
| H-07 | DashboardSummary no contiene ObdLiveMetrics. DD-29 aprueba captura compartida en Presentation, resumen en Inicio e interruptores de visibilidad en Ajustes sin ampliar ese modelo; integración y preferencias pendientes de implementación. |
| H-08 | task.md marca prototipo construido/validado; reporte fase 1 y cierre del propio checklist lo dejan pendiente. FigJam fue inspeccionado y corregido después en esta conversación; no certifica aquel cierre ni aprobación visual. Referencias remotas actualizadas en figma-references.md. |
| H-09 | Plan usa DUE una vez; enum/contexto usan OVERDUE. Usar OVERDUE, sin renombrar código. |
| H-10 | Contexto especifica columnas snake_case e índices diferentes; Entities usa camelCase e índices descritos arriba. No se da por implementado el esquema conceptual literal ni se altera el aprobado. |
| H-11 | SQL de recurrencia y sesiones vacías tienen pruebas; consultar reporte de saneamiento. Reinicio de proceso DataStore no probado. |
| H-12 | ACCESS_LOCAL_NETWORK está declarado y targetSdk=37, pero no se encontró solicitud/comprobación runtime en producción. Para conexión TCP local en Android 17/API 37 falta especificar e implementar concesión/denegación/revocación. Fuente: [Android — Local network permission](https://developer.android.com/privacy-and-security/local-network-permission). |

Implementación pendiente: guardias OBD, ViewModels/UiState/StateFlow para conservación DD-32, métricas/preferencias DD-29 y composición manual de dependencias. DD-24: año 0 representa ausencia por decisión del usuario; Presentation deberá mostrarlo vacío o como no informado. No añadir rangos u obligatoriedades por esta auditoría. Decisiones de interacción todavía abiertas consolidadas en [screen-flows](screen-flows.md#saldo-de-la-revisión-final-de-flujos); no confundir acuerdos aprobados con implementación terminada.
