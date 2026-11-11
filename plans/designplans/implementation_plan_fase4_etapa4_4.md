# Etapa 4.4 — Revisión de recorridos y preparación DESIGN_READY

> **CERRADA — QA APROBADO para alcance documental y R03.** Las tareas de implementación y verificaciones se transfirieron a 4.5–4.8 según [reporte](../reporte_fase4_etapa4_4.md). Los wireframes base son DESIGN_READY; las pantallas aún no están implementadas.

**Meta:** completar navegación, estados y contratos de presentación de UI-01–07, aprobar el sistema visual Material 3 y liberar cada paquete DESIGN_READY con evidencia.
**Arquitectura:** mantener Domain/Data y AppSession de 4.3; especificar adaptadores por pantalla y reutilizar Navigation Compose. No introducir tablas, dependencias ni una segunda coordinación.
**Tecnología:** Kotlin/Compose/Material 3 existentes, Room/DataStore y Navigation Compose 2.10.2. Figma Design como referencia visual y FigJam como mapa de recorridos.

## 1. Alcance y evidencia

Esta etapa amplía la revisión original de tema/componentes con la auditoría funcional solicitada. La implementación de pantallas sigue en 4.5–4.8. La aprobación de wireframes UI-01–07 permanece; solo cambios y variantes nuevos se someten a revisión.

Lecturas: Contexto_Maestro_App_OBDII_v2.md, agent-designer-instruction.md, implementation_plan_fase4.md, screen-flows.md, ui-ux-specification.md, design-decisions.md, design-system.md, figma-references.md, design-handoff.md y screen-dependencies.md, todos bajo plans o plans/designplans.
Código inspeccionado bajo app/src/main/java/cl/cristian/bitacoraobd/: ui/navigation/AppNavigation.kt, ui/AppSession.kt, ui/HomeViewModel.kt, domain/repository/AppPreferencesRepository.kt y domain/usecase/SaveMaintenanceUseCase.kt. El cierre 4.3 en plans/reporte_fase4_etapa4_3.md es evidencia histórica de pruebas, no una ejecución de esta auditoría.

Figma consultado de solo lectura mediante herramientas studio_: Design yIZcqwztYdI5SZNs5VPwPt, subárboles 10:2, 11:2, 36:3 y 29:3. Sesión c48c6b09-c046-4113-b3fa-625f33d4d9fd. La evidencia nueva es estructural (nombres de capas, visibilidad, jerarquía y dimensiones); no se inspeccionaron capturas ni reacciones mediante Plugin API en esta sesión. Los nombres de capa pueden diferir del texto renderizado: cualquier discrepancia detectada por nombre se trata como candidata hasta comprobar texto/captura. Los problemas visuales históricos se citan como históricos, no como nuevas mediciones.

Al planificar no se ejecutaron pruebas ni se modificaron Figma/código. Posteriormente se verificó R03; por decisión del usuario los wireframes base UI-01–07 son DESIGN_READY como referencia de diseño, sin acreditar implementación ni validación de variantes nuevas.

## 2. Línea base vigente y contradicciones documentales

- 4.1/4.2/4.3 cerradas. Room v2 y MIGRATION_1_2 registrados en builder productivo; preferencias y coordinación compartida disponibles.
- AppNavigation tiene rutas home, vehicles, diagnostic, maintenance, settings, history/{vehicleId}, diagnostic-detail/{sessionId}, connection-flow y connection. Sus contenidos siguen siendo Text: no hay barras, formularios ni botones de producto conectados.
- AppSession ofrece captura pendiente, decisiones Save/Discard/Stay, lecturas explícitas, selección/borrado y salida autorizada. Que exista la API no demuestra cumplimiento de cada interacción del diseño.
- HomeViewModel reinicia colección al activarse y tiene retry. Falta integración de la representación DD-39 y protección verificable frente a datos de otro vehículo durante transición/error.
- screen-flows y ui-ux-specification describen el grafo vigente; design-system distingue la línea base histórica. Las pantallas siguen pendientes de implementación.
- El inventario histórico de Figma ya registra variantes de carga/error: reconciliar sus IDs antes de crear duplicados.
- Historial, ajustes, vehículos y mantenimiento tienen soporte Domain/Data. Persistencia post-handshake, formularios, permisos y navegación contextual aún requieren integración por pantalla.

## 3. Lista priorizada de arreglos

P0 bloquea contrato de interacción; P1 bloquea paquete visual; P2 mejora claridad sin ampliar MVP.

