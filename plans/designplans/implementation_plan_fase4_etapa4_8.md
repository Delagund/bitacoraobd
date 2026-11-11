# Fase 4.8 — Inicio y Ajustes: Implementation Plan

> Estado: APROBADO por el usuario y ejecutado; cierre QA/Ponytail APROBADO. Evidencia de ejecución en [reporte_fase4_etapa4_8.md](../reporte_fase4_etapa4_8.md).
> Se conserva el plan aprobado como alcance de referencia; límites de verificación manual constan en el reporte de cierre.

**Goal:** completar UI-01/UI-07 respetando las decisiones aprobadas y los contratos existentes.
**Architecture:** Presentation con ViewModels, StateFlow/UDF, AppSession y DI manual existentes. Domain/Data no requieren ampliación demostrada.
**Tech Stack:** Kotlin, Compose/Material 3, Navigation Compose, Room y DataStore instalados.

## 1. Fuentes, precedencia y evidencia
Auditoría de planificación del 2026-11-11. Fuentes: AGENTS.md; contexto maestro (MVP, reglas y fases); plan general implementation_plan_fase4.md §§0,2,10; reporte 4.7; decisiones D1–D4, DD-29/36–41/45/46/48; referencias, contratos y dependencias locales consultados por búsqueda. El §9 del plan general es 4.7; 4.8 está en §10. Prevalecen acuerdos posteriores frente a textos históricos contradictorios.

Código leído: HomeViewModel, GetDashboardDataUseCase, AppSettings, AppPreferencesRepository, DataStoreAppPreferencesRepository y navegación (coordinación de salidas). AppSession inspeccionado por búsqueda de métricas y contexto. Pruebas leídas: HomeViewModelTest y VehicleMileageTest. Reporte 4.7 leído íntegro; acredita 128 JVM y 26 instrumentadas, con una remota omitida, en su ejecución histórica, no en esta sesión.

Git status real: cambios anteriores en MainActivity, AppContainer, AppSession, AppNavigation, MainActivityRecreationTest, AppSessionTest, AppRouteTest; directorios nuevos connection/diagnostic/history/maintenance y pruebas history/maintenance. Conservar íntegramente; no limpiar ni revertir.

Entorno comprobado: ./gradlew --version funciona, Gradle 9.8.2, JVM JetBrains 25.0.4. Kotlin 2.4.10 mostrado allí es Kotlin embebido de Gradle, no el plugin Compose declarado 2.2.10. Catálogo: AGP 9.5.1, BOM 2026.02.01, Room 2.7.2, DataStore 1.1.7, navegación 2.10.2. App minSdk26/target37/compile37, Java source/target11. adb devices -l muestra SM_A066M R7AY40TDB1T autorizado. No se verificó hoy su API ni conectividad OBD.

Auditoría complementaria tras Ponytail: leídos MainActivity, AppContainer, resto de AppNavigation, HistoryScreen, MaintenanceScreen, DiagnosticScreen, ConnectionScreen, TestBitacoraApplication, AppCompositionTest y MainActivityRecreationTest; inspeccionados ViewModels de vehículos/mantenimiento y AppSession para contratos de edición, conversión, métricas y salida. La búsqueda de consumidores de settings/distanceUnit/temperatureUnit/mileageForSave confirma el mapa de §3.1. No se reejecutaron suites ni build ni se inspeccionó Figma en vivo. Los resultados históricos no son una validación actual.

