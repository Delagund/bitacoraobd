# Etapa 4.5 — Vehículos y formularios (UI-02): Implementation Plan

> **Para agentes:** usar writing-plans para mantener este artefacto y executing-plans al ejecutar. Protocolo obligatorio y secuencial: Coder → Tester → QA/Ponytail; dispatching-parallel-agents para crear los hilos especializados, sin ejecutar roles simultáneamente.
> **Estado: APROBADO; ejecución completada y QA/Ponytail APROBADO.** Aprobación explícita del usuario recibida antes de codificar. El alcance, los resultados y los límites de verificación están registrados en §11 y en `plans/reporte_fase4_etapa4_5.md`.

**Objetivo:** implementar gestión real de vehículos y formularios, guardia D2 y tema fijo claro/oscuro transferido de 4.4, conservando datos y recuperación de fallos.
**Arquitectura:** mantener AppSession como único coordinador de operaciones de vehículo/OBD y su Mutex. Un VehiclesViewModel conserva borrador y presentación durante recreación; Compose representa estado y emite eventos. Room/DataStore conservan contratos y esquema.
**Stack:** Kotlin, Flow/coroutines, ViewModel y factories manuales, Navigation Compose 2.10.2, Material 3, Room y DataStore existentes. No agregar dependencias.

## 1. Alcance y puertas

Incluye lista con Activo, Usar este vehículo, menú Editar/Eliminar, alta/edición, confirmación de cascada, validación de entrada, carga/error/reintento, descarte, recuperación parcial y navegación UI-02. Incluye tema y muestras de componentes en ambos modos. Los wireframes base UI-01–07 siguen DESIGN_READY por decisión del usuario; esto no acredita variantes posteriores, accesibilidad ni producto terminado.

No incluye pantallas completas de Inicio, Diagnóstico, Conexión, Historial, Mantenimiento ni Ajustes. Agregar solo accesos mínimos reales al alta desde vacíos y retorno a Inicio, sin presentar un dashboard terminado. No Demo, hardware adicional, sondeo, Hilt, tablas, migraciones, nuevas librerías ni recuperación de captura tras muerte de proceso.

La aprobación de este plan autorizará las extensiones concretas de Presentation descritas abajo. No se propone cambiar firmas Domain/Data ni nuevas validaciones de negocio. Si implementación descubre que necesita hacerlo, documentar la desviación material antes de actuar. Tokens finales y muestras se revisan antes de cerrar la integración visual; no reabrir apariencia A, modo sistema o wireframes base.

## 2. Auditoría y evidencia actual (lectura estática, sin ejecución de pruebas)

Raíz de producción: `app/src/main/java/cl/cristian/bitacoraobd/`. Raíz JVM: `app/src/test/java/cl/cristian/bitacoraobd/`. Raíz instrumentada: `app/src/androidTest/java/cl/cristian/bitacoraobd/`.