| ID | Prioridad / evidencia | Problema y efecto | Arreglo propuesto y cierre |
|---|---|---|---|
| R01 | P0, AppNavigation y DD-13 | Hay rutas, pero los placeholders no ofrecen navegación; wireframes usan texto para barra/menús | Especificar una NavigationBar M3 con cuatro destinos, acceso Ajustes en top bar de principales y acciones secundarias. Tabla origen/acción/guarda/destino/Atrás para cada control; sin nueva pestaña. |
| R02 | P0, rutas history/detail | ID válido sintácticamente no demuestra existencia ni pertenencia al contexto | Especificar carga, registro inexistente/eliminado y volver seguro. Detalle desde Inicio vuelve Inicio; desde Historial vuelve lista del mismo vehículo; Ajustes vuelve origen. Conservar origen/back stack, no redirigir siempre a Inicio. |
| R03 | P0, RESUELTO en 4.3; regresiones verificadas en 4.4 | Ruta inválida se rechaza antes de autorizar; excepción ordinaria al navegar libera autorización con error recuperable y permite reintentar. `CancellationException` se repropaga. | `AppSession.requestExit` valida la ruta; `AppNavigation` llama `completeExit` solo al navegar y `failExit` al fallar. Pruebas verifican `history/0` + reintento válido, recuperación mediante `failExit` y flujo MainActivity. Sin cambio Domain. |
| R04 | P0, UI-02/DD-27/28 y 10:3/10:39 | Menú es anotación; falta representar primer alta parcial y sus destinos | Menú por fila con Editar/Eliminar; confirmación de cascada; guardar primer vehículo → selección → Inicio, fallo selección → reintentar con mismo ID; alta adicional → lista, mantiene activo; último borrado → Inicio vacío. Representar Connected/cierre y captura pendiente. |
| R05 | P0, DD-30/31/35/36 | Conflicto potencial entre borrador local, captura pendiente y salida global | Resolver primero cambios locales (Seguir/Descartar; Guardar solo por acción del formulario), después captura global Save/Discard/Stay y después salida. Si cualquiera cancela/falla, no navegar. Definir prioridad en tabla y no mostrar dos diálogos simultáneos; conservar intención original. |
| R06 | P0, F03 y AppSession.connect | Connect no persiste configuración; boolean de permiso no es adaptador Android | Diseñar aviso Wi-Fi → permiso real → formulario → handshake → persistencia. Error TCP reintenta conexión; fallo DataStore mantiene Connected y reintenta solo guardado. Cancelar intento permanece; salida final vuelve Diagnóstico. Permiso revocado/retorno desde ajustes siempre se revalida. Integrar en 4.6. |
| R07 | P0, UI-04/AppSession.savedDiagnosticId | Después de guardar se limpia pendingDiagnostic y queda ID; no hay pantalla de resultado guardado | Cargar sesión por savedDiagnosticId o conservar representación derivada del resultado; mostrar payload/fecha original sin reescaneo. Pendiente limpio/con DTC, guardando, error guardado, guardado y desconocido son estados distintos. Discard explícito vuelve Inicio según DD-25; salida genérica conserva intención según decisión de usuario. |
| R08 | P0, principales sin vehículo | Diagnóstico/Mantenimiento/Historial no tienen contrato visible completo sin activo | Aplicar D1 aprobada: alta desde estos vacíos termina en Inicio tras guardar y seleccionar. Deshabilitar conexión/lectura/registro sin vehículo; Ajustes y lista Vehículos siguen accesibles; Historial no se construye sin ID. |
| R09 | P1, UI-05 11:249/13:41 y 28:77 | Lista/detalle no explicitan sesión eliminada o contexto invalidado | Completar lista vacía/carga/error/actualización, detalle limpio/DTC/desconocido, retorno y aislamiento; consultar repositorio antes de mostrar resultado. Sin botones borrar historial/exportar. |
| R10 | P1, UI-06 11:286 y SaveMaintenanceUseCase | Aceite es ejemplo; faltan selector ocho tipos, campos y errores accionables completos | Selector OIL_AND_FILTER/OIL_ONLY/FILTER_ONLY/BRAKES/TIRES/BATTERY/COOLANT/OTHER; fecha/km, notas y próximos límites siempre visibles; confirmación km histórico; error conserva borrador; editar mismo ID; sin programación para OTHER como alerta. |
| R11 | P1, UI-07 11:344/29:3/29:20 y AppPreferencesRepository | Hay saveSettings; faltan controles finales y estado de guardado | Borrador común, Guardar explícito, defaults solo tras lectura exitosa, carga/error/retry y descarte. Distinguir fallo lectura de defaults. Guardar ajustes es una edit DataStore; no inventar fallo parcial entre sus campos. Parcial Room/DataStore corresponde a otras operaciones. |
| R12 | P1, UI-01 11:4/11:42 | Bloques y targets no están inequívocamente conectados; capa sin diagnóstico se nombra códigos | Revisar texto renderizado de 11:42 antes de corregir. Jerarquía vehículo→mantenimiento→diagnóstico→métricas, CTA por vacío, detalle último guardado, Historial y conexión vía Diagnóstico. Ocultar métricas si todas desactivadas; null distinto de cero. |
| R13 | P1, design-system.md y referencias históricas | Dirección A y modo sistema están elegidos; faltan tokens finales, roles M3, muestras de pantallas y contraste de componentes/estados | En 4.5 preparar muestras representativas de A (grafito + estados semánticos), ambos modos del sistema, y validar el mapeo visual; consultar al usuario ante desviaciones materiales. No reabrir B ni color dinámico. Verificar contraste real, tamaño táctil, texto ampliado, scroll/IME e insets. |
| R14 | P1, figma-references / F03 | Histórico sin reacciones verificadas y rama persistencia por texto | Conectar prototipo o aportar recorrido comprobable paso a paso de todas las ramas. Corregir etiqueta TCP/conectores en F03 al ejecutar diseño autorizado. No llamar prototipo a cuadros estáticos. |
| R15 | P2, documentos históricos | Aprobado wireframe se confunde con funcional/implementado | Una fila por paquete con referencia vigente, estados, decisiones, backend, validación y aprobación. Evitar borrar historia o exigir reaprobación del wireframe entero. |

