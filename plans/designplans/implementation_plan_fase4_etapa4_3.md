# Etapa 4.3 — Composición, ciclo de vida y navegación: plan de implementación

> **Estado: EJECUTADO Y CERRADO.** Plan aprobado explícitamente por el usuario en la sesión de ejecución; `androidx.navigation:navigation-compose:2.10.2` autorizada. Coder → Tester → QA/Ponytail completados. QA final: APROBADO. Evidencia de cierre en [reporte_fase4_etapa4_3.md](../reporte_fase4_etapa4_3.md).

**Meta:** componer las dependencias reales una vez por proceso, conservar la sesión OBD y captura pendiente durante recreación y preparar navegación y coordinación compartidas sin implementar pantallas de 4.4–4.8.

**Arquitectura:** inyección manual mediante Application/AppContainer. Un propietario de sesión en Presentation recibe todas las acciones sensibles; Activity obtiene un ViewModel que observa/delega al mismo propietario. Un solo Mutex excluye conexión y cambios de vehículo; cancelación y cierre siguen accesibles fuera de ese Mutex.

**Tecnología:** Kotlin, coroutines/Flow, Room 2.7.2, DataStore 1.1.7, Activity Compose 1.13.0 y Lifecycle 2.11.0 existentes. Navegación Compose requiere dependencia explícitamente aprobada antes de añadirse. Sin Hilt.

## 1. Evidencia y línea base

Inspección estática mediante studio_read_file, studio_grep, studio_find_files, studio_find_declaration y outlines; no Gradle ni pruebas ejecutadas en esta sesión. git status --short inicial: limpio. Verificación final: /plans está excluido por .gitignore:16; status/diff no cubren estos artefactos. Su escritura se comprobó por lectura IDE y el contenido histórico de task.md se comparó íntegramente con el original (conservado). No se cambió .gitignore ni se forzó versionado. Raíz: /Users/cristian/Desarrollo/kotlin/BitacoraOBD. Las rutas siguientes son relativas a esa raíz.

| Evidencia exacta | Implementación actual / consecuencia para 4.3 |
|---|---|
| AGENTS.md; plans/Contexto_Maestro_App_OBDII_v2.md; plans/designplans/implementation_plan_fase4.md §§0–2,5–10 | Puerta por etapa, inyección manual, cuatro destinos, sin polling; Compose sujeto a DESIGN_READY. |
| plans/designplans/implementation_plan_fase4_etapa4_1.md y etapa4_2.md; plans/reporte_fase4_etapa4_1.md y etapa4_2.md | 4.1/4.2 cerradas QA APROBADO. 4.1: instrumentadas Pixel_10 API37, 19 pruebas, 0 fallos, 1 omisión remota sin elm327Host. 4.2: JVM/errores DataStore e instrumentadas aprobadas. No equivale a ejecución nueva ni valida permiso runtime. |
| app/src/main/java/cl/cristian/bitacoraobd/MainActivity.kt:15 | Solo Greeting/Scaffold; sin composición, ViewModel ni navegación funcional. |
| app/src/main/AndroidManifest.xml | Sin Application propia. INTERNET y ACCESS_LOCAL_NETWORK declarados; solicitud runtime aún ausente. |
| app/src/main/java/cl/cristian/bitacoraobd/data/local/database/AppDatabase.kt:16; Migrations.kt | Room v2, exportSchema=true; MIGRATION_1_2 aditiva, UUID por sesión, índice único. No builder en main. |
| app/schemas/cl.cristian.bitacoraobd.data.local.database.AppDatabase/1.json y 2.json; app/build.gradle.kts | Schemas v1/v2 presentes; v2 tiene captureId NOT NULL, sentinel de esquema e índice UNIQUE. KSP room.schemaLocation=$projectDir/schemas. Mantener versión, default e índices. |
| app/src/main/java/cl/cristian/bitacoraobd/data/local/entity/Entities.kt y EntityMappers.kt; data/local/dao/VehicleDao.kt, DiagnosticDao.kt, MaintenanceDao.kt | Cascadas, consultas ordenadas/aisladas, mappers e identidad listos; no necesitan cambios. |
| app/src/main/java/cl/cristian/bitacoraobd/data/repository/RoomVehicleRepository.kt, RoomDiagnosticRepository.kt, RoomMaintenanceRepository.kt | Constructor DAO para vehículos, AppDatabase para diagnóstico/mantenimiento. Transacciones e idempotencia listas; no activar vehículo desde saveVehicle. |
| app/src/main/java/cl/cristian/bitacoraobd/data/local/datastore/DataStoreAppPreferencesRepository.kt; domain/repository/AppPreferencesRepository.kt | Constructor DataStore; preferencias, config y activo son Flow, errores se propagan. No creación de DataStore de producción ni guardia en setActiveVehicleId. |
| app/src/main/java/cl/cristian/bitacoraobd/data/obd/Elm327ObdGateway.kt:32,92,169 | operationMutex interno para connect/lecturas; disconnect no lo adquiere. Error se publica después de client.disconnect; cancelación limpia con NonCancellable y se propaga. Generación evita Connected tardío. |
| app/src/main/java/cl/cristian/bitacoraobd/data/obd/connection/Elm327SocketClient.kt:25 | TCP IO, connect bloqueante máximo 5 s, sondeo de cancelación de lectura 250 ms, límite comando 10 s/64 KiB; cerrar socket interrumpe lectura. Cancelar Job por sí solo no garantiza interrupción instantánea del connect bloqueante. |
| app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/DeleteVehicleUseCase.kt:23 | Solo Connected bloquea, comprobado una vez. DataStore cambia antes de Room, Failed puede dejar selección cambiada; cancelación se propaga. |
| app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/ScanDiagnosticCodesUseCase.kt; SaveDiagnosticSessionUseCase.kt; domain/model/DiagnosticScanResult.kt | Escaneo exige vehículo/Connected y genera Success con UUID; guardado conserva payload/captureId y puede lanzar excepción. No propietario de pendiente. |
| app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetDashboardDataUseCase.kt; GetMaintenanceStatusUseCase.kt | Flow frío, fallback sin reparar DataStore. Fecha se calcula al evaluar; volver a colectar obtiene fuentes actuales. No evento de medianoche. |
| app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/AnalyzeDiagnosticHistoryUseCase.kt y SaveMaintenanceUseCase.kt | Reutilizables por constructor; no trasladar reglas a Presentation. Formularios fuera de 4.3. |
| gradle/libs.versions.toml; app/build.gradle.kts | AGP 9.5.1, Kotlin Compose 2.2.10, KSP 2.3.13, BOM 2026.02.01, coroutines/core/test 1.10.2, min26/target37, Java11. No alias ni dependencia Navigation. ViewModelProvider, viewModelScope y repeatOnLifecycle encontrados en fuentes Gradle Lifecycle 2.11.0; no hace falta lifecycle-viewmodel-compose para obtener VM desde Activity. |

