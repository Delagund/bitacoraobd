# CONTEXTO MAESTRO DEL PROYECTO
## Aplicación Android de diagnóstico OBD-II y gestión de mantenimiento vehicular

**Versión del contexto:** 2.0  
**Modalidad:** Prototipo académico acelerado, plazo aproximado de una semana  
**Estrategia:** Offline-first, persistencia local y diseño UX/UI en paralelo con backend  
**Plataforma:** Android nativo  
**Uso previsto:** Contexto para agentes de IA de desarrollo (Gemini, Codex u otros)

---

# 1. Rol y directrices para el agente

Actúa como ingeniero de software sénior especializado en Android nativo, Kotlin, Jetpack Compose, arquitectura móvil, Room, DataStore y comunicación OBD-II/ELM327.

Acompaña el diseño y la implementación con criterio de ingeniería. Prioriza simplicidad, mantenibilidad, confiabilidad e integridad de datos, respetando el alcance acordado.

## Principios de trabajo

- Aplica KISS y YAGNI sin sacrificar buenas prácticas fundamentales.
- Considera este documento como fuente de continuidad de las decisiones tomadas.
- No rediseñes el sistema ni cambies acuerdos previos silenciosamente.
- No agregues dependencias, capas, interfaces, tablas ni patrones sin una necesidad concreta.
- Explica los problemas detectados: qué está mal, por qué, qué principio afecta, cómo corregirlo y qué impacto tiene.
- Diferencia decisiones cerradas, detalles pendientes y recomendaciones nuevas.
- Inspecciona primero el proyecto y sus archivos de configuración antes de modificar versiones, dependencias o estructura.
- No inventes compatibilidad de un adaptador OBD, PID soportado ni respuesta de ECU.
- Prioriza pruebas pequeñas que validen reglas importantes antes de integrar todas las capas.

---

# 2. Objetivo y alcance

## 2.1 Propósito

Desarrollar una aplicación Android para conductores particulares que permita:

- Conectar con un adaptador OBD-II ELM327 mediante Wi-Fi/TCP.
- Consultar parámetros básicos del vehículo: RPM, temperatura del refrigerante y velocidad.
- Leer códigos DTC almacenados mediante el Modo 03.
- Interpretar los códigos mediante un catálogo local estático y orientación no taxativa.
- Mantener un historial de escaneos exitosos.
- Registrar intervenciones de mantenimiento y programar las próximas por fecha y/o kilometraje.
- Mostrar un Dashboard con el vehículo activo, conexión, diagnóstico y mantenimiento.

La aplicación traduce información técnica a un lenguaje comprensible. No es una herramienta de diagnóstico mecánico definitivo y nunca debe afirmar que un DTC confirma por sí solo la avería de una pieza.

## 2.2 Modelo de usuario y vehículos

Decisiones cerradas:

- Una instalación representa un único usuario local implícito.
- El usuario puede registrar varios vehículos.
- Solo puede existir un vehículo activo a la vez.
- Cada diagnóstico, código DTC y mantenimiento pertenece a un vehículo mediante `vehicleId`.
- El identificador del vehículo activo se conserva en DataStore Preferences.
- No hay autenticación, cuentas, perfiles, mecánicos, roles ni múltiples usuarios.

No crear `UserProfile`, `Driver`, `Role` ni tabla de usuarios.

## 2.3 Exclusiones del MVP

No implementar:

- Autenticación, perfiles o roles.
- Firebase, nube o sincronización remota.
- ABS, airbags, transmisión u otros sistemas propietarios/extendidos.
- Modo 04 para borrar DTC ni escritura/reprogramación de ECU.
- Telemetría continua o servicio de escaneo en segundo plano.
- IA generativa para interpretar códigos.
- Catálogo exhaustivo de piezas o sistemas mecánicos.
- Fotografías, adjuntos, exportación PDF/XLSX.
- Notificaciones push o automatizaciones avanzadas.

---

# 3. Stack tecnológico y restricciones

| Área | Decisión |
|---|---|
| Plataforma | Android nativo |
| Lenguaje | Kotlin |
| UI | Jetpack Compose |
| Presentación | MVVM simplificada + UDF |
| Persistencia relacional | Room / SQLite |
| Preferencias | DataStore Preferences |
| Concurrencia | Kotlin Coroutines |
| Reactividad | Flow / StateFlow |
| OBD | Wi-Fi / TCP con ELM327 |
| Pruebas | JUnit, pruebas de Room en memoria y simulador ELM327 |
| DI | Manual mediante `AppContainer` |
| Gradle | Un solo módulo `:app`, organizado por paquetes |

No incorporar Hilt, Dagger, módulos Gradle adicionales ni servicios en segundo plano para telemetría.

**Versiones Android/Gradle:** se buscarán versiones actuales y compatibles al inspeccionar/configurar el proyecto. La selección final de Kotlin, AGP, Gradle, Compose BOM, Room, DataStore, KSP, JDK, `minSdk`, `targetSdk` y `compileSdk` queda sujeta a verificación y confirmación posterior.

---

# 4. Arquitectura

Se adopta una arquitectura limpia simplificada de tres capas:

`Presentation → Domain ← Data`

## 4.1 Presentation (`ui/`)

Responsabilidades:

- Renderizar interfaces Compose.
- Capturar interacciones del usuario.
- Enviar eventos a ViewModels.
- Observar estados inmutables con `StateFlow<UiState>`.
- Representar carga, éxito, error, estado vacío y confirmaciones.

Pantallas principales:

- `DashboardScreen`
- `VehicleManagementScreen`
- `ObdConnectionScreen`
- `DiagnosticScreen`
- `MaintenanceScreen`

UI y ViewModels no conocen comandos AT, SQL, tramas hexadecimales ni detalles de sockets.

## 4.2 Domain (`domain/`)

Contiene:

- Modelos de negocio persistibles.
- Modelos efímeros y derivados.
- Interfaces de repositorios.
- Contrato `ObdGateway`.
- Casos de uso con reglas reales.

Debe mantenerse independiente de Room, Compose, sockets, SDK Android y excepciones de infraestructura (`Throwable`).

## 4.3 Data (`data/`)

Contiene:

- Implementaciones de repositorios.
- Room: entidades, DAOs, base de datos, relaciones y transacciones.
- DataStore.
- Cliente TCP ELM327.
- Parser de protocolo.
- Intérprete de PIDs.
- Catálogo DTC estático.
- Simulador OBD.
- Mappers entre dominio y persistencia/protocolo.

## 4.4 Organización de paquetes

```text
app/src/main/java/com/example/vehicleapp/
  ui/
    dashboard/
    vehicle/
    scanner/
    diagnostics/
    maintenance/
    navigation/
    components/
  domain/
    model/
    repository/
    obd/
    usecase/
  data/
    local/
      database/
      dao/
      entity/
      datastore/
    repository/
    obd/
      connection/
      protocol/
      parser/
      simulator/
  di/
```

Puede ajustarse la organización interna por razones concretas, conservando responsabilidades y el módulo único.

---

# 5. Requisitos funcionales y no funcionales

## 5.1 Requisitos funcionales

### RF-01. Gestión de vehículos

- Crear, consultar, editar y eliminar varios vehículos.
- Datos: marca, modelo, año, combustible, descripción del motor y kilometraje actual.
- Seleccionar un vehículo activo.
- Recuperar el vehículo activo tras reiniciar la aplicación, si sigue existiendo.
- Permitir modificar manualmente el kilometraje como parte de la edición normal del vehículo. No crear una operación o flujo independiente solo para cambiarlo.
- Mantener separados los datos de cada vehículo.
- Eliminar en cascada sus diagnósticos, códigos y mantenimientos.

### RF-02. Comunicación OBD-II

- Configurar IP y puerto TCP.
- Conectar y desconectar un adaptador ELM327 Wi-Fi.
- Inicializar el adaptador mediante comandos AT.
- Exponer estado de conexión.
- Consultar RPM, temperatura de refrigerante y velocidad.
- Diferenciar PID no soportado/no disponible (`null`) de un valor numérico cero.
- No ejecutar sondeo continuo. Las lecturas se activan por acción explícita del usuario.

### RF-03. Diagnóstico e historial

- Consultar DTC almacenados mediante Modo 03.
- Mostrar código, descripción, sistema, posibles causas y acciones orientativas cuando estén disponibles en el catálogo.
- Un escaneo exitoso sin códigos es un resultado válido y puede guardarse como diagnóstico limpio.
- Un escaneo fallido solo genera un resultado temporal de error: no se persiste y no modifica ningún dato.
- Los resultados exitosos quedan pendientes hasta que el usuario elija guardarlos o descartarlos.
- Guardar vehículo, fecha/hora y kilometraje de la captura.
- Consultar historial y detectar recurrencia.

### RF-04. Mantenimiento

- Registrar aceite, filtro u otros servicios.
- Registrar fecha calendario, kilometraje y notas.
- Programar próxima intervención por fecha, kilometraje o ambos.
- Consultar y editar historial sin sobrescribir intervenciones anteriores.

### RF-05. Alertas y Dashboard

- Mostrar mantenimiento `UPCOMING` y `OVERDUE`.
- Mostrar DTC recurrentes de acuerdo con la regla de alerta activa definida más adelante.
- Deduplicar alertas por condición.
- Presentar vehículo activo, kilometraje, estado de conexión, último diagnóstico y mantenimiento.
- Si no existen vehículos, mostrar un estado vacío con el mensaje equivalente a “Agrega tu primer vehículo para comenzar” y una acción para agregarlo.

### RF-06. Persistencia offline

- Guardar y recuperar vehículos, diagnósticos exitosos, códigos y mantenimientos localmente.
- Mantener integridad referencial.
- Funcionar sin Internet.

## 5.2 Requisitos no funcionales

- Lenguaje claro para conductores no especializados.
- Objetivo de carga del Dashboard: menos de 2 segundos con datos locales.
- Objetivo de consultas locales: menos de 1 segundo.
- No bloquear el hilo principal con operaciones de red.
- Mantener coherencia histórica y relaciones.
- Gestionar desconexiones y errores sin corromper ni perder registros ya guardados.
- Separar UI, lógica, persistencia y OBD.
- Permitir sustituir simulador por adaptador físico sin modificar Domain ni UI.

---

# 6. Modelos de dominio

Todos los modelos de Domain son Kotlin puro, sin anotaciones Room.

## 6.1 `Vehicle`

```kotlin
data class Vehicle(
    val id: Long = 0L,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: String,
    val engineDescription: String,
    val currentMileageKm: Int
)
```

Reglas:

- `currentMileageKm` es el kilometraje actual conocido.
- Nunca se actualiza desde el OBD-II.
- El usuario lo puede corregir mediante la edición normal del vehículo.
- Un registro de diagnóstico o mantenimiento con kilometraje superior puede avanzar el valor actual según las reglas de guardado.
- Un registro histórico inferior nunca reduce automáticamente el kilometraje actual.
- No se permiten kilometrajes negativos.

## 6.2 `DiagnosticSession`

La entidad histórica representa exclusivamente un escaneo exitoso que el usuario decidió guardar.

```kotlin
data class DiagnosticSession(
    val id: Long = 0L,
    val vehicleId: Long,
    val timestampMillis: Long,
    val mileageAtScanKm: Int,
    val codes: List<String> = emptyList()
)
```

Reglas:

- No existe `DiagnosticSessionStatus` en la entidad persistible.
- No se guardan sesiones fallidas.
- Una sesión exitosa con `codes = emptyList()` es válida.
- Fecha/hora y kilometraje son una fotografía histórica de cuando se ejecutó el escaneo, no cuando se confirma posteriormente el guardado.
- Las sesiones históricas no se reemplazan accidentalmente.