| Archivo / contrato auditado | Evidencia y consecuencia |
|---|---|
| `domain/model/Vehicle.kt` | ID ≥0 y km ≥0; no exige textos ni rango de año. year Int, 0 representa ausencia según DD-24. No inventar obligatoriedad, catálogo o rango de años. |
| `domain/repository/VehicleRepository.kt`, `data/repository/RoomVehicleRepository.kt` | saveVehicle(Vehicle): Long inserta ID 0, actualiza ID existente y falla si no existe. No selecciona activo. Retener ID antes de cualquier segunda operación. |
| `data/local/dao/VehicleDao.kt` | Lista ordenada brand/model/id; update no REPLACE; getFirst ordena por ID pero sesión usa primer elemento de lista. Mantener orden actual y fallback de sesión. |
| `data/local/entity/Entities.kt`, `EntityMappers.kt` | Mapeo directo; FK CASCADE vehículo→sesión→códigos y vehículo→mantenimiento. No nueva tabla ni cambio de schema. |
| `domain/usecase/DeleteVehicleUseCase.kt` | Connected bloquea cualquier borrado; DataStore se actualiza antes de Room si se elimina activo. Failed puede dejar otro activo y vehículo original existente. CancellationException se propaga. |
| `domain/repository/AppPreferencesRepository.kt`, `data/local/datastore/DataStoreAppPreferencesRepository.kt` | Activo nullable; escritura exige ID positivo o null, fallos no se convierten en defaults. Settings incluye unidades; no transacción común con Room. |
| `ui/AppSession.kt` | Propietario por proceso; un Mutex no reentrante, runSensitive, pendingDecision, authorizedExit, jobs de conexión/cierre/lectura/guardado. select/delete existentes no devuelven resultado; rechazos Connected pueden ser silenciosos. No API de alta/edición. Estado inicial lista vacía no distingue carga. |
| `ui/AppSession.kt` refresco | refreshVehicleContextLocked consulta Room y DataStore, repara activo inválido; primer alta necesita controlar esta reparación para no ocultar fallo de selección. contextRefreshFailed bloquea operaciones; éxito parcial de borrado refresca manteniendo error. |
| `ui/AppViewModel.kt`, `AppContainer.kt`, `MainActivity.kt` | Factories manuales; Application conserva sesión. MainActivity crea App/HomeViewModel y tema. Reutilizar la misma sesión, repositorios y scope, no otro mutex/gateway. |
| `ui/navigation/AppNavigation.kt` | Destinos Text; whitelist rechaza argumentos inválidos. R03 consume autorización, failExit libera y CancellationException se propaga. No barra ni formularios reales. |
| `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` | Paleta morada de plantilla; dynamicColor=true. bodyLarge 16sp/24sp y resto Material. Cambiar esquema, conservar tipografía de plataforma y evitar sistema propio de componentes. |
| `app/build.gradle.kts`, `gradle/libs.versions.toml` | Compose, Material3, Navigation, Room, DataStore, coroutines-test, Compose UI test y tooling declarados; minSdk26/target37. No Gradle ejecutado para esta auditoría. |
| JVM `ui/AppSessionTest.kt` | Casos de Connected, cancelación de connect, fallo Room tras selección, Save/Stay/fallo, autorización R03. FakeVehicles actual no inserta ni muta realmente filas; adaptarlo para pruebas de alta/edición. |
| JVM `domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt` | Borrado, fallback, fallo de preferencias y aislamiento; conservar regresiones. |
| Instrumentada `data/local/AppDatabaseTest.kt` | Cascada y update sin destruir relaciones; año 0. Son pruebas DAO, falta recorrido RoomVehicleRepository→UI. |
| Instrumentada `MainActivityRecreationTest.kt` | Activity real, captura/recreación, autorización y F03; no formulario/borrador ni guardia D2. |
| Reportes `plans/reporte_fase4_etapa4_3.md`, `reporte_fase4_etapa4_4.md` | Evidencia histórica de suites y QA; no resultados de esta sesión. 4.4: SM-A066M/API36, 24 casos, 0 fallos, 1 omitido sin elm327Host; excepción real de navigate no inyectada. |

Fuentes funcionales revisadas: AGENTS, contexto maestro, plan general 4.4 y Fase4, instrucciones de diseñador, design-system, DD-24/27/28/30/31/32/37/42 y D1–D4, screen-flows R01–05/R08, ui-ux-specification, domain-contracts, matriz, handoff y referencias Figma. Prevalecen las cabeceras vigentes y decisiones del usuario; los snapshots que dicen “sin consumidor”, “no DESIGN_READY”, “4.3 siguiente” o “colores dinámicos” no reabren decisiones.

### Callers y alcance de cambios