### Pruebas existentes consultadas y límites

- app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DiagnosticMigrationTest.kt: fixture SQLite v1, builder de prueba registra migración y conserva relaciones; **no prueba un builder de producción**.
- app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt: cascadas, consultas, rollback, idempotencia y concurrencia. Outline contrastado con reportes; sin nueva ejecución.
- app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DataStoreAppPreferencesRepositoryTest.kt: defaults/round-trip en la misma instancia; no prueba reinicio de proceso.
- app/src/test/java/cl/cristian/bitacoraobd/data/local/DataStoreAppPreferencesFailureTest.kt: IOException de lectura/escritura se conserva.
- app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt: selección, aislamiento, Connected, cancelación, fallo DataStore y Room posterior a selección; no cubre coordinador.
- app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt: guardias de escaneo, Failure sin escritura, payload original y fecha controlada; no recreación ni estado Presentation.
- app/src/test/java/cl/cristian/bitacoraobd/data/obd/Elm327ObdGatewayTest.kt: handshake, cancelación en lecturas, cierre antes de Error, desconectar durante inicialización, no intercalación, desconexión idempotente.
- app/src/test/java/cl/cristian/bitacoraobd/data/obd/connection/Elm327SocketClientTest.kt: cierre/cancelación de lectura y reconexión. No test determinista actual de cancelación durante TCP connect; no inventar cobertura.

**Callers:** búsqueda en app/src de connect/disconnect, setActiveVehicleId y constructores de Delete/Scan/SaveDiagnostic/GetDashboard: usos en gateway/socket, DeleteVehicleUseCase, pruebas JVM e integración remota; no entradas UI existentes. Se preservan firmas de Domain/Data. Los nuevos callers de UI irán al propietario de sesión; repositorios/gateway directos de pruebas no quedan cubiertos por esa exclusión.

## 2. Discrepancias documentales (sin nuevas reglas funcionales)

1. screen-dependencies.md DEP-018 todavía afirma ausencia de preferencias; código, contratos actualizados y reporte 4.2 prueban backend listo. La brecha actual es Presentation/conversión/formularios.
2. design-decisions.md DD-11 y texto histórico del contexto describen Room v1 o política pendiente. AppDatabase/schema v2 y reporte 4.1 son evidencia vigente; falta únicamente registro de migración de producción.
3. screen-flows.md conserva párrafos históricos que llaman abiertos guardado de Ajustes/salida de lectura; DD-33/36/37 y actualizaciones superiores ya los resolvieron. Prevalecen esas actualizaciones y F03 del 2026-11-10.
4. El título inicial del reporte 4.2 menciona observaciones de errores; su cierre y pruebas IOException posteriores documentan resolución. Preservar ese historial, no interpretar la observación como fallo actual.
5. No hay navegación instalada/declarada encontrada (NavHost no resuelve en el índice). No cumplir navegación creando un back stack casero. Es una dependencia técnica que requiere aprobación concreta.

No se identificó necesidad de cambiar un contrato Domain ni una regla ya aprobada. Estas diferencias no justifican reaprobar wireframes. En ejecución sincronizar solo el estado factual de las filas afectadas; conservar antecedentes.

## 3. Alternativas y decisión mínima

- **Elegida:** Application con AppContainer concreto y sesión Presentation de proceso, ViewModel de Activity como adaptador. Preserva sesión aunque se reemplace la Activity y evita segunda instancia OBD. Coste: propietario explícito con scope de proceso y pruebas de cierre.
- Solo ViewModel de Activity alojando toda la sesión: menos un objeto, pero al terminar Activity con proceso vivo perdería el pendiente/operación y dejaría gateway de proceso sin dueño; no cumple la conservación solicitada.
- Hilt, servicio en primer plano o repositorio persistente de pendientes: complejidad y capacidades fuera del alcance; excluidos.

No crear interfaces para AppContainer ni wrappers de cada caso de uso. El propietario de sesión aporta la coordinación real; el ViewModel aporta conexión con lifecycle, sin copiar estado.

## 4. Composición y propiedad

Archivos nuevos bajo app/src/main/java/cl/cristian/bitacoraobd/:

- BitacoraApplication.kt: propiedad lazy de AppContainer(applicationContext); registrar android:name en manifest. Único punto de sustitución para instrumentación: clase open con propiedad open val appSession: AppSession get() = container.session. MainActivity obtiene esa sesión y la pasa a la Factory de AppViewModel; sin setter ni registro global mutable en producción.
- AppContainer.kt: propiedad lazy de Room, DataStore, tres repositorios, preferencias, un Elm327ObdGateway y casos de uso existentes; sesión compartida. Scope SupervisorJob + Dispatchers.Main.immediate para sesión, IO para DataStore.
- data/local/database/DatabaseFactory.kt: única función interna de construcción compartida por producción y pruebas de migración.
- ui/AppSession.kt: único propietario de operaciones, pendiente, métricas y coordinación; dependencias Domain por constructor y CoroutineScope inyectado para pruebas.
- ui/AppSessionState.kt: valores mínimos de Presentation, sin socket/Context/Job/NavController.
- ui/AppViewModel.kt: referencia a AppSession, expone su StateFlow y delega eventos; Factory local por constructor, obtenido con ViewModelProvider(this, factory) en Activity. No VM por destino para esta sesión.
- ui/HomeViewModel.kt: propietario únicamente de colección de Inicio y estado de carga/error del contexto actual.
- ui/navigation/AppNavigation.kt: grafo oficial, rutas internas y adaptador único de autorizaciones; condicionado a aprobación de dependencia. ExitAction y AuthorizedExit pueden declararse en ese mismo archivo, sin archivo/clase de servicio adicional.

Firmas técnicas propuestas (no APIs existentes):
```kotlin
internal fun buildAppDatabase(context: Context, name: String): AppDatabase
// Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
//     .addMigrations(MIGRATION_1_2).build()
class AppContainer(context: Context)
class AppSession(
    vehicles: VehicleRepository,
    preferences: AppPreferencesRepository,
    gateway: ObdGateway,
    scanDiagnostic: ScanDiagnosticCodesUseCase,
    saveDiagnostic: SaveDiagnosticSessionUseCase,
    deleteVehicle: DeleteVehicleUseCase,
    scope: CoroutineScope
)
class AppViewModel(session: AppSession) : ViewModel()
class HomeViewModel(getDashboard: GetDashboardDataUseCase) : ViewModel()
```