## 6.3 `MaintenanceType`

```kotlin
enum class MaintenanceType(val supportsAutoAlerts: Boolean) {
    OIL_AND_FILTER(true),
    OIL_ONLY(true),
    FILTER_ONLY(true),
    OTHER(false)
}
```

`OIL_AND_FILTER` es un tipo independiente. No sustituye automáticamente los registros vigentes `OIL_ONLY` ni `FILTER_ONLY`. `OTHER` no genera alertas automáticas en el MVP.

## 6.4 `MaintenanceRecord`

Las fechas de mantenimiento representan fechas calendario sin hora.

```kotlin
data class MaintenanceRecord(
    val id: Long = 0L,
    val vehicleId: Long,
    val serviceType: MaintenanceType,
    val performedDate: LocalDate,
    val mileageAtServiceKm: Int,
    val notes: String = "",
    val nextDueDate: LocalDate? = null,
    val nextDueMileageKm: Int? = null
)
```

Reglas:

- El mantenimiento realizado es un hecho histórico.
- La próxima intervención puede estar programada por fecha, kilometraje, ambos o ninguno.
- Si ambos límites están definidos, alcanzar cualquiera determina vencimiento.
- Una nueva intervención del mismo tipo reemplaza la programación vigente para efectos de alertas, sin modificar registros anteriores.
- Para fechas se utiliza `LocalDate`, no un instante con hora.
- La representación en Room se define en la sección de persistencia.

**Compatibilidad pendiente:** verificar `minSdk` y disponibilidad de `java.time.LocalDate`; si corresponde, configurar desugaring o adoptar una representación compatible sin cambiar la semántica de fecha calendario.

---

# 7. Modelos derivados y temporales

## 7.1 Orientación DTC

```kotlin
data class DtcGuidance(
    val code: String,
    val description: String,
    val relatedSystem: String,
    val possibleCauses: List<String>,
    val orientativeActions: List<String>,
    val isKnownInCatalog: Boolean
)
```

Reglas:

- El catálogo local es estático y no se persiste en Room.
- Si un código es desconocido, se conserva el código original.
- El fallback utiliza descripción y orientación genéricas.
- No atribuir causas específicas no conocidas ni inventar un diagnóstico.
- Nunca afirmar que el DTC confirma una pieza defectuosa.

## 7.2 Resultado temporal del escaneo

Distinguir resultado de comunicación y entidad histórica.

```kotlin
sealed interface DiagnosticScanResult {
    data class Success(
        val vehicleId: Long,
        val timestampMillis: Long,
        val mileageAtScanKm: Int,
        val codes: List<String>
    ) : DiagnosticScanResult

    data class Failure(
        val message: String
    ) : DiagnosticScanResult
}
```

Reglas:

- `Success` queda temporalmente pendiente de decisión del usuario.
- `Failure` solo se muestra en UI; no se persiste ni actualiza datos.
- Un `Success` con lista vacía es un diagnóstico limpio válido.
- El `vehicleId`, timestamp y kilometraje se capturan al realizar el escaneo. No deben recalcularse al guardar más tarde.
- El resultado pendiente vive en memoria. Recuperarlo tras cierre forzado de la aplicación queda fuera del MVP; la documentación de Diagnóstico debe advertir que una captura no guardada se perderá si el proceso termina.

## 7.3 DTC interpretado

```kotlin
data class DiagnosedDtc(
    val code: String,
    val guidance: DtcGuidance,
    val isRecurrent: Boolean
)
```

## 7.4 Estado de mantenimiento

```kotlin
enum class MaintenanceStatus {
    UNSCHEDULED,
    UPCOMING,
    OVERDUE
}

data class MaintenanceStatusItem(
    val record: MaintenanceRecord,
    val status: MaintenanceStatus
)
```

- Sin fecha y sin kilometraje programado: `UNSCHEDULED`.
- Algún límite alcanzado o superado: `OVERDUE`.
- Existe programación y ningún límite se ha alcanzado: `UPCOMING`.

## 7.5 Alertas

```kotlin
sealed interface VehicleAlert {
    val conditionKey: String

    data class RecurrentDtcAlert(
        val code: String,
        val description: String,
        val occurrencesCount: Int
    ) : VehicleAlert {
        override val conditionKey = "DTC_RECURRENT_$code"
    }

    data class MaintenanceAlert(
        val serviceType: MaintenanceType,
        val status: MaintenanceStatus,
        val dueDate: LocalDate?,
        val dueMileageKm: Int?
    ) : VehicleAlert {
        override val conditionKey = "MAINT_${serviceType.name}"
    }
}
```

Alertas calculadas en memoria, deduplicadas por `conditionKey`; no crear tabla Room.

## 7.6 Dashboard

```kotlin
data class DashboardSummary(
    val activeVehicle: Vehicle,
    val connectionState: ObdConnectionState,
    val lastDiagnosticSession: DiagnosticSession?,
    val recurrentCodesInLastSession: List<DiagnosedDtc>,
    val activeMaintenanceSchedules: List<MaintenanceStatusItem>,
    val alerts: List<VehicleAlert>
)

sealed interface DashboardState {
    data object NoVehicles : DashboardState
    data class Ready(val summary: DashboardSummary) : DashboardState
}
```

`NoVehicles` es un estado vacío de la propia pantalla Dashboard, no una nueva arquitectura ni una pantalla funcional separada. Muestra una invitación a agregar el primer vehículo.

El Dashboard debe ser reactivo mediante `Flow`, reaccionando a cambios de vehículos, preferencias, historial y conexión OBD.

---

# 8. Modelos y contrato OBD en Domain

## 8.1 Configuración