Búsqueda de APIs en Kotlin realizada con studio_grep:
- saveVehicle: implementación Room y fakes de `DiagnosticAndMaintenanceUseCasesTest.kt`, `MaintenanceDashboardDeleteUseCasesTest.kt`, `AppSessionTest.kt`, `HomeViewModelTest.kt`, `TestBitacoraApplication.kt`; no consumidor de alta productivo actual.
- selectVehicle/deleteVehicle de sesión: `AppSessionTest.kt`; nuevos callers serán VehiclesViewModel. deleteVehicle del repositorio: DeleteVehicleUseCase y fakes.
- setActiveVehicleId: AppSession (refresco/selección), DeleteVehicleUseCase, DataStore y fakes/prueba instrumentada DataStore.
- Constructores AppSession: `BitacoraApplication.kt`, `AppSessionTest.kt`, `TestBitacoraApplication.kt`. Mantener firma si basta.
- AppNavigation: MainActivity. BitacoraOBDTheme: MainActivity; colores morados usados solo por Theme. Mantener pruebas de navegación y recreación al sustituir etiquetas.
- Nuevas piezas: `ui/vehicles/VehiclesViewModel.kt`, `VehiclesScreen.kt`, `VehicleForm.kt`; tipos de borrador/resultado pueden permanecer en ViewModel/AppSession mientras sean legibles. No separar archivos vacíos por patrón.
- Modificar: AppSession, AppNavigation, MainActivity (factory/ViewModel de Activity), Color/Theme, `app/src/main/res/values/strings.xml`. Type solo si una muestra demuestra necesidad. AppViewModel/AppContainer no requieren nuevas capas; exponer dependencias ya disponibles mediante factory.
- Pruebas nuevas: JVM `ui/vehicles/VehiclesViewModelTest.kt`, `VehicleFormValidationTest.kt`; instrumentada `ui/vehicles/VehiclesScreenTest.kt`, `data/local/RoomVehicleRepositoryTest.kt`. Ampliar AppSessionTest y MainActivityRecreationTest/fixture existente. No cambiar generated KSP ni schemas.

## 3. Decisiones técnicas y firmas propuestas

Elegida: extender AppSession y añadir un ViewModel de UI-02. Alternativa descartada: guardar directamente desde ViewModel; permite competir con conexión y omite D2. Otro coordinador/use case con mutex propio duplicaría coordinación y orden de locks. Domain/Data actuales bastan; la guardia global es de sesión.

Firmas de Presentation propuestas (no implementadas):

```kotlin
// Mantener selectVehicle(Long), deleteVehicle(Long), refreshVehicleContext().
fun openVehicleEditor(vehicleId: Long)
fun saveVehicle(vehicle: Vehicle, original: Vehicle?) // original null = alta
fun retryVehicleSelection(vehicleId: Long)
fun consumeVehicleResult(resultId: Long)

// Nuevos campos de AppSessionState:
// vehicleLoadStatus: VehicleLoadStatus = Loading
// vehicleResult: VehicleActionResult? = null
// Resultado con identidad monotónica y consumo explícito.
enum class VehicleLoadStatus { Loading, Ready, Failed }
enum class VehicleAction { OpenEditor, Save, Select, Delete }
enum class VehicleRecovery { None, RetrySave, RetrySelection, RefreshOnly, Reconcile }
data class VehicleActionResult(
    val id: Long,
    val action: VehicleAction,
    val vehicleId: Long?,
    val vehicle: Vehicle? = null,
    val error: String? = null,
    val recovery: VehicleRecovery = VehicleRecovery.None
)
```

Un resultado es específico de vehículos, sin bus ni framework genérico. Retenerlo hasta consumo explícito; impedir doble operación desde la sesión y conservarlo durante recreación. Con error nunca navegar. La sesión valida estas invariantes; el ViewModel no elige recuperaciones a partir de textos de error:

| recovery | Contrato y única acción permitida |
|---|---|
| None | Éxito completo o rechazo sin retry automático; error no nulo sigue impidiendo navegación. |
| RetrySave | Fallo confirmado sin persistencia. Revalidar guardias/original antes de guardar el borrador. No usar ante resultado incierto. |
| RetrySelection | Alta persistida: vehicleId positivo obligatorio. Consultar Room/DataStore y seleccionar ese ID solo si hace falta; nunca insertar. |
| RefreshOnly | Mutación confirmada y contexto sin actualizar: vehicleId conserva el ID afectado. Repetir solo lectura; no save/delete. Si era primer alta, comprobar selección real y pasar a RetrySelection si falta. |
| Reconcile | Persistencia incierta o lectura de recuperación fallida: bloquear nueva mutación y consultar estado real. No deduplicar por textos ni reinsertar automáticamente. |

Después de RefreshOnly correcto, completar la intención original una sola vez. Si la reconciliación de alta sigue sin poder identificar un ID, conservar aviso y exigir revisar lista antes de iniciar explícitamente otra alta; no prometer idempotencia tras muerte de proceso. retry() del ViewModel despacha según recovery; usa refreshVehicleContext() para lectura y retryVehicleSelection(id) para selección. Un nuevo intento sustituye el resultado anterior solo bajo la reserva de operación de sesión.