Room: nombre estable bitacora_obd.db (no existe nombre previo de producción encontrado), sin allowMainThreadQueries ni fallbackToDestructiveMigration. Construcción lazy, acceso suspendido fuera de main. No cambiar schema ni regenerarlo sin diferencia de entidades. DataStore: delegado preferencesDataStore top-level único con nombre bitacora_preferences y applicationContext, envuelto por DataStoreAppPreferencesRepository; no crear un store por pantalla. Claves históricas intactas.

Room/DataStore pertenecen al proceso: Activity/VM no los cierran. No usar Application.onTerminate como garantía en dispositivo. Sistema libera recursos al terminar proceso. En pruebas, cerrar bases y cancelar/join scopes propios antes de borrar archivos; DataStore de prueba con archivo único. Sesión no se cierra por rotación/stop; cierre explícito OBD cancela/espera sus trabajos. No agregar servicio/background polling.

## 5. Estado, operaciones y protocolo del único Mutex

AppSession recibe eventos desde Main.immediate; nunca expone setters públicos de pendiente. Estado observable incluye conexión real del gateway, vehículo efectivo, operación en curso, Success pendiente, resultado guardado/ID, última captura de métricas con vehicleId y fecha/hora, petición de decisión y error recuperable. Pending/guardado no se confunden; Failure no sustituye una captura válida. Mantener una sola última captura de métricas, mostrarla solo si coincide su vehicleId; sin historial nuevo.

Entrada propuesta de eventos:
```kotlin
fun connect(config: ObdConnectionConfig, localNetworkAllowed: Boolean)
fun cancelConnection()
fun selectVehicle(vehicleId: Long)
fun deleteVehicle(vehicleId: Long) // solo después de confirmación explícita
fun scanDiagnostic()
fun readMetrics()
fun savePendingDiagnostic()
fun resolvePending(choice: PendingChoice) // Save, Discard, Stay
fun disconnect()
fun requestExit(action: ExitAction) // retiene intención; NavController ejecuta
fun completeExit(authorizationId: Long) // confirma solo la autorización vigente
fun refreshVehicleContext()
```
Permiso concedido se obtiene/verifica en adaptador Android, nunca se restaura como true en saved state. En 4.3 no exponer un control de conexión de producto hasta implementar ese adaptador en 4.6. Fakes pueden suministrar concesión al probar el coordinador. Config tipada ya válida; formulario y política de solicitud/ajustes quedan en 4.6.

### Protocolo preciso

1. Solicitud sensible primero resuelve pendiente **fuera del Mutex**. Si guarda, esperar el Job existente; nunca disparar otro guardado. Save conserva la misma instancia/captureId, limpia pendiente solo tras ID confirmado; fallo conserva captura y cancela continuación. Discard explícito elimina pendiente; Stay cancela acción. Petición de decisión vive en StateFlow para sobrevivir recreación, sin callback de Activity retenido.
2. Una solicitud en resolución no se duplica por doble toque. Guardar la acción solicitada y el captureId que motivó la decisión; no ejecutar una continuación vieja sobre otra captura. Si llegó una nueva captura se vuelve a resolver, nunca se descarta por una decisión anterior.
3. Antes de intentar adquirir Mutex para selección/borrado, comprobar el estado real: Connected bloquea y conserva socket/selección aunque quede un Job registrado. Si hay un intento Connecting o en cola, cancelarlo sin adquirir Mutex, pedir cierre al gateway y esperar join. Registrar connectJob antes de arrancarlo, incluyendo intentos aún no iniciados. Mantener señal de cierre desde antes de cancelar hasta finalización; no permitir nuevo connect durante esa ventana. Revalidar nuevamente bajo Mutex.
4. Cancelación/limpieza usa un Job de cierre único, independiente del connectJob. El finally común del intento solo libera su referencia/estado operativo y el Mutex: **no llama disconnect tras Success**. Reutilizar la limpieza de cancelación/error ya implementada por el gateway. El cierre externo marca cierre en curso, cancela el intento y puede ejecutar disconnect para interrumpir IO; espera join fuera del Mutex y confirma con un disconnect final tras el join que no quedó una apertura tardía. El intento nunca espera al Job de cierre externo ni hace join de sí mismo. Mantener la barrera de cierre hasta terminar; si falla, conservar bloqueo/error recuperable. Cancelar Job por sí solo puede esperar hasta timeout TCP; no prometer instantaneidad. Un Success conserva Connected hasta desconexión explícita.
5. Si ya está Disconnecting, esperar el cierre registrado fuera del Mutex. Ante Error, esperar trabajos previos y completar disconnect para confirmar estado real; la API no expone socket, por lo que usar su operación de cierre, no inferir recursos cerrados por el nombre Error. El gateway concreto además cierra antes de publicar Error. No sondear estado con temporizador.
6. Adquirir Mutex; revalidar pendiente/guardado, cierre en curso, conexión real y existencia del vehículo. Si pendiente apareció, soltar Mutex y volver al paso 1. Si estado transitorio/cierre apareció, soltar y esperar fuera. Connected bloquea selección y **cualquier** borrado, sin desconectar silenciosamente.
7. Bajo Mutex ejecutar solo la escritura de selección o DeleteVehicleUseCase; ningún helper vuelve a adquirir el mismo Mutex. Para conectar, tomar ese mismo Mutex, releer activo/vehículo y validar permiso/no captura/operación/cierre; mantenerlo durante todo connect hasta retorno o cancelación con limpieza terminada. Connected impide otro connect. No reconectar ni leer al crear VM/coleccionar Flow.
8. Lecturas explícitas usan el mismo Mutex para iniciar y publicar captura completa antes de soltarlo, con guardia de operación para no encolar dobles toques. Esto impide publicar una captura entre la última revalidación y un cambio de vehículo. Cancelación de lectura queda accesible fuera; salida voluntaria cancela/join, conserva última completa y comunica estado real, DD-33. Recreación no envía requestExit.
9. Guardado de pendiente ocurre fuera del Mutex; bandera/Job de guardado se establece antes de lanzar, impide nuevo escaneo y decisiones de descarte/salida hasta resultado. Tras esperar, revalidar al adquirir. No esperar decisiones de usuario dentro de withLock. No invocar métodos públicos que toman Mutex desde otra sección bloqueada.

Conectar mantiene exclusión contra selección/borrado; cierre nunca espera por ella. Los Mutex internos de gateway/socket se conservan y no son sustitutos del Mutex Presentation. La garantía vale solo para entradas que pasan por AppSession en este proceso.

### Activo y fallos Room/DataStore

Inicio de sesión ejecuta refreshVehicleContext explícitamente: leer lista y activeVehicleId, conservar válido o elegir primer vehículo del mismo orden de observeVehicles, persistir fallback bajo coordinación, sin escrituras dentro de un Flow. Sin vehículos, null. Si falla lectura/escritura, bloquear conectar hasta consulta válida; no fingir activo persistido.

