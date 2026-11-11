# Handoff de diseño — histórico previo a Fase 4

## Etapa 4.6 — estado vigente

UI-03 Conexión y UI-04 Diagnóstico están implementadas y QA/Ponytail emitió **APROBADO para la implementación verificable**. Ver [reporte de cierre 4.6](../reporte_fase4_etapa4_6.md). La tabla 4.4 inferior conserva su carácter histórico; sus columnas de integración pendiente para UI-03/04 ya no describen el código actual.

Límites: permiso LAN runtime no comprobado en API 37, ELM327 sin host, estados Compose y métricas sin prueba visual específica, ni comprobación manual de TalkBack/IME. Los wireframes base siguen DESIGN_READY como referencia; no se aprobaron variantes nuevas.

## Etapa 4.5 — estado vigente

[Plan UI-02 y tema](implementation_plan_fase4_etapa4_5.md) fue aprobado por el usuario y ejecutado. La implementación UI-02 y el tema fijo están completos; QA/Ponytail emitió **APROBADO**. Ver [reporte de cierre 4.5](../reporte_fase4_etapa4_5.md). Los wireframes base permanecen DESIGN_READY como referencia, sin acreditar pantallas de otras etapas ni variantes de diseño.

Límites: faltan verificaciones manuales de TalkBack, foco, fuente ampliada, contraste renderizado, IME/insets y barras. ELM327 se omitió sin elm327Host y no se comprobó permiso LAN runtime. No se editaron variantes Figma. La tabla 4.4 y registros de preparación de paquetes que siguen debajo son históricos; el estado vigente de UI-02 está en esta nota y en el reporte.


> La cabecera 4.5 añade la planificación vigente; la tabla 4.4 conserva el estado de soporte e integración auditado. El contenido posterior a “Evaluación de preparación por paquete” preserva registros históricos; sus estados técnicos o decisiones deben leerse junto con esta cabecera, el reporte de 4.3 y las decisiones actuales.

## Actualización de etapa 4.4 — revisión por paquete, 2026-11-10

Los wireframes base UI-01–07 aprobados por el usuario conservan estado **DESIGN_READY como diseño base**. Esto no marca el paquete de producto como implementado, no aprueba variantes posteriores ni acredita interacción, accesibilidad, contraste o pruebas. Inspección estática de código/documentación con Android Studio; no se ejecutaron Gradle ni pruebas en esta revisión. El flujo disponible no expuso Figma `use_figma`, búsqueda de librerías ni generación de muestras; no se editaron nodos ni se produjeron variantes A/B. Root actualiza por separado figma-references.md con las capturas revisadas.

| Pantalla | Soporte real / captura | Integración pendiente | Etapa | Estado del paquete |
|---|---|---|---|---|
| UI-01 Inicio | GetDashboardDataUseCase, HomeViewModel, colección que se reinicia al activar y AppSession compartida. 11:42 renderiza correctamente «Aún no hay diagnósticos»; 11:4 muestra la barra inferior como texto compacto. | UI aún Text placeholder; integrar carga/error por contexto, vacíos, métricas visibles, D3 (tarjeta de última sesión al detalle), navegación y protección contra datos de otro vehículo. No incluir admisión. | 4.8 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-02 Vehículos | VehicleRepository y AppSession coordinan selección/borrado y guardias. | Lista, formulario, edición/borrado y errores; alta parcial con retry del mismo ID; selección/cascada con refresh tras fallo. D2 bloquea edición del activo conectado; revalidar en guardado y carreras. | 4.5 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-03 Conexión | Elm327 gateway, estado/cancelación de AppSession y ruta F03 bajo Diagnóstico. 13:75 muestra un acceso visual a Ajustes. | Aviso y permiso LAN runtime real, formulario y validación previa al socket, handshake/persistencia separada, retry correcto, estados y salida a Diagnóstico. La captura no acredita concesión runtime. | 4.6 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-04 Diagnóstico | Scan/Save Domain, captura pendiente y Save/Discard/Stay en AppSession. | Presentación de lectura/resultado, guardar/reintentar con captura original, resultado guardado y guardias de salida; sin recuperación tras muerte de proceso. | 4.6 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-05 Historial | Repositorio de sesiones/consulta por ID y rutas declaradas. | ViewModel, lista/detalle/vacío/error, ID/contexto y retorno al origen. | 4.7 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-06 Mantenimiento | MaintenanceRepository/SaveMaintenanceUseCase cubren guardar/editar; 11:286 presenta «Aceite» como ejemplo, no como selector completo. | Lista y formulario, selector de siete tipos alertables + OTHER, km histórico, validación, errores y borrador. | 4.7 | Wireframe base DESIGN_READY; integración UX_REVIEW |
| UI-07 Ajustes | AppSettings/DataStore de 4.2 cubre nombre, unidades y visibilidad. | Borrador común, Guardar, descarte y error/retry con estado real. La dirección D4 está elegida; muestras, tokens y contraste se transfieren a 4.5. | 4.8 | Wireframe base DESIGN_READY; integración UX_REVIEW |

**Decisiones vigentes:** D1, D2 y D3 APROBADAS por el usuario; D4 dirección visual elegida (apariencia A, paleta fija y modos claro/oscuro según sistema), muestras/tokens/contraste pendientes. No reaperturar estas decisiones ni confundirlas con aprobación de variantes. Connected bloquea cambio/borrado y edición del activo cuando corresponda; la captura pendiente debe resolverse antes de acciones incompatibles. Actualizar activo en DataStore y borrar en Room no es una transacción atómica; ante fallo se consulta/refresca estado real.


## Snapshot histórico — Evaluación de preparación por paquete

