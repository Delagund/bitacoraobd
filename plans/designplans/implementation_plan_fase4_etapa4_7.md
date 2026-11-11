# Plan de implementación — Fase 4, etapa 4.7: Historial y mantenimiento

> Ejecución delegada secuencial Coder → Tester → QA. Estado: **implementado; QA Ponytail APROBADO**. El usuario aprobó el plan y prefirió entrega estática; la validación visual manual y accesibilidad quedan registradas como no ejecutadas.

**Objetivo:** entregar UI-05 y UI-06 para el vehículo correspondiente, preservando historial, reglas y contexto.
**Arquitectura:** Presentation con ViewModel/estado y navegación existentes; repositorios y casos de uso actuales son la fuente de datos y reglas. Compose representa estados y eventos, sin recalcular reglas de negocio.
**Tecnología:** Kotlin, Compose Material 3, Navigation Compose, coroutines/Flow, Room, JUnit y pruebas instrumentadas existentes; sin nuevas dependencias.

## Evidencia y límites de auditoría

- Contexto maestro RF-03/04: sesiones exitosas guardadas, incluso limpias; fallos no persistidos. Mantenimiento con fecha calendario y límites opcionales.
- DD-16/30: edición del mismo ID, confirmación de kilometraje histórico, descarte y conservación del borrador ante error. DD-43/44: lista por fecha descendente, límites por registro y estado textual solo para programación vigente. OTHER no alerta.
- DD-22/31/37: km canónicos enteros; entrada km entera, millas con hasta un decimal, conversión 1,609344 y HALF_UP al km entero; valor original intacto si el campo no se editó. La implementación existente de VehiclesViewModel usa BigDecimal, un decimal de presentación y HALF_UP. No regrabar valores por conversión visual.
- DiagnosticDao ordena timestamp e ID descendentes; MaintenanceDao fecha epochDay e ID descendentes. getSessionById y getMaintenanceById no filtran por vehículo: verificar explícitamente el vehicleId antes de mostrar o editar. AnalyzeDiagnosticHistoryUseCase compara códigos anteriores del mismo vehículo y delega orientación al catálogo.
- SaveMaintenanceUseCase valida fechas y kilometraje y devuelve confirmación antes de guardar; RoomMaintenanceRepository realiza inserción/edición transaccional, comprueba propietario y solo avanza odómetro en alta. GetMaintenanceStatusUseCase devuelve únicamente el registro más reciente por tipo alertable; no asignar sus estados a registros antiguos ni a OTHER.
- AppNavigation tiene destinos de historial, detalle y mantenimiento aún como marcadores. Rutas de detalle incluyen sessionId pero no vehículo: conservar contexto explícito en Presentation y verificarlo; respetar back stack para volver a Inicio o lista, y lista a Inicio/Diagnóstico. Las salidas deben pasar por las guardias AppSession existentes y resolver el borrador de mantenimiento.
- Android: minSdk 26, compile/target 37, LocalDate disponible; dependencias Compose, Room y pruebas ya instaladas. Git en main presenta modificaciones existentes en MainActivity, AppSession, AppNavigation y pruebas, y paquetes connection/diagnostic sin seguimiento: preservarlos. Dispositivo conectado SM-A066M, serial R7AY40TDB1T; API y disponibilidad durante pruebas deben verificarse.
- Referencia visual base aprobada: UI-05 lista 11:249 y detalle 13:41; UI-06 lista 11:267, alta 11:286, edición 11:307, confirmación 11:326 y vacío 13:22, según figma-references.md. Su aprobación no demuestra interacción ni accesibilidad. El formulario presenta «Aceite» como ejemplo; el usuario confirmó en chat el selector de ocho tipos durante esta revisión. No existe variante Figma ni validación visual acreditada por esa confirmación. El plan general §9 exige DESIGN_READY y selector visual confirmado.
- MainActivity compone ViewModels a nivel Activity; AppContainer mantiene privados analyzeHistory y maintenanceStatus y aún no expone SaveMaintenanceUseCase. La integración requiere exponer o construir esos casos de uso existentes sin cambiar sus contratos. TestBitacoraApplication sustituye AppSession, pero MainActivity utiliza también el contenedor real: los fixtures deben asegurar datos coherentes. No se ejecutaron tests ni revisión visual manual en esta planificación.

## Puerta de diseño

