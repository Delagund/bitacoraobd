# Matriz de dependencias de pantallas y recorridos

## Etapa 4.6 — estado vigente

UI-03 y UI-04 están integradas con AppSession, Navigation Compose y las operaciones explícitas OBD; QA/Ponytail aprobó la implementación verificable. Ver [reporte de cierre 4.6](../reporte_fase4_etapa4_6.md). Las filas inferiores de UI-03/04 son una foto previa a la implementación y no representan pendientes actuales de código. Continúan pendientes la validación runtime del permiso API 37, hardware ELM327 y comprobaciones visuales/manuales indicadas en el reporte.

## Etapa 4.5 — estado vigente

[Plan UI-02 y tema](implementation_plan_fase4_etapa4_5.md) fue aprobado por el usuario y ejecutado. La implementación UI-02 y el tema fijo están completos; QA/Ponytail emitió **APROBADO**. Ver [reporte de cierre 4.5](../reporte_fase4_etapa4_5.md). Los wireframes base permanecen DESIGN_READY como referencia, sin acreditar pantallas de otras etapas ni variantes de diseño.

Límites: faltan verificaciones manuales de TalkBack, foco, fuente ampliada, contraste renderizado, IME/insets y barras. ELM327 se omitió sin elm327Host y no se comprobó permiso LAN runtime. La limitación Figma de 4.4 es histórica; no se editaron variantes en 4.5. Las fichas DEP y la tabla de soporte al cierre de 4.3 que siguen debajo conservan su contexto histórico; para estado actual de UI-02 prevalece esta nota y el reporte. El resto de la matriz mantiene sus responsables 4.6–4.8.


## Seguimiento UI-01–07 para etapas 4.5 en adelante

Por indicación del usuario, los **wireframes aprobados UI-01–07 son DESIGN_READY como referencia de diseño**. Esto no acredita que sus controles, variantes posteriores, accesibilidad, backend o pruebas estén terminados. Al preparar cada paquete se contrastará cada acción con el código, el soporte existente, la integración pendiente, los bloqueos reales y la etapa responsable. La aprobación base vigente no se solicita de nuevo; se presentan para revisión solo las variantes o decisiones materiales aún no aprobadas. Los detalles no críticos se resuelven dentro del alcance aprobado y pueden delegarse. La ausencia de backend no autoriza simular éxito ni eliminar guardias, validación o recuperación; los controles omitidos no deben aparentar funcionar.

| Pantalla | Wireframe base | Soporte real al cierre 4.3 | Integración de producto pendiente | Etapa prevista | Diferimientos no críticos dentro del MVP |
|---|---|---|---|---|---|
| UI-01 Inicio | DESIGN_READY (diseño base aprobado) | GetDashboardDataUseCase, HomeViewModel, activación que reinicia colección, estado AppSession para vehículo y métricas OBD; códigos/recurrencia y mantenimiento provienen del dominio. | Componer estados por vehículo, carga/error DD-39, vacíos independientes, métricas según visibilidad, navegación contextual, última sesión y protección frente a datos de otro contexto. El DashboardSummary no incluye métricas; no presentar disponibilidad como si ya estuviera conectada. | 4.8 | Sin temporizador ni sondeo; las métricas solo se leen por acción explícita en UI-03. No incluir admisión, eliminada del MVP. |
| UI-02 Vehículos | DESIGN_READY (diseño base aprobado) | VehicleRepository y alta/lectura/borrado; AppSession coordina selección/borrado, Connected y captura pendiente; migración 1→2 no altera la entidad. | Lista/formularios/menú/confirmación, alta parcial (guardar vehículo y seleccionar activo con retry del mismo ID), selección y fallo parcial de borrado con refresco del estado real. Implementar D2: bloquear edición del activo conectado, explicar y revalidar al guardar; revalidar carreras/transitorios. | 4.5 | Búsqueda/filtros, deshacer y sincronización no son MVP. Mantener acción explícita y confirmación de borrado en cascada. |
| UI-03 Conexión | DESIGN_READY (diseño base aprobado) | Elm327ObdGateway y AppSession conectan/desconectan con estado y cancelación; F03 existe como ruta anidada bajo Diagnóstico; preferencias contienen config OBD. | Aviso Wi-Fi y permiso local runtime por plataforma; formulario IPv4/puerto; conectar solo por acción; guardar config tras handshake y separar retry TCP de retry de persistencia; progreso, errores y destino de salida. Verificar permiso real al volver de Ajustes. | 4.6 | Sin Demo, reconexión automática, lectura al conectar ni sondeo. Hardware ELM327 no se declara probado por faltar host/dispositivo. |
| UI-04 Diagnóstico | DESIGN_READY (diseño base aprobado) | ScanDiagnosticCodesUseCase exige Connected; SaveDiagnosticSessionUseCase guarda la captura; AppSession mantiene captura pendiente y coordina Save/Discard/Stay, cancelación y lecturas. | Pantalla de lectura/resultado/error; guardar y reintentar con la misma captura sin reescaneo; mostrar resultado guardado por ID; guardias de pendiente antes de salida, reconexión/desconexión y cambio de vehículo. Captura solo vive mientras viva el proceso. | 4.6 | Sin recuperación de captura tras muerte del proceso, borrado de DTC ECU ni diagnósticos en segundo plano. |
| UI-05 Historial | DESIGN_READY (diseño base aprobado) | DiagnosticRepository consulta lista por vehículo y detalle por ID; AnalyzeDiagnosticHistoryUseCase calcula recurrencia; rutas History/detail están declaradas. | ViewModel/UiState, carga/error/actualización DD-40, vacío, asociación y validación del ID, detalle inexistente y retorno al origen (Inicio o lista del mismo vehículo). Aislar datos al cambiar contexto. | 4.7 | Sin exportación, borrado de sesiones, búsqueda ni filtros; lista de solo consulta. |
| UI-06 Mantenimiento | DESIGN_READY (diseño base aprobado) | MaintenanceRepository y SaveMaintenanceUseCase; alta/edición por ID, siete tipos alertables + OTHER, confirmación de km histórico y alertas derivadas. Captura 11:286 muestra “Aceite” como ejemplo únicamente. | ViewModel/formularios/lista, selector de los ocho tipos, validación y errores recuperables, borrador, estados DD-40, edición del mismo ID, conversión de millas en Presentation y confirmación histórica sin bajar odómetro. | 4.7 | OTHER no genera alertas; no añadir frecuencias ni tareas automáticas. Sin crear un frame por tipo; el selector completo sí es requisito funcional. |
| UI-07 Ajustes | DESIGN_READY (diseño base aprobado) | AppSettings/DataStore 4.2 soporta nombre opcional, unidades y visibilidad; estado compartido de preferencias. | Borrador común y Guardar, carga/error/reintento, descarte, conservar valor persistido real ante error; convertir millas solo al editar y no reescribir históricos. Tema y tokens se definen y validan en 4.5; no son una preferencia configurable en Ajustes. | 4.8 | Sin tema configurable dentro de Ajustes: apariencia fija, clara/oscura según sistema conforme decisión visual; sin restaurar preferencias no guardadas tras muerte de proceso. |