## 2. Alcance aprobado y aceptación
- Inicio: vehículo → mantenimiento/vencimientos → diagnóstico → métricas. Sin vehículos es vacío real también tras borrar último, con acción de agregar.
- Sin mantenimiento y sin diagnóstico son vacíos independientes con sus accesos. Sesión limpia guardada es “Sin códigos detectados” con fecha, nunca ausencia de diagnóstico.
- Mostrar todos los códigos analizados de última sesión; recurrencia solo etiqueta según análisis existente, sin gravedad ni alerta DTC adicional.
- Resumen mantenimiento abre Mantenimiento del vehículo vigente. Último diagnóstico abre detalle por vehicleId/sessionId; Ver historial es acción separada y conserva retorno.
- Mantener conexión textual visible. F03 se inicia en Diagnóstico y termina allí; no restaurar apertura directa de Conexión desde Inicio por una referencia antigua.
- Métricas: última captura completa de AppSession, vehículo y fecha/hora; conservar tras desconexión, null = No disponible, 0 = valor. Sin captura = Sin lectura. Ocultar todas las métricas está permitido; no ocultar por ello el estado de conexión. No lecturas automáticas ni polling.
- Ajustes secundario: nombre opcional, distancia km/millas y velocidad vinculada, temperatura °C/°F, visibilidad RPM/refrigerante/velocidad. Defaults: nombre ausente, km, °C, tres métricas visibles.
- Borrador único con Guardar explícito, seguir editando/descarte ante salida sucia. Error conserva borrador y permite reintentar; no anunciar éxito antes de persistir. No aplicar preferencias no guardadas.
- Preferencias cambian representación, nunca distancias almacenadas. Entradas editadas en millas admiten un decimal y convierten a km entero más cercano: 10,0 →16. Campo no editado conserva entero original.
- Reactivar Inicio recalcula fecha sin temporizador; aislamiento al cambiar vehículo y protección frente a emisiones tardías.
- Recreación con proceso vivo conserva borrador y decisiones; muerte de proceso no promete recuperación de datos temporales.

## 3. Contratos y riesgos concretos
GetDashboardDataUseCase ya produce NoVehicles/Ready, analiza última sesión y calcula mantenimiento; no duplicar reglas en Compose.
HomeViewModel ya cancela colección previa y usa generación al activar. Aplicar DD-39 sin ambigüedad: primera carga sin datos del vehículo actual muestra carga completa y, si falla, error completo con Reintentar. Al actualizar el mismo vehículo conservar dashboard, indicar actualización y, ante fallo, advertir datos posiblemente desactualizados con Reintentar. Al cambiar vehículo invalidar inmediatamente datos anteriores y descartar respuestas tardías; nunca limpiar datos propios solo por refrescar. Asociar en Presentation el dashboard conservado a su vehicleId y contrastarlo con el contexto de AppSession; contexto aún desconocido no autoriza presentar el anterior como actual.
AppSession.metrics es Pair<Long, ObdLiveMetrics>; ya se borra al cambiar vehículo. Verificar también igualdad con vehículo mostrado antes de renderizar. ObdLiveMetrics ya contiene timestampMillis: usarlo para fecha/hora de captura con formato local existente, nunca la hora de renderizado. No requiere ampliar contratos.
saveSettings usa un único DataStore.edit para todos los campos; no inventar guardados independientes o transacciones Room/DataStore. Preserva claves de activo/IP/puerto. Ante excepción conservar borrador y permitir releer persistido sin sobrescribir automáticamente edición en curso.
Captura pendiente OBD y borrador de Ajustes son decisiones distintas: resolver salida local antes de enviar acción a AppSession y conservar destino original; éxito/fallo de navegación no debe borrar borrador por anticipado.
No reescribir históricos para “normalizar” unidades. Reutilizar mileageForSave/mileageToKilometers existentes y sus regresiones; no crear otro convertidor.
No importar deudas de accesibilidad/formularios de 4.7 ni permisos API37/hardware a esta etapa.