El usuario confirmó en chat el selector de ocho tipos propuesto: desplegable Material 3 de selección única con OIL_AND_FILTER, OIL_ONLY, FILTER_ONLY, BRAKES, TIRES, BATTERY, COOLANT y OTHER, etiquetas legibles y objetivos táctiles accesibles. Esta confirmación no acredita una variante Figma ni una validación visual. Los wireframes base ya son DESIGN_READY; las variantes posteriores conservan sus puertas. El usuario aceptó en chat la entrega estática por ahora para esta etapa, resolviendo la alternativa del handoff sin acreditar prototipo navegable, interacción ni accesibilidad. Mantener la comparación visual y las comprobaciones funcionales y de accesibilidad previstas. No modificar Figma sin autorización.

## Alcance y tareas

### A. Coder — tras aprobación explícita del plan

1. El agente principal actualiza plans/task.md sin borrar registros previos, con fases A/B/C, antes de delegar. El Coder preserva cambios y buffers existentes; MainActivity crea ViewModels con alcance Activity y AppNavigation consume salidas autorizadas de AppSession. Integrar los nuevos ViewModels y casos de uso existentes en esa composición sin crear otro coordinador.
2. Crear Presentation y pantallas en `app/src/main/java/cl/cristian/bitacoraobd/ui/history/`: observar sesiones por vehicleId, respetar orden y representar carga, vacío, error con reintento y sesiones limpias; mostrar fecha/hora, kilometraje y cantidad. Cancelar observación anterior y limpiar estado al cambiar contexto; no mostrar respuestas obsoletas.
3. Cargar detalle por ID, comprobar que pertenece al vehículo del contexto antes de mostrarlo, analizar mediante AnalyzeDiagnosticHistoryUseCase y conservar código original y orientación genérica para desconocidos. Distinguir inexistente/error; no guardar, borrar ni alterar códigos. Conectar historial desde Diagnóstico; preparar acceso de Inicio sin implementar Dashboard 4.8. Retorno por pila real según origen, con pruebas de entrada directa y cambio de vehículo.
4. Crear Presentation y pantallas en `ui/maintenance/`: observar lista por vehículo y obtener estado real con GetMaintenanceStatusUseCase al abrir/volver y tras cambios relevantes; asociar estado exclusivamente por ID vigente. Mostrar tipo, fecha, distancia y límites propios; carga, vacío y error/reintento.
5. Crear y editar mediante borrador conservado por ViewModel. Cargar por ID y comprobar vehículo; conservar ID y valores canónicos originales por cada campo de distancia sin editar. Mostrar campos opcionales de límites, notas, fecha LocalDate y ocho tipos. Validar formato antes de construir modelo, delegar reglas a SaveMaintenanceUseCase; explicar corrección de fecha y no desplazar límites absolutos. Confirmación histórica llama de nuevo al caso de uso con confirmación explícita; impedir doble guardado, conservar borrador en fallo y cancelar sin persistir.
6. Integrar en AppNavigation y construcción existente de MainActivity/AppContainer según corresponda. Para Atrás, pestaña o Ajustes desde el formulario: si hay guardado en curso, esperar resultado real; si falla conservar borrador y ofrecer seguir o descartar; si hay cambios, resolver Seguir/Descartar antes de llamar a session.requestExit; si no hay cambios, cerrar borrador y solicitar salida. Después AppSession resuelve cualquier diagnóstico pendiente mediante Guardar/Descartar/Seguir; solo authorizedExit navega y se consume una vez. Seguir o fallo cancela la navegación. Cancelar explícito descarta solo el borrador de mantenimiento. Mantener estado y scroll ante recreación sin repetir operaciones. Tema fijo 4.5, etiquetas, errores textuales, objetivos táctiles y scroll. No añadir navegación principal ni cambios de Room/Domain; ante brecha funcional parar y solicitar autorización.

### B. Tester — después de Coder

- Pruebas unitarias de presentación con repositorios falsos: sesiones limpias y error/reintento, cambio de vehículo durante carga, detalle ajeno rechazado, recurrencia y código desconocido; mantenimiento de ocho tipos, límites y estado vigente por ID, validaciones, confirmación sin escritura previa, error con reintento, edición mismo ID, cancelar y doble pulsación.
- Conversión: 10,0 mi → 16 km; entrada inválida/negativa/desbordamiento; km intactos al editar solo notas o fecha; cada distancia opcional preservada por separado; cambios de unidad no reescriben datos.
- Room instrumentado: aislamiento de dos vehículos, orden con empates, transacción y edición sin avanzar odómetro, histórico confirmado sin retroceso, recomputación tras cambio de fecha/tipo; OTHER sin alerta. Reutilizar fixtures existentes.
- Compose/navegación: Inicio/Diagnóstico → historial → detalle → origen, detalle directo, vacío/error, confirmación y descarte, rotación con borrador y operación única, etiquetas y scroll. Probar salida durante guardado exitoso y fallido; Seguir conserva borrador; descarte seguido de decisión de diagnóstico pendiente no navega al elegir Seguir ni al fallar guardado, y autorización exitosa se consume una sola vez. MainActivityRecreationTest usa TestBitacoraApplication: su sesión emplea repositorios falsos, pero MainActivity obtiene diagnósticos/preferencias del AppContainer real; al integrar nuevas dependencias, proporcionar fixtures coherentes para ambas rutas sin alterar producción solo para pruebas. Adaptar aserciones existentes de marcadores History/Maintenance. Verificar accesibilidad y comparación visual manual por separado de pruebas automáticas.