VehiclesViewModel usa `StateFlow<VehiclesUiState>` con lista/activo desde AppSession, formulario nullable, original, campos String, unidad leída, presentación derivada de carga/operación/error de sesión, confirmación de borrado y salida pendiente local. Eventos concretos `addVehicle()`, `editVehicle(id)`, `save()`, `selectVehicle(id)`, `requestDelete(id)`, `confirmDelete()`, `requestLeave(action)`, `discardDraft()`, `keepEditing()`, `retry()`. No mutable state en composables para negocio. Capturar snapshot original al abrir; rechazar guardar si registro fue eliminado o cambió desde esa lectura, conservando borrador y ofreciendo recargar explícitamente. No sobrescribir corrección de odómetro hecha por otro guardado. No cambiar ID del formulario al cambiar activo.

Propiedad única del estado: AppSession conserva lista/activo, carga de contexto, operación en curso, resultado/recuperación, errores de backend y autorización de salida. VehiclesViewModel combina esos flujos para producir VehiclesUiState sin copias mutables de loading/saving/error del backend. Solo conserva borrador/original, validación de campos, unidad del formulario y su carga/error local, confirmaciones y origen de retorno. No copia el resultado ni el ID persistido a otro registro de operaciones; los deriva de sesión. Compose solo conserva estado visual guardable, como scroll. Recrear Activity no reinicia operaciones ni borra errores de sesión.

Carga de preferencias de unidades debe poder fallar: no usar defaults como lectura exitosa. Lista/carga/errores de contexto proceden del propietario; formulario no se inicializa antes de completar registro y unidad. Reintentar carga no borra borrador ya editado. Usar collector lifecycle con APIs disponibles y repeatOnLifecycle; no agregar lifecycle-compose solo por conveniencia.

## 4. Guardia D2, concurrencia y captura pendiente

1. UI muestra razón del bloqueo y no permite editar activo Connected: “Desconecta el OBD antes de editar este vehículo”. No desconectar silenciosamente. Editar vehículo no activo Connected sí está permitido; selección y cualquier borrado Connected siguen bloqueados.
2. La comprobación visual no autoriza escritura. openVehicleEditor entra por runSensitive y consulta registro/activo real; saveVehicle revalida bajo el mismo Mutex inmediatamente antes de Room. Ningún caller UI puede escribir VehicleRepository ni preferencias directamente.
3. Reserva sensitiveOperationRunning se establece antes de lanzar trabajo como ahora; reutilizar runSensitive y su manejo de pendingDecision. No mantener el mutex mientras se espera respuesta de usuario ni saveJob/closeJob/connectJob. Para transitorios reutilizar cancelConnectingAndAwait fuera del lock; esperar cierre real. Error/Disconnecting no habilitan escritura sensible hasta confirmar Disconnected. Si connect ya terminó Connected, no cerrarlo automáticamente: bloquear edición del activo.
4. Para editar no activo Connected, no llamar al cierre de conexión. Releer activo/registro dentro del lock; si ahora es activo y Connected, rechazar. Para alta adicional Connected mantener activo válido, sin tocar socket.
5. Después de cerrar transitorios adquirir mutex; comprobar de nuevo contexto, operación, conexión, vehículo y original. contextRefreshFailed exige refresco explícito correcto antes de mutar. No confiar en snapshot del botón.
6. connect conserva el mismo Mutex durante handshake: si ganó conexión, el guardado verá Connected y se bloqueará; si ganó guardado, conexión esperará y leerá el registro actualizado. No adquirir dos veces el mutex; crear helpers privados *Locked* solo donde evitan llamadas públicas recursivas.
7. Revalidar captura después de cualquier espera/cancelación: una lectura que terminaba puede producir una captura después de la primera revisión de runSensitive. Si aparece pendiente, salir del lock y volver al bucle de decisión, nunca mutar vehículo ni descartar implícitamente. Evitar join de lectura bajo mutex. Esta regresión es necesaria para nuevas acciones y selección/borrado ya conectados.
8. UI común de pendingDecision en AppNavigation muestra Guardar/Descartar/Seguir aquí. Guardar conserva captureId y espera operación existente; fallo conserva captura y cancela intención. Descartar avisa pérdida y continúa la intención original global; el botón diagnóstico específico “Descartar y volver a Inicio” pertenece a 4.6. Stay no ejecuta operación. No autorizar navegación por alta parcial o guardia rechazada.
9. Primero resolver borrador local ante salida, después captura de sesión, después navegación. No dos diálogos simultáneos. Cancelar explícito del formulario descarta borrador; Back/flecha/pestaña con cambios pregunta; cerrar IME no es salir. Guardado en curso espera resultado, no descarta ni duplica escritura.
10. CancellationException se propaga en límites públicos; no convertir cancelación en éxito. Solo para el alta con ID0, tras validar guardias y adquirir el Mutex, acotar NonCancellable a inserción Room y publicación inmediata del ID y recuperación en AppSession. No extenderlo a edición, borrado, selección DataStore, refresco, socket, espera del usuario ni navegación. Antes de entrar comprobar cancelación; al salir volver a comprobarla y propagar CancellationException: el ID permanece retenido, pero la cancelación no autoriza navegación. Evita perder el ID entre commit de inserción y recepción por cancelación con proceso vivo. Si falla antes de obtener ID, mostrar error; no inferir éxito por coincidencia de marca/modelo ni reinsertar automáticamente ante resultado incierto. No prometer idempotencia durable del alta tras muerte de proceso: recargar lista real antes de nueva acción.