Este inventario limita DESIGN_READY a wireframes base aprobados por el usuario; no afirma aprobación de variantes posteriores, tokens, accesibilidad ni interacción. “Soporte real” significa contrato o integración de 4.3 existente, no pantalla funcional. No elimina guardias: Connected bloquea selección/borrado y edición del activo cuando corresponda; captura pendiente se resuelve antes de acciones incompatibles; la actualización activo/DataStore y borrado Room puede fallar parcialmente y no es transacción atómica; fallos deben refrescar el estado real.

## Reconciliación por DEP — etapa 4.4, 2026-11-10

Los estados visuales aprobados se refieren a los wireframes UI-01–07 del 2026-11-09. La evidencia visual nueva aquí se limita a 11:42, 11:4, 11:286 y 13:75, confirmadas mediante capturas; estructura previa, texto visible y comportamiento no se intercambian como evidencia. El código se inspeccionó estáticamente; en esta actualización no se ejecutó Gradle ni pruebas.

| DEP | Soporte técnico observado | Falta para el paquete / evidencia visual relacionada |
|---|---|---|
| 001–003 | VehicleRepository, AppSession.selectVehicle/deleteVehicle y guardia de conexión disponibles | Lista/formulario/menú/confirmación y recorridos de alta parcial; Connected/fallo parcial aún sin UI verificada |
| 004–005 | GetDashboardDataUseCase + HomeViewModel; refresco de colección al activar | UI-01 sigue placeholder; 11:42 confirma texto de vacío “Aún no hay diagnósticos”, no confirma el contenido/CTA completo ni diseño listo |
| 006–009 | Gateway real y eventos explícitos de AppSession; F03 está anidado bajo Diagnóstico en el grafo | Permiso runtime no está integrado; aviso, formulario, estados y persistencia post-handshake faltan; 13:75 visualmente incluye acceso a Ajustes |
| 010–012 | AppSession conserva captura pendiente, acciones Save/Discard/Stay y lecturas | UI-04 y diálogo/resultado no existen; navegación/guardar fallido no se validó mediante interacción |
| 013–014 | Repositorios pueden observar/listar sesión y consultar detalle por ID; las rutas history/detail están declaradas | No hay ViewModel ni carga/estado de detalle, validación de ID/contexto o retorno visible; prueba pendiente |
| 015–017 | MaintenanceRepository, SaveMaintenanceUseCase y edición del mismo ID existentes | UI-06 y selector de ocho opciones no implementados; 11:286 usa “Aceite” de ejemplo, no demuestra selector completo |
| 018 | AppSettings/DataStore y valores por defecto implementados en 4.2 | UI-07/borrador/guardar/error no existen; no añadir token/tema por esta dependencia |
| 019 | AppSession puede retener decisión y salida autorizada; autorización de navegación se libera en rechazo/excepción | Formularios locales y prioridad visible entre borrador/captura/salida faltan; sin dos diálogos simultáneos |
| 020 | AppSession es de proceso; ViewModels se recrean con Activity. El reporte 4.3 registra pruebas de recreación real de Activity, captura pendiente, autorización de navegación/F03 y acciones únicas: 23 AndroidTest, 0 fallos, 1 omitida por falta de `elm327Host`. | Quedan pruebas por pantalla para integración de producto UI-01–07 (formularios, navegación contextual y operaciones propias); no falta la evidencia básica de recreación de 4.3. No se promete recuperación de captura tras muerte del proceso |
| 021 | Navigation Compose declara cuatro principales, secundarios y conexión anidada | Los destinos tienen Text solamente; barra compacta visible en 11:4 es evidencia visual, no control funcional ni aprobación de variante |
| 022 | HomeViewModel y AppSession exponen estado básico de carga/error | Integrar conservación de datos del mismo contexto y error de listas en cada paquete; sin validación |
| 023 | Inicio reinicia colección al activarse, sin temporizador. `HomeViewModelTest.reactivationRecalculatesMaintenanceAgainstCurrentDateWithoutTimer` pasa con reloj controlado: avanzar la fecha sin reactivar no cambia el estado; reactivar cambia Próximo a Vencido. `MainActivity.onResume/onPause` conecta el ciclo de vida con `setActive`. | Falta probar el retorno real a Inicio en integración de UI-01 y sus estados visuales. La prueba JVM valida el ViewModel, no una navegación de pantalla implementada |