### C. QA / Ponytail

Revisar código conectado y consumidores: aislamiento, pertenencia de IDs, resultados asíncronos obsoletos, integridad transaccional, fechas, campos canónicos sin editar, confirmación previa a persistir, retorno, errores, cobertura real y ausencia de duplicación de reglas. Revisar KISS/SOLID/YAGNI y plan al 100 %. Corregir hallazgos y repetir pruebas afectadas. No declarar APROBADO con fallos o pruebas obligatorias omitidas.

## Verificación secuencial

Ejecutar desde raíz, registrando salida y dispositivo; usar `--rerun-tasks` para no presentar UP-TO-DATE como ejecución:

1. `./gradlew :app:testDebugUnitTest --rerun-tasks`
2. `./gradlew :app:compileDebugAndroidTestKotlin`
3. `adb devices -l` y confirmar API; `./gradlew :app:connectedDebugAndroidTest --rerun-tasks` con dispositivo disponible. La integración remota ELM depende de su entorno: registrar cualquier omisión o fallo, sin afirmar suite completa.
4. `./gradlew :app:lintDebug :app:assembleDebug`
5. `git diff --check`; revisar diff y comparación visual/accesibilidad manual en claro y oscuro.

Solo tras QA APROBADO marcar checklist y crear `plans/reporte_fase4_etapa4_7.md` con comandos, resultados reales, dispositivo, omisiones y límites; actualizar handoff/dependencias afectados. La validación API 37 con Wi-Fi real o adaptador físico de 4.6 queda fuera de 4.7.

## Auditoría Ponytail del plan — cerrada

Carga asumida: una instalación local, con varios vehículos y datos Room locales. Los ocho tipos existen en MaintenanceType; DatabaseConverters, declarado en AppDatabase.kt, persiste sus nombres y los reconstruye con valueOf. MaintenanceDao excluye OTHER de latestAlertable; AppDatabaseTest contiene una prueba para los cuatro tipos añadidos y exclusión de OTHER, pero no se ejecutó en esta auditoría.

1. **Corregido para nueva revisión:** AppSession.requestExit pasa por runSensitive, espera guardado diagnóstico, solicita decisión y emite authorizedExit; AppNavigation consume la autorización. Se especificó primero resolver el borrador de mantenimiento y después solicitar la salida, incluyendo fallo, recreación y consumo único. Se identificó que el fixture instrumentado mezcla sesión falsa con dependencias del contenedor real y se incluyó su ajuste y la adaptación de aserciones de marcadores.
2. **Resuelto parcialmente:** el usuario confirmó el selector de ocho tipos propuesto. La referencia 11:286 sigue sin representar esa variante; la confirmación no acredita prototipo ni accesibilidad. Los wireframes base son DESIGN_READY y las variantes posteriores mantienen su revisión.
3. **Corregido en el plan:** asociar estado de mantenimiento por ID evita marcar un registro histórico como vigente; conservar cada valor canónico no editado evita pérdida por redondeo. Comprobar propiedad de IDs evita mostrar datos de otro vehículo. Reutilizar conversión existente sin introducir dependencias ni capas especulativas.

Veredicto de revisión del plan: **APROBADO** tras confirmación del usuario del selector de ocho tipos y aceptación de entrega estática. No se modificó Figma ni se declara validado un prototipo o variante visual.

En la QA de implementación aparecieron dos defectos de Presentation que no estaban identificados en la revisión estática del plan: respuestas tardías de alta/edición podían reabrir un borrador tras cambiar de contexto, y el estado de alerta no se recalculaba al volver a Mantenimiento sin una emisión nueva de Room. Ambos se corrigieron en `MaintenanceViewModel`/`MaintenanceScreen`/navegación y se cubrieron con tres regresiones deterministas. La revisión Ponytail posterior emitió **APROBADO**, sin hallazgos adicionales.

Límites: no se hizo comparación visual manual ni validación manual de accesibilidad/tema. No se inspeccionó Figma remotamente; la preferencia de entrega estática no equivale a prototipo ni a aprobación visual adicional. La integración ELM remota quedó omitida por falta de `elm327Host`.