## 5. Datos y recuperación

- Año vacío → 0 al construir; 0 → vacío/No informado al mostrar. Año informado debe poder convertirse a Int; no nuevo rango de fechas ni prohibición de datos que Domain acepta.
- Texto opcional vacío permitido, sin catálogos ni validación no acordada. Etiqueta de fila con marca/modelo vacíos usa “Vehículo” y contexto inequívoco para accesibilidad, sin inventar datos persistidos.
- Km enteros no negativos; vacío/inválido/overflow muestra error de campo y no guarda, alta propone 0 visible. Permitir corrección descendente manual; conservar sesiones históricas. ID solo lo provee selección/Room.
- Millas DD-37: un decimal, separador local coma o punto normalizado sin aceptar miles ambiguos, convertir ×1.609344 y redondear al entero cercano; rechazar no finito, negativo u overflow antes de Int. 10,0 mi →16 km. Si no se edita distancia, conservar currentMileageKm original, no convertir el texto mostrado de vuelta. Cambiar otro campo no altera odómetro. Unidad congelada solo durante formulario activo; Ajustes no puede quedar detrás del formulario.
- Dirty se deriva de valores actuales frente a iniciales, no flag irreversible; volver a originales elimina confirmación. Scroll guardable; borrador y errores de validación en ViewModel de Activity; operación/errores backend derivados de AppSession; limpiar tras salida consumida para que restoreState no resucite formulario descartado.
- Alta inicial: bajo coordinación consultar lista/activo real, insertar una sola vez, publicar y retener ID; intentar selección separada. Si falla, mostrar “Vehículo guardado; no se pudo seleccionarlo” y Reintentar selección por ese ID. Verificar existencia y preferencia real antes del retry; si ya es activo, completar sin reescribir; si desapareció, informar y recargar. No volver a saveVehicle con ID0.
- Alta adicional: conservar activo válido y volver a lista. Si activo era inválido, aplicar fallback DD-27 del listado real sin convertir automáticamente el adicional en activo.
- No ejecutar reparación genérica de activo entre inserción inicial y registro del resultado de selección: podría ocultar fallo parcial. En recuperación separar lectura de estado real y reparación explícita; mantener error hasta acción/confirmación correcta.
- Selección exitosa → Inicio. Fallo → lista y estado real; no anunciar éxito por cambio optimista. Reintento refresca Room/DataStore antes de escribir.
- Borrado confirma vehículo y pérdida de diagnósticos/códigos/mantenimientos. Cancelar no llama backend. Si quedan vehículos, lista; si último, Inicio vacío y active=null. Fallo no navega: refrescar los dos almacenes, conservar error incluso si cambió activo. Si consulta también falla, bloquear mutaciones y ofrecer retry de lectura; no repetir ciegamente borrado.
- En edición, conservar mismo ID; error conserva borrador. Guardado correcto refresca contexto y vuelve a lista. Si escritura fue exitosa pero refresco falla, informar “Guardado; no se pudo actualizar la lista” y repetir solo lectura, no escritura.
- Durante edición no reemplazar borrador al emitir lista o preferencias. Si cambia registro/contexto, advertir y revalidar; recargar necesita descarte explícito si hay cambios.