```kotlin
data class ObdConnectionConfig(
    val ipAddress: String = DEFAULT_IP,
    val port: Int = DEFAULT_PORT
) {
    companion object {
        const val DEFAULT_IP = "192.168.0.10"
        const val DEFAULT_PORT = 35000
    }
}
```

Son valores iniciales editables, no una dirección garantizada para todo adaptador.

## 8.2 Estado de conexión

```kotlin
sealed interface ObdConnectionState {
    data object Disconnected : ObdConnectionState
    data object Connecting : ObdConnectionState
    data class Connected(val config: ObdConnectionConfig) : ObdConnectionState
    data object Disconnecting : ObdConnectionState
    data class Error(val message: String) : ObdConnectionState
}
```

No exponer `Throwable` ni excepciones de infraestructura en Domain.

## 8.3 Métricas

```kotlin
data class ObdLiveMetrics(
    val rpm: Int?,
    val coolantTempCelsius: Int?,
    val speedKmh: Int?,
    val timestampMillis: Long
)
```

`null` significa no soportado/no disponible. Nunca equivale a cero.

## 8.4 `ObdGateway`

```kotlin
interface ObdGateway {
    val connectionState: StateFlow<ObdConnectionState>

    suspend fun connect(config: ObdConnectionConfig): Result<Unit>
    suspend fun disconnect()
    suspend fun readLiveMetrics(): Result<ObdLiveMetrics>
    suspend fun readDiagnosticCodes(): Result<List<String>>
}
```

Garantías:

- Solo lectura.
- No borrar DTC.
- No modificar parámetros de ECU.
- PIDs no soportados se representan como `null` en su métrica.
- Escaneo exitoso sin códigos: `Result.success(emptyList())`.
- Error de comunicación o trama inválida: `Result.failure(...)`, que el caso de uso adapta a un resultado de fallo para UI.

---

# 9. Contratos de repositorios

Paquete `domain/repository/`.

## 9.1 `AppPreferencesRepository`

```kotlin
interface AppPreferencesRepository {
    val activeVehicleId: Flow<Long?>
    val obdConnectionConfig: Flow<ObdConnectionConfig>

    suspend fun setActiveVehicleId(vehicleId: Long?)
    suspend fun saveObdConnectionConfig(config: ObdConnectionConfig)
}
```

DataStore conserva:

- `active_vehicle_id: Long?`
- `obd_ip: String`
- `obd_port: Int`

Nombre acordado: `AppPreferences`, no `UserPreferences`.

## 9.2 `VehicleRepository`

```kotlin
interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeVehicleById(vehicleId: Long): Flow<Vehicle?>
    suspend fun getVehicleById(vehicleId: Long): Vehicle?
    suspend fun saveVehicle(vehicle: Vehicle): Long
    suspend fun deleteVehicle(vehicleId: Long)
}
```

La edición normal de `Vehicle` incluye `currentMileageKm`. No crear `updateManualMileage()` como operación funcional separada.

La operación de guardado debe distinguir inserción de actualización y devolver siempre el ID válido del vehículo.

## 9.3 `DiagnosticRepository`

Contrato conceptual:

```kotlin
interface DiagnosticRepository {
    fun observeSessionsByVehicle(vehicleId: Long): Flow<List<DiagnosticSession>>
    suspend fun getLatestSession(vehicleId: Long): DiagnosticSession?
    suspend fun getSessionById(sessionId: Long): DiagnosticSession?
    suspend fun saveDiagnosticSession(session: DiagnosticSession): Long
    suspend fun getSuccessfulCodesBefore(
        vehicleId: Long,
        timestampMillis: Long,
        sessionId: Long
    ): Set<String>
    suspend fun getSuccessfulCodeOccurrenceCounts(
        vehicleId: Long
    ): Map<String, Int>
    fun getDtcGuidance(code: String): DtcGuidance
}
```

Las consultas analíticas deben considerar sesiones históricas guardadas, que por definición son escaneos exitosos.

`getSuccessfulCodesBefore` considera sesiones del mismo vehículo con:

- timestamp menor que el de la sesión objetivo; o
- mismo timestamp y `id` menor que el de la sesión objetivo.

`getSuccessfulCodeOccurrenceCounts` cuenta sesiones exitosas históricas distintas por código para `occurrencesCount`.

## 9.4 `MaintenanceRepository`

```kotlin
interface MaintenanceRepository {
    fun observeMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceRecord>>
    suspend fun getMaintenanceById(recordId: Long): MaintenanceRecord?
    suspend fun saveMaintenanceRecord(record: MaintenanceRecord): Long
    suspend fun getLatestRecordsPerAlertableType(
        vehicleId: Long
    ): List<MaintenanceRecord>
}
```

La última intervención se determina por fecha de realización y, en empate, ID descendente. Se excluye `OTHER`.

---

# 10. Modelo relacional Room

Decisión: exactamente cuatro tablas. No crear tablas de alertas, catálogo DTC ni usuarios.

## 10.1 `vehicles`

- `id`: PK autogenerada.
- `brand`
- `model`
- `year`
- `fuel_type`
- `engine_description`
- `current_mileage_km`

## 10.2 `diagnostic_sessions`

- `id`: PK autogenerada.
- `vehicle_id`: FK → `vehicles.id`, `ON DELETE CASCADE`.
- `timestamp_millis`.
- `mileage_at_scan_km`.

No incluir `status` ni `error_message`: solo se persisten sesiones exitosas.

Índice compuesto: `(vehicle_id, timestamp_millis)`.

## 10.3 `diagnostic_codes`

- `session_id`: FK → `diagnostic_sessions.id`, `ON DELETE CASCADE`.
- `code`.
- PK compuesta: `(session_id, code)`.
- Índice adicional únicamente sobre `code`; `session_id` ya es prefijo de la PK compuesta.

## 10.4 `maintenance_records`