### 3.1 Mapa cerrado de integración y consumidores
Rutas relativas a app/src/main/java/cl/cristian/bitacoraobd/.
| Archivo/caller | Estado comprobado y cambio mínimo previsto |
|---|---|
| MainActivity.kt → AppNavigation | HomeViewModel se crea pero no se pasa; onResume/onPause lo activan para toda la Activity. Pasarlo a navegación y retirar esos callbacks como propietarios de activación. |
| ui/navigation/AppNavigation.kt → HomeScreen | Inicio es placeholder y Ajustes es Text("Settings"). Crear conexión real y acceso secundario a Ajustes; mantener rutas/retornos existentes. |
| ui/HomeViewModel.kt | Mantener ubicación y caso de uso; concretar contexto/carga/error de DD-39. |
| AppContainer.kt | Ya expone getDashboard/preferences; sin nuevos servicios ni cambios previstos. |
| ui/vehicles/VehiclesViewModel.kt | Ya lee unidad al abrir borrador y usa mileageForSave/mileageToKilometers con BigDecimal y HALF_UP. Conservar enteros originales y helpers; sin reimplementar. |
| ui/maintenance/MaintenanceViewModel.kt | Ya captura unidad del borrador y conserva originales de ambos campos. Sin cambios de reglas ni conversión de entrada. |
| ui/history/HistoryScreen.kt | Lista y detalle muestran mileageAtScanKm fijo en km. Recibir preferencias vigentes y representar km/mi, sin escribir sesiones. |
| ui/maintenance/MaintenanceScreen.kt | Lista, próximo límite y confirmación histórica muestran km fijos. Representar con unidad vigente; en confirmación de formulario usar unidad capturada por el borrador y etiquetar ambos valores igual. No alterar evaluación del dominio. |
| ui/diagnostic/DiagnosticScreen.kt | No representa distancias ni métricas; sin cambio de unidades necesario. |
| ui/connection/ConnectionScreen.kt | Ya aplica temperatura/velocidad según preferencias y muestra timestamp. Mantener adquisición, permiso y reglas; no cambiar precisión aprobada silenciosamente. Visibilidad filtra solo Inicio. |
| ui/AppSession.kt | Propietario existente de métricas y captura; no crear otro. Mantener guardias/salidas y comparar ID antes de mostrar datos. |

### 3.2 Activación única de Inicio (hallazgo 1)
En AppNavigation coordinar una sola señal booleana: destino actual HOME y ciclo de vida de Activity RESUMED. Usar observación del Lifecycle existente y currentBackStackEntryFlow; llamar setActive solo cuando esa señal cambie, y desactivar al disponer. Eliminar activación de onResume/onPause en MainActivity. No sumar un LaunchedEffect de ruta a los callbacks actuales, porque duplicaría reinicios. Volver desde Ajustes/Historial u otra pestaña reinicia colección y recalcula fecha; reanudar app en otro destino no activa Inicio. Retry sigue siendo explícito y no crea temporizador.

### 3.3 Pruebas y fixtures: brechas concretas (hallazgo 3)
- MainActivityRecreationTest busca el marcador "Settings": actualizarlo al título real conservando la prueba de ruta inválida y consumo de salida.
- AppCompositionTest solo verifica identidad de dependencias: conservarlo; no adaptarlo por títulos que no contiene.
- TestBitacoraApplication sustituye únicamente appSession; MainActivity crea HomeViewModel con appContainer.getDashboard real. No usar esa mezcla para afirmar aislamiento ni refresco de Inicio.
- Para nuevas pruebas de Inicio/navegación, montar AppNavigation con HomeViewModel construido con repositorios fake y el mismo contexto de preferencias/vehículos que AppSession. Usar el punto de inyección de parámetros de AppNavigation, sin nueva interfaz de producción ni sustituir AppContainer global.
- TestPreferences.saveSettings actualmente no hace nada y settings no expone mutación: habilitar estado mutable, guardado y error/gate controlados en fixture. TestVehicles devuelve flujos nuevos y no conserva borrados/altas: para escenarios de cambio/borrado usar un StateFlow estable y actualizarlo. TestGateway devuelve fallo fijo de métricas: permitir captura controlada con null/0/timestamp sin socket.
- Si una prueba usa createAndroidComposeRule<MainActivity>, verificar antes qué dependencias reales usa; las nuevas pruebas de comportamiento de Inicio deben usar montaje controlado. Mantener pruebas existentes de recreación/captura y agregar recreación de Ajustes con ViewModel conservado.
- Archivos de prueba previstos: ui/HomeViewModelTest.kt, nuevo ui/settings/SettingsViewModelTest.kt, MainActivityRecreationTest.kt, TestBitacoraApplication.kt y nuevo androidTest HomeSettingsNavigationTest.kt. Helpers/fakes locales, sin framework adicional.
- Regresiones de unidades: conservar VehicleMileageTest y MaintenanceViewModelTest; probar representación km/mi de lista/detalle/confirmación y verificar que guardar preferencias no llama a guardar vehículos/mantenimientos/sesiones.