Seleccionar exige existencia y escribe DataStore; al terminar relee estado. Borrado conserva DeleteVehicleUseCase, incluidos sus resultados y guardia Connected. Failed o cancelación después de empezar escritura marca contexto por refrescar: releer preferencias y lista antes de permitir reintento o conexión. Si falla refresco, estado de error recuperable, acciones sensibles bloqueadas hasta refresh explícito exitoso. No restaurar selección anterior automáticamente, no Deshacer ficticio y **no atomicidad conjunta**. Un fallo de Room posterior a DataStore puede dejar vehículo intacto y activo cambiado. La reparación de activo ausente se hace explícita bajo el mismo coordinador. La secuencia de alta y reintento usando ID creado se conecta en 4.5, sin duplicar inserciones.

### Contrato mínimo de salida diferida (corrección de auditoría 2)

Tipos nuevos de Presentation, alojados junto al grafo; sin NavController ni callback retenido:
```kotlin
sealed interface ExitAction {
    data object Back : ExitAction
    data class ToRoute(val route: String) : ExitAction
}
data class AuthorizedExit(val id: Long, val action: ExitAction)
```

ToRoute usa exclusivamente rutas construidas por el grafo interno; validar los IDs al construirlas. No aceptar rutas arbitrarias externas. AppSession conserva una sola solicitud con ID creciente de proceso, acción y captureId que requiere resolución; expone authorizedExit: AuthorizedExit? en su StateFlow. Una solicitud nueva no reemplaza una decisión/guardado en curso: se ignora el doble toque hasta resolver o cancelar la actual.

- Sin pendiente/operación incompatible, autorizar la acción; con pendiente, esperar Save/Discard/Stay fuera del Mutex. Stay o fallo de guardado cancelan la solicitud sin navegar; fallo conserva la captura. Save exitoso autoriza la acción original. Descartar en la salida específica DD-25 autoriza home; salida final de connection autoriza diagnostic según F03.
- Si apareció otra captureId antes de autorizar, volver a resolverla; una decisión anterior nunca la descarta. No serializar solicitud/autorización para recuperar después de muerte de proceso.
- Un único adaptador de navegación activo, ligado a Activity RESUMED, observa la autorización. En Main comprueba que su ID sigue vigente, ejecuta navigate/pop y llama completeExit(id) sin suspensión entre esos pasos. completeExit solo borra el ID vigente; un callback o emisión vieja no consume otra autorización.
- Al recrear antes del consumo, la autorización sigue pendiente y la nueva Activity la ejecuta una vez. Después del consumo, nueva colección no la repite. Si navegar falla, no confirmar consumo: mostrar error y permitir reintento explícito, sin bucle automático.
- NavController conserva el back stack. AppSession retiene únicamente la intención todavía no consumida. No introducir Channel/bus genérico, copia de back stack ni callbacks de Activity persistidos.

## 6. Inicio, ciclo de vida y navegación

Operaciones/capturas pertenecen a AppSession; recrear Activity/VM solo vuelve a observarlas. Borradores específicos y scroll se implementan con cada pantalla posterior; no crear modelos vacíos de formularios ahora. Nunca SavedStateHandle para socket, Job o captura no persistida. Nuevo proceso: nueva sesión Disconnected/sin pendiente, recargar Room/DataStore, sin repetir comandos/guardados.

HomeViewModel ofrece setActive(active: Boolean) y retry(). Activación = Activity RESUMED y destino actual Inicio; desactivar al salir o pausar, cancelar/join colección anterior; cada nueva activación crea colección nueva de getDashboard(). No usar stateIn compartido permanente que entregue caché sin reevaluar. Retorno desde pantalla secundaria y foreground reinician cálculo. Mantener datos solo del mismo vehículo; error inicial completo, actualización con datos propios/error, retry crea nueva colección. Prueba con Clock mutable inyectado a GetMaintenanceStatusUseCase; no temporizador.

### Dependencia de navegación: condición de aprobación explícita

No hay librería Navigation encontrada. Propuesta concreta: androidx.navigation:navigation-compose:2.10.2, versión estable informada por studio_version_lookup el 2026-11-10. Añadir alias navigationCompose y una implementation solo si el usuario la aprueba; la consulta de versión no prueba compatibilidad del grafo Gradle. Comprobar resolución/compile en Fase B. No añadir plugins serialization ni navigation-testing: rutas string simples e instrumentación Compose existente bastan.