## 4. Preguntas nuevas que sí necesitan decisión

No se reabren DD-13/17/20/25–48 ni las aprobaciones de wireframes.

- **D1 — APROBADA:** el alta del primer vehículo desde un vacío secundario termina en Inicio tras guardarlo y seleccionarlo.
- **D2 — APROBADA:** bloquear la edición del vehículo activo mientras esté conectado y explicar que debe desconectarse. Amplía DD-28; especificar coordinación con conexión y revalidación al guardar y probar las carreras antes de integrar UI-02.
- **D3 — APROBADA:** el bloque del último diagnóstico guardado en Inicio abre el detalle de esa sesión; Ver historial es independiente.
- **D4 — dirección elegida:** apariencia A (grafito y estados semánticos), paleta fija y variantes clara y oscura según el sistema. Muestras, tokens y validación de contraste se transfieren a 4.5; consultar al usuario ante desviaciones materiales.

Las demás correcciones son aplicación de reglas aprobadas o detalles técnicos. Un control no documentado no autoriza nueva función: no añadir búsqueda, filtros, Deshacer, borrar DTC, cuentas ni edición de sesiones.

## 5. Contraste backend → pantalla

| Pantalla | Reutilizar | Trabajo restante para implementación |
|---|---|---|
| UI-01 | GetDashboardDataUseCase, HomeViewModel, métricas AppSession, settings | Estado por vehicleId, composición visual, conversiones y targets; prueba cambio de contexto + fallo, sin datos anteriores ajenos. |
| UI-02 | VehicleRepository.saveVehicle/getVehicleById, AppSession.selectVehicle/deleteVehicle | Formulario, ID retornado retenido para selección parcial y destinos. No insertar de nuevo al reintentar DataStore. |
| UI-03 | ObdConnectionConfig, gateway y AppSession | Adaptador LAN, borrador/config persistida y retry exclusivo post-handshake; textos por estado, sin autolectura. |
| UI-04 | scan/save idempotente y pendingDecision | Controles de decisión, resultado guardado por ID, traducción de error; sin exponer mensajes internos crudos. |
| UI-05 | observeSessionsByVehicle/getSessionById y AnalyzeDiagnosticHistoryUseCase | VM/UiState lista/detalle y retorno contextual. |
| UI-06 | MaintenanceRepository y SaveMaintenanceUseCase | Conversión/validación de entrada, tres resultados (Saved/Failed/RequiresHistoricalMileageConfirmation), excepciones IO recuperables; fecha calendario y mismo ID. |
| UI-07 | settings/saveSettings DataStore | VM/borrador, unidades y representación; campo no editado conserva km interno. |

No se identificó necesidad de cambiar tablas o contratos Domain para preparar estos diseños. Guardias AppSession son la entrada común para acciones sensibles; llamadas directas no heredan su garantía. Los casos de borde técnicos se prueban al ejecutar sus arreglos; la ausencia de hardware no impide aprobar un contrato visual, y tampoco acredita validación runtime LAN.

## 6. Archivos y entregas por fase

### Fase A — Coder / diseñador tras aprobar este plan

