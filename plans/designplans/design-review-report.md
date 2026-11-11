# Revisión preparatoria de diseño y recorridos

**Lectura histórica:** las inspecciones, alternativas y estados de aprobación de las secciones cronológicas siguientes reflejan lo conocido en sus fechas; las aprobaciones y acuerdos posteriores constan en [decisiones](design-decisions.md), [referencias](figma-references.md) y [evaluación vigente](design-handoff.md#evaluación-de-preparación-por-paquete--revisión-de-cierre). La idempotencia mediante identificador estable ya fue aceptada como pendiente prioritario de backend para fase 4; ver [seguimiento](../task.md#entrada-prioritaria-para-fase-4--backend-requerido-por-ui).

## Paso 3 — clasificación parcial, 2026-11-10

La [tabla de contratos](domain-contracts.md#paso-3--contraste-de-contratos-y-consumidores-2026-11-10) registra firmas actuales, consumidores encontrados, errores, defaults, responsables y evidencia exigida para cada grupo. La auditoría es estática; no acredita ejecución.

- **Documental corregible:** separar API existente de coordinación propuesta y precisar que «sin vehículos» incluye el borrado del último; aplicar decisiones ya vigentes sin repetir consulta.
- **Implementación pendiente:** Presentation, permisos LAN, preferencias de unidades y visibilidad, continuidad y guardias. Responsable Android; condiciones de prueba por grupo en la tabla. El fallo entre Room y DataStore exige comprobar estado real.
- **Visual pendiente:** selector completo de tipos de mantenimiento, revisión de variantes posteriores y legibilidad de F03 y barras. Responsable diseño/QA; no modificar Figma sin autorización aplicable.
- **Validación pendiente:** prototipo interactivo, accesibilidad y recorridos; diseño/QA debe registrar evidencia antes de DESIGN_READY. No se infiere aprobación de variantes posteriores a partir de la aprobación previa.
- **Consulta nueva:** no se identificó una decisión funcional nueva como condición para documentar estos grupos; cualquier cambio de regla, navegación principal o diseño aprobado requiere autorización específica. El odómetro ECU es futuro; admisión fuera de alcance.
- **Dependencia técnica DEP-011:** Room inserta una sesión nueva por llamada. Un resultado incierto posterior al commit puede duplicar la captura al reintentar; Android/Data debe definir en el plan técnico cómo distinguir o resolver ese caso y probarlo, sin atribuir idempotencia a la API. El flujo acordado DD-25 no cambia. Fuente y condiciones en [contraste por operación](domain-contracts.md#ampliación-del-contraste-por-operación--paso-3).

**Registro histórico del 2026-11-08:** inspección documental, código y pruebas por lectura, y consulta Figma MCP. En aquella revisión no se ejecutaron pruebas ni se aprobó diseño. La aprobación posterior de los wireframes UI-01–UI-07 del 2026-11-09 se conserva según [referencias](figma-references.md); no acredita variantes posteriores ni prototipo.

## Línea base — 2026-11-08

Punto de partida documental para el plan de diseño; las evidencias proceden de la revisión realizada el 2026-11-08, no de una nueva ejecución de pruebas ni de una instantánea inmutable del repositorio o Figma.

| Fuente | Línea base y alcance |
|---|---|
| Requisitos y acuerdos | [Contexto maestro](../Contexto_Maestro_App_OBDII_v2.md), [instrucciones de diseño](agent-designer-instruction.md), [decisiones](design-decisions.md), [contratos](domain-contracts.md), [especificación](ui-ux-specification.md) y [flujos](screen-flows.md). Su contraste detallado y las decisiones abiertas siguen en auditoría. |
| Historia y seguimiento | [Plan técnico raíz](../implementation_plan.md), [checklist técnico](../task.md), reportes de fases 0–3 y [seguimiento de diseño](designtask.md). El plan raíz conserva historia y su ajuste vigente excluye completar diseño del cierre de Pre-Fase 4. |
| Código y pruebas | Lectura de modelos, casos de uso, repositorios, OBD, preferencias y pruebas referenciadas en el contraste por recorrido de este reporte. MainActivity muestra Greeting; la interfaz funcional y navegación siguen pendientes. La existencia de pruebas no implica ejecución actual. |
| Figma | [Design 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2), [variantes 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2) y [FigJam F01–F10](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=0-1), inspeccionados visualmente el 2026-11-08. Inventario y límites en [referencias Figma](figma-references.md) y en la sección visual de este reporte. |

Esta línea base identifica fuentes y evidencia disponible; no cierra la auditoría, no congela versiones y no supone aprobación funcional o visual adicional. Antes de cada decisión se comprobará si las fuentes afectadas cambiaron.

## Auditoría por recorridos — 2026-11-08

Revisión de fuentes y código mediante Android Studio y contraste con la inspección visual existente. Los hallazgos siguientes son evidencia para la matriz y las sesiones de decisión; no modifican acuerdos ni aprueban diseños.

### Requisitos, contratos y pruebas

- **Vehículos / Inicio:** [DeleteVehicleUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/DeleteVehicleUseCase.kt) bloquea el borrado en Connected; no garantiza por sí solo la guardia de selección ni la coordinación con una captura pendiente. Puede actualizar DataStore antes de que falle el borrado Room. [GetDashboardDataUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetDashboardDataUseCase.kt) usa el primer vehículo disponible como fallback sin persistirlo; el cambio de día por sí solo no emite una actualización. [MaintenanceDashboardDeleteUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt) contiene casos de vacío, selección obsoleta, cambio, fallos parciales y cancelación. Su prueba con nombre histórico sobre alerta recurrente comprueba códigos y lista de alertas vacía, no una alerta DTC.
- **Diagnóstico / historial / OBD:** [ScanDiagnosticCodesUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/ScanDiagnosticCodesUseCase.kt) exige conexión y conserva vehículo y kilometraje originales; [SaveDiagnosticSessionUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveDiagnosticSessionUseCase.kt) guarda solo éxito. El estado pendiente y reintento sin releer requieren Presentation. [DiagnosticAndMaintenanceUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt), [Elm327ObdGatewayTest](../../app/src/test/java/cl/cristian/bitacoraobd/data/obd/Elm327ObdGatewayTest.kt) y [Elm327SocketClientTest](../../app/src/test/java/cl/cristian/bitacoraobd/data/obd/connection/Elm327SocketClientTest.kt) incluyen casos de captura, fallos, cancelación y transporte. Un fallo de interpretación no implica necesariamente desconexión; la UI debe reflejar el estado real.
- **Mantenimiento y persistencia:** [SaveMaintenanceUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveMaintenanceUseCase.kt) valida fechas y kilometrajes; [RoomMaintenanceRepository](../../app/src/main/java/cl/cristian/bitacoraobd/data/repository/RoomMaintenanceRepository.kt) actualiza el mismo ID y solo avanza odómetro al insertar. Los tipos BRAKES, TIRES, BATTERY y COOLANT existen; OTHER no genera alertas. [AppDatabaseTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt) contiene casos de cascada, edición, tipos, selección del último registro y rollback.
- **Preferencias:** [AppPreferencesRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/AppPreferencesRepository.kt) expone activo y configuración OBD; nombre, unidades y visibilidad aún no forman parte del contrato. [DataStoreAppPreferencesRepositoryTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DataStoreAppPreferencesRepositoryTest.kt) comprueba valores por defecto, escritura y limpieza de esas preferencias existentes, no las propuestas.
- **Presentation y tema:** [MainActivity](../../app/src/main/java/cl/cristian/bitacoraobd/MainActivity.kt) sigue mostrando Greeting; el directorio UI contiene el tema, no pantallas MVP. [design-system.md](design-system.md) distingue tema de plantilla y componentes propuestos. No hay evidencia de navegación, gestión de estados ni pruebas de recorrido UI implementadas.
- **Red local:** [build.gradle.kts](../../app/build.gradle.kts) configura targetSdk 37 y [AndroidManifest.xml](../../app/src/main/AndroidManifest.xml) declara ACCESS_LOCAL_NETWORK. Según [Android Developers: Local network permission](https://developer.android.com/privacy-and-security/local-network-permission), consultado el 2026-11-08, en Android 17 con target 37 el acceso TCP directo a LAN requiere gestionar el permiso en ejecución cuando se usa acceso amplio; la declaración sola no lo concede. La comprobación, solicitud, denegación y recuperación no están implementadas en Presentation.

### Contradicciones y decisiones residuales

1. [screen-flows.md](screen-flows.md) conserva texto histórico que considera abiertos el destino de descartar, el reescaneo y el momento de guardar IP/puerto, aunque su actualización posterior indica volver a Inicio tras descartar, resolver una captura pendiente antes de reescanear y guardar configuración tras handshake exitoso. El contrato conectar no persiste por sí solo; sigue pendiente concretar salida con campos modificados. Las secciones locales fueron reconciliadas en esta revisión sin añadir reglas.
2. Se reconciliaron las referencias locales a la coordinación de captura pendiente y al retorno del historial con los acuerdos posteriores. La interacción visual aún debe validarse.
3. Sobre salida voluntaria durante lectura, se registra descartar lecturas incompletas y cierre de socket al cancelar, pero el saldo aún pregunta si se termina o cancela y el estado final de conexión. Precisar el alcance de Atrás, cambio de pestaña y desconexión con el usuario; no deducirlo solo de la rotación o de Connecting.
4. El contexto histórico RF-05 solicita alertas de DTC recurrentes; decisiones y backend posteriores separan códigos de última sesión de alertas de mantenimiento. Verificar el alcance exacto de la decisión posterior y la presentación de recurrencia antes de considerar satisfecho o reemplazado el requisito.
5. Ajustes y unidades conservan pendientes el momento de guardado, errores, precisión y redondeo de millas; la UX de permiso LAN y sus alternativas de recuperación también requiere definición. El requisito histórico de prototipo navegable no queda acreditado por capturas estáticas.
6. FigJam tiene una sincronización histórica, no garantiza concordancia actual: ver las contradicciones F01–F06 y panel de abiertos en la inspección visual. No se modifica el tablero durante esta auditoría.

Las pruebas citadas fueron inspeccionadas, no ejecutadas en esta revisión; no acreditan PASS actual ni validación de UI, accesibilidad o hardware.

## Reconciliación

- El plan de diseño registra aprobación del alcance el 2026-11-08; se actualizó designtask.md para reflejar ese registro. La aprobación del plan no aprueba pantallas ni decisiones nuevas.
- El plan técnico raíz conserva tareas históricas de diseño y fase 4; su sección «Ajuste de alcance vigente» excluye esos trabajos del cierre de Pre-Fase 4. Mantener historia; no usar esas tareas antiguas como criterio vigente.
- El texto inicial del frame Design 3:5 indica que la edición de mantenimiento requiere implementación; el backend ya la soporta. La interfaz sigue pendiente. Evaluar el hallazgo después de crear la matriz y actualizar el frame solo con autorización aplicable.
- Las sincronizaciones de FigJam son históricas; el handoff preliminar ya existe sin estar listo para implementación. Las diferencias de RF-05 y del alcance de salida durante lectura se preservan para decisión posterior, sin alterar el contexto maestro ni atribuir aprobación nueva.
- Por instrucción posterior del usuario, la evaluación de hallazgos de diseño ocurre después de crear screen-dependencies.md. La inspección previa se conserva como evidencia, no como aprobación.

## Contraste por recorrido

Las rutas Kotlin parten de app/src/main/java/cl/cristian/bitacoraobd/; las pruebas homónimas están en app/src/test o app/src/androidTest.

| Recorrido y requisito | Contrato y pruebas inspeccionadas | Brecha para diseño e integración |
|---|---|---|
| Primer vehículo, selección y borrado (RF-01, UI-02, F01/F02) | VehicleRepository y DeleteVehicleUseCase; MaintenanceDashboardDeleteUseCasesTest cubre bloqueo Connected y fallos parciales de DataStore/Room. | Preferencias no bloquean selección; borrado solo comprueba Connected. Definir coordinación con captura pendiente y estados transitorios conforme acuerdos; recuperar estado real tras fallo parcial. Año 0 representa ausencia. |
| Inicio (RF-05, UI-01) | GetDashboardDataUseCase observa selección, sesiones, mantenimiento y conexión; pruebas cubren vacío, selección obsoleta y cambio. | Fallback al primer vehículo no se escribe en preferencias; cambio de día no genera emisión autónoma. Carga/error y métricas compartidas requieren Presentation. Design 3:21 distingue códigos y alertas. |
| Conexión y lectura (RF-02, UI-03, F03) | ObdGateway, Elm327ObdGateway y Elm327SocketClient; pruebas de gateway, transporte y parser. Métricas nullable y lectura explícita. | Permiso runtime LAN y coordinación UI pendientes; salida durante lectura, IP/puerto y reintentos requieren revisar decisiones abiertas. Design 3:76/3:108 representa configuración y lectura; no demuestra interacción. |
| Diagnóstico e historial (RF-03, UI-04/05, F04/F05) | ScanDiagnosticCodesUseCase exige Connected y captura vehículo/hora/km; SaveDiagnosticSessionUseCase persiste solo éxito. DiagnosticAndMaintenanceUseCasesTest cubre limpio, fallo y captura; AppDatabaseTest cubre consultas históricas. | Resultado pendiente, guardar/descartar y reintento sin nuevo escaneo requieren coordinación Presentation. Verificar lista y detalle por separado y retorno al origen. |
| Mantenimiento (RF-04, UI-06, F06) | SaveMaintenanceUseCase valida límites y confirmación histórica; RoomMaintenanceRepository inserta o actualiza el mismo ID sin avanzar odómetro al editar. MaintenanceType incluye frenos, neumáticos, batería y refrigerante; OTHER sin alertas. Pruebas unitarias y AppDatabaseTest cubren reglas y consultas. | Diseñar alta/edición, validación y recuperación de errores; verificar representación de tipos y programación opcional. |
| Ajustes y continuidad (UI-07, F07–F09) | AppPreferencesRepository solo persiste activo e IP/puerto. No hay ViewModels ni navegación funcional; MainActivity muestra Greeting. | Nombre, unidades y visibilidad no están implementados. Persistencia de ajustes, redondeo de millas y salida durante lectura siguen abiertos; recreación y formularios requieren pruebas de integración/UI. |

La existencia de pruebas no acredita ejecución actual ni cobertura integral; consultar reportes históricos para resultados anteriores. No se verificaron exhaustivamente las pruebas instrumentadas.

## Inspección Figma

- [Design 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2): metadatos consultados. Incluye Inicio vacío/resumen, vehículos, conexión, diagnóstico y otras variantes DRAFT. El frame 3:55 representa edición, por lo que el alta requiere revisión diferenciada. El texto 3:67 explica detalles de ECU dentro del formulario; evaluar si aporta a la persona usuaria. El texto 3:48 sobre bloqueo debe contrastarse con el flujo de captura pendiente y estados transitorios.
- [FigJam raíz](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=0-1): F01–F10 y decisiones abiertas inspeccionados mediante capturas; se identificaron contradicciones detalladas abajo.
- La inspección estática no valida accesibilidad, interacciones ni aprobación visual. No se modificó Figma.

## Inspección visual — 2026-11-08

Se revisaron capturas de los siete grupos de [wireframes 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2), las seis variantes adicionales de [conexión 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2), y capturas individuales de F01–F10 y decisiones abiertas en FigJam. Inspección visual estática de borradores; no prueba interacciones, contraste medido, TalkBack, adaptación ni aprobación.

| Grupo | Evidencia y antecedentes para diseño |
|---|---|
| Inicio/vehículos 3:7; 8:4 | Vacío, resumen, limpio y sin historial diferenciados. Barra inferior se parte en dos líneas; «Usar este vehículo» no identifica claramente a qué fila aplica. Alta no tiene variante propia; formulario 3:55 representa edición. Faltan estados visuales de errores/selección parcial. Ajustes no tiene acceso evidente en las capturas principales. |
| Conexión 3:75 y 9:2 | Configuración, conexión, lectura, error y seis variantes adicionales visibles. Hay «Volver» duplicado en configuración/error; el aviso de cancelación incluye redacción técnica. Distinguir lectura no disponible, sin lectura y cero; verificar recuperación de permiso y fallo de guardado con los acuerdos vigentes. |
| Diagnóstico 3:147/3:217 | Hay listo, pendiente, limpio, guardado, escaneando, fallo y fallo al guardar. En pendiente se muestra «Código desconocido» sin código real: contrasta con DD-18 y fallback del catálogo. 3:218 expone «política ... pendiente de definición» al usuario. El detalle 3:265 mezcla lista de sesiones y detalle; separar recorridos y vacío. |
| Mantenimiento 3:283 | Lista, alta, edición e histórico visibles; falta mostrar opciones de tipos y variantes de validación, vacío y fallo. Edición 3:324 no representa tipo ni notas aunque existen en el registro; comprobar preservación y edición según contrato. |
| Ajustes/confirmaciones 3:361 | Nombre, unidades e interruptores aparecen como texto de esquema; precisar controles y errores tras resolver guardado y redondeo. 3:398 muestra una regla interna sobre apertura de Ajustes. Confirmaciones incluyen acciones pero falta validar consecuencias y foco accesible. |
| Presentación transversal | Capturas legibles a escala inspeccionada, sin recortes evidentes en los grupos, salvo salto de navegación inferior. Son composiciones fijas de baja fidelidad: faltan evidencias para scroll, teclado, tamaños de ventana y texto ampliado. |

### Contradicciones de FigJam

- [F01 2:74](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-74) dice que la ausencia numérica requiere concreción; DD-24 ya define año 0 como ausencia. Mantener pendiente solo su presentación.
- [F02 2:121](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-121) tiene conectores y etiquetas superpuestos en la parte inferior; su representación de desconectar primero debe incorporar la prioridad de resolver captura pendiente según actualización de DD-17/28.
- [F03 2:220](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220) afirma que al salir durante lectura se descarta y cierra conexión, y define guardado tras conexión. El [panel de abiertos 2:616](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-616) conserva ambos puntos abiertos. La actualización posterior de screen-flows.md sí registra guardado tras handshake; persiste una contradicción sobre el alcance de la salida voluntaria durante lectura. Reconciliar antes de modificar el tablero. Conectores cruzan nodos y texto.
- [F04 2:323](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-323) mantiene abiertos cambio/desconexión con captura pendiente, mientras la actualización DD-17/28 ya prioriza guardar, descartar con advertencia o seguir aquí. Precisar qué aspectos residuales siguen abiertos. Varios conectores atraviesan textos.
- [F05 2:426](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-426) cita recurrentCodesInLastSession y alertas DTC recurrentes como base; Dashboard actual usa codesInLastSession y alertas de mantenimiento. Corregir representación sin cambiar la regla de recurrencia.
- [F06 2:505](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-505) dice que repositorio solo inserta y enumera tipos antiguos; edición y tipos adicionales ya existen. El diagrama conserva correctamente el requisito de mismo ID, pero debe actualizar su estado técnico.
- [F07–F10](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-554) representan preferencias, formularios, continuidad y odómetro futuro; no acreditan implementación. El panel 2:616 también conserva como pendientes edición/validación y corrección de recurrentCodesInLastSession, ya superados en backend.

No se cambió Figma. Actualizar nodos solo con autorización aplicable, conservando trazabilidad; no convertir un dibujo en decisión confirmada.

## Revisión editable ampliada — 2026-11-09

Se prepararon copias de Inicio, Conexión, Diagnóstico, Historial, Mantenimiento, Ajustes y estados adicionales en [Design 11:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-2); Vehículos sigue en 10:2. Capturas estáticas de grupos y variantes inspeccionadas; 604 nodos editables y sin imágenes aplanadas. Pendientes exactos y límites en [referencias](figma-references.md#revisión-de-wireframes-por-recorrido--2026-11-09). Ninguna variante ha sido aprobada visualmente ni probada como prototipo.

## FigJam actualizado parcialmente — 2026-11-08

El tablero F01–F10 había sido revisado estáticamente y sincronizado antes de DD-33–48. Se actualizaron nodos del mapa, F01–F08 y panel histórico; F09/F10 se conservaron. La inspección de F04, F07 y panel confirma los textos modificados; F04 mantiene conectores que cruzan nodos y etiquetas. La revisión detallada de todos los nodos, su trazado y variantes sigue pendiente. Ver [registro](figma-references.md#figjam--sincronización-funcional-parcial-2026-11-08).

## Vehículos actualizado para revisión — 2026-11-08

Se creó [grupo editable 10:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-2) con lista, alta y edición basadas en DD-42; se conservaron los originales. Comprobación visual estática tras corregir recortes. La lista es la variante desconectada, el menú es esquemático y la navegación inferior sigue en dos líneas. Faltan estado conectado y guardias, descarte, errores y selección parcial, interacciones, accesibilidad y aprobación visual. Ver [referencias](figma-references.md#vehículos--revisión-editable-autorizada-2026-11-08).

## Prioridad de revisión visual — 2026-11-08

El usuario eligió comenzar por Vehículos e indicó que la aplicación trata de vehículos y mantenimiento y que el escaneo no debe ser la función principal todo el tiempo. Usar ese criterio para proponer jerarquía y recorridos; no implica cambiar automáticamente la navegación DD-13 ni aprueba un diseño visual. Primera revisión: alta, lista, selección y gestión de vehículos (DEP-001–003/019); después se abordarán los recorridos restantes.

### Decisión posterior — estado de conexión y métricas

El usuario confirmó 1A/2A/3A el 2026-11-08. [DD-48](design-decisions.md#dd-48-estado-de-conexión-y-métricas) concreta estado textual, conservación de lectura desconectada y valores no disponibles conforme DD-29; diseño y validación pendientes.

### Decisión posterior — Diagnóstico e Historial

El usuario confirmó 1A/2A/3A/4A el 2026-11-08. [DD-47](design-decisions.md#dd-47-presentación-de-diagnóstico-e-historial) define descripción por código, fallback genérico, diagnóstico limpio e información de historial. DiagnosticSession contiene fecha, kilometraje y códigos; DtcCatalog provee fallback local. Presentación y orden deben validarse durante implementación.

### Decisión posterior — vacíos de Inicio

El usuario confirmó A/A el 2026-11-08. [DD-46](design-decisions.md#dd-46-estados-vacíos-de-inicio-por-vehículo) mantiene visibles los estados sin mantenimiento y sin diagnóstico con sus respectivos accesos. El diseño sigue pendiente de aprobación.

### Decisión posterior — jerarquía de Inicio

El usuario confirmó A/A el 2026-11-08: [DD-45](design-decisions.md#dd-45-jerarquía-de-inicio-y-acceso-a-mantenimiento) prioriza vehículo y mantenimiento, y conecta el resumen con Mantenimiento. Falta diseñar y aprobar la representación.

### Decisión posterior — orden y programación

El usuario confirmó 1A/2A/3A el 2026-11-08. [DD-44](design-decisions.md#dd-44-orden-y-programación-de-mantenimiento) define orden cronológico descendente, campos opcionales visibles y etiquetas textuales de programación vigente; sin aprobación visual.

### Decisión posterior — lista y vacío de Mantenimiento

El usuario confirmó 1A/2A/3A el 2026-11-08. [DD-43](design-decisions.md#dd-43-lista-y-estado-vacío-de-mantenimiento) define datos por registro, acción principal y vacío; el diseño visual aún no se ha actualizado ni aprobado.

### Decisión posterior — lista y formulario de Vehículos

El usuario confirmó 1A/2A/3A el 2026-11-08. [DD-42](design-decisions.md#dd-42-presentación-y-gestión-de-vehículos) especifica contenido de fila, acciones asociadas y formulario común de alta/edición; las capturas aún no representan necesariamente estas decisiones y no están aprobadas.

## Evaluación de hallazgos posterior a la matriz — 2026-11-08

Evaluación sobre la inspección estática ya registrada y la [matriz](screen-dependencies.md). «Discrepancia» significa diferencia observable con un acuerdo o contrato, no un defecto validado en un prototipo ejecutable. Las propuestas no modifican decisiones ni suponen autorización para editar Figma.

| Recorrido y DEP | Evaluación y evidencia | Tratamiento propuesto |
|---|---|---|
| Vehículos y formularios ([DEP-001–003](screen-dependencies.md#dep-001), [DEP-019](screen-dependencies.md#dep-019)) | En Design 3:7/3:55 se ve edición sin alta diferenciada y la acción de selección no identifica claramente la fila. F01 conserva la ausencia de año como abierta pese a DD-24; F02 no refleja claramente la prioridad de resolver una captura pendiente. Faltan evidencias visuales de fallos parciales. | Corregir representación conforme a DD-24/27/28/30; distinguir alta, selección y recuperación sin crear reglas nuevas. Revisar con el usuario los estados resultantes. |
| Inicio ([DEP-004–005](screen-dependencies.md#dep-004), [DEP-009](screen-dependencies.md#dep-009)) | 3:21 separa códigos y alertas; F05 cita un contrato anterior de recurrencia. El requisito histórico RF-05 habla de alertas DTC recurrentes y DD-18 posterior incluye todos los códigos sin duplicarlos como alertas de mantenimiento: su relación exacta sigue abierta. El fallback de activo no se persiste y el cambio de día no emite por sí solo. | Actualizar representación del contrato conocido sin cerrar RF-05 por inferencia. Definir con el usuario si y cómo destacar recurrencia; contemplar actualización de fecha en el plan técnico posterior. |
| Conexión ([DEP-006–009](screen-dependencies.md#dep-006)) | 3:75 y 9:2 muestran variantes, pero no prueban permiso runtime ni interacción; hay retorno duplicado y texto de cancelación técnico. F03 contradice el panel de abiertos sobre salida durante lectura. Guardar IP/puerto tras handshake sí consta en el flujo posterior; salida con cambios y recuperación del permiso siguen abiertas. | Corregir duplicación y lenguaje durante diseño; acordar salida voluntaria durante lectura, campos modificados y recuperación de permiso antes de cerrar variantes. Sin cambiar DD-26 para conexión en curso. |
| Diagnóstico e historial ([DEP-010–014](screen-dependencies.md#dep-010)) | El resultado pendiente muestra «Código desconocido» sin código real y 3:218 expone una política interna; contradice la necesidad de conservar el código capturado y usar descripción genérica. 3:265 mezcla lista y detalle; retorno al origen está acordado pero no validado. F04 no refleja plenamente la coordinación posterior de captura pendiente. | Mostrar el código real con descripción disponible/genérica, separar lista y detalle y representar guardar/reintentar/descartar conforme DD-25/28. Verificar navegación y estados de error después de actualizar los borradores. |
| Mantenimiento ([DEP-015–017](screen-dependencies.md#dep-015)) | F06 y la nota 3:5 describen implementación antigua; el backend ya actualiza el mismo ID y admite tipos adicionales. 3:324 no muestra tipo ni notas; no hay evidencia de variantes completas de validación, vacío o error. | Actualizar notas externas y representar alta, edición y estados con DD-15/16/30. Comprobar preservación de todos los campos existentes; no inferir que un campo omitido en la captura se elimina. |
| Ajustes ([DEP-018–019](screen-dependencies.md#dep-018)) | 3:361 presenta ajustes como esquema y 3:398 muestra una regla interna. Nombre, unidades y visibilidad no están en el contrato de preferencias actual. Momento de guardado, errores y redondeo de entradas en millas siguen abiertos. | Diseñar controles y estados tras acordar las decisiones pendientes; mostrar el significado de unidades de DD-31 sin exponer instrucciones internas. No aprobar guardado inmediato por aparecer en un borrador. |
| Transversal ([DEP-019–021](screen-dependencies.md#dep-019)) | Barra inferior partida en la captura; acceso a Ajustes no es evidente. Capturas fijas no verifican scroll, teclado, texto ampliado, adaptación ni TalkBack. DD-30–32 definen descarte y continuidad, pero no hay Presentation funcional inspeccionada. | Revisar cuatro destinos y accesos secundarios; preparar variantes de confirmación y recreación. Validar adaptación, accesibilidad e interacciones con medios adecuados antes de DESIGN_READY. |

### Decisiones que siguen abiertas

1. **Salida voluntaria durante lectura** ([DEP-008](screen-dependencies.md#dep-008)): definir alcance de Atrás, cambio de destino y desconexión, incluida conexión final. Rotación sigue DD-32 y no es salida.
2. **Permiso LAN** ([DEP-006](screen-dependencies.md#dep-006)): explicar el permiso, denegación y recuperación sin presuponer que el manifiesto lo concede.
3. **IP/puerto modificados** ([DEP-007](screen-dependencies.md#dep-007)): concretar qué sucede al salir sin intentar conectar; el guardado después del handshake exitoso ya está acordado.
4. **Ajustes** ([DEP-018](screen-dependencies.md#dep-018)): decidir guardado/errores y precisión/redondeo de nuevas entradas en millas, preservando DD-22/31.
5. **Recurrencia** ([DEP-005](screen-dependencies.md#dep-005)): reconciliar expresamente RF-05 con DD-18 y definir presentación sin inferir que un documento cancela al otro.

### Decisiones posteriores — listas y cambio de día

El usuario confirmó 1A y 2A el 2026-11-08: [DD-40](design-decisions.md#dd-40-carga-y-error-de-listas) extiende el patrón de carga/error a listas y [DD-41](design-decisions.md#dd-41-actualización-de-vencimientos-al-volver-a-inicio) recalcula vencimientos al activar Inicio, sin exigir actualización a medianoche con pantalla visible. Implementación y validación visual pendientes.

### Decisión posterior — carga y error de Inicio

El usuario confirmó la opción B el 2026-11-08: conservar datos previos del mismo vehículo al actualizar, advertir si fallan y ofrecer reintento; primera carga usa estado completo. [DD-39](design-decisions.md#dd-39-carga-y-error-de-inicio). La recuperación de flujos, presentación y validación siguen pendientes.

### Decisión posterior — recurrencia en Inicio

El usuario confirmó la opción A el 2026-11-08: etiqueta por código recurrente usando el análisis existente, sin favoritos ni alerta separada. [DD-38](design-decisions.md#dd-38-recurrencia-de-códigos-en-inicio) reconcilia el alcance funcional de RF-05 y DD-18; las menciones anteriores a esta cuestión como abierta son antecedentes. La presentación visual y sus pruebas siguen pendientes.

### Decisión posterior — entrada en millas

El usuario autorizó B si no exige cambio de backend y A en caso contrario. Los modelos Vehicle y MaintenanceRecord almacenan kilómetros como Int y SaveMaintenanceUseCase valida esos valores; se registra B en [DD-37](design-decisions.md#dd-37-entrada-de-distancias-en-millas). Se necesita cálculo de conversión/redondeo en Presentation, no un cambio de tipo o esquema. Las referencias anteriores al redondeo como abierto son históricas.

### Decisión posterior — guardado de Ajustes

El usuario confirmó la opción B el 2026-11-08: borrador común y botón Guardar, con confirmación de descarte coherente con DD-30/35. Véase [DD-36](design-decisions.md#dd-36-guardado-explícito-de-ajustes). La precisión y redondeo de entradas en millas permanecen abiertos; las referencias anteriores al momento de guardado como pendiente son históricas.

### Decisión posterior — configuración OBD modificada y coherencia

El usuario confirmó la opción B el 2026-11-08 y solicitó coherencia entre flujos similares. [DD-35](design-decisions.md#dd-35-salida-con-ip-o-puerto-modificados) alinea Atrás/cambio de destino con DD-30 para vehículos y mantenimiento y DD-31 para acceso a Ajustes. DD-25/26/28/33 conservan sus reglas específicas para resultados u operaciones OBD. El guardado de ajustes de preferencias sigue abierto; no se decidió por analogía. El listado anterior conserva antecedentes históricos.

### Decisión posterior — recuperación del permiso LAN

El usuario confirmó la opción C el 2026-11-08: reintentar cuando la plataforma lo permita y orientar a ajustes cuando no. Véase [DD-34](design-decisions.md#dd-34-recuperación-del-permiso-de-red-local). La lista y evaluación previas conservan antecedentes históricos; falta validar los detalles de plataforma y actualizar Figma.

### Decisión posterior — salida voluntaria durante lectura

El usuario confirmó la opción C el 2026-11-08 por coherencia con situaciones similares. Se registra el alcance en [DD-33](design-decisions.md#dd-33-salida-voluntaria-durante-lectura-obd); la ficha y menciones anteriores se conservan como antecedentes históricos. La implementación y actualización de Figma siguen pendientes.

### Primera ficha para decisión: salida voluntaria durante lectura

**Escenario:** el usuario inicia una lectura OBD y pulsa Atrás o cambia de destino antes de recibirla. DD-32 exige que una recreación conserve la operación; DD-26 regula conectar y desconectar, pero no resuelve este caso. La cancelación del transporte puede cerrar el socket; la interfaz debe observar el estado real.

- **A. Esperar en la pantalla:** impedir la salida hasta recibir resultado o fallo. Coste bajo de coordinación, pero puede hacer esperar al usuario.
- **B. Permitir salir y mantener la lectura:** conservar una sola operación y mostrar su resultado real cuando corresponda. Requiere coordinar estado compartido y origen de la captura; una salida no implica desconexión.
- **C. Cancelar y salir:** interrumpir la lectura, descartar la captura incompleta e informar el estado real de conexión; requiere coordinar cancelación y posible cierre de socket.

**Recomendación para discutir: C**, porque mantiene explícita la relación entre abandonar y cancelar, siempre que se comunique la posible desconexión y se verifique el estado real. Las tres opciones requieren especificar si se aplican igual a Atrás y cambio de destino; desconectar explícitamente debe resolver primero un diagnóstico ya pendiente. Ninguna opción está aprobada.

## Siguientes pasos para definir el diseño

1. Crear la matriz de interacciones con evidencia por requisito, DD, frame, contrato y prueba; registrar incertidumbres y hallazgos visuales sin evaluarlos todavía.
2. Después de crearla, evaluar variantes y hallazgos de diseño por recorrido y revisar decisiones abiertas con el usuario.
3. Registrar decisiones explícitas y después actualizar flujos y frames autorizados.
4. Obtener aprobación visual por variante y completar el handoff preliminar; un contrato implementado o un frame existente no equivalen a DESIGN_READY.

Fuentes: [plan de diseño](implementation_plan.md), [contratos](domain-contracts.md), [decisiones](design-decisions.md), [flujos](screen-flows.md), [referencias](figma-references.md), [contexto maestro](../Contexto_Maestro_App_OBDII_v2.md) y [plan técnico](../implementation_plan.md).