## 6. Navegación concreta sin anticipar 4.6–4.8

Conservar rutas AppRoute y R03. UI-02 lista/formulario son estados dentro de vehicles, no un segundo NavController ni grafo paralelo. Añadir NavigationBar Material3 de cuatro destinos con selección por back stack real. Todas las salidas pasan por resolver borrador y requestExit; BackHandler canaliza Back y respeta IME. AppNavigation recibe VehiclesViewModel creado en MainActivity mediante factory manual; propiedad de Activity conserva formulario durante recreación.

Inicio mantiene placeholder del resto del contenido: cuando carga real termina sin vehículos, texto de vacío y botón Agregar; con vehículos solo acceso a Vehículos/contexto mínimo, no simular métricas. Diagnóstico/Mantenimiento/Historial sin vehículo ofrecen entrada al mismo alta mediante acción del ViewModel y requestExit(VEHICLES), sin implementar sus pantallas. Iniciar formulario solo tras autorización consumida, no al pulsar antes de resolver captura. Primer alta+selección desde cualquiera de estos orígenes termina HOME; alta adicional/edición lista; selección HOME; último borrado HOME. Cancelación sin guardar vuelve al origen real guardado al abrir el flujo, con Back autorizado; no duplicar entradas por recomposición. Desde lista abrir alta/edición no añade destino.

F03 conserva retorno a Diagnóstico y no se amplía su comportamiento; probar regresión. Ajustes no se abre conservando formulario debajo: resolver descarte, limpiar tras navegación consumida. Si navegación falla, conservar estado para recuperación, sin éxito falso. Consumo del resultado y autorización deben ser una sola transición por ID, sin loops por state.collect.

## 7. Tema transferido y validación visual

Modificar Theme para eliminar rama y parámetro dynamicColor, mantener darkTheme=isSystemInDarkTheme() para producción y previews forzadas. ColorScheme claro/oscuro explícitos, sin selector de tema. Material3 Button, OutlinedButton, TextButton, OutlinedTextField, Card, AlertDialog, DropdownMenu, NavigationBar y Scaffold; no wrappers de un solo uso.

Punto de partida propuesto, no tokens finales aprobados:
- Oscuro: background grafito #161D1C, surface #252E2C, onSurface/onBackground #E5E9E7; primary #55E6A5, onPrimary #092118; error #FFB4AB y texto oscuro sobre contenedor de error definido en muestra.
- Claro: background #E5E9E7, surface #FFFFFF, onSurface/onBackground #161D1C; primary #006B47, onPrimary #FFFFFF; error #B3261E.
- Definir explícitamente pares container/onContainer, surfaceVariant/onSurfaceVariant, outline, inverse y scrim empleados por componentes, evitando restos morados. Secondary puede reutilizar familia verde; no crear paleta decorativa adicional. Ámbar #7C2D00 claro / #FFB77D oscuro para avisos con texto, solo donde UI-02 lo use; no añadir abstracción semántica para pantallas futuras.

Los ocho ratios existentes en design-system son evidencia de pares candidatos opacos, no componentes aprobados. Antes de cerrar A: producir previews/capturas de lista, formulario, diálogo, botón principal/secundario/deshabilitado, campo foco/error, carga y fallo parcial en ambos modos. Registrar hex, rol, fondo adyacente, alpha compuesto y ratio de cada texto/control/indicador necesario: normal ≥4.5:1, grande ≥3:1, componente/indicador ≥3:1; el verde luminoso sobre blanco no se asume legible. Diferenciar Activo/error/advertencia con texto/forma.

Controles táctiles ≥48dp, semántica de selección/error, etiquetas de acciones asociadas al vehículo, orden de foco y TalkBack, contraste de barras del sistema, fuente 1x/2x, ventana estrecha/apaisada, scroll y teclado sin ocultar Guardar/error. Usar padding de Scaffold y consumir insets una sola vez; imePadding donde corresponda, sin alturas fijas que recorten texto. No copiar otras pantallas de la app de referencia.