Este registro anterior a completar 4.3 no altera el estado vigente de la cabecera. La aprobación DESIGN_READY de wireframes base UI-01–07 se conserva; el estado UX_REVIEW describía integración de producto y no cancela esa aprobación de diseño. Las variantes posteriores, accesibilidad, contraste, interacción y validación mantienen sus propias puertas. Backend 4.1, preferencias 4.2 y composición/ciclo de vida/navegación técnica 4.3 están implementados y cerrados; quedan pantallas Presentation/UI de 4.5–4.8. La tabla inferior se conserva como snapshot histórico. Evidencia y alcance en [referencias](figma-references.md#revisión-de-cierre--evidencia-y-límites).

| Paquete | Evidencia y alcance | Pendiente para cerrar |
|---|---|---|
| UI-01 Inicio | Wireframes aprobados y variantes posteriores de vacío/carga/error; captura 36:36 muestra vacío aplicable también tras borrar el último. | Revisar variantes nuevas; corregir o validar barra partida; verificar recorridos, accesibilidad y prototipo; Presentation debe aplicar preferencias y recalcular al activar Inicio. |
| UI-02 Vehículos | Wireframes aprobados; referencias de conectado, fallo parcial y carga/error posteriores. Captura 10:3 muestra barra partida. | Revisar variantes nuevas y barra; verificar alta, selección, borrado y recuperación de fallos en prototipo; guardias y coordinación Android pendientes. |
| UI-03 Conexión | F03 y wireframes aprobados; 36:3, 36:22 y 38:2 inspeccionados estáticamente, con corrección/cancelación, progreso y null distinto de 0. | Revisar variantes nuevas, legibilidad de conectores F03 y vínculo textual de persistencia; comprobar recorridos, permiso y accesibilidad. |
| UI-04 Diagnóstico | Wireframes aprobados de limpio, DTC, fallo y guardado; captura íntegra prevista por DD-05/25. | Verificar interacciones, salida y guardado; backend DEP-011 idempotente implementado en 4.1, Presentation aún debe conservar la captura pendiente y reintentar sin reescaneo. |
| UI-05 Historial | Wireframes aprobados de lista/detalle y variantes posteriores de vacío/carga/error. | Revisar variantes nuevas y verificar detalle, retorno, aislamiento de vehículo y accesibilidad en recorridos. |
| UI-06 Mantenimiento | Wireframes aprobados; captura 11:286 usa Aceite como ejemplo y 11:267 tiene barra pequeña. | Representar y revisar selector de siete tipos alertables más OTHER; resolver o validar barra, interacciones y accesibilidad. Requiere autorización aplicable para editar Figma. |
| UI-07 Ajustes | Wireframe aprobado y estados posteriores de carga/error inspeccionados estáticamente. | Revisar variantes nuevas; comprobar guardado explícito, descarte, fallo parcial y presentación de preferencias implementadas en 4.2; validar accesibilidad. |

**Bloqueo común:** no se ha demostrado prototipo navegable ni validado accesibilidad, scroll, teclado, contraste, adaptación y focos; la ausencia de reacciones en los grupos inspeccionados no prueba que no exista prototipo en otro archivo. El contexto exige prototipo navegable: resolver con un prototipo mínimo y su validación, o aceptación explícita del usuario de entrega estática. Crear o modificar Figma requiere autorización aplicable. No se ha dispensado el requisito.



**Estado de backend al cierre de 4.2:** idempotencia/captureId y migración están implementados y probados (reporte 4.1); preferencias y errores DataStore están implementados y probados (reporte 4.2). Room builder/registro de migración, composición, ciclo de vida y coordinación Presentation siguen planificados para 4.3.

**Paso 3 — contraste técnico parcial, 2026-11-10:** [firmas, consumidores, defaults, errores y trabajo propuesto](domain-contracts.md#paso-3--contraste-de-contratos-y-consumidores-2026-11-10). Ningún paquete queda DESIGN_READY por este contraste; variantes nuevas, interacción, accesibilidad y prototipo requieren evidencia final. Android es responsable de planificar e implementar los contratos y pruebas pendientes; diseño/QA de validar representación e interacción. La aprobación de wireframes del 2026-11-09 se conserva.

**Ampliación paso 3:** [contraste por operación y cobertura DEP-001–023](domain-contracts.md#ampliación-del-contraste-por-operación--paso-3). Verificación independiente realizada y QA/Ponytail **APROBADO para el alcance documental ampliado**. El trabajo posterior debe conectar DEP-011 a Presentation con captura en memoria y reintento idempotente, y resolver guardias de conexión/navegación conforme DD-25 y decisiones aprobadas. Siguen pendientes enlaces exhaustivos y cierre visual, interactivo, de accesibilidad y prototipo; no se declara DESIGN_READY ni cierre integral.

**Actualización F03, 2026-11-10:** Diagnóstico es el origen MVP del flujo Conexión y métricas y destino de sus salidas finales; después se puede navegar a Inicio. Esta decisión reemplaza para F03 las referencias anteriores de acceso/salida directa desde Inicio. Véase [decisión](design-decisions.md#actualización-de-navegación-f03--2026-11-10).

Estado: handoff en actualización tras aprobación visual de wireframes UI-01–UI-07 del usuario, 2026-11-09; paquetes DESIGN_READY aún requieren verificar trazabilidad requisito→DD→DEP→frame→contrato→criterio de aceptación y registrar alcance por pantalla. Reglas en [design-decisions.md](design-decisions.md), flujos en [screen-flows.md](screen-flows.md); plan técnico preliminar en [plan técnico](../plan_tecnico_fase4_borrador.md). Aprobación de wireframes no equivale a prototipo interactivo ni integración validada.

## Registro histórico — Paso 2: fichas UI-02, UI-03 y UI-04

Inspección de 2026-11-10: textos visibles de frames individuales; interacción no comprobada. La aprobación de wireframes de 2026-11-09 se conserva; las variantes creadas posteriormente no quedan aprobadas individualmente por existir. [Saneamiento y límites](figma-references.md#saneamiento-de-variantes--2026-11-10). Tarjetas, filas y botones son representaciones estáticas; [componentes y tema](design-system.md#componentes-e-iconografía). Contraste, tamaño táctil, scroll, teclado y adaptación no verificados; observaciones de conectores y barras se revisarán al final salvo bloqueo.

### UI-02 Vehículos

**RF-01/RF-06; DD-06/17/24/27/28/30/40/42; [DEP-001](screen-dependencies.md#dep-001), [002](screen-dependencies.md#dep-002), [003](screen-dependencies.md#dep-003), [019](screen-dependencies.md#dep-019), [022](screen-dependencies.md#dep-022).** Reglas en [flujos](screen-flows.md#punto-3-activo-al-crear-vehículos-dd-27-aprobado).

**Referencias:** [lista 10:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-3), [alta 10:39](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-39), [edición 10:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-20), [conectado 27:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-3), [selección fallida 27:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-20), [borrado 11:397](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-397), [carga 28:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-3), [error 28:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-20), [actualización 28:37](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-37), [fallo de actualización 28:56](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-56). Los frames 27 y 28 son posteriores a la aprobación general y requieren revisión individual.

**Datos y comportamiento:** lista con activo, marca/modelo, año y km; combustible y motor en edición. Año 0 se muestra vacío o No informado; km internos enteros no negativos. Guardar primer vehículo y persistir selección son operaciones separadas; agregar otro conserva el activo válido. Seleccionar, editar y borrar requieren acciones explícitas; borrado confirma cascada. Connected bloquea cambiar activo y borrar; antes de desconectar/cambiar se resuelve captura pendiente. El vacío exitoso conduce al alta del primer vehículo, no es error. Primera carga/error son estados completos; actualización conserva solo datos del contexto actual, informa y reintenta. Borrador con cambios sigue DD-30.

**Contratos:** [VehicleRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/VehicleRepository.kt) expone lista Flow y saveVehicle devuelve ID; [AppPreferencesRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/AppPreferencesRepository.kt) persiste activo por separado. [DeleteVehicleUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/DeleteVehicleUseCase.kt) bloquea Connected y puede fallar tras cambiar preferencia. Room/DataStore no son transacción común. Presentation, guardias transitorias y reintento de selección usando el ID creado están pendientes.

**Criterios:** Dado primer Toyota guardado y fallo al seleccionar, reintentar usa el mismo ID sin insertar otro. Con Toyota activo, guardar Suzuki no cambia selección; elegir Suzuki desconectado sí. Año ausente permanece vacío y se acepta corrección de km no negativos. Connected o captura pendiente impiden cambio/borrado hasta resolver y desconectar. Confirmar borrado elimina vehículo e historial; ante fallo se consulta estado real. Ante error de actualización, conservar datos solo del mismo contexto y ofrecer reintento.

**Pruebas existentes:** [MaintenanceDashboardDeleteUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt) cubre Connected, selección y fallo tras cambiar activo; [AppDatabaseTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt) cubre cascada y conservación de relaciones al editar. Leídas, no ejecutadas ahora. Pendiente: pruebas UI de alta, reintento sin duplicación, guardias y navegación; equipo Android planifica. Interacción y variantes nuevas por validar. Estado en revisión; no DESIGN_READY.

### UI-03 Conexión y métricas

**RF-02; DD-20/26/29/33/34/35/48; [DEP-006](screen-dependencies.md#dep-006), [007](screen-dependencies.md#dep-007), [008](screen-dependencies.md#dep-008), [009](screen-dependencies.md#dep-009).** Según [decisión 2026-11-10](design-decisions.md#actualización-de-navegación-f03--2026-11-10), Diagnóstico inicia el flujo y recibe su salida final. Cancelar el intento permanece en configuración como acción intermedia; Historial mantiene retorno independiente.

**Referencias:** [aviso 11:415](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-415), [configuración 11:62](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-62), [conectando 11:81](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-81), [lectura 11:94](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-94), [fallo 11:115](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-115), [permiso denegado 11:432](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-432), [ajustes 13:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-75), [fallo guardar 11:449](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-449), [cambiar IP 11:466](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-466), [lectura interrumpida 11:483](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-483). [F03](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220) representa validación/cancelación y [49:724](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=49-724) persistencia con enlace textual. [IP/puerto inválido 36:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-3) y [desconectando 36:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-22) son nuevas referencias pendientes de revisión final; el diagrama no sustituye prueba interactiva.

**Datos y eventos:** IPv4 y puerto, defaults 192.168.0.10:35000; permiso LAN según plataforma. Entrada inválida no abre socket y conserva borrador para corregir/revalidar o cancelar sin guardar. TCP/init fallido permite reintentar; handshake exitoso precede guardado de configuración. Si falla persistencia, conexión sigue activa y se reintenta solo guardar. Connecting permite cancelar; Disconnecting termina recursos y bloquea conectar/leer; Connected no inicia lecturas automáticas. Lectura explícita presenta RPM, °C, km/h, fecha/hora y null distinto de cero. Vacío/carga de lista no aplican a configuración; sí progreso/error y recuperación.

**Contratos:** [ObdConnectionConfig](../../app/src/main/java/cl/cristian/bitacoraobd/domain/obd/ObdConnectionConfig.kt), [ObdGateway](../../app/src/main/java/cl/cristian/bitacoraobd/domain/obd/ObdGateway.kt), [ObdLiveMetrics](../../app/src/main/java/cl/cristian/bitacoraobd/domain/obd/ObdLiveMetrics.kt) y [preferencias](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/AppPreferencesRepository.kt). Permiso runtime, formulario, coordinación de handshake/guardado y captura compartida requieren Presentation.

**Resolución 2026-11-10:** el usuario indicó no contemplar temperatura de admisión. La tarjeta en 11:94 quedó oculta y conservada como histórica; no ampliar backend ni atribuirle soporte. La variante [38:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=38-2) muestra RPM no disponible y velocidad 0; está pendiente de revisión final. Se mantienen las tres métricas de DD-29.

**Criterios:** IP 192.168.0.999 o puerto 0 no abren socket; corregir revalida y cancelar vuelve a Diagnóstico sin guardar. Permiso denegado impide socket; reintento o ajustes dependen de plataforma y al volver se verifica concesión. Fallo TCP conserva campos y reintenta conexión; fallo de persistencia conserva conexión y reintenta solo guardado. Cancelar Connecting libera intento sin error; durante Disconnecting no inicia otra operación. RPM null y velocidad 0 se muestran diferentes sin lectura automática.

**Pruebas existentes:** [ObdContractsTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/obd/ObdContractsTest.kt) cubre límites IPv4/puerto y null/cero; [DataStoreAppPreferencesRepositoryTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DataStoreAppPreferencesRepositoryTest.kt) cubre persistencia. Leídas, no ejecutadas ahora. Pendientes: UI/runtime, ausencia de socket con datos inválidos, recuperación y hardware; equipo Android planifica. Revisar al cierre las nuevas variantes, incluida 38:2, antes de DESIGN_READY.

### UI-04 Diagnóstico

**RF-03/RF-06; DD-05/17/25/28/32/33/47; [DEP-010](screen-dependencies.md#dep-010), [011](screen-dependencies.md#dep-011), [012](screen-dependencies.md#dep-012), [008](screen-dependencies.md#dep-008).** Escaneo bajo demanda; conservar captura exitosa íntegra hasta guardar o descartar. Un fallo no crea historial.

**Referencias:** [desconectado 11:500](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-500), [listo 11:133](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-133), [leyendo 11:202](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-202), [códigos y desconocido 11:150](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-150), [limpio 11:167](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-167), [fallo lectura 11:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-217), [fallo guardado 11:232](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-232), [guardado 11:184](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-184), [limpio guardado 13:58](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-58), [salida pendiente 11:363](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-363). 23:61 está archivado, no vigente.

**Datos y eventos:** vehículo, fecha/hora, kilometraje local y códigos distintos. Desconocido muestra código e información genérica sin inventar causa. Conectar, escanear, guardar, reintentar guardado, descartar, seguir aquí y nuevo escaneo. Estados: desconectado/listo/leyendo/error/pendiente limpio o con DTC/guardando/error de guardado/guardado. Carga o actualización de lista no aplican a captura bajo demanda; progreso de lectura y guardado sí. Mientras guarda impedir repetición y esperar resultado; falla conserva captura. Resolver pendiente antes de salida, reescaneo o desconexión; cancelar lectura incompleta no guarda. Nuevo escaneo requiere Connected.

**Contratos:** [ScanDiagnosticCodesUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/ScanDiagnosticCodesUseCase.kt) exige vehículo y Connected y devuelve Success/Failure; Success captura fecha, km y códigos distintos. [SaveDiagnosticSessionUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveDiagnosticSessionUseCase.kt) no escribe Failure y guarda Success. Captura pendiente, guardias, prevención de doble guardado y continuidad son Presentation pendiente; recuperación tras cierre forzado está fuera del MVP.

**Criterios:** lectura exitosa vacía a 42.000 km puede guardarse como sesión limpia. Con P0300 y P0999 desconocido se muestran ambos y se guarda captura completa. Fallo no crea historial ni equivale a limpio. Si guardar falla, reintentar usa mismo vehículo/fecha/km/códigos sin nueva lectura; descartar elimina solo el pendiente. Seguir aquí conserva captura; guardado permite volver a Inicio o nuevo escaneo si sigue conectado. Informar que el resultado pendiente solo vive en memoria y se pierde si el proceso termina antes de guardarlo; persistencia/recuperación tras muerte del proceso queda fuera del MVP.

**Pruebas existentes:** [DiagnosticAndMaintenanceUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt) cubre limpio, conexión, fallo sin escritura, deduplicación y vehículo original con fakes; [DomainModelsTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/model/DomainModelsTest.kt) cubre captura limpia. Leídas, no ejecutadas ahora. Pendientes: UI, rotación, reintento de guardado y desconexión con pendiente; equipo Android planifica. Interacción por validar; no DESIGN_READY.

---

### UI-05 Historial

**RF-03; DD-09/13/18/38/40/47; [DEP-013](screen-dependencies.md#dep-013), [014](screen-dependencies.md#dep-014), [022](screen-dependencies.md#dep-022).** Consulta por vehículo, sin iniciar lectura OBD ni modificar alertas. Desde Inicio o Diagnóstico abre historial y retorna al origen; detalle vuelve a su origen.

**Referencias comprobadas:** [lista 11:249](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-249), [detalle 13:41](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-41), [carga 28:78](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-78), [error inicial 28:95](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-95), [actualización 28:112](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-112), [fallo con datos 28:131](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-131), [vacío 28:152](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-152). Lista muestra una acción textual de ejemplo para primera sesión; interacción por fila no comprobada. Las variantes 28 son posteriores a la aprobación general.

**Datos y estados:** lista ordenada por captura descendente e ID descendente, muestra fecha/hora, km locales y cantidad DTC; sesión limpia tiene cero códigos. Detalle muestra códigos y recurrencia anterior del mismo vehículo. Carga/error iniciales sustituyen lista; en actualización conservar datos del mismo vehículo, advertir y ofrecer reintento; vacío es consulta exitosa. Al cambiar contexto no mostrar datos del vehículo anterior. Guardado o borrador no aplica: consulta de sesiones ya persistidas.

**Contratos y criterios:** [DiagnosticRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/DiagnosticRepository.kt) observa sesiones por vehículo y obtiene detalle; [AnalyzeDiagnosticHistoryUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/AnalyzeDiagnosticHistoryUseCase.kt) consulta antecedentes por vehículo/fecha/ID. Dado Toyota con sesión de 07 nov de un código y otra de 02 nov limpia, lista aparece en ese orden y la limpia muestra 0; Suzuki sin sesiones ve vacío. Un P0300 anterior solo de Toyota marca recurrencia en Toyota, no en Suzuki. Al abrir detalle y volver se regresa al origen; fallo de actualización conserva datos del mismo vehículo con reintento. Presentation y navegación pendientes.

**Evidencia:** [AppDatabaseTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt) verifica sesiones, orden e aislamiento; [DiagnosticAndMaintenanceUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt) cubre recurrencia con fakes. No ejecutados ahora; faltan tests UI de origen, contexto, vacío y errores. Diseño/QA debe validar interacción por sesión y variantes nuevas; equipo Android planifica Presentation. No DESIGN_READY.

### UI-06 Mantenimiento

**RF-04/05; DD-07/08/15/16/30/40/43/44; [DEP-015](screen-dependencies.md#dep-015), [016](screen-dependencies.md#dep-016), [017](screen-dependencies.md#dep-017), [019](screen-dependencies.md#dep-019), [022](screen-dependencies.md#dep-022).** Consultar y registrar intervención; editar modifica mismo ID. Siete tipos alertables y OTHER sin alerta automática; no crear intervalo recurrente adicional.

**Referencias:** [lista 11:267](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-267), [alta 11:286](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-286), [editar 11:307](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-307), [confirmación km histórico 11:326](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-326), [vacío 13:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-22), [carga 28:170](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-170), [error inicial 28:189](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-189), [actualizando 28:208](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-208), [error con datos 28:229](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-229), [descarte 11:380](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-380). Aceite es solo un ejemplo visual. Por decisión del usuario, la implementación final debe ofrecer un selector con todos los tipos de MaintenanceType: aceite y filtro, solo aceite, solo filtro, frenos, neumáticos, batería, refrigerante y OTHER; no se exige crear un frame por tipo. Nuevos 28 requieren revisión individual.

**Datos y estados:** vehículo activo, tipo, fecha calendario, km enteros, notas y próximos límites opcionales. Mostrar lista por fecha más reciente y programación vigente separada del historial. Primera carga/error completos, actualización conserva solo datos de mismo vehículo; vacío exitoso ofrece registrar. Registrar, editar, guardar, cancelar, confirmar histórico y reintentar error. Próxima fecha/km no pueden preceder fecha/km realizados; fecha realizada no futura. Si intervención tiene km inferior al actual, pedir confirmación y no reducir odómetro. Salida con cambios pregunta; error conserva borrador. La representación de error de guardado del formulario no tiene frame específico identificado.

**Contratos y criterios:** [SaveMaintenanceUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/SaveMaintenanceUseCase.kt) devuelve Saved, Failed o RequiresHistoricalMileageConfirmation. Las validaciones devuelven Failed y, durante persistencia, solo IllegalArgumentException se convierte en Failed; otras excepciones de almacenamiento se propagan y requieren coordinación y manejo en Presentation. [MaintenanceRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/MaintenanceRepository.kt) guarda y consulta; [GetMaintenanceStatusUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetMaintenanceStatusUseCase.kt) compara fecha o km. Dado Toyota con 125.400 km, registrar intervención de 120.000 requiere confirmación y conserva 125.400. Editar registro existente conserva ID; cambiar fecha recalcula programación y no desplaza límites. Una fecha futura o límite anterior al realizado no se guarda; OTHER no genera alerta. Si falla actualización se conserva lista del mismo vehículo y se ofrece reintentar.

**Evidencia:** [DiagnosticAndMaintenanceUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/DiagnosticAndMaintenanceUseCasesTest.kt) y [MaintenanceDashboardDeleteUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt) contienen validaciones y confirmación; [AppDatabaseTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/AppDatabaseTest.kt) cubre consultas por fecha/ID y transacciones. No ejecutados ahora. Equipo Android: Presentation y pruebas UI, error, edición y alertas; diseño/QA: error de formulario, interacción y legibilidad de barra; equipo Android: selector con los ocho tipos definidos. No DESIGN_READY.

### UI-07 Ajustes

**DD-22/23/29/31/36/37; [DEP-018](screen-dependencies.md#dep-018), [019](screen-dependencies.md#dep-019).** Pantalla secundaria desde barra o menú; no quinto destino principal. Nombre opcional, distancia km/millas, temperatura °C/°F y tres interruptores de métricas Inicio; por defecto km/°C y los tres visibles.

**Referencias:** [preferencias 11:344](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-344), [descarte 29:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-3), [error 29:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-20). Dos últimos son posteriores a la aprobación general; requieren revisión.

**Datos y eventos:** borrador común con Guardar, Atrás, Seguir editando, Descartar, Revisar ajustes y reintento después de comprobar estado real. Sin cambios se vuelve al origen; con cambios se pregunta. En fallo parcial conservar borrador sin suponer atomicidad. Cambiar unidades solo cambia representación: km enteros persistidos, entrada en millas a un decimal redondeada a km al guardar, campo no editado conserva entero original; temperaturas °C y velocidades km/h en dominio. Carga inicial aplica al leer preferencias; referencias nuevas [carga 36:84](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-84) y [error 36:103](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-103), pendientes de revisión final. Un estado vacío no aplica porque existen defaults.

**Contrato y criterios:** [AppPreferencesRepository](../../app/src/main/java/cl/cristian/bitacoraobd/domain/repository/AppPreferencesRepository.kt) contiene solo activo y configuración OBD; nombre, unidades y visibilidad son ampliaciones futuras, no API disponible. Dado borrador con nombre vacío y millas, salir y seguir editando conserva borrador; descartar mantiene preferencias anteriores. Guardar cambia representación sin reescribir históricos; cambiar campo distinto no altera kilometraje no editado. Ante fallo parcial revisar estado real antes de reintentar. Ocultar los tres interruptores oculta bloque de métricas, no altera captura.

**Evidencia:** [DataStoreAppPreferencesRepositoryTest](../../app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DataStoreAppPreferencesRepositoryTest.kt) solo cubre activo y OBD, no los ajustes propuestos; no ejecutado ahora. Equipo Android debe planificar contrato, compatibilidad, conversión y pruebas; diseño/QA validar al cierre las variantes y referencias de carga/error inicial. No DESIGN_READY.

### UI-01 Inicio y transversal

**RF-05; DD-09/10/13/27/29/32/38/39/41/45/46/48; [DEP-004](screen-dependencies.md#dep-004), [005](screen-dependencies.md#dep-005), [020](screen-dependencies.md#dep-020), [021](screen-dependencies.md#dep-021), [023](screen-dependencies.md#dep-023).** Vehículo activo → mantenimiento → diagnóstico → métricas; últimos DTC todos visibles con marca de recurrencia, separados de mantenimiento. Cuatro destinos: Inicio, Vehículos, Diagnóstico y Mantenimiento; Conexión se inicia desde Diagnóstico para MVP.

**Referencias:** [sin vehículos 36:36](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-36) sustituye como referencia propuesta el original histórico 3:8; aplica también tras eliminar el último vehículo. [Resumen 11:4](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-4), [limpio 11:23](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-23), [sin diagnóstico 11:42](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-42), [sin mantenimiento 13:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-3), [cargando conexión 23:79](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=23-79), [error conexión 23:137](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=23-137). Estos dos últimos representan estado de conexión con datos previos. Las nuevas referencias para primera carga/error completo son [36:49](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-49) y [36:66](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-66), pendientes de revisión final.

**Datos y eventos:** activo y km, alertas derivadas, última sesión, todos sus códigos con recurrencia, lectura OBD compartida e identificada por fecha; sin vehículos ofrece agregar. Abrir mantenimiento, diagnóstico, historial, cambiar vehículo y preferencias con guardias; recálculo al activar usa fecha actual. Actualización conserva datos del mismo vehículo con aviso de posible desactualización; al cambiar contexto no presentar datos anteriores como actuales. Cierre forzado no recupera resultado diagnóstico pendiente; recreación con proceso vivo conserva estado y operación sin duplicar.

**Contratos y criterios:** [GetDashboardDataUseCase](../../app/src/main/java/cl/cristian/bitacoraobd/domain/usecase/GetDashboardDataUseCase.kt) expone NoVehicles o Ready, selecciona activo válido o primero disponible y no repara preferencia. [DashboardSummary](../../app/src/main/java/cl/cristian/bitacoraobd/domain/model/DashboardSummary.kt) incluye códigos y mantenimientos pero no métricas; captura compartida, refresco al activar, carga/error y navegación son Presentation pendientes. Dado ningún vehículo aparece invitación a agregar; Toyota con P0300 recurrente y P0420 nuevo muestra ambos y solo etiqueta el primero. Sin mantenimiento o diagnóstico se muestran vacíos independientes. Tras cambiar de Toyota a Suzuki nunca se muestran datos de Toyota como actuales; actualización fallida conserva datos Suzuki con aviso. Al volver al día siguiente se recalcula vencimiento; rotación no duplica lectura o guardado.

**Evidencia:** [MaintenanceDashboardDeleteUseCasesTest](../../app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/MaintenanceDashboardDeleteUseCasesTest.kt) cubre vacío, fallback, cambio/aislamiento y códigos; no ejecutado ahora. Faltan UI, recreación, activación por fecha, accesibilidad y prototipo. Equipo Android planifica coordinación; diseño/QA revisa al cierre las nuevas referencias sin vehículos y de estados iniciales. No DESIGN_READY.

---

## Registro histórico — Criterios de aceptación por escenario (paso 2)

Son casos previstos para validar el comportamiento acordado, no resultados de prueba. Las referencias y contratos de cada paquete están en las fichas anteriores; cuando falte un frame o contrato, el caso sigue pendiente de verificación.

**UI-02 Vehículos**

- Dado que no hay vehículos, cuando se guarda Toyota y se persiste su ID activo, entonces Inicio muestra Toyota; si falla la selección, se informa que ya fue guardado y reintentar usa el mismo ID.
- Dado Toyota activo y Suzuki adicional, cuando se guarda Suzuki sin seleccionarlo, entonces Toyota sigue activo; cuando se selecciona Suzuki desconectado, entonces Suzuki pasa a ser activo.
- Dado Toyota con año no informado y 42.000 km, cuando se edita, entonces el año aparece vacío o No informado y una corrección a 41.000 km puede guardarse.
- Dado Toyota activo y Suzuki restante, cuando la selección cambia a Suzuki y falla el borrado de Toyota, entonces se informa el fallo y se consulta el estado real: Toyota sigue existiendo y Suzuki puede haber quedado activo; no se anuncia una operación atómica ni se repite el borrado automáticamente.
- Dado Toyota con diagnósticos y mantenimientos, cuando se confirma su eliminación exitosa, entonces desaparecen Toyota y sus registros asociados. Si OBD está Connected, cuando se solicita cambiar activo o borrar, entonces se bloquea la operación. Si existe diagnóstico pendiente, cuando se intenta desconectar o cambiar vehículo, entonces se resuelve primero el pendiente.
- Dada la primera carga sin datos, cuando está en curso o falla, entonces se muestra carga o error con reintento, respectivamente; dado un refresco fallido, cuando hay datos previos del mismo contexto, entonces se conservan y se advierte que pueden estar desactualizados.

**UI-03 Conexión**

- Dado el permiso LAN faltante, cuando se pulsa Conectar, entonces se solicita si la plataforma lo permite; ante denegación se informa y se ofrece recuperación según plataforma, sin abrir socket. Al regresar de ajustes se comprueba concesión real.
- Dado que el usuario concede el permiso LAN, cuando regresa al flujo y se comprueba la concesión real, entonces se revalida la configuración y se inicia como máximo un intento de conexión si los datos son válidos.
- Dado IP 192.168.0.999 o puerto 0, cuando se pulsa Conectar, entonces se identifica la entrada inválida sin abrir socket; cuando se corrige y reintenta, se valida de nuevo. Si se cancela la rama inválida, vuelve a Diagnóstico sin guardar borrador.
- Dados IP y puerto válidos, cuando fallan TCP o inicialización, entonces se conservan los campos y se puede reintentar conexión; cuando handshake tiene éxito, entonces se intenta guardar la configuración.
- Dado handshake exitoso con conexión activa, cuando falla guardar IP/puerto, entonces se informa el fallo manteniendo la conexión; cuando se reintenta, se repite solo la persistencia, sin otro handshake.
- Dado Connecting, cuando se cancela el intento, entonces se interrumpe y permanece en configuración sin tratar cancelación voluntaria como fallo; cuando se sale del flujo, se vuelve a Diagnóstico. Dado Disconnecting, cuando se solicita otra conexión o lectura antes de terminar, entonces no se inicia.
- Dada una conexión activa sin pulsación de lectura, cuando se abre la pantalla, entonces no se realiza lectura automática. Dada lectura explícita con RPM no disponible y velocidad 0, cuando se presenta, entonces se distingue No disponible de 0.

**UI-04 Diagnóstico**

- Dada conexión activa y captura exitosa sin códigos, cuando se guarda, entonces se crea una sesión limpia con vehículo, fecha/hora y km originales; dado fallo de escaneo, cuando termina, entonces no se crea historial ni se presenta como limpio.
- Dada captura de P0300 y P0999 desconocido, cuando se muestra y guarda, entonces aparecen ambos códigos con información disponible o genérica y se conserva la captura íntegra.
- Dada captura pendiente, cuando falla su guardado y se pulsa reintentar, entonces se usa la misma captura sin nuevo escaneo; cuando se elige descartar, se elimina solo el pendiente y se vuelve a Inicio.
- Dada captura pendiente, cuando se intenta salir o desconectar, entonces se pide resolverla; si se elige Seguir aquí, permanece intacta. Dada una captura guardada y conexión activa, cuando se elige Nuevo escaneo, entonces comienza otra captura sin reemplazar la sesión guardada.

**UI-05 Historial**

- Dado Toyota con sesiones del 7 de noviembre con un DTC y del 2 de noviembre limpia, cuando se consulta, entonces aparecen en ese orden y la limpia indica cero códigos; dado Suzuki sin sesiones, se muestra vacío.
- Dado P0300 previo solo en Toyota, cuando se analiza una sesión posterior de cada vehículo, entonces solo Toyota muestra recurrencia. Cuando se abre un detalle desde Inicio o desde lista y se vuelve, entonces se retorna al origen correspondiente.
- Dado historial sin datos previos, cuando carga o falla, entonces se muestra carga o error con reintento; dado un refresco fallido, cuando existen datos del mismo vehículo, entonces se conservan con advertencia y reintento.

**UI-06 Mantenimiento**

- Dado Toyota sin registros, cuando se abre Mantenimiento, entonces se muestra vacío y acción de registrar. Dada una primera carga fallida, cuando se pulsa Reintentar, entonces se vuelve a consultar; un fallo de actualización conserva datos del mismo vehículo.
- Dado Toyota con 125.400 km, cuando se intenta registrar intervención a 120.000 km, entonces se pide confirmar histórico; al confirmar se guarda sin reducir el odómetro.
- Dado un registro de aceite con ID existente, cuando se edita fecha y se guarda, entonces se conserva el ID y se recalcula la programación vigente sin desplazar límites absolutos. Cuando se cancela edición, el original queda igual.
- Dada fecha futura, próxima fecha anterior a la realizada o próximo km inferior al realizado, cuando se intenta guardar, entonces no se persiste. Dados siete tipos alertables y OTHER, cuando se calcula programación, entonces OTHER no produce alerta automática.
- Dados registros vigentes para los siete tipos alertables y OTHER, cuando se alcanza el límite de uno de los siete, entonces se evalúa su estado de forma independiente de los demás; OTHER no genera alerta automática.
- Dado formulario modificado, cuando se solicita salir, entonces se permite seguir editando o descartar; ante error de guardado se conserva el borrador y se ofrece recuperación. El frame específico de ese error sigue pendiente.

**UI-07 Ajustes**

- Dada una instalación sin preferencias de unidades o métricas, cuando se cargan correctamente los defaults previstos, entonces se muestran km, °C y los tres interruptores activos. Dada una lectura de preferencias en curso o fallida, cuando se abre Ajustes, entonces debe existir estado de carga o error recuperable; representación y coordinación son pendientes de diseño/implementación.
- Dado borrador con nombre vacío y unidades cambiadas, cuando se intenta salir y se elige Seguir editando, entonces se conserva; al elegir Descartar se conservan los ajustes anteriores.
- Dado valor almacenado 100 km sin editar, cuando se cambia solo la unidad o el nombre y se guarda, entonces el valor almacenado sigue siendo 100 km. Dada entrada editada de 10,0 millas, cuando se guarda, entonces se convierte y redondea al km entero más cercano: 16 km.
- Dado fallo parcial al guardar, cuando se ofrece recuperación, entonces se conserva el borrador y se comprueba el estado real antes de reintentar; no se afirma que todo se guardó o que nada se guardó.

**UI-01 y transversal**

- Dado que no hay vehículos, cuando se abre Inicio, entonces se presenta invitación a agregar uno. Dado Toyota con P0300 recurrente y P0420 nuevo en última sesión, cuando se abre Inicio, entonces se ven ambos y solo P0300 se etiqueta recurrente.
- Dado vehículo sin mantenimiento o sin diagnósticos, cuando se consulta Inicio, entonces se muestran los vacíos correspondientes; un diagnóstico limpio guardado se identifica de forma distinta a la ausencia de diagnóstico.
- Dada primera carga de Inicio sin datos del contexto, cuando falla, entonces se presenta error con reintento; si falla una actualización con datos del vehículo actual, se conservan y advierte su posible desactualización. Al cambiar de Toyota a Suzuki no se muestran datos de Toyota como actuales.
- Dado un límite de mantenimiento alcanzado al día siguiente, cuando Inicio vuelve a activarse, entonces se recalcula usando la fecha actual. Dada lectura o guardado en curso, cuando gira el dispositivo con proceso vivo, entonces la operación no se duplica y se conserva el contexto.
- Dada la barra principal, cuando se selecciona cada destino Inicio, Vehículos, Diagnóstico o Mantenimiento, entonces se muestra el destino elegido; al volver de los flujos secundarios se respeta su retorno acordado, incluido Diagnóstico para Conexión.
- Dado resultado diagnóstico pendiente, cuando el proceso finaliza forzosamente, entonces no se promete recuperación de ese resultado en el MVP. El requisito de prototipo navegable y las pruebas de recreación siguen pendientes.

---

La tabla siguiente es histórica: sus referencias de frames y bloqueos visuales se están reconciliando en el paso 2. Para variantes vigentes y aprobación, consultar [referencias actuales](figma-references.md#saneamiento-de-variantes--2026-11-10).

| Paquete / requisito | DD y contrato | Referencia visual y evidencia | Prueba prevista | Bloqueo para implementación |
|---|---|---|---|---|
| UI-01 Inicio | DD-27/29/32; GetDashboardDataUseCase | Design 3:8, 3:21, 8:5, 8:24; FigJam 2:19 | vacío, selección, métricas null/0, recreación | Integración de códigos y preferencias; validación visual en plan de diseño |
| UI-02 Vehículos | DD-24/27/28/30; VehicleRepository, DeleteVehicleUseCase | Design 3:38, 3:55; FigJam 2:74, 2:121 | alta y fallo parcial, selección, cascada | Mostrar año 0 como no informado, guardias transitorias y aprobación visual |
| UI-03 Conexión | DD-20/26/29/34/35/48; ObdGateway, ObdConnectionConfig y AppPreferencesRepository | Design 9:2; FigJam F03 2:220 (bifurcación aprobada) | IP inválida no abre socket; conservar borrador; corregir/revalidar, cancelar al origen; fallo TCP/init reintenta conexión; handshake persiste config | Implementar validación y coordinación en Presentation, permiso runtime y guardado tras handshake; pruebas API 37 y hardware |
| UI-04 Diagnóstico | DD-25/32; Scan/SaveDiagnosticSessionUseCase | Design 3:148, 3:165, 3:182, 3:199, 3:218, 3:233, 3:248; FigJam 2:323 | captura, fallo, reintento sin relectura, rotación | Coordinación de pendiente y aprobación visual |
| UI-05 Historial | DD-13; DiagnosticRepository y AnalyzeDiagnosticHistoryUseCase | Design 3:265; FigJam 2:426 | aislamiento, limpio, recurrencia y retorno | Falta identificar frame separado de lista y aprobar variantes |
| UI-06 Mantenimiento | DD-16/30; MaintenanceRepository/DAO | Design 3:284, 3:303, 3:324, 3:343; FigJam 2:505 | mismo ID, validaciones, vigente, rollback | Backend de edición implementado; formulario, variantes de tipos nuevos y aprobación visual pendientes |
| UI-07 Ajustes | DD-22/23/29/31; AppPreferencesRepository | Design 3:362; FigJam 25:554 | defaults, conversión, error, compatibilidad | Persistencia, precisión/redondeo y aprobación visual |
| Transversal formularios | DD-30/31/32 | Design 3:398, 3:415; FigJam 25:555/556 | borrador, cambios de configuración, evitar operación duplicada | Aprobación visual y guardias de flujo |

## Registro histórico — Actualización visual parcial

Inicio 3:21 incluye línea independiente de códigos; variantes 8:5 y 8:24 representan diagnóstico limpio y ausencia de diagnóstico. Notas técnicas obsoletas de mantenimiento y ajustes quedaron ocultas. Capturas revisadas sin recortes visibles; falta validar interacción, accesibilidad y aprobación visual. El retorno está decidido: desde Inicio vuelve a Inicio y desde Historial vuelve a la lista; falta implementarlo y validarlo. Ver [figma-references.md](figma-references.md).

## Registro histórico — Criterios iniciales Dado/Cuando/Entonces

- Dado un primer vehículo guardado y fallo al guardar selección, cuando se reintenta, entonces se reutiliza el ID sin insertar otro vehículo (DD-27).
- Dado un resultado diagnóstico pendiente, cuando falla guardar, entonces se conserva la captura original y reintentar no hace otra lectura (DD-25).
- Dado un resultado limpio, cuando se guarda explícitamente, entonces queda una sesión sin códigos; un fallo de lectura no crea historial.
- Dado un mantenimiento editado, cuando se guarda, entonces se actualiza el mismo ID y se recalcula el vigente por fecha/ID, conservando límites absolutos (DD-16/30).
- Dada una lectura, conexión o guardado en curso, cuando se recrea Activity con el proceso vivo, entonces continúa una sola operación y se conserva el estado (DD-32).
- Dado que no existe preferencia previa, cuando se abren ajustes, entonces se usan km/°C y las tres métricas visibles; cambiar unidades no reescribe históricos (DD-22/29/31).
- Dada una captura con valor no disponible, cuando se presenta, entonces no se representa como cero; las lecturas se inician explícitamente.

- Dado un diagnóstico pendiente, cuando se intenta salir y se elige Seguir aquí, entonces se conserva la captura y permanece el usuario en diagnóstico; si elige descartar, se elimina el pendiente sin guardar y vuelve a Inicio.
- Dado Connected, cuando se intenta cambiar vehículo, entonces se bloquea la selección y se indica desconectar primero (DD-28). Si existe captura pendiente, resolver primero guardar/descartar/seguir aquí; solo después desconectar antes de cambiar vehículo.
- Dado acceso LAN sin permiso en API 37, cuando se intenta conectar, entonces no se inicia el socket sin autorización; ante denegación o revocación debe ser posible informar y recuperar, pero textos, reintento y acceso a ajustes requieren decisión UX. No inferir concesión desde un resultado anterior.

## Registro histórico — Revisión posterior a aprobación de wireframes

El usuario aprobó los wireframes revisados UI-01–UI-07 el 2026-11-09; evidencia y alcance en [figma-references.md](figma-references.md). F03 incluye la bifurcación aprobada para formato inválido y fallo de conexión. Permanecen como trabajo de implementación/validación: interacción navegable, accesibilidad en Compose, adaptación y pruebas funcionales/hardware. El siguiente paso de esta etapa es reconciliar cada fila DEP con frames, contratos y criterios, y completar paquetes DESIGN_READY donde la trazabilidad esté completa; los bloqueos técnicos quedan expresos y no invalidan la aprobación del wireframe.