## 4. Puertas de diseño
Wireframes base UI-01–07 siguen DESIGN_READY: no pedir nueva aprobación de su contenido. Tema fijo claro/oscuro según sistema y dirección A ya decididos.
Referencias locales: Inicio 11:3; Ajustes 11:344 y descarte 11:380 en figma-references.md. No se certifica su apariencia actual ni interactividad.
Antes de integrar variantes nuevas presentar al usuario propuesta concreta de Inicio (carga/error, vacíos, métricas ocultas/no disponibles/desconectadas) y Ajustes (guardado/error/recuperación/descarte) que no esté cubierta por referencia aprobada. Aprobar únicamente diferencias visuales reales. La entrega estática de 4.7 no autoriza nuevas variantes ni acredita prototipo navegable de 4.8. No modificar Figma.
Si aparece discrepancia funcional/contrato, registrar opciones e impacto y dejar esa parte pendiente de decisión; no resolver silenciosamente.

## 5. Ejecución futura por tareas
### A — Coder, después de aprobación
- [ ] Principal actualiza plans/task.md con A/B/C sin borrar antecedentes y verifica que el mapa de §3.1 sigue vigente frente a cambios nuevos del workspace.
- [ ] Mantener ui/HomeViewModel.kt; crear ui/home/HomeScreen.kt para representación de estados y callbacks. No mover ViewModel solo para uniformar carpetas.
- [ ] Crear ui/settings/SettingsViewModel.kt y SettingsScreen.kt: cargar preferencias antes de editar, snapshot persistido + borrador, dirty por comparación, guardado único y exclusión de dobles pulsaciones, propagación de cancelación y error recuperable.
- [ ] En MainActivity.kt crear SettingsViewModel con Factory y preferences existentes, pasar HomeViewModel/SettingsViewModel a AppNavigation y retirar activación de Inicio de onResume/onPause. Implementar señal única de §3.2. AppContainer no requiere cambio. Conectar HomeScreen a estado del HomeViewModel y preferencias/session sin segundo propietario de métricas.
- [ ] Modificar ui/navigation/AppNavigation.kt para UI real de Inicio/Ajustes, retorno, guardias de borrador y accesos D3; conservar cuatro rutas principales, F03 y guardias existentes.
- [ ] Aplicar exclusivamente los ajustes de representación por archivo enumerados en §3.1; reutilizar conversión de entrada existente. Para formato km/mi repetido en Inicio/Historial/Mantenimiento extraer solo una función de Presentation si esos usos lo necesitan; no crear servicio ni cambiar precisión de Conexión.
- [ ] Mantener semántica accesible, etiquetas textuales, errores y áreas táctiles; sin componentes compartidos nuevos salvo reutilización real.
Coder no ejecuta pruebas, según AGENTS.md.

### B — Tester
- [ ] Ampliar ui/HomeViewModelTest.kt con cambio vehículo durante carga, fallo/reintento sin datos del anterior, cancelación y refresco al reingresar. Probar DD-39: mismo vehículo conserva datos ante actualización/error, nuevo vehículo nunca recibe los anteriores. Añadir prueba de navegación real con reloj controlado: salir de Inicio, avanzar fecha, volver sin pausar Activity y verificar nuevo vencimiento; pausar/reanudar en Inicio refresca, hacerlo en otra pestaña no activa Inicio. Reutilizar reloj controlado y fakes actuales.
- [ ] Crear ui/settings/SettingsViewModelTest.kt: defaults tras carga exitosa; lectura fallida no se presenta como defaults; edición no persiste; guardado único; error conserva todos los campos; emisión tardía no pisa borrador; descarte/seguir editando y recreación.
- [ ] Conservar VehicleMileageTest: 10,0 millas→16 km; 17 km no editados permanecen17 tras cambios de preferencias. Extender únicamente si se modifica helper.
- [ ] Añadir pruebas Compose en androidTest para orden/vacíos, limpio vs ausencia, recurrencia textual, detalle/historial/retornos, métricas null/0/sin lectura/desconectadas y ocultar todas; probar unidades también en pantallas consumidoras afectadas.
- [ ] Conservar AppCompositionTest; adaptar MainActivityRecreationTest al título real de Ajustes y conservar guardias/capturas. Implementar montaje controlado y cambios de fixture de §3.3. Fixtures: dos vehículos con datos distintos, sesión limpia, DTC recurrente y desconocido, reloj controlado, captura con null y cero; fakes locales sin infraestructura nueva.
- [ ] Ejecutar secuencialmente comandos de §6; registrar conteos y omisiones reales.