Estado de decisiones actualizado contra design-decisions.md: D1, D2 y D3 están APROBADAS; D4 tiene dirección elegida (apariencia A, paleta fija y modo claro/oscuro del sistema). `design-system.md` contiene pares cromáticos candidatos y ratios provisionales, no tokens Material 3 aprobados ni muestras de pantallas de producto. La aprobación DESIGN_READY aplica a los wireframes base UI-01–07, no a variantes posteriores ni a la integración funcional. Prototipo navegable, accesibilidad, contraste, tamaños táctiles y operación de controles requieren evidencia independiente. La captura 11:42 renderiza correctamente «Aún no hay diagnósticos»; 11:4 muestra barra inferior de texto compacto; 11:286 usa «Aceite» como ejemplo; 13:75 muestra acceso a Ajustes para el permiso. Son observaciones visuales de capturas, no validación de navegación, permisos runtime o variantes nuevas.


## Estado de integración técnica 4.3 — 2026-11-10

Se implementaron en el código de trabajo composición manual por Application/AppContainer, builder Room con migración explícita 1→2, propietario AppSession y ViewModels de sesión/Inicio. La coordinación y reinicio de colección al activar Inicio están implementados y probados según el reporte 4.3. Navigation Compose 2.10.2 tiene autorización explícita del usuario y está declarada; el NavHost técnico cubre cuatro destinos, destinos secundarios y F03 anidado bajo Diagnóstico, pero sus destinos siguen siendo Text placeholders. El reporte 4.3 registra resolución Gradle de Navigation Compose 2.10.2 con Lifecycle 2.11.0/Compose BOM 2026.02.01, compilación y pruebas de navegación/recreación aprobadas (23 AndroidTest, 0 fallos, 1 omitida por falta de `elm327Host`). No se implementaron pantallas de producto: el diseño base UI-01–07 conserva DESIGN_READY; las integraciones de producto siguen pendientes para 4.5–4.8. La captura pendiente es solo del proceso vivo.