- `id`: PK autogenerada.
- `vehicle_id`: FK → `vehicles.id`, `ON DELETE CASCADE`.
- `service_type`.
- `performed_date_epoch_day`.
- `mileage_at_service_km`.
- `notes`.
- `next_due_date_epoch_day` nullable.
- `next_due_mileage_km` nullable.

Índice compuesto: `(vehicle_id, performed_date_epoch_day)`.

Las fechas `LocalDate` se representan como días desde epoch (`toEpochDay()`), sin componente horario. Implementar conversión explícita mediante TypeConverter o mappers. No usar timestamps UTC a medianoche para simular fechas calendario.

## 10.5 Relación de sesión y códigos

```kotlin
data class DiagnosticSessionWithCodes(
    @Embedded val session: DiagnosticSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val codes: List<DiagnosticCodeEntity>
)
```

Las consultas que cargan la relación deben ejecutarse con `@Transaction`.

## 10.6 Políticas Room

- `DiagnosticSessionEntity` se inserta con `OnConflictStrategy.ABORT`; no utilizar `REPLACE` para historial.
- Vehículos y mantenimientos pueden usar `@Upsert`.
- Room puede devolver `-1L` en un `@Upsert` que actualiza; el repositorio debe devolver el ID existente en vez de `-1`.
- Actualización de kilometraje derivado solo si el registrado es superior:

```sql
UPDATE vehicles
SET current_mileage_km = :recordedMileageKm
WHERE id = :vehicleId
  AND current_mileage_km < :recordedMileageKm
```

---

# 11. Transacciones y reglas de persistencia

No pasar un DAO como argumento a otro DAO para coordinar varias tablas.

La implementación del repositorio coordina operaciones multi-tabla con `RoomDatabase.withTransaction`.

## Guardar un diagnóstico exitoso

1. Verificar que el vehículo de la captura sigue existiendo.
2. Insertar la sesión.
3. Insertar códigos DTC asociados; si la lista está vacía, no insertar filas en `diagnostic_codes`.
4. Actualizar kilometraje del vehículo únicamente si el kilometraje capturado supera el actual.
5. Confirmar toda la operación atómicamente.

El escaneo fallido no llega a este repositorio de guardado y no modifica ninguna tabla ni preferencia.

## Guardar mantenimiento

1. Verificar vehículo y reglas de dominio.
2. Insertar o actualizar el registro.
3. Avanzar kilometraje solo si el valor de la intervención es superior al actual.
4. Confirmar ambas operaciones atómicamente.

## Borrado de vehículo

`ON DELETE CASCADE` elimina las sesiones, códigos y mantenimientos relacionados.

`DeleteVehicleUseCase` debe coordinar además la preferencia de vehículo activo:

- Si se elimina el activo, limpiar o reasignar a otro vehículo existente.
- No dejar un `activeVehicleId` que apunte a un vehículo inexistente.

La coordinación entre Room y DataStore no es una transacción ACID común. Implementar el orden y recuperación de forma segura y simple.

---

# 12. Reglas de negocio

## 12.1 Edición y kilometraje

- El usuario puede editar kilometraje como parte de la edición normal del vehículo.
- No crear un caso de uso independiente para esa edición.
- Un diagnóstico o mantenimiento guardado puede avanzar el kilometraje si registra un valor superior.
- Un registro histórico inferior no reduce el kilometraje actual.
- Para mantenimiento, si `mileageAtServiceKm < currentMileageKm`, solicitar confirmación antes de guardar.
- Sin confirmación, no guardar.
- Con confirmación, guardar el dato histórico y conservar el kilometraje actual.
- No se permiten valores negativos.

El kilometraje de una sesión fallida nunca se persiste ni modifica el vehículo.

## 12.2 Sesiones y resultado de escaneo

- Un resultado exitoso vacío es un diagnóstico limpio válido.
- Un resultado fallido no crea sesión histórica, no agrega códigos, no actualiza kilometraje ni modifica ningún dato.
- El resultado exitoso queda pendiente hasta que el usuario elija guardar o descartar.
- Se conserva temporalmente en memoria.
- Antes de abandonar la pantalla de diagnóstico o cambiar de vehículo, si existe un resultado pendiente, solicitar guardar o descartar.
- Si se guarda, persistir usando los datos capturados al momento del escaneo.
- Si se descarta, eliminarlo de memoria.
- Recuperar un resultado pendiente después de cierre forzado no está dentro del MVP.

## 12.3 Conexión y vehículo activo

No permitir cambiar el vehículo activo mientras exista conexión OBD activa.

El usuario debe desconectar primero el adaptador. Esto impide asociar accidentalmente un diagnóstico leído desde un vehículo al `vehicleId` de otro.

La navegación debe considerar también si existe un resultado exitoso pendiente y solicitar guardar/descartar antes de abandonar el flujo correspondiente.

## 12.4 Recurrencia DTC

Un código en una sesión objetivo es recurrente si:

- La sesión objetivo es un escaneo exitoso guardado.
- El código aparece en al menos otra sesión guardada del mismo vehículo.
- Esa sesión ocurrió antes: timestamp menor o mismo timestamp e ID menor.

No exigir sesiones consecutivas ni ventanas temporales. No contar sesiones fallidas; por la nueva política, ninguna sesión fallida se persiste.

### Conteo y alerta activa

- `occurrencesCount` cuenta todas las sesiones exitosas históricas distintas del vehículo que contienen ese código.
- La alerta activa de recurrencia se construye usando los códigos recurrentes detectados en el último escaneo exitoso guardado.
- Un código que apareció muchas veces en el pasado, pero no aparece en el último escaneo exitoso, no genera por sí solo una alerta activa.

## 12.5 Mantenimiento

Se evalúa la intervención más reciente, por fecha calendario y luego ID, de cada tipo:

- `OIL_AND_FILTER`
- `OIL_ONLY`
- `FILTER_ONLY`

`OTHER` no genera alertas automáticas.

Estados:

- `UNSCHEDULED`: fecha y kilometraje próximos son ambos null.
- `OVERDUE`: se alcanza o supera la fecha programada o el kilometraje programado.
- `UPCOMING`: existe al menos un límite y ninguno se ha alcanzado.

Si existen ambos límites, basta con alcanzar uno para quedar vencido.

`OIL_AND_FILTER` es independiente de `OIL_ONLY` y `FILTER_ONLY`.

## 12.6 Dashboard

- Dashboard es reactivo mediante `Flow`.
- Sin vehículos: `DashboardState.NoVehicles`, con mensaje y acción de agregar.
- Con vehículo: `DashboardState.Ready(DashboardSummary)`.
- Alertas calculadas en memoria y deduplicadas por `conditionKey`.
- La alerta recurrente activa se basa en el último escaneo exitoso guardado.

---

# 13. Casos de uso

No crear casos de uso triviales que solo reenvíen una llamada DAO sin aportar lógica o un límite útil.

Prioritarios:

## `SaveMaintenanceUseCase`

- Validar existencia del vehículo.
- Validar kilometraje no negativo.
- Comparar kilometraje ingresado con kilometraje actual.
- Si es menor, retornar confirmación requerida sin persistir.
- Si el usuario confirma, guardar historial sin retroceder odómetro.
- Si es superior, guardar y avanzar kilometraje transaccionalmente.

Resultado sugerido:

```kotlin
sealed interface SaveMaintenanceResult {
    data class Saved(val recordId: Long) : SaveMaintenanceResult
    data class RequiresHistoricalMileageConfirmation(
        val enteredMileageKm: Int,
        val currentMileageKm: Int
    ) : SaveMaintenanceResult
    data class Failed(val message: String) : SaveMaintenanceResult
}
```

## `ScanDiagnosticCodesUseCase`

- Verificar que existe un vehículo objetivo y conexión válida.
- Solicitar lectura Modo 03 a `ObdGateway`.
- Capturar `vehicleId`, timestamp y kilometraje al momento de la lectura.
- Retornar `DiagnosticScanResult.Success` o `Failure`.
- No persistir.
- El fallo no modifica ningún dato.

## `SaveDiagnosticSessionUseCase`

- Recibir un resultado exitoso confirmado por el usuario.
- Verificar que el vehículo capturado sigue existiendo.
- Convertirlo a `DiagnosticSession`.
- Persistir transaccionalmente sesión, códigos y eventual avance del kilometraje.

## `AnalyzeDiagnosticHistoryUseCase`

- Recibir sesión guardada/objetivo.
- Consultar códigos anteriores exitosos del mismo vehículo.
- Determinar recurrencia temporal.
- Añadir `DtcGuidance` de catálogo o fallback.
- No mezclar esta regla con el conteo histórico global.

## `GetMaintenanceStatusUseCase`

- Obtener últimas intervenciones por tipo alertable.
- Evaluar fecha actual y kilometraje.
- Generar `MaintenanceStatusItem`.
- Excluir `OTHER` de alertas automáticas.

## `GetDashboardDataUseCase`

- Exponer un `Flow<DashboardState>`.
- Observar vehículo activo, lista de vehículos, datos Room y conexión.
- Producir `NoVehicles` o `Ready`.
- Combinar último escaneo exitoso, recurrencias y mantenimientos.
- Deduplicar alertas por `conditionKey`.

## `DeleteVehicleUseCase`

- Eliminar el vehículo y relaciones en cascada.
- Resolver `activeVehicleId` si se elimina el activo.
- No permitir cambio mientras OBD esté conectado; coordinar desconexión según el flujo de UI.

---

# 14. OBD-II: transporte, parser e intérprete

Separar tres responsabilidades en Data.

## 14.1 `Elm327SocketClient`

- Abrir/cerrar socket TCP.
- Leer y escribir comandos ASCII.
- Gestionar timeouts y desconexiones abruptas.
- Liberar recursos correctamente.
- Ejecutar operaciones de socket con `Dispatchers.IO`.
- No filtrar tipos de `java.net.Socket` a Domain.

## 14.2 `ObdResponseParser`

- Limpiar eco de comandos, saltos de línea, espacios y prompt `>`.
- Normalizar respuestas.
- Detectar `NO DATA`, `?`, `UNABLE TO CONNECT`, `SEARCHING...`, `ERROR` y tramas incompletas.
- Validar cabeceras de respuesta para Modo 01 (`41 XX`) y Modo 03 (`43`).
- Entregar bytes normalizados o DTC de cinco caracteres.
- No realizar cálculos físicos de PIDs.

## 14.3 `ObdPidInterpreter`

| Dato | Comando | Respuesta | Fórmula |
|---|---|---|---|
| RPM | `010C` | `41 0C A B` | `((A * 256) + B) / 4` |
| Temperatura refrigerante | `0105` | `41 05 A` | `A - 40` °C |
| Velocidad | `010D` | `41 0D A` | `A` km/h |

Respuesta inválida o `NO DATA` para una métrica individual no debe convertirse en cero. Se representa como `null` para esa métrica. Manejar cada PID sin hacer fallar innecesariamente todas las métricas válidas.

## 14.4 Comandos de referencia

Inicialización y lectura:

```text
ATZ
ATE0
ATL0       (opcional)
ATS0       (opcional)
ATSP0
0100
010C
0105
010D
03
```

Solo lectura. No enviar Modo 04 ni comandos de escritura ECU.

## 14.5 Política de lectura: sin escaneo continuo

- No implementar polling continuo ni telemetría en segundo plano.
- La consulta se inicia mediante una acción explícita del usuario (por ejemplo, botón “Escanear”).
- No iniciar escaneos automáticos al entrar en una pantalla.
- Los parámetros en vivo tampoco deben sondearse continuamente en el MVP; consultarlos mediante una acción explícita cuando corresponda.
- Mantener la información recibida hasta que el usuario actualice, guarde o descarte según el tipo de dato.