- [x] A1. Actualizar estado vigente en screen-flows.md y ui-ux-specification.md: grafo técnico 4.3 integrado, pantallas aún placeholders; conservar antecedentes.
- [x] A2. Conservar D1–D3 aprobadas y D4 con dirección elegida en design-decisions.md; no reabrirlas. Escribir tabla por control con precondición, intención retenida, éxito, fallo, cancelación, destino y Atrás para R01–R12, solicitando decisión solo si surge una regla funcional nueva.
- [x] A3. Actualizar screen-dependencies.md DEP-001–023 y design-handoff.md: backend existente, integración restante y referencia exacta por variante.
- [x] A4. Cuatro capturas revisadas; variantes y recorridos no críticos transferidos a 4.5–4.8 con referencias existentes, sin duplicar grupos 27/28/29/36/38.
- [x] A5. Transferir a 4.5 la definición final de tokens/roles Material 3 y muestras clara/oscura según apariencia A y paleta fija; medir contraste de componentes y estados reales. No reabrir A/B ni modo del sistema; consultar solo si surge una desviación funcional o visual relevante respecto de lo acordado.
- [x] A6. R03 corregido, probado y aprobado por QA; excepción real de NavController no inyectada.
- [x] A7. Transferir a 4.5 la adaptación del tema Color.kt/Theme.kt/Type.kt y su validación, sin implementar pantallas posteriores en 4.4.

### Fase B — Tester / verificador

- [ ] B1. Recorrer Inicio vacío→alta parcial→retry→Inicio; adicional→lista; seleccionar→Inicio; borrar activo/último/fallo con guardias.
- [ ] B2. Recorrer F03 válido/inválido, LAN denegado/ajustes/revocación, TCP/handshake, guardar config fallido, Cancelar vs salir y cierre; cada salida final Diagnóstico.
- [ ] B3. Recorrer captura limpia/DTC, Save exitoso/fallido, doble acción, Stay/Discard, lectura cancelada, rotación y retorno; comprobar que éxito guardado mantiene resultado visible.
- [ ] B4. Historial desde Inicio y Diagnóstico, detalle y Atrás; ID inexistente/contexto cambiado; Ajustes desde cada principal y regreso.
- [ ] B5. Formularios: cambios/sin cambios/volver a originales, teclado, cancelar/descartar/error, abrir Ajustes con borrador; mantenimiento ocho tipos, km histórico y edición.
- [ ] B6. Validar contraste medido, objetivos táctiles ≥48dp en propuesta Android, texto ampliado, ventana compacta/expandida, scroll/IME/insets y controles accesibles. Figma no prueba TalkBack; programar verificación Compose durante implementación.
- [ ] B7. Para cambios técnicos/tema autorizados: studio_ ejecuta tests dirigidos de navegación/sesión, suite JVM y lint/assemble según alcance. Si cambia navegación real, ejecutar AndroidTest de recreación/F03. Registrar comando y resultado; no ejecutar tests por editar Markdown.

### Fase C — QA/Ponytail

- [ ] C1. Revisar connected contracts y R01–R15; sin funciones especulativas, dependencias nuevas ni bloqueos generales inventados.
- [ ] C2. Comprobar referencias y ramas; una acción crítica sin destino, recuperación o cancelación bloquea su implementación, sin retirar DESIGN_READY al wireframe base.
- [ ] C3. Registrar decisiones y trabajo pendiente por paquete. Las variantes y tokens se validan durante implementación; consultar al usuario solo ante decisiones materiales no resueltas.
- [ ] C4. QA aprobado → actualizar task.md y generar plans/reporte_fase4_etapa4_4.md con paquetes liberados y bloqueados. No iniciar 4.5 automáticamente.

## 7. Puerta DESIGN_READY verificable por paquete

Los wireframes base UI-01–07 son DESIGN_READY por decisión del usuario. Para implementar cada control se necesita conocer sus datos, acción, destino, guardias y recuperación; si falta soporte, asignar la integración a su etapa o diferir lo no crítico sin simular funcionamiento. Los tokens, variantes posteriores, accesibilidad y pruebas runtime se validan en implementación; DESIGN_READY del wireframe no los acredita.

Aceptación global de 4.4: R01–R15 resueltos o transferidos explícitamente a una etapa responsable; D1–D4 con dirección y reglas acordadas; matriz UI-01–07 y límites trazables; QA, checklist e informe coherentes. El tema y su validación se completan en 4.5; DESIGN_READY del wireframe no significa DONE.

## 8. Exclusiones y riesgos

Sin hardware ELM327, Demo, polling, nube, escritura ECU, odómetro ECU, nuevas tablas, recuperación de captura tras muerte del proceso ni implementación de pantallas posteriores. La revisión estructural actual no demuestra interacción, contraste ni contenido exacto renderizado; se exige esa evidencia en ejecución antes de aprobar diseño. El estado actual de Figma no debe inferirse del nombre APROBADO de un grupo.

**Siguiente paso tras cierre QA:** detenerse al finalizar 4.4; la planificación específica de 4.5 define el tema, UI-02, sus pruebas y las dependencias transferidas.