Capacidades Figma redescubiertas en catálogo de esta sesión: herramientas de lectura/capturas/creación/variantes y use_figma en conector general; studio expone creación/lectura pero no se asume escritura arbitraria. No se probó permiso de edición ni se modificó archivo Figma. La limitación histórica de 4.4 no demuestra bloqueo actual. En ejecución, cargar skills Figma apropiadas y comprobar sesión/permisos antes de operar si se necesita editar variantes; usar previews Compose para preparación técnica sin bloquearla por Figma. No declarar variantes aprobadas solo por producirlas.

## 8. Tareas pequeñas — ejecución después de aprobación

### Fase A — Implementación de Código (Coder; COMPLETADA)

- [x] A1–A6. AppSession mantiene el Mutex único y coordina carga, D2, CRUD, selección, cascada, alta parcial, ID retenido, recuperación real y cancelación.
- [x] A7–A9. VehiclesViewModel y componentes Material 3 implementan borrador, validación, conversión de millas, lista/formulario, confirmación, errores y reintento.
- [x] A10. MainActivity y navegación integran UI-02, retornos mínimos desde destinos vacíos, diálogo global de captura y salida encolada durante guardado.
- [x] A11–A12. Tema fijo claro/oscuro y roles documentados; callers, pruebas, matriz y handoff reconciliados. Sin nuevas dependencias ni coordinadores.

### Fase B — Tests TDD (Tester; AUTOMATIZADA COMPLETADA, VALIDACIÓN MANUAL PARCIAL)

El Tester amplió pruebas de AppSession, millas, recreación, selección/recuperación y salida durante guardado. Se usaron pruebas controladas para carreras; no se afirma una fase RED no registrada. La inspección manual de TalkBack, fuente ampliada, contraste renderizado e IME sigue pendiente.