**Paso 3, 2026-11-10:** [contraste de soporte real y coordinación propuesta por UI/DEP](domain-contracts.md#paso-3--contraste-de-contratos-y-consumidores-2026-11-10). Los textos repetidos de fichas históricas que solicitan decidir nuevamente acuerdos ya confirmados no implican una nueva consulta; revisar solo diferencias efectivamente abiertas. Los estados visuales de las fichas se refieren a wireframes aprobados, no a las variantes posteriores ni a DESIGN_READY.

**Actualización F03, 2026-11-10:** DEP-006–009 y DEP-021 deben aplicar la decisión vigente: Diagnóstico inicia Conexión y métricas y recibe sus salidas finales; desde Diagnóstico se puede volver a Inicio. Ver [decisión](design-decisions.md#actualización-de-navegación-f03--2026-11-10); el retorno de Historial mantiene su regla independiente.

Revisión inicial: 2026-11-08. Evidencia inspeccionada en [reporte](design-review-report.md), [contratos](domain-contracts.md), [decisiones](design-decisions.md), [flujos](screen-flows.md), [especificación](ui-ux-specification.md) y [referencias Figma](figma-references.md). Es una matriz de trazabilidad; la [evaluación posterior de hallazgos](design-review-report.md#evaluación-de-hallazgos-posterior-a-la-matriz--2026-11-08) no constituye aprobación nueva. Los criterios recogen acuerdos existentes o expresan un mínimo sujeto a las decisiones abiertas; no cierran esas decisiones.

Los nodos son referencias inspeccionadas: cuando se indica un grupo, no se afirma que exista una variante específica. FigJam y Design son borradores; ausencia de evidencia se indica como NO_VERIFICADO. Todas las pruebas citadas se leyeron, no se ejecutaron en esta revisión; consultar el reporte para rutas y alcance. El estado técnico PARCIAL significa soporte de dominio o datos existente, no interfaz terminada. Estado visual común: APROBADA (wireframe) por el usuario el 2026-11-09; evidencia y alcance en figma-references.md. No incluye prototipo, accesibilidad instrumentada ni integración. Responsable de decisión: usuario; preparación y verificación: agente; implementación: equipo Android, pendiente de plan técnico aprobado. El usuario estableció como prioridad inicial de fase 4 el backend necesario para la UI, incluido DEP-011; ver [seguimiento principal](../task.md#entrada-prioritaria-para-fase-4--backend-requerido-por-ui).

| ID | Pantalla | Capacidad o variante | Funcional | Técnico | Prerrequisitos |
|---|---|---|---|---|---|
| [DEP-001](#dep-001) | UI-02 | Alta primer vehículo y selección inicial | APROBADA | PARCIAL | — |
| [DEP-002](#dep-002) | UI-02 | Selección y cambio de vehículo | APROBADA | PARCIAL | DEP-001, DEP-008 |
| [DEP-003](#dep-003) | UI-02 | Eliminar y recuperar fallo parcial | APROBADA | PARCIAL | DEP-001, DEP-008 |
| [DEP-004](#dep-004) | UI-01 | Inicio sin vehículos y selección obsoleta | APROBADA | PARCIAL | DEP-001 |
| [DEP-005](#dep-005) | UI-01 | Resumen de códigos, recurrencia y alertas | APROBADA | PARCIAL | DEP-001, DEP-011, DEP-015 |
| [DEP-006](#dep-006) | UI-03 | Permiso LAN y conexión | APROBADA | PARCIAL | DEP-001 |
| [DEP-007](#dep-007) | UI-03 | IP/puerto, handshake y fallo al persistir | APROBADA | PARCIAL | DEP-006 |
| [DEP-008](#dep-008) | UI-03/04 | Operaciones y salida durante lectura | APROBADA | PARCIAL | DEP-006 |
| [DEP-009](#dep-009) | UI-03/01 | Lectura explícita y métricas compartidas | APROBADA | PARCIAL | DEP-006, DEP-018 |
| [DEP-010](#dep-010) | UI-04 | Escaneo limpio, códigos y fallo | APROBADA | PARCIAL | DEP-006 |
| [DEP-011](#dep-011) | UI-04 | Guardar, fallar y reintentar captura | APROBADA | PARCIAL | DEP-010 |
| [DEP-012](#dep-012) | UI-04 | Descartar, reescanear o desconectar | APROBADA | PARCIAL | DEP-011 |
| [DEP-013](#dep-013) | UI-05 | Historial: lista y vacío por vehículo | APROBADA | PARCIAL | DEP-001, DEP-011 |
| [DEP-014](#dep-014) | UI-05 | Detalle y retorno al origen | APROBADA | PARCIAL | DEP-013 |
| [DEP-015](#dep-015) | UI-06 | Lista, alertas y tipos de mantenimiento | APROBADA | PARCIAL | DEP-001 |
| [DEP-016](#dep-016) | UI-06 | Alta, límites y confirmación histórica | APROBADA | PARCIAL | DEP-015, DEP-019 |
| [DEP-017](#dep-017) | UI-06 | Edición del mismo registro | APROBADA | PARCIAL | DEP-016 |
| [DEP-018](#dep-018) | UI-07 | Nombre, unidades y visibilidad | APROBADA | PARCIAL | DEP-019 |
| [DEP-019](#dep-019) | UI-02/06/07 | Borradores, salida y contexto de edición | APROBADA | PARCIAL | — |
| [DEP-020](#dep-020) | UI-01–07 | Recreación y continuidad de operaciones | APROBADA | PARCIAL | DEP-008, DEP-011, DEP-019 |
| [DEP-021](#dep-021) | UI-01–07 | Navegación principal, accesos y retornos | APROBADA | PARCIAL | DEP-001, DEP-019 |
| [DEP-022](#dep-022) | UI-02/05/06 | Carga y error de listas | APROBADA | PENDIENTE | DEP-001, DEP-013, DEP-015 |
| [DEP-023](#dep-023) | UI-01 | Recalcular vencimientos al activar Inicio | APROBADA | PARCIAL | DEP-005 |

**Registro histórico previo a la aprobación del 2026-11-09:** la primera revisión editable de Vehículos [Design 10:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-2) y la revisión adicional [Design 11:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-2) se describieron como DRAFT en aquel momento. La aprobación posterior de wireframes UI-01–UI-07 se conserva; las variantes añadidas después requieren revisión individual. [Alcance y evidencia vigente](figma-references.md).

## Actualización de trazabilidad UI-02–04 — 2026-11-10

Las fichas originales de DEP-001–023 se conservan debajo como historia; sus referencias anteriores a ausencia de aprobación visual, acuerdos abiertos y frames originales están superadas cuando contradicen decisiones posteriores o los wireframes aprobados. Consultar las actualizaciones por UI/DEP, [decisiones](design-decisions.md) y [evaluación vigente](design-handoff.md#evaluación-de-preparación-por-paquete--revisión-de-cierre). La aprobación de 2026-11-09 no cubre automáticamente las variantes añadidas el 2026-11-10 ni acredita interacción o implementación.

| DEP | Referencia actual y pendiente |
|---|---|
| 001 | [Alta 10:39](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-39), [fallo de selección 27:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-20). Reintento del ID guardado, sin duplicar; coordinación Presentation pendiente. |
| 002 | [Lista 10:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-3), [conectado 27:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-3). Guardias de activo y captura pendiente requieren Presentation. |
| 003 | [Confirmación de cascada 11:397](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-397). Ante Failed refrescar estado; preferencias y Room no forman transacción común. |
| 006 | [Aviso 11:415](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-415), [permiso denegado 11:432](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-432), [ajustes 13:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-75). Verificar concesión real y comportamiento runtime por plataforma. |
| 007 | [Configuración 11:62](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-62), [persistencia fallida 11:449](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-449), [F03 bifurcación](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220) y [rama de persistencia](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=49-724). IP/puerto inválidos no abren socket; corregir revalida, cancelar vuelve a Diagnóstico sin guardar; fallo TCP reintenta conexión y fallo tras handshake solo guardado. [Error de formato 36:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-3) pendiente de revisión final. |
| 008 | [Connecting 11:81](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-81), [lectura interrumpida 11:483](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-483), [escaneando 11:202](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-202). [Disconnecting 36:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-22) pendiente de revisión final; cancelación real y continuidad requieren Presentation. |
| 009 | [Lectura 11:94](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-94). Muestra RPM/refrigerante/velocidad; admisión está oculta como histórica. [No disponible y cero 38:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=38-2) pendiente de revisión final. |
| 010 | [Resultado 11:150](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-150), [limpio 11:167](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-167), [fallo 11:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-217). Código desconocido muestra información genérica; fallo no genera historial. |
| 011 | [Error de guardado 11:232](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-232), [guardado 11:184](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-184), [limpio guardado 13:58](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-58). Reintentar preserva captura y no reescanea; coordinación pendiente. |
| 012 | [Salida con pendiente 11:363](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-363). Resolver guardar/descartar/seguir antes de salida o desconexión. 23:61 está archivado como obsoleto. |
| 019/022 (UI-02) | [Formulario 10:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-20); carga [28:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-3), error [28:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-20), actualización [28:37](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-37), fallo con datos [28:56](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-56). Guardias, borrador y recuperación aún sin implementación. |

### Continuación UI-05–07, UI-01 y transversal

| DEP | Referencia comprobada y pendiente |
|---|---|
| 004 | [Sin vehículos 36:36](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-36) sustituye el original histórico 3:8 y aplica tras borrar el último vehículo. Fallback actual no persiste selección; coordinación pendiente. |
| 005 | [Resumen 11:4](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-4), [limpio 11:23](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-23), [sin diagnóstico 11:42](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-42), [sin mantenimiento 13:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-3). Estado de carga [23:79](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=23-79) y [error 23:137](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=23-137) se refieren a conexión; carga inicial [36:49](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-49) y error inicial [36:66](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-66) pendientes de revisión final. |
| 013 | [Lista 11:249](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-249) y [vacío 28:152](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-152); lista por vehículo, sesión limpia cuenta cero. |
| 014 | [Detalle 13:41](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-41) muestra código y volver al origen; interacción por sesión y retorno no probados. |
| 015 | [Lista 11:267](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-267), [vacío 13:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-22). Aceite es ejemplo; implementación final requiere selector con siete tipos alertables y OTHER existentes. |
| 016 | [Alta 11:286](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-286), [confirmación histórica 11:326](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-326). Validar límites y conservar odómetro ante histórico; error de formulario sin frame específico identificado. |
| 017 | [Edición 11:307](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-307); mismo ID y recalcular programación, sin alterar odómetro por edición. |
| 018 | [Ajustes 11:344](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-344), [descarte 29:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-3), [fallo 29:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-20). Contrato de nombre/unidades/visibilidad pendiente; [carga inicial 36:84](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-84) y [error 36:103](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-103) pendientes de revisión final. |
| 019 (UI-06/07) | [Descarte genérico 11:380](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-380) y [Ajustes 29:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-3); conservar borrador y estado previo según DD-30/36. |
| 020 | [FigJam 25:556](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-556) documenta continuidad; no demuestra recreación ejecutada. |
| 021 | Barra visible en [Inicio 11:4](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-4); no hay navegación funcional comprobada. F03 inicia/termina en Diagnóstico; Historial retorna a su origen. |
| 022 (UI-05) | [carga 28:78](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-78), [error 28:95](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-95), [actualización 28:112](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-112), [error con datos 28:131](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-131). |
| 022 (UI-06) | [carga 28:170](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-170), [error 28:189](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-189), [actualización 28:208](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-208), [error con datos 28:229](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-229). |
| 023 | Recálculo al activar Inicio es decisión DD-41; caso de uso usa fecha actual al invocarlo pero aún no existe disparador Presentation. |

Para datos, criterios, contratos y pruebas, consultar [fichas UI-02–04](design-handoff.md#paso-2--fichas-ui-02-ui-03-y-ui-04-en-revisión) y fichas UI-05–07/Inicio siguientes en el mismo documento. Todas requieren revisión independiente, y las variantes añadidas después del 2026-11-09 no heredan aprobación individual automáticamente.

## Fichas

### DEP-001

- **UI-02 — Alta primer vehículo y selección inicial.** Requisito/acuerdo: RF-01; DD-24/27/42. Referencia: [Design 3:7](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-7).
- **Soporte y prueba inspeccionados:** VehicleRepository; AppPreferencesRepository; MaintenanceDashboardDeleteUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation y prueba: alta y recuperación de selección parcial pendientes. Prerrequisitos: ninguno.
- **Aceptación:** Dado el primer vehículo guardado, cuando falla seleccionar el activo, entonces se informa y se puede reintentar la selección sin duplicar el vehículo.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-002

- **UI-02 — Selección y cambio de vehículo.** Requisito/acuerdo: RF-01; DD-17/28/42. Referencia: [Design 3:7](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-7).
- **Soporte y prueba inspeccionados:** AppPreferencesRepository; DeleteVehicleUseCase; MaintenanceDashboardDeleteUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation: coordinación de conexión y resultado pendiente. Prerrequisitos: [DEP-001](#dep-001), [DEP-008](#dep-008).
- **Aceptación:** Dado un vehículo no activo, cuando se muestra su fila, entonces «Usar este vehículo» se asocia a ella y editar/eliminar están en su menú. Dada una captura diagnóstica pendiente, cuando se solicita cambiar de vehículo, entonces se resuelve primero mediante guardar, descartar o seguir aquí.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-003

- **UI-02 — Eliminar y recuperar fallo parcial.** Requisito/acuerdo: RF-01; DD-17/28/42. Referencia: [Design 3:7](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-7).
- **Soporte y prueba inspeccionados:** DeleteVehicleUseCase; MaintenanceDashboardDeleteUseCasesTest; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Domain/Data y Presentation: Connected bloquea; cambios DataStore y Room pueden fallar parcialmente. Prerrequisitos: [DEP-001](#dep-001), [DEP-008](#dep-008).
- **Aceptación:** Dado un fallo al eliminar o seleccionar, cuando termina la operación, entonces se informa y se refresca el estado real antes de reintentar.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-004

- **UI-01 — Inicio sin vehículos y selección obsoleta.** Requisito/acuerdo: RF-05; DD-27/28/39. Referencia: [Design 3:7](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-7).
- **Soporte y prueba inspeccionados:** GetDashboardDataUseCase; MaintenanceDashboardDeleteUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation: vacío; Domain: fallback no persiste selección. Prerrequisitos: [DEP-001](#dep-001).
- **Aceptación:** Dado que no hay vehículos, cuando se abre Inicio, entonces se presenta el estado vacío; sin datos previos del vehículo actual, carga y fallo usan estados completos con reintento.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-005

- **UI-01 — Resumen de códigos, recurrencia y alertas.** Requisito/acuerdo: RF-05; DD-09/18/38/39/45/46. Referencia: [Design 3:21](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-21), [Design 8:5](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=8-5), [Design 8:24](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=8-24).
- **Soporte y prueba inspeccionados:** GetDashboardDataUseCase; AnalyzeDiagnosticHistoryUseCase; MaintenanceDashboardDeleteUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/prueba: etiqueta recurrente y recálculo al activar Inicio DD-41 pendientes de implementación. Prerrequisitos: [DEP-001](#dep-001), [DEP-011](#dep-011), [DEP-015](#dep-015).
- **Aceptación:** Dada una última sesión con códigos recurrentes y no recurrentes, cuando se abre Inicio, entonces se muestran todos y se etiqueta solo a los identificados como recurrentes, separados de alertas de mantenimiento. Si falla una actualización posterior, se conservan los datos previos del mismo vehículo con aviso y reintento. Los bloques siguen DD-45 y el resumen de mantenimiento abre Mantenimiento del vehículo actual; sin mantenimiento o diagnósticos guardados se muestran vacíos y accesos según DD-46.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-006

- **UI-03 — Permiso LAN y conexión.** Requisito/acuerdo: RF-02; DD-20/26/34. Referencia: [Design 3:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-75), [Design 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2).
- **Soporte y prueba inspeccionados:** AndroidManifest.xml; Elm327ObdGateway; Elm327SocketClient; Elm327SocketClientTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Plataforma/Presentation y pruebas: solicitud y recuperación del permiso sin implementar; detectar disponibilidad de solicitud según plataforma. Prerrequisitos: [DEP-001](#dep-001).
- **Aceptación:** Dada una denegación, cuando se intenta conectar, entonces se explica la necesidad del permiso y se ofrece reintentar si procede o acceso a ajustes si no; al volver se comprueba la concesión real.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-007

- **UI-03 — IP/puerto, handshake y fallo al persistir.** Requisito/acuerdo: RF-02; DD-20/26/35; flujos actualización posterior. Referencia: [Design 3:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-75), [Design 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2).
- **Soporte y prueba inspeccionados:** AppPreferencesRepository; DataStoreAppPreferencesRepositoryTest; Elm327ObdGateway; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/Data y pruebas: conectar no guarda configuración; confirmar descarte y coordinar persistencia tras handshake aún pendientes. Prerrequisitos: [DEP-006](#dep-006).
- **Aceptación:** Dados IP o puerto modificados sin conectar, cuando se intenta salir, entonces se ofrece seguir editando o descartar sin alterar configuración persistida. Dado un handshake exitoso, cuando se confirma la conexión, entonces se guarda la configuración y un fallo informa el estado real.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-008

- **UI-03/04 — Operaciones y salida durante lectura.** Requisito/acuerdo: RF-02/03; DD-25/26/32/33. Referencia: [Design 3:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-75), [Design 3:147](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-147), [Design 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2).
- **Soporte y prueba inspeccionados:** Elm327ObdGateway; Elm327ObdGatewayTest; Elm327SocketClientTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/OBD y pruebas: cancelación voluntaria, descarte de captura incompleta y comunicación del estado real pendientes de implementación. Prerrequisitos: [DEP-006](#dep-006).
- **Aceptación:** Dada una lectura en curso, cuando se pulsa Atrás o se cambia de pantalla, entonces se cancela la lectura, se descarta la captura incompleta y se comunica el estado real de conexión; al recrear por configuración se conserva la operación (DD-32).
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-009

- **UI-03/01 — Lectura explícita y métricas compartidas.** Requisito/acuerdo: RF-02; DD-29/48. Referencia: [Design 3:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-75), [Design 3:21](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-21).
- **Soporte y prueba inspeccionados:** ObdGateway; Elm327ObdGateway; Elm327ObdGatewayTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation: captura compartida y null frente a cero; preferencias de visibilidad pendientes. Prerrequisitos: [DEP-006](#dep-006), [DEP-018](#dep-018).
- **Aceptación:** Dada una lectura solicitada, cuando se muestran métricas, entonces se identifica vehículo y fecha; los parámetros visibles sin valor muestran «No disponible». Desconectado conserva la última lectura completa con estado textual real.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-010

- **UI-04 — Escaneo limpio, códigos y fallo.** Requisito/acuerdo: RF-03; DD-18/25/47. Referencia: [Design 3:147](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-147), [Design 3:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-217).
- **Soporte y prueba inspeccionados:** ScanDiagnosticCodesUseCase; DiagnosticAndMaintenanceUseCasesTest; Elm327ObdGatewayTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/prueba: estados de lectura y código desconocido; sin UI funcional. Prerrequisitos: [DEP-006](#dep-006).
- **Aceptación:** Dado un escaneo solicitado con conexión, cuando el resultado es limpio, con códigos o falla, entonces se distingue el estado sin guardar un fallo; se muestran código real y descripción disponible o genérica.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-011

- **UI-04 — Guardar, fallar y reintentar captura.** Requisito/acuerdo: RF-03; DD-25/28. Referencia: [Design 3:147](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-147), [Design 3:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-217).
- **Soporte y prueba inspeccionados:** SaveDiagnosticSessionUseCase; DiagnosticAndMaintenanceUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Backend resuelta en 4.1: captureId estable, índice UNIQUE, resolución idempotente transaccional y migración v1→v2. Presentation debe conservar la captura original y reintentar sin reescaneo; no se promete recuperación tras muerte del proceso. Reintento, concurrencia, rollback y migración pasaron instrumentadas (ver [reporte 4.1](../reporte_fase4_etapa4_1.md)). El builder Room aún debe registrar la migración en 4.3. Prerrequisito: [DEP-010](#dep-010).
- **Aceptación:** Dada una captura original pendiente, cuando falla su guardado, entonces sigue disponible y reintentar usa la misma captura sin otra lectura OBD.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-012

- **UI-04 — Descartar, reescanear o desconectar.** Requisito/acuerdo: RF-03; DD-17/25/28. Referencia: [Design 3:147](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-147), [Design 3:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-217).
- **Soporte y prueba inspeccionados:** ScanDiagnosticCodesUseCase; SaveDiagnosticSessionUseCase; DiagnosticAndMaintenanceUseCasesTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation: guardias y navegación de resultado pendiente. Prerrequisitos: [DEP-011](#dep-011).
- **Aceptación:** Dado un resultado pendiente, cuando se solicita reescanear o desconectar, entonces se resuelve primero guardando, descartando con advertencia o permaneciendo allí.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-013

- **UI-05 — Historial: lista y vacío por vehículo.** Requisito/acuerdo: RF-03; DD-18/47. Referencia: [Design 3:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-217).
- **Soporte y prueba inspeccionados:** DiagnosticRepository; RoomDiagnosticRepository; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/visual: lista y vacío requieren variantes verificables. Prerrequisitos: [DEP-001](#dep-001), [DEP-011](#dep-011).
- **Aceptación:** Dado un vehículo activo, cuando se abre historial, entonces se muestran sus sesiones de más reciente a más antigua con fecha, kilometraje local y cantidad de códigos, o un estado vacío.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-014

- **UI-05 — Detalle y retorno al origen.** Requisito/acuerdo: RF-03; DD-18; flujos actualización posterior. Referencia: [Design 3:279](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-279).
- **Soporte y prueba inspeccionados:** DiagnosticRepository; RoomDiagnosticRepository; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/prueba: detalle separado y retorno desde Inicio o lista sin interacción validada. Prerrequisitos: [DEP-013](#dep-013).
- **Aceptación:** Dado un detalle abierto desde Inicio o la lista, cuando se vuelve, entonces se regresa al origen.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-015

- **UI-06 — Lista, alertas y tipos de mantenimiento.** Requisito/acuerdo: RF-04/05; DD-15/18/43/44. Referencia: [Design 3:283](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-283).
- **Soporte y prueba inspeccionados:** GetMaintenanceStatusUseCase; MaintenanceType; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/visual: opciones, vacío y alertas requieren revisión; OTHER sin alerta. Prerrequisitos: [DEP-001](#dep-001).
- **Aceptación:** Dado mantenimiento con programación, cuando se consulta su estado, entonces se calcula a partir de los datos y OTHER no genera alerta. Dado un registro se muestran tipo, fecha, kilometraje y próximos límites disponibles en orden más reciente primero; el estado vigente se identifica con texto y sin registros se ofrece registrar.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-016

- **UI-06 — Alta, límites y confirmación histórica.** Requisito/acuerdo: RF-04; DD-16/30/43/44. Referencia: [Design 3:283](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-283).
- **Soporte y prueba inspeccionados:** SaveMaintenanceUseCase; DiagnosticAndMaintenanceUseCasesTest; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation: validaciones, confirmación y recuperación de error. Prerrequisitos: [DEP-015](#dep-015), [DEP-019](#dep-019).
- **Aceptación:** Dada una intervención nueva, cuando se abre el formulario, entonces se muestran los dos límites opcionales; si sus fechas y kilometrajes cumplen las reglas, puede guardarse sin impedir límites ya vencidos.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-017

- **UI-06 — Edición del mismo registro.** Requisito/acuerdo: RF-04; DD-16/30/43/44. Referencia: [Design 3:324](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-324).
- **Soporte y prueba inspeccionados:** SaveMaintenanceUseCase; RoomMaintenanceRepository; AppDatabaseTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/visual: formulario y preservación de campos; backend actualiza mismo ID. Prerrequisitos: [DEP-016](#dep-016).
- **Aceptación:** Dado un mantenimiento existente, cuando se guarda una edición válida, entonces se actualiza el mismo ID y se recalcula el estado.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-018

- **UI-07 — Nombre, unidades y visibilidad.** Requisito/acuerdo: DD-22/23/29/31/36/37. Referencia: [Design 3:361](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-361).
- **Soporte y prueba inspeccionados:** AppPreferencesRepository; DataStoreAppPreferencesRepositoryTest; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Data/Presentation: contrato y persistencia no incluyen estos ajustes; guardado, recuperación ante error y conversión de entradas en millas pendientes de implementación y prueba. Prerrequisitos: [DEP-019](#dep-019).
- **Aceptación:** Dado un borrador de Ajustes modificado, cuando se sale sin guardar, entonces se ofrece seguir editando o descartar; al guardar se comunica el resultado real. Las unidades confirmadas convierten la representación sin modificar cantidades almacenadas; entradas en millas admiten un decimal y se redondean al km entero más cercano.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-019

- **UI-02/06/07 — Borradores, salida y contexto de edición.** Requisito/acuerdo: DD-30/31/42. Referencia: [Design 3:361](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-361).
- **Soporte y prueba inspeccionados:** MainActivity; AppPreferencesRepository; sin pruebas UI identificadas; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/prueba: formularios, descarte y coordinación con Ajustes pendientes. Prerrequisitos: ninguno.
- **Aceptación:** Dado un formulario con cambios pendientes, cuando se intenta salir, entonces se puede seguir editando o descartar antes de navegar.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-020

- **UI-01–07 — Recreación y continuidad de operaciones.** Requisito/acuerdo: DD-32. Referencia: [Design 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2), [FigJam 25:556](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-556).
- **Soporte y prueba inspeccionados:** MainActivity; Elm327ObdGateway; sin pruebas UI de recreación identificadas; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/prueba: estado, navegación y propietario de operaciones pendientes. Prerrequisitos: [DEP-008](#dep-008), [DEP-011](#dep-011), [DEP-019](#dep-019).
- **Aceptación:** Dado un cambio de configuración con operación en curso, cuando se recrea la interfaz, entonces conserva el contexto y observa la misma operación sin duplicarla.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-021

- **UI-01–07 — Navegación principal, accesos y retornos.** Requisito/acuerdo: DD-13/23/28/45. Referencia: [Design 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2), [FigJam 2:19](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-19).
- **Soporte y prueba inspeccionados:** MainActivity; sin navegación funcional ni pruebas UI identificadas; evidencia y rutas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08), 2026-11-08. Prueba UI no verificada salvo indicación expresa.
- **Brecha:** Presentation/visual/prueba: cuatro destinos y accesos secundarios por validar. Prerrequisitos: [DEP-001](#dep-001), [DEP-019](#dep-019).
- **Aceptación:** Dado un destino secundario, cuando se vuelve, entonces se conserva el origen según el recorrido definido.
- **Estados:** funcional APROBADA; visual APROBADA (wireframe); técnico PARCIAL. Aprobación funcional: únicamente acuerdos DD citados en [decisiones](design-decisions.md), sin ampliar su alcance; en ABIERTA subsisten decisiones indicadas en la brecha. Aprobación visual al registrar la ficha originalmente: ninguna; este estado histórico fue superado por la aprobación de wireframes del 2026-11-09, sin incluir automáticamente variantes posteriores.
- **Bloqueo y siguiente paso:** revisar la brecha y evidencias en sesión de diseño; usuario decide lo abierto y equipo Android planifica lo pendiente tras aprobación.

### DEP-022

- **UI-02/05/06 — Carga y error de listas.** Acuerdo: [DD-40](design-decisions.md#dd-40-carga-y-error-de-listas); referencias de grupos [vehículos 3:7](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-7), [historial 3:217](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-217), [mantenimiento 3:283](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-283); variantes completas NO_VERIFICADAS.
- **Soporte y prueba:** repositorios y pruebas inspeccionadas en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08); Presentation y prueba UI no identificadas. **Brecha:** Presentation/prueba, recuperación de flujos por verificar. Prerrequisitos: [DEP-001](#dep-001), [DEP-013](#dep-013), [DEP-015](#dep-015).
- **Aceptación:** Dada una lista con datos previos del contexto actual, cuando falla su actualización, entonces conserva datos con aviso y Reintentar; sin datos previos usa error completo y distingue vacío exitoso.
- **Estados:** funcional APROBADA, visual APROBADA (wireframe), técnico PENDIENTE. Confirmación usuario 2026-11-08; responsable de implementación: equipo Android; siguiente paso: diseñar variantes y planificar pruebas.

### DEP-023

- **UI-01 — Vencimientos al activar Inicio.** Acuerdo: [DD-41](design-decisions.md#dd-41-actualización-de-vencimientos-al-volver-a-inicio), RF-05; referencia [Inicio 3:21](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-21).
- **Soporte y prueba:** GetDashboardDataUseCase y GetMaintenanceStatusUseCase; [evidencia](design-review-report.md#auditoría-por-recorridos--2026-11-08), pruebas específicas de activación NO_VERIFICADAS. **Brecha:** Presentation/Domain/prueba: el cambio de día no emite solo; coordinar recálculo al activar sin prejuzgar capa. Prerrequisito: [DEP-005](#dep-005).
- **Aceptación:** Dado un vencimiento cuyo estado cambia por fecha, cuando Inicio se abre o vuelve a activarse, entonces se recalcula con la fecha vigente; no se exige emisión a medianoche mientras está visible.
- **Estados:** funcional APROBADA, visual APROBADA (wireframe), técnico PARCIAL. Confirmación usuario 2026-11-08; responsable de implementación: equipo Android; siguiente paso: planificar coordinación y prueba.

## Orden y alcance

Prerrequisitos sin dependencias: DEP-001 y DEP-019. Después: DEP-006/015, DEP-008/010/016/018 y sus dependientes; DEP-020 requiere DEP-008, DEP-011 y DEP-019. Las flechas indican prerrequisitos de preparación o integración, no que una pantalla deba terminarse visualmente antes que otra. DEP-022 depende de DEP-001/013/015 y DEP-023 de DEP-005; la relación dirigida se comprobó sin ciclos. La tarea existente es la sección «Matriz» de [designtask.md](designtask.md); decisiones e implementación corresponden a las secciones posteriores del mismo seguimiento, no a fases técnicas nuevas.

Cobertura: UI-01 Inicio, UI-02 vehículos, UI-03 conexión, UI-04 diagnóstico, UI-05 historial, UI-06 mantenimiento y UI-07 ajustes; formularios, navegación y recreación son transversales. El odómetro ECU de DD-21 es futuro y queda fuera de esta entrega. Las variantes visuales concretas, interacciones de Figma, accesibilidad y comportamiento de hardware permanecen por verificar. El análisis de hallazgos de diseño empieza después de crear esta matriz.