### C — QA/Ponytail
- [ ] Revisar diff y código conectado contra §2; buscar escrituras involuntarias, datos obsoletos, dobles guardados y pérdida de borrador.
- [ ] Comparar solo variantes aprobadas y comprobar accesibilidad de UI nueva en claro/oscuro, texto grande, foco/IME y objetivos táctiles. No declarar revisada UI-06 pendiente.
- [ ] Tras APROBADO, finalizar checklist y reporte plans/reporte_fase4_etapa4_8.md, registrar límites y consultar siguiente paso.

## 6. Verificación futura
Ejecutar mediante studio_* y de uno en uno:
1. ./gradlew :app:testDebugUnitTest --rerun-tasks
2. ./gradlew :app:compileDebugAndroidTestKotlin
3. ./gradlew :app:connectedDebugAndroidTest --rerun-tasks
4. ./gradlew :app:lintDebug
5. ./gradlew :app:assembleDebug
6. git diff --check

Antes: adb devices -l y adb -s R7AY40TDB1T shell getprop ro.build.version.sdk; actualizar serial si cambia. Resultado esperado: comandos exitosos, cero fallos/errores; leer XML con studio_read_file para conteos, no inferir de BUILD SUCCESSFUL. Una omitida por elm327Host se declara omitida. No exigir hardware para cerrar pruebas de Presentation ni prometer 100% de pruebas ejecutadas si existe bloqueo. Repetir solo comprobaciones afectadas tras correcciones.
Pruebas manuales nuevas: navegar desde ambos orígenes, rotar Ajustes sucio y diálogo, guardar/reintentar, último vehículo eliminado, cambio de fecha al reingresar, desconectar conservando captura. No conectar ni consultar OBD al entrar Inicio.

## 7. Autorrevisión Ponytail incorporada
- Eliminada propuesta implícita de otra capa/servicio Dashboard: reutilizar caso de uso y ViewModel.
- Evitada duplicación de conversión y reglas mantenimiento/recurrencia: helpers y Domain existentes.
- Corregido alcance de “fallo parcial”: DataStore guarda AppSettings en una edición; UI necesita error/recuperación, no atomicidad inventada.
- Separadas aprobación funcional, wireframe base y variantes nuevas; no atribuir aprobación visual por entrega estática.
- Añadida protección de estado anterior en carga/error y de respuesta tardía sobre borrador; conservar contratos hasta demostrar brecha.
- Sustituida promesa de suite 100% por evidencia de ejecución y omisiones explícitas.
- Eliminados migraciones, dependencias, polling, odómetro ECU, admisión, rediseño global y deudas manuales de4.7.
SOLID: persistencia permanece en repositorio, reglas en Domain y representación/coordinación en Presentation. KISS/YAGNI: dos pantallas y un ViewModel nuevo donde hace falta, sin refactorización general.

**Resultado:** propuesta mínima lista para revisión del usuario, con puerta técnica y visual explícitas. No equivale a QA de código ni autorización para implementar.

## 8. Correcciones de la revisión Ponytail 1–4
1. Activación resuelta con una sola señal destino HOME + Lifecycle RESUMED, retirada de callbacks duplicados y prueba de retorno por pestaña.
2. DD-39 especificado por primera carga, refresco del mismo vehículo y cambio de contexto, con regresiones diferenciadas.
3. Completado mapa de callers/unidades y fixtures; alcance por archivo cerrado. Identificada y evitada mezcla de AppSession falso y dashboard real en instrumentadas; AppCompositionTest se conserva.
4. Confirmado timestampMillis de ObdLiveMetrics y eliminada puerta condicional de contrato.
Revisión documental: las cuatro observaciones están atendidas en la propuesta. No es un veredicto QA sobre implementación. Permanecen puertas visuales de §4 y aprobación explícita antes de Coder.