- [x] B1–B6. JVM cubre D2, concurrencia/estado, captura y recuperación, ID retenido, selección, borrado y cancelación; suites instrumentadas cubren Room y recreación relevante.
- [x] B7–B12. ViewModel/formulario, Room, navegación y recreación cuentan con cobertura automatizada en JVM/instrumentadas y regresiones existentes; el detalle ejecutado figura en el reporte. No se inyectó una excepción real de NavController.navigate.
- [ ] B13. Pendiente manual: TalkBack/foco, fuente 2x, ventana/IME y contraste renderizado de controles en ambos modos. No se considera verificado por ratios calculados ni tests de otra capa.
- [x] B15–B16. Pruebas de cancelación/retención de ID, RefreshOnly/Reconcile, no duplicación y estado durante recreación se ejecutaron dentro de las suites registradas.
- [x] B14. `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, `:app:connectedDebugAndroidTest`, `:app:lintDebug`, `:app:assembleDebug` y `git diff --check` pasaron. SM-A066M/API 36; elm327Host no configurado, por lo que la integración ELM327 se omitió.

Ejemplo de aserciones de comportamiento para B4 (usar fixture ampliado, no una API productiva nueva):
```kotlin
assertEquals(1, vehicles.insertCalls)
assertEquals(createdId, session.state.value.vehicleResult?.vehicleId)
assertEquals(VehicleRecovery.RetrySelection, session.state.value.vehicleResult?.recovery)
// Tras reparar DataStore y pulsar retry:
assertEquals(1, vehicles.insertCalls)
assertEquals(createdId, preferences.activeVehicleId.first())
```

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)

- [x] C1–C3. Revisión conectada de navegación, ViewModel, pantalla, tema y tests; sin bypass a repositorios, sin segundo coordinador ni hallazgos concretos pendientes. Veredicto: APROBADO.
- [x] C4. Diferenciados evidencia automatizada y ratios calculados de inspección visual/accesible manual aún no realizada.
- [x] C5. Hallazgo sobre salida durante guardado fue resuelto y cubierto por prueba de fallo/reintento; QA final aprobó. Checklist y reporte de cierre completados; detener antes de 4.6.

## 9. Comandos y criterios de verificación — evidencia de ejecución

Los comandos se ejecutaron secuencialmente con studio_run_shell_command. Los seis terminaron correctamente:
```text
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
git diff --check
```

Resultado: BUILD SUCCESSFUL en JVM, compilación AndroidTest, instrumentadas, lint y assemble; diff check sin errores. Suite instrumentada en SM-A066M/API 36; el test ELM327 se omitió al no haber elm327Host. Esto no certifica hardware, permiso LAN, TalkBack, texto ampliado, contraste renderizado ni IME.

Dispositivo: UI/Room/DataStore/recreación en emulador o dispositivo disponible; procurar API36 y37, indicar la matriz realmente cubierta. ELM327 remoto requiere elm327Host y entorno según guía existente, no está en alcance ampliar hardware. Ni pruebas JVM ni colores calculados verifican hardware, permiso LAN, TalkBack o IME. Permiso LAN y conexión de producto pertenecen a 4.6; hardware físico fase5. No bloquear auditoría técnica por ausencia de hardware, pero no declarar pruebas no ejecutadas.

## 10. Implementable, dependencias y diferidos

| Grupo | Responsable/etapa | Condición |
|---|---|---|
| UI-02, validación, conversión, borrador, tema y navegación mínima | Android 4.5 | Implementado y revisado bajo este plan aprobado. |
| D2 y alta parcial | Android 4.5 antes de UI dependiente | Implementado en AppSession; usar resultados/recuperación coordinados, sin guardias solo visuales. |
| Roles y pares de color de componentes | Android 4.5 | Documentados y calculados; inspección manual renderizada queda sin verificar. Las variantes Figma no se crearon ni se declararon aprobadas. |
| Pantallas y permiso real de conexión/diagnóstico | Android 4.6 | Reutilizar coordinación/diálogo global; no simular backend. |
| Lista/detalle historial y mantenimiento | Android 4.7 | Rutas existentes, no implementar ahora. |
| Dashboard completo, Ajustes y métricas visibles | Android 4.8 | Accesos mínimos de 4.5 no cierran estas pantallas. |
| Hardware físico y alcance final | Fase5 / integración4.9 | Entorno real explícito, no extrapolar de fakes. |
| Recuperación de captura/borrador no persistido tras muerte de proceso | Fuera MVP | Recargar persistido, sin comandos automáticos ni promesa de restauración. |
| Búsqueda/filtros, deshacer, tablas/idempotency key durable de alta | Fuera alcance | No necesarios para contrato aprobado; no deduplicar por textos. |

Riesgos funcionales principales —carrera conexión/edición, captura durante espera, pérdida de ID, fallo parcial, borrador, navegación repetida y conversión de distancia— cuentan con implementación y cobertura automatizada descrita arriba. El contraste de componentes, TalkBack, texto ampliado e IME no se inspeccionaron manualmente; se registran como límite de verificación, no como evidencia completada.

## 11. Auto-revisión y estado de entrega

La auditoría Ponytail del plan se incorporó: VehicleRecovery explícito, cancelación limitada a alta y propiedad del estado separada entre AppSession/ViewModel. Implementación mantiene Mutex único, ViewModel específico y componentes Material 3, sin nuevo coordinador ni dependencia. Ponytail final revisó código conectado y pruebas, resolvió la salida encolada durante guardado y emitió **APROBADO**.


Cobertura: lista/activo/CRUD/cascada (§5,A8); año/opcionales/km (§5,B8); primer/adicional/último/fallo selección (§5,B4–6); borrador/error/retry (§3,5,B7/11); D2/Mutex/transitorios/contexto (§4,B1–3); captura (§4.7–9); fallo Room/DataStore (§5,B6); navegación sin anticipar pantallas (§6,B10/12); tema/contraste/accesibilidad (§7,B13); dependencias/diferidos (§10). No nuevo contrato Domain ni dependencia.

Implementación y verificación de esta etapa están cerradas con QA aprobado. La validación manual de accesibilidad, fuente ampliada, insets/IME y contraste renderizado sigue pendiente; ELM327 fue omitido sin elm327Host. No se editaron variantes Figma. La aprobación base UI-01–07 sigue siendo solo referencia de wireframe y no amplía el alcance de 4.5.