Documentación oficial consultada con studio_search_android_docs/studio_fetch_android_docs: [NavController](https://developer.android.com/guide/navigation/navcontroller), que define un controlador asociado a cada NavHost. Reutilizar rememberNavController/NavHost, sin lista propia de back stack.

Rutas propuestas: home (inicial), vehicles, diagnostic, maintenance; secundarias settings, history/{vehicleId}, diagnostic-detail/{sessionId}; F03 connection anidado desde diagnostic. Rutas reciben IDs, nunca objetos Room, capturas o sockets. NavController es autoridad del destino; AppSession solo retiene intención pendiente, no copia back stack.

- Cuatro principales: navigate con launchSingleTop, popUpTo al inicio y saveState/restoreState; evitar duplicados al tocar destino actual. Estado de scroll por pantalla más tarde.
- Historial desde Inicio/Diagnóstico: apilar y pop para volver al origen; detalle desde Inicio o lista, pop a ese origen. Vehículo de argumento se valida y nunca se atribuyen datos de otro.
- Ajustes: apilar y volver al origen. Antes de abrir desde formularios, resolver y cerrar editor conforme DD-30/31/36 en sus etapas.
- F03 solo desde Diagnóstico. Abrir no conecta/lee. Cancelar intento permanece en configuración; Atrás/salida durante Connecting cancela y vuelve a Diagnóstico. Toda salida final de F03 vuelve a Diagnóstico: no ejecutar directamente una pestaña solicitada durante Connecting. Desde Diagnóstico el usuario elige otro destino.
- Connected: salir de F03 conserva conexión. Disconnecting permite salir, mantiene bloqueo de conectar/leer/cambiar/borrar hasta cierre confirmado. Cancelar lectura voluntariamente aplica DD-33.
- Antes de salir de Diagnóstico con pendiente se resuelve Save/Discard/Stay; guardando se espera. Fallo permanece, reintenta misma captura; descarte explícito de DD-25 vuelve a Inicio.
- Restauración de navegación por mecanismo oficial; tras muerte de proceso ninguna ruta restaurada inicia socket/lectura/guardado. Estado de sesión siempre nuevo y real.

**Límite de 4.3:** preparar grafo con slots mínimos de contenido y harness de pruebas, sin formularios, métricas, listas ni diálogos de producto no DESIGN_READY. Integrar propietario en Activity manteniendo tema existente; contenido técnico de destinos no acredita implementación de UI-01–07. Pruebas pueden activar eventos mediante harness en androidTest, sin botones de depuración en main. La barra y acabado visual pertenecen a 4.4+; no eludir la puerta con pantallas completas llamadas placeholders.

## 7. Mapa de cambios y pruebas propuestas

Además de archivos nuevos de §4, modificar:
- app/src/main/AndroidManifest.xml: registrar BitacoraApplication.
- app/src/main/java/cl/cristian/bitacoraobd/MainActivity.kt: obtener contenedor/VM una vez por Activity y observar estado/ciclo de Inicio; grafo mínimo cuando dependencia esté aprobada.
- gradle/libs.versions.toml y app/build.gradle.kts: solo alias/dependencia Navigation aprobada; conservar schema export y el resto de versiones.
- app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DiagnosticMigrationTest.kt: abrir fixture mediante buildAppDatabase para probar la ruta real de producción.
- plans/designplans/domain-contracts.md y screen-dependencies.md: estado factual/propietario y límites, sin reescribir reglas históricas.
- plans/task.md y plans/reporte_fase4_etapa4_3.md al cierre real.

Crear pruebas:
- app/src/test/java/cl/cristian/bitacoraobd/ui/AppSessionTest.kt.
- app/src/test/java/cl/cristian/bitacoraobd/ui/HomeViewModelTest.kt.
- app/src/androidTest/java/cl/cristian/bitacoraobd/AppCompositionTest.kt.
- app/src/androidTest/java/cl/cristian/bitacoraobd/ui/AppLifecycleNavigationTest.kt.

### Host y sustitución de pruebas (precisión de auditoría)

Probar recreación sobre **MainActivity real**, no sobre una Activity alternativa que solo use el grafo. Añadir únicamente en app/src/androidTest/java/cl/cristian/bitacoraobd/:
- BitacoraTestRunner.kt: AndroidJUnitRunner que sustituye BitacoraApplication por TestBitacoraApplication en newApplication.
- TestBitacoraApplication.kt: hereda la Application productiva y sobrescribe appSession para devolver la sesión de una fixture de test. La fixture se instala antes de ActivityScenario.launch y se retira al cerrar el escenario; contiene fakes Domain, contadores, Deferred y scope propio. No sustituye el AppContainer de producción ni cambia la lógica de MainActivity.
- En app/build.gradle.kts, cambiar testInstrumentationRunner a cl.cristian.bitacoraobd.BitacoraTestRunner. El runner conserva el comportamiento estándar salvo newApplication; confirmar que las instrumentadas Room/DataStore/remotas anteriores siguen funcionando sin acceder a una fixture no instalada.

La fixture mutable existe solo en el APK de pruebas y no guarda Activity/Context de Activity. No añadir setter a Application/AppContainer de producción, service locator ni botones de prueba. Para escenarios sin Activity, no acceder a appSession de test sin fixture; esos tests siguen usando sus dependencias explícitas.

AppCompositionTest usa el AppContainer real de la Application, comprueba identidad sin abrir la base y verifica que la Factory productiva recibe appSession sin recrearla; no cierra recursos de proceso. Los tests de builder/migración B2 son los propietarios de bases temporales. AppLifecycleNavigationTest usa la misma MainActivity/Factory/adaptador real, pero appSession proviene de TestBitacoraApplication. Después de cada prueba: cerrar ActivityScenario, cancelar/join scope de fixture y retirar fixture. No borrar ni cerrar recursos compartidos mientras una Activity los usa. AppContainer mantiene las pruebas de builder/migración y cierre de bases temporales existentes.

No modificar Domain, entidades, DAOs, parser, socket ni gateway salvo defecto reproducible distinto: presentar evidencia y alcance antes de ampliarlo.

## 8. Checklist de entrada verificable

- [x] Reportes 4.1/4.2 revisados; omisión remota/permiso runtime y ausencia de recuperación de proceso conservados.
- [x] Código de builder ausente confirmado; único builder y addMigrations(MIGRATION_1_2) planificados.
- [x] Room v2/exportación/schema y dependencias declaradas inspeccionados.
- [x] Gateway/socket y pruebas de handshake/cancelación/cierre contrastados; límite TCP documentado.
- [x] Propietario de proceso y política de recreación/proceso definidos.
- [x] Mutex no reentrante, cierre fuera del lock y pruebas deterministas definidos.
- [x] UI-01–07 y DESIGN_READY separados de preparación técnica; no se necesita emulador para planificar.
- [ ] Usuario aprueba plan específico 4.3.
- [ ] Usuario autoriza dependencia Navigation Compose 2.10.2; sin ella se puede componer/probar sesión, pero no cerrar navegación/4.3 completa.
- [ ] En ejecución: registrar builder y probar apertura de v1 mediante ese builder, no marcarlo implementado por estar planificado.
- [ ] En ejecución: comprobar resolución/compatibilidad Navigation y runtime de recreación; evidencia aún no existe.

No quedan preguntas funcionales abiertas dentro de este plan. Compatibilidad de dependencia, comportamiento runtime y ausencia de duplicación son hipótesis técnicas con pruebas propuestas, no éxitos anticipados.

## 9. Ejecución en tareas pequeñas

Respetar orden Coder → Tester → QA exigido por el repositorio. Antes de codificar se entregan los casos observables siguientes al Coder; Tester añade/verifica tests después. No afirmar ciclo RED previo cuando no ocurrió.

### Fase A — Implementación de Código (Coder; no ejecuta tests)

- [ ] A0. Antes de componer Activity/grafo, fijar y usar los contratos corregidos de §5: finally conserva Success, Connected bloquea sin cierre y ExitAction/AuthorizedExit con consumo identificado. Confirmar archivos de test/runner y propiedad appSession de §7. Los casos B0 se entregan al Coder como criterios, sin ejecutar tests ni declarar los hallazgos resueltos.
- [ ] A1. Reconfirmar aprobación/dependencia y callers; crear DatabaseFactory con nombre estable y migración explícita, sin modificar esquema.
- [ ] A2. Crear Application/AppContainer, DataStore único y repositorios/casos de uso compartidos; registrar manifest.
- [ ] A3. Crear estado y AppSession con scope inyectado; refresh explícito de activo y barrera ante error de refresco.
- [ ] A4. Implementar connectJob/cierre único, cancelación fuera del Mutex y revalidaciones; no reconexión automática.
- [ ] A5. Implementar selección/borrado con el mismo Mutex, guardia Connected y refresco tras fallo parcial/cancelación.
- [ ] A6. Implementar estado mínimo de captura/guardado/decisión, lecturas explícitas y cancelación de lectura; no duplicar comandos, conservar captureId y última completa.
- [ ] A7. Integrar VM de Activity por constructor y HomeViewModel con activación/retry, sin temporizador.
- [ ] A8. Solo con dependencia autorizada, declarar alias e integrar NavHost/rutas/retornos/guardias, slots técnicos sin pantallas posteriores.
- [ ] A9. Actualizar contratos documentales factuales y entregar archivos/casos al Tester.

### Fase B — Tests TDD (Tester)

Usar runTest, StandardTestDispatcher/Main de prueba, CompletableDeferred y runCurrent; sin sleeps ni red externa para carreras. Fakes de interfaces Domain existentes; no introducir abstracción de transporte solo para tests.

- [ ] B0. **Primera verificación del Tester, antes del resto de la suite:** (a) completar totalmente connect exitoso → Connected y cero disconnect; seleccionar/borrar → cero cambios y cero disconnect; (b) cancelar Connecting con Deferred → cierre confirmado, sin deadlock ni Connected tardío; (c) solicitar Historial con captura → Save → recrear antes de consumir → un navigate; recrear después → ninguno adicional; Stay/fallo → cero navigate; F03 → diagnostic. Usar MainActivity real con runner/fixture de §7 para recreación y fakes directos de AppSession para carreras. No adelantar grafo sin dependencia autorizada.
- [ ] B1. AppCompositionTest: el contenedor de la Application entrega las mismas instancias Room/preferencias/gateway/sesión a dos VM; comprobar identidad sin abrir la base ni iniciar socket, escrituras o trabajos de sesión. No crear otro contenedor de proceso ni cerrar Room/DataStore compartidos desde este test. La base temporal y su cierre corresponden a B2. Verificar que no se guarda Context de Activity.
- [ ] B2. DiagnosticMigrationTest abre fixture v1 mediante builder de producción: v2 valida, IDs/relaciones/códigos/mantenimiento íntegros, captureIds únicos; sin fallback destructivo. Apertura nueva también funciona.
- [ ] B3. AppSessionTest: conectar primero suspende en fake; selección/borrado cancelan fuera del Mutex y no escriben hasta que se libera limpieza. Confirmar cancelación recibida, cierre terminado y entonces escritura; timeout de test detecta deadlock.
- [ ] B4. Invertir orden: selección y borrado suspendidos dentro de escritura; connect no llega al gateway hasta terminar. Caso Connecting en cola cancelado antes de iniciar. Doble connect no encola otro intento.
- [ ] B5. Disconnecting mantiene bloqueo hasta Deferred de cierre; Error con cierre pendiente tampoco escribe. Connected bloquea cambiar y cualquier borrado. Error tras cierre confirmado permite acción. Excepción de cierre deja bloqueo recuperable.
- [ ] B6. Captura aparece mientras solicitud espera Mutex: revalidación libera lock y solicita decisión nueva; ninguna escritura. Captura distinta tras una decisión vieja no se elimina. Stay no ejecuta acción.
- [ ] B7. Save pendiente concurrente: una sola invocación; salida/descarte esperan ID; error conserva mismo payload/captureId y cancela continuación; reintento no escanea; Failure no escribe. Descartar explícito elimina solo pendiente.
- [ ] B8. Read/scan doble toque produce una lectura; salir cancela incompleta y conserva completa anterior/vehicleId; recreación no cancela ni repite. Null y cero no se fusionan.
- [ ] B9. DataStore falla antes de Room: no delete; Room falla después: activo nuevo y fila conservada; refresco real antes de habilitar reintento. Refresco falla: bloqueo; retry exitoso recupera. Cancelación tras escritura exige refresco.
- [ ] B10. Selección ausente/obsoleta: fallback persistido explícitamente, sin escrituras desde Flow; válido no cambia; último borrado lleva a vacío. Conectar usa vehículo real actualizado.
- [ ] B11. HomeViewModelTest con Clock mutable: colección día 1 UPCOMING; avanzar al vencimiento sin emisión no cambia solo; desactivar/reactivar devuelve OVERDUE; una colección activa, retry de error, sin datos de otro vehículo.
- [ ] B12. ActivityScenario.recreate sobre MainActivity real durante connect/lectura/guardado con Deferred: mismo propietario/operación/pendiente/destino/error y contadores iguales a 1. Usar TestBitacoraApplication/BitacoraTestRunner y cleanup de §7; observar resultado después de recrear y verificar autorizaciones antes/después del consumo. Sin controles nuevos en producto.
- [ ] B13. Navegación instrumentada con grafo oficial: cuatro principales sin duplicados; Ajustes/Historial/detalle retornan al origen; F03 siempre retorna a Diagnóstico, cancelar intento permanece; Connected conserva conexión y Disconnecting permite salir sin habilitar acciones.
- [ ] B14. Nueva instancia de sesión simula nuevo proceso: Disconnected/sin pendiente, sin comandos/guardados; distinguir simulación de prueba real de muerte de proceso. No prometer recuperación.
- [ ] B15. Ejecutar comandos de §10 secuencialmente; registrar conteos, omisiones y dispositivo. Si no hay runtime disponible, dejar prueba pendiente; no declarar 100% PASS ni etapa cerrada.

### Fase C — Code Review & QA (Ponytail Reviewer)

- [ ] C0. Revisar primero evidencia B0 y los puntos de auditoría 1/2 y fixture/runner: Success no cierra, salida diferida no se duplica y la prueba de recreación usa MainActivity real. Si fallan, devolver a Coder → Tester antes de continuar el QA global.
- [ ] C1. Leer ponytail-review y revisar diff conectado a callers, contratos y tests.
- [ ] C2. Confirmar builder único, migración real, exportación, propiedad y cierre; sin pérdida ni fallback destructivo.
- [ ] C3. Trazar cada espera/lock: ninguna decisión/join propio/reentrada en Mutex; cancelación siempre accesible; revalidación después de adquirir; errores reales.
- [ ] C4. Verificar inyección manual mínima, único socket, sin dependencias extra ni clases especulativas, sin operaciones desde recomposición.
- [ ] C5. Contrastar todos los criterios de aceptación y límite visual; corregir hallazgos con Coder → Tester → QA.
- [ ] C6. Solo con veredicto APROBADO y comprobaciones requeridas ejecutadas: completar task.md, generar reporte_fase4_etapa4_3.md con evidencia/límites y detenerse consultando siguiente paso. No iniciar 4.4 automáticamente.

## 10. Comandos de verificación (solo después de aprobar)

Desde la raíz, usar studio_run_shell_command y studio_interact_shell secuencialmente:

1. ./gradlew :app:dependencies --configuration debugRuntimeClasspath — comprobar una versión resuelta de Navigation y Lifecycle, sin conflicto.
2. ./gradlew :app:testDebugUnitTest --tests 'cl.cristian.bitacoraobd.ui.*' — tests nuevos deterministas pasan.
3. ./gradlew :app:testDebugUnitTest — suite JVM completa pasa.
4. ./gradlew :app:compileDebugAndroidTestKotlin — compila harness/migración/navegación.
5. ./gradlew :app:connectedDebugAndroidTest — suite completa en dispositivo disponible; preferir Pixel_10 API37. Registrar remota omitida sin elm327Host como omisión, no éxito de hardware.
6. ./gradlew :app:lintDebug — sin errores nuevos.
7. ./gradlew :app:assembleDebug — BUILD SUCCESSFUL.
8. git diff --check — sin errores de espacios; inspeccionar cambios finales y schemas sin alteraciones inesperadas.

BUILD SUCCESSFUL no implica que pruebas omitidas hayan pasado; extraer conteos de informes. Si requiere corrección, repetir pruebas afectadas y suite requerida tras el cambio; no repetir por rutina sin cambios.

## 11. Puertas visuales y dependencias 4.4–4.8

Fuente: design-handoff.md, screen-dependencies.md, design-decisions.md, screen-flows.md, ui-ux-specification.md y agent-designer-instruction.md, contrastadas en esta sesión. Wireframes UI-01–07 aprobados el 2026-11-09 siguen aprobados; ningún paquete figura DESIGN_READY. No Figma en esta sesión.

| Entrega posterior | Dependencias técnicas / visuales todavía necesarias |
|---|---|
| 4.4 Tema/componentes | Aprobar valores de paleta, claro/oscuro/dinámico y contraste; no convertir tema actual en aprobación de producto. |
| 4.5 UI-02 | DEP-001–004,019,022: alta/reintento con ID, guardias de 4.3, formularios; variantes/barra/accesibilidad pendientes. |
| 4.6 UI-03/04 | DEP-006–012,020: formulario y permiso LAN runtime/revocación, handshake/persistencia parcial y decisiones visibles; F03/variantes/prototipo por validar. |
| 4.7 UI-05/06 | DEP-013–017,019,022: aislamiento, formularios, selector siete alertables + OTHER, detalle/retorno y validación visual. |
| 4.8 UI-01/07 | DEP-004/005/009/018/023: datos propios, preferencias/conversión y borrador; activación Inicio preparada en 4.3, representación pendiente. |
| Transversal | DEP-020/021: continuidad y grafo preparados técnicamente; scroll/formularios/diálogos/accesibilidad por pantalla y prototipo navegable pendientes. |

No bloquear composición/sesión por variantes visuales o falta de emulador durante planificación. Sí bloquear el paquete visual afectado hasta su puerta y mantener pendiente la prueba runtime ausente. No afirmar completada toda una DEP porque se preparó su soporte en 4.3.

## 12. Aceptación y exclusiones

4.3 es aceptable cuando:
- Application entrega una instancia compartida real por proceso; builder de producción migra v1 sin pérdida y valida v2.
- Todas las entradas técnicas UI sensibles van a AppSession y las carreras de ambos órdenes están probadas.
- Cancelar puede interrumpir un connect que retiene Mutex; se espera cierre confirmado antes de cambiar/borrar; Connected bloquea y Error no se toma como prueba de cierre.
- Captura/operación conservadas en recreación sin duplicar lectura/guardado; reintento conserva identidad y errores parciales refrescan datos reales.
- Activar Inicio reinicia evaluación con fecha actual, sin temporizador; no se filtran datos del contexto anterior.
- Dependencia aprobada/resuelta, grafo oficial y retornos probados sin implementar pantallas posteriores; sin nueva navegación propia.
- Suites requeridas/evidencias, checklist y QA APROBADO registrados con límites reales.

Excluidos: pantallas funcionales Compose 4.4–4.8, rediseño/Figma, aprobación visual repetida, Hilt, servicio, Demo, polling, reconexión/escaneo automático, hardware adicional, escritura ECU, odómetro ECU, cambios de contratos Domain, recuperación de captura tras muerte de proceso y transacción ficticia Room/DataStore.

**Riesgos concretos:** cancelación TCP puede esperar hasta timeout; no ocultar progreso ni liberar guardias antes de cierre. Un bypass directo de gateway/repositorios evade coordinación. DataStore/Room pueden quedar parcialmente actualizados; consultar antes de reintentar. Navigation 2.10.2 está consultada pero no resuelta/compilada aquí. Ninguna prueba runtime nueva fue ejecutada.

**Puerta final:** presentar este plan y dependencia al usuario y esperar confirmación explícita antes de cualquier codificación.

## 13. Auditoría Ponytail del plan — 2026-11-10

**Estado histórico de la auditoría: REQUIERE CORRECCIONES. Actualización de planificación:** las correcciones propuestas ya se incorporaron a §4/5/7 y a las tareas iniciales A0/B0/C0. Su implementación y validación siguen pendientes; este ajuste documental no aprueba ejecución ni dependencia. Los hallazgos siguientes conservan la descripción de la versión auditada para trazabilidad. Alcance: un usuario local, un proceso de app y acciones concurrentes de UI. Revisión principal y contraste independiente QA de solo lectura con ponytail-review; sin Gradle, pruebas ni Figma.

**Qué propone el plan:** crear composición única, conservar sesión/captura y coordinar operaciones OBD y navegación. Mantiene Room/DataStore separados y reserva las pantallas para sus etapas visuales.

### Debe corregirse

1. **El finally puede cerrar una conexión que terminó correctamente.**
   - **Evidencia:** §5, protocolo paso 4, ordena que el finally del connectJob ejecute gateway.disconnect sin limitarlo a cancelación. En app/src/main/java/cl/cristian/bitacoraobd/data/obd/Elm327ObdGateway.kt:67–82, el gateway publica Connected y devuelve Success manteniendo el socket; disconnect en :92 realiza un cierre real. DD-26 exige conservar la conexión hasta desconexión explícita.
   - **Caso y efecto:** handshake exitoso → termina connectJob → finally descrito llama disconnect → el usuario queda desconectado después de conectar.
   - **Corrección mínima propuesta:** reemplazar la frase del finally por: «Si el intento se cancela o aborta, completar la limpieza necesaria sin esperar al Job de cierre externo; al terminar con éxito conservar la conexión. El finally común libera referencias/estado operativo y el Mutex; nunca desconecta incondicionalmente. La limpieza interna de cancelación del gateway se reutiliza. El cierre externo cancela el intento, puede pedir disconnect para interrumpir IO, espera join fuera del Mutex y confirma cierre final antes de habilitar acciones».
   - **Precisión asociada:** selección/borrado comprueban Connected antes de decidir cancelar un intento; no desconectar una sesión establecida por el mero hecho de que quede un Job registrado. El gateway real publica Connected y retorna sin suspensión intermedia; no se afirma una segunda carrera reproducida distinta del error de finally.
   - **Prueba que debe añadirse a B3/B5:** fake connect devuelve Success y publica Connected; completar totalmente connectJob, comprobar que no hubo disconnect y que permanece Connected. Seleccionar y borrar bloquean, conservan activo/filas y no cierran socket. Cancelación real sigue limpiando y liberando el Mutex.
   - **Si se omite:** conectar podría tener un éxito transitorio seguido de desconexión involuntaria.

### Debe concretarse en el plan

2. **La salida diferida carece de destino y consumo definidos.**
   - **Evidencia:** §5 propone requestExit(): Unit y retener la acción solicitada durante decisión/guardado. §6 exige destinos distintos según Atrás, pestaña, F03 y descarte DD-25; B12 exige conservar destino sin repetir navegación al recrear. La firma no describe cómo entregar o consumir esa autorización.
   - **Caso y efecto:** con captura pendiente el usuario solicita Historial, guarda y rota antes de completar el guardado. Sin acción concreta y regla de consumo, no queda especificado qué destino se abre ni cómo evitar un segundo navigate al observar el estado de nuevo. Es una carencia del contrato propuesto, no un fallo runtime reproducido.
   - **Corrección mínima propuesta:** requestExit(action: ExitAction), donde ExitAction es dato de Presentation con Back o destino permitido y sus IDs. Retener una única solicitud en memoria junto con su identidad y la captureId a resolver, nunca callbacks de Activity/NavController. Tras resolver, publicar autorización identificada; el adaptador Main ejecuta la navegación y confirma su consumo sin suspensión entre comprobar, navegar y consumir. Recreación vuelve a observar únicamente lo no consumido. No crear bus genérico de eventos ni un segundo back stack.
   - **Reglas concretas del resultado:** Stay cancela la solicitud; fallo de guardado la cancela y mantiene captura; éxito autoriza la acción retenida una vez; «Descartar y volver a Inicio» autoriza Inicio; salida final F03 normaliza a Diagnóstico. En 4.3 solo definir las acciones necesarias por el grafo técnico, sin implementar formularios/diálogos futuros.
   - **Pruebas que deben concretarse en B7/B12/B13:** guardar pendiente → rotar antes de autorizar → un retorno al destino solicitado; rotar después de consumir → cero navegaciones adicionales; fallo/Stay → no navegar; salida de F03 → Diagnóstico aunque la intención inicial fuera otra pestaña.
   - **Si se omite:** Coder y Tester deberán inventar durante ejecución un protocolo que afecta DD-25/26/32.

### Precisión adicional de pruebas

B12 menciona un harness con sesión inyectada, pero §4 solo define la creación productiva mediante Application/AppContainer. Antes de implementar esa prueba, describir el host y la factory de test y qué integra realmente: un host alternativo prueba sesión/grafo compartidos; no demuestra por sí solo que MainActivity obtenga una única instancia correctamente. Mantener AppCompositionTest para ese cableado y probar recreación sobre el adaptador de lifecycle/navegación realmente compartido por producción y harness. No añadir un service locator mutable, setters de prueba al contenedor de producción ni controles de depuración en main. No se considera un bug de código existente.

### Elementos conservados y límites de la auditoría

- AppSession de proceso está justificado por conservar pendiente mientras viva el proceso; no se propone eliminarlo solo por reducir archivos. ViewModel no debe duplicar su estado.
- Room builder con migración explícita, DataStore único, constructor injection, errores parciales y ausencia de recuperación tras muerte del proceso son coherentes.
- No se encontró otro deadlock concreto: el cierre externo espera al intento, pero el intento no espera al cierre ni vuelve a tomar el Mutex. Se preservan sus pruebas deterministas.
- No se eleva refreshVehicleContext a defecto sin un camino normal que repare el activo estando Connected: el plan ya exige coordinación y bloquea conectar antes de inicializar. El bypass directo está expresamente fuera de su garantía.
- Navigation sigue pendiente de autorización y compatibilidad. No se recomienda otra librería ni cambiar versión por esta auditoría.
- La lista de casos B1–B15 es propuesta de verificación; no evidencia de comportamiento aprobado ni ejecución.

**Veredicto histórico Ponytail: corregir 1 y concretar 2 antes de implementar.** Correcciones ahora descritas en el cuerpo operativo y priorizadas en A0/B0/C0; no se declara QA aprobado por editar documentos. En ejecución implementar ese contrato desde el principio y validar primero los casos B0, después revisar con C0. No requiere cambios de contrato Domain ni nueva aprobación de wireframes; la aprobación de ejecución 4.3 y de dependencia continúa pendiente.

**No comprobado en la primera auditoría:** runtime, compatibilidad Gradle, hardware ni ausencia de duplicados en ejecución. El riesgo detectado pertenecía a la instrucción de cierre de la versión anterior del plan, no a código implementado.

## 14. Segunda auditoría Ponytail — 2026-11-10

**Veredicto documental: sin discrepancias funcionales nuevas detectadas; apto para someter a aprobación y preparar ejecución.** Revisión principal y QA independiente de solo lectura contrastaron los cambios con gateway real, coordinación aprobada en 4.2 y reglas de salida. No equivale a QA APROBADO de implementación ni a prueba runtime.

- §5 conserva Success/Connected, con limpieza de cancelación/error y cierre externo fuera del Mutex; no se encontró ciclo concreto de esperas.
- ExitAction/AuthorizedExit y completeExit definen destino y consumo una vez desde Main sin suspensión; se contemplan recreación, Stay, fallo de guardado y F03.
- §7 prueba MainActivity real mediante TestApplication/runner, con sustitución confinada al APK de pruebas.
- A0/B0/C0 priorizan los hallazgos anteriores; §13 conserva el resultado histórico sin sustituir el cuerpo operativo actual.
- Precisión documental incorporada: B1 comprueba identidad sin abrir/cerrar recursos de proceso; B2 posee y cierra su base temporal. No se agrega otro punto de inyección solo para ello.

La aprobación del plan y de Navigation Compose sigue pendiente en esta sesión. El prompt de traspaso está en plans/prompt_ejecutar_fase4_etapa4_3.md: su texto autoriza expresamente ambas al enviarlo como instrucción de usuario en la nueva sesión; su mera existencia no marca aprobación en task.md.

**Límites:** sin código, Figma, Gradle ni pruebas ejecutadas. Compatibilidad de dependencia, carreras, migración y recreación requieren evidencia en la ejecución autorizada.