---

# 15. Simulador ELM327

Simulador externo investigado: **ELM327-emulator de Ircama**.

Repositorio: https://github.com/ircama/ELM327-emulator

Instalación propuesta en macOS:

```bash
mkdir elm327-test
cd elm327-test
python3 -m venv .venv
source .venv/bin/activate
python -m pip install ELM327-emulator
```

Comando de ejecución propuesto, sujeto a verificación con la versión instalada y la documentación actual:

```bash
python -m elm -s car -n 35000 -i 0.0.0.0
```

Configuración prevista:

- Puerto TCP de prueba: `35000`.
- Usar la IP LAN actual del Mac.
- Android y Mac deben estar en la misma red y la red debe permitir tráfico entre dispositivos.
- No asumir que el valor IP por defecto del modelo de configuración es la dirección real del simulador o adaptador.

También se había identificado como alternativa un emulador Android:

https://github.com/autodiag2/elm327emu

El adaptador físico disponible es un escáner Wi-Fi comercial rotulado ELM327 V1.5. La etiqueta no garantiza implementación completa ni compatibilidad real.

---

# 16. Diseño UX/UI: etapa nueva en paralelo

Se incorpora formalmente una etapa de diseño de interfaz mediante Figma o Stitch.

## 16.1 Momento de inicio

Comienza después de definir modelos de dominio, contratos principales y flujos funcionales suficientes para saber qué datos y operaciones debe representar la interfaz.

No es necesario esperar a terminar Room, repositorios o casos de uso. El diseño visual debe avanzar en paralelo con esa implementación.

## 16.2 Entradas para el diseño

Entregar a la herramienta de diseño:

- Requisitos funcionales y exclusiones.
- Historias y flujos de usuario.
- Modelos de datos y contratos relevantes.
- Campos que debe mostrar cada pantalla.
- Acciones disponibles.
- Estados vacío, carga, éxito, error y confirmación.
- Restricciones offline y de conexión OBD.
- Reglas de guardado/descartado de un resultado de escaneo pendiente.

## 16.3 Pantallas y flujos a prototipar

- Dashboard: sin vehículos y con vehículo activo.
- Gestión y edición de vehículos.
- Conexión OBD y configuración IP/puerto.
- Diagnóstico: estado desconectado, lectura, resultado limpio, DTC, error y resultado pendiente de guardar/descartar.
- Mantenimiento: historial, formulario, programación y confirmación de kilometraje histórico.
- Navegación y confirmaciones al abandonar pantallas.

## 16.4 Entregable de diseño

Un prototipo navegable que establezca:

- Arquitectura de información.
- Jerarquía visual.
- Componentes reutilizables.
- Estados de cada pantalla.
- Navegación y transiciones.
- Comportamiento de diálogos y confirmaciones.

El prototipo es referencia para implementar Jetpack Compose, no sustituto de los contratos, la lógica ni la arquitectura Kotlin. No asumir que el código generado por herramientas de diseño está listo para producción.

---

# 17. Pruebas

## 17.1 Pruebas unitarias de Domain

### Diagnósticos

- Código en dos sesiones exitosas: recurrente.
- Código en primera aparición: no recurrente.
- Sesión posterior no cuenta como anterior.
- Empate de timestamp: desempate por ID.
- Escaneo exitoso vacío: válido.
- Error de lectura: no persiste ni cambia datos.
- Conteo global de apariciones por código coincide con sesiones distintas.

### Mantenimiento

- Vencimiento por fecha.
- Vencimiento por kilometraje.
- Vencimiento si cualquiera de los dos límites se alcanza.
- `UNSCHEDULED` si ambos límites son null.
- `UPCOMING` si no se alcanzó ningún límite.
- Nueva intervención del mismo tipo reemplaza la programación vigente.
- `OTHER` no genera alerta.
- Fechas comparadas como fecha calendario, sin hora.

### Kilometraje

- Registro superior puede avanzar kilometraje actual.
- Registro inferior requiere confirmación antes de persistir un mantenimiento.
- Registro histórico confirmado conserva el kilometraje actual.
- Edición manual del vehículo permite corregir su kilometraje.
- Nunca aceptar valores negativos.
- Fallo OBD no modifica kilometraje.

### Dashboard y navegación

- Sin vehículos produce `NoVehicles`.
- Con vehículo produce `Ready`.
- Cambios de datos actualizan el Flow.
- Alertas deduplicadas.
- Alertas DTC activas basadas en último escaneo exitoso.
- No cambiar vehículo con conexión OBD activa.
- Navegación con resultado pendiente solicita guardar o descartar.

## 17.2 Pruebas de integración Room

Agregar pruebas usando Room en memoria para verificar:

- Borrado en cascada desde vehículo.
- Relación sesión-códigos.
- Sesión exitosa sin códigos.
- Inserción de sesión y códigos.
- Transacciones y rollback ante error.
- Consultas de historial y recurrencia.
- Selección de intervención vigente por fecha/ID.
- Actualización condicional de kilometraje.
- Mapeo de fechas calendario.

## 17.3 Pruebas parser e intérprete

- Tramas hexadecimales correctas.
- Eco, espacios, saltos y prompt.
- `NO DATA`, `?`, `ERROR`, `UNABLE TO CONNECT` y tramas truncadas.
- Cabeceras esperadas.
- Fórmulas de RPM, temperatura y velocidad.
- PID no disponible no se convierte en cero.

## 17.4 Pendientes de infraestructura de pruebas

Las pruebas de Room en memoria se incorporan ahora al alcance.

La política de versionado de base de datos, exportación de esquemas y migraciones Room queda pendiente de decisión. No usar migración destructiva como solución automática si se necesita conservar historial.

---

# 18. Plan actualizado por fases

El plan incorpora dos líneas paralelas después del cierre de contratos.

## Fase 0. Cierre de Domain y contratos

- Confirmar modelos, reglas y contratos.
- Representar solo diagnósticos exitosos en historial.
- Definir `DiagnosticScanResult` temporal.
- Separar escaneo de guardado explícito.
- Definir fechas de mantenimiento con `LocalDate`.
- Definir estados Dashboard y bloqueo de cambio de vehículo conectado.

## Fase 1A. Backend: persistencia y casos de uso

- Implementar cuatro entidades Room.
- Implementar mappers, DAOs y `AppDatabase`.
- Implementar DataStore.
- Implementar repositorios.
- Coordinar operaciones multi-tabla con `withTransaction`.
- Implementar `SaveMaintenanceUseCase`.
- Implementar `ScanDiagnosticCodesUseCase` y `SaveDiagnosticSessionUseCase`.
- Implementar `AnalyzeDiagnosticHistoryUseCase`.
- Implementar `GetMaintenanceStatusUseCase`.
- Implementar `GetDashboardDataUseCase` como Flow.
- Implementar `DeleteVehicleUseCase`.

## Fase 1B. UX/UI: en paralelo con Fase 1A

- Definir arquitectura de información.
- Diseñar wireframes y flujos.
- Crear prototipo navegable en Figma o Stitch.
- Cubrir estados vacíos, errores, resultados y confirmaciones.
- Validar que los diseños representen los contratos reales.

## Fase 2. Pruebas de Domain y Room

- Ejecutar pruebas unitarias de reglas.
- Ejecutar pruebas Room en memoria.
- Verificar cascadas, transacciones, rollback, índices y mappers.
- Ajustar contratos solo si las pruebas evidencian una carencia real.

## Fase 3. Subsistema OBD-II

- Implementar `ObdResponseParser`.
- Implementar `ObdPidInterpreter`.
- Implementar `Elm327SocketClient`.
- Implementar inicialización AT.
- Implementar simulador.
- Implementar adaptador concreto de `ObdGateway`.
- Probar errores, timeouts, desconexiones y lecturas puntuales.

## Fase 4. Compose e integración

- Configurar `AppContainer` con inyección manual.
- Implementar ViewModels con `StateFlow<UiState>` y UDF.
- Traducir el prototipo visual a Compose.
- Conectar pantallas y casos de uso.
- Implementar guardado/descartado de escaneos pendientes.
- Bloquear cambio de vehículo mientras OBD está conectado.

## Fase 5. Validación del MVP

Validar flujos completos con simulador y, si es posible, adaptador físico. Verificar persistencia offline, datos históricos, desconexiones, errores y todos los criterios funcionales.

---

# 19. Decisiones pendientes

No tratarlas como decisiones cerradas hasta verificarlas:

1. **Versionado y migraciones Room:** política de exportación de esquemas, versión inicial y estrategia de migración. Queda expresamente pendiente.
2. **Versiones y configuración Android:** Kotlin, AGP, Gradle, Compose BOM, Room, DataStore, KSP, JDK, SDKs y `minSdk`. Buscar versiones actuales compatibles y confirmar.
3. **Compatibilidad de `LocalDate`:** depende de `minSdk` y posible core library desugaring.
4. **Cambio manual descendente del kilometraje:** está permitido mediante edición normal del vehículo. La UX puede advertir al usuario, pero no crear una operación separada. No debe confundirse con las actualizaciones automáticas históricas, que nunca retroceden el odómetro.

No retrasar la implementación de Domain por las decisiones de versión o migración; resolverlas cuando el proyecto y su configuración hayan sido inspeccionados.

---

# 20. Criterios de aceptación del MVP

1. Registrar varios vehículos y seleccionar uno activo.
2. Mostrar estado vacío cuando no existan vehículos.
3. Editar datos del vehículo, incluido kilometraje.
4. Impedir cambio de vehículo con conexión OBD activa.
5. Conectar con simulador o ELM327 Wi-Fi/TCP.
6. Consultar parámetros mediante acción explícita, sin sondeo continuo.
7. Ejecutar un escaneo DTC mediante acción explícita.
8. Diferenciar escaneo exitoso limpio de error.
9. No persistir errores ni modificar datos ante fallos de escaneo.
10. Permitir guardar o descartar un escaneo exitoso pendiente.
11. Solicitar decisión antes de abandonar la pantalla/cambiar flujo con escaneo pendiente.
12. Guardar y consultar historial de diagnósticos exitosos.
13. Mostrar orientación local y fallback para DTC desconocidos.
14. Detectar recurrencia histórica correctamente.
15. Registrar mantenimientos y fechas calendario.
16. Programar por fecha, kilometraje o ambos.
17. Mostrar próximos y vencidos.
18. Deduplicar alertas.
19. Mantener datos locales sin Internet.
20. Eliminar un vehículo y sus registros asociados.

---

# 21. Instrucciones para retomar el trabajo en un nuevo hilo

El diseño funcional y la arquitectura están definidos en este documento. No vuelvas a preguntar decisiones ya cerradas ni repitas un análisis general del proyecto.

Primero inspecciona los archivos actuales del proyecto para conocer qué está implementado y su configuración. No asumas que el código existente coincide con este documento.

Después:

1. Identifica diferencias entre implementación y contexto maestro.
2. Señala solo problemas reales o decisiones pendientes.
3. No cambies acuerdos silenciosamente.
4. Continúa con Domain y persistencia según el plan.
5. Inicia la línea UX/UI una vez que contratos y flujos estén suficientemente definidos, en paralelo con backend.
6. Implementa y prueba casos de uso antes de comenzar con el parser OBD-II y la integración visual final.

**Punto de continuación recomendado:** revisar/implementar los contratos definitivos y la persistencia Room/DataStore, incluyendo la nueva política de escaneos exitosos pendientes y no persistencia de errores. En paralelo, preparar los insumos para el prototipo UX/UI en Figma o Stitch.
