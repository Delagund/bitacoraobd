# Preparación vigente — Automotive Dark

2026-11-12: Etapa 1 AUTORIZADA expresamente: tokens y componentes compartidos Automotive Dark con previews; no sustituir pantallas, cambiar contratos ni implementar B-02 en esta etapa. Ejecución secuencial Coder → Tester → QA. Mantener tema/componentes legacy mientras nuevos consumidores se incorporan en etapas futuras. Plan técnico: [ui-migration-plan.md](designplans/ui-migration-plan.md). Auditoría: [current-presentation-audit.md](designplans/current-presentation-audit.md). Matriz de 31 variantes: [figma-compose-contract-map.md](designplans/figma-compose-contract-map.md). Requiere aprobación antes de código; preservar Domain/Data y resolver decisiones documentadas por etapa.

---

# Pre-Fase 4 — Preparación de diseño e integración

**Plan vigente de etapa 4.6:** [Conexión y diagnóstico UI-03/04](designplans/implementation_plan_fase4_etapa4_6.md), PROPUESTO y pendiente de aprobación explícita antes de código. 4.5 está cerrada según reporte_fase4_etapa4_5.md. Auditoría estática y planificación documental, sin Gradle ni código. El historial siguiente se conserva.

**Referencia histórica de Pre-Fase 4:** [Plan técnico de fase 4](designplans/implementation_plan_fase4.md), aprobado por el usuario. La indicación anterior de ejecución no iniciada correspondía a aquel cierre; actualmente 4.1–4.4 están cerradas y 4.5 requiere su propia aprobación.

**Estado vigente: CERRADA para el alcance ajustado.** QA global/Ponytail APROBADO con contraste selectivo; evidencia, límites y pendientes de fases posteriores en [reporte de cierre](reporte_prefase4.md). Diseño y plan técnico de fase 4 continúan por separado. Las secciones y casillas originales de este plan conservan su historia; el seguimiento efectivo está en [task.md](task.md).

## Saneamiento documental y visual — 2026-11-10

**Autorizado en chat:** retirar la variante de selección parcial de DTC, actualizar F03 y completar las variantes y referencias solicitadas. Es trabajo de diseño y documentación; no autoriza código Android.

**Regla F03:** para el MVP el flujo de Conexión y métricas se inicia en Diagnóstico y sus salidas finales vuelven a Diagnóstico; desde allí se puede navegar a Inicio. Preservar validación de IP/puerto, reintento TCP, persistencia tras handshake y captura íntegra. Conciliar entradas y textos anteriores que indiquen acceso directo desde Inicio sin extender el cambio al historial u otros flujos.

**Fase A — editor:** reutilizar wireframes existentes para representar lista de vehículos conectado y recuperación de fallo al persistir activo (sin insertar duplicados); estados de carga/error/actualización y vacío de historial conforme DD-39/40; salida y error de Ajustes conforme DD-36. Retirar 23:61 del conjunto vigente, separar visualmente históricos y renombrar capas obsoletas sin borrar originales. Actualizar F03 en FigJam, registrar IDs, decisiones y referencias en design-decisions.md, screen-flows.md, ui-ux-specification.md, screen-dependencies.md, design-handoff.md y figma-references.md según corresponda.

**Fase B — verificador:** contrastar cada variante con texto real, visibilidad, límites y conectores; comprobar que las salidas de F03 y sus fuentes locales concuerdan y que no hay acción de guardar selección parcial vigente. Revisar enlaces y registrar los casos no cubiertos; no ejecutar pruebas Android por revisión documental.

**Fase C — QA/Ponytail:** revisar alcance, trazabilidad, ausencia de duplicaciones, preservación de decisiones y evidencia visual. Corregir y volver a verificar cuando corresponda. Actualizar seguimiento y reporte solo con resultados comprobados; no declarar DESIGN_READY por la sola existencia de wireframes.

**Siguiente fase:** continuar el paso 2 del [plan de cierre](designplans/handoff_closure_plan.md) con trazabilidad por recorrido; el plan técnico de Android requiere aprobación explícita antes de codificar.

---

> **Para ejecución:** aplicar writing-plans, dispatching-parallel-agents y Ponytail; roles secuenciales Coder → Tester → QA conforme a AGENTS.md.

**Estado histórico al iniciar:** plan aprobado; ejecución documental iniciada por instrucción del usuario. La aprobación no autoriza por sí sola cambios de código ni de Figma que requieran aprobación específica. Para el cierre posterior consultar el estado vigente de la cabecera.

**Objetivo actualizado por el usuario:** conciliar los acuerdos y detalles técnicos con el trabajo de diseño. La finalización del diseño y su aprobación visual se gestionan en [el plan propio de diseño](designplans/implementation_plan.md); preparar o completar el plan de fase 4 queda fuera de esta Pre-Fase. El handoff técnico conserva requisitos, contratos, referencias y pruebas previstas.

**Arquitectura:** conservar Domain/Data existentes, Room y DataStore; Presentation y permisos se implementan en fase 4 salvo un plan técnico adicional aprobado. Reutilizar los documentos de `plans/designplans` y evitar nuevas dependencias.

**Tecnología:** Kotlin, Room, DataStore, coroutines, Compose/Material 3 y Figma existentes.

## Ampliación propuesta — saneamiento de Domain/Data antes de fase 4

**Estado histórico:** ampliación aprobada por el usuario e iniciada; saneamiento Domain/Data posteriormente completado y aprobado según [reporte](reporte_saneamiento_prefase4.md). Modifica el alcance anterior exclusivamente para los paquetes siguientes; Compose, navegación y permiso runtime siguen en fase 4.

- **Códigos DTC:** `GetDashboardDataUseCase.kt`, `DashboardSummary.kt`, `VehicleAlert.kt` y pruebas de Dashboard: exponer todos los códigos del último diagnóstico guardado en un campo con nombre coherente; destacar recurrencia sin ocultar otros ni asignar gravedad o urgencia. Evitar duplicar la lista como alertas DTC; conservar alertas de mantenimiento. Probar código nuevo, recurrente, desconocido, diagnóstico limpio y ausencia de diagnóstico. En fase 4, una línea de Inicio abre el detalle guardado o historial del vehículo, sin ejecutar escaneo; la captura pendiente sigue en Diagnóstico y no pertenece al historial.
- **Nombres de códigos:** el catálogo local ya ofrece descripción genérica para códigos desconocidos; las sesiones guardan únicamente los códigos. En el MVP mostrar siempre el código real y la descripción disponible, sin editor ni almacenamiento de nombres personalizados. Posponer «códigos personalizados» hasta existir necesidad concreta; definir entonces alcance por código/vehículo/sesión, precedencia y persistencia antes de implementarlo. No atribuir significado técnico inventado ni modificar el código capturado.
- **Vehículo:** `Vehicle.kt`, entidad/mappers y pruebas: el contrato actual no admite null; usar 0 como centinela de año no informado por decisión del usuario, sin cambiar esquema. Presentation lo muestra como campo vacío/No informado, nunca como año real; al guardar vacío persiste 0. Probar ida y vuelta, ausencia y conservación de años existentes; no convertir datos anteriores automáticamente.
- **Mantenimiento:** `MaintenanceType.kt`, `MaintenanceDao.kt`, `MaintenanceRepository.kt`, `RoomMaintenanceRepository.kt`, `SaveMaintenanceUseCase.kt` y pruebas: agregar frenos, neumáticos, batería y refrigerante con la misma programación opcional por fecha o kilometraje de tipos alertables; conservar OTHER y su campo notes existente. Son alertas derivadas, no notificaciones programadas. Implementar edición del mismo ID y validaciones DD-30, preservar transacciones y odómetro; definir respuesta ante ID inexistente y probar inserción, edición, histórico y recálculo. Verificar persistencia por nombre de enum y compatibilidad.
- **Selección y borrado (flujo aceptado por el usuario):** `DeleteVehicleUseCase.kt` y pruebas: diseñar y probar fallo de DataStore y fallo de Room después de actualizar selección, sin repetir ciegamente una operación; exponer resultado suficiente para que fase 4 informe fallo, refresque estado real y permita reiniciar. En fase 4, impedir cambio de vehículo o desconexión mientras exista diagnóstico pendiente; ofrecer guardar o descartar explícitamente con advertencia de pérdida, o seguir aquí. Tras resolverlo, aplicar bloqueo de conexión y desconectar antes de cambiar. Un fallo de selección/borrado no exige reescanear ni descartar captura.
- **Trazabilidad y regresión:** `AppDatabaseTest.kt`: comprobar directamente que no quedan filas de diagnostic_codes tras cascada; corregir documentación histórica sin reescribir resultados de ejecución. Probar SQL de recurrencia y mantenimiento, DataStore, migración si corresponde y errores parciales. Tester ejecuta `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew :app:lintDebug :app:assembleDebug` secuencialmente; registrar dispositivo y bloqueos reales. QA Ponytail revisa alcance y evidencia.
- **Contrato OBD:** mantener `ObdResult` existente, decisión confirmada por usuario. Ajustes, unidades, permiso LAN runtime y sus flujos siguen en fase 4; no basta la declaración del manifiesto.

**Ejecución:** concretar contrato técnico de edición y recuperación parcial, conservar evidencia y completar Tester y QA. No modificar Figma sin autorización; la revisión visual sigue pendiente.

## Evidencia inicial y alcance

- Fase 3 y QA declarados completos en `plans/reporte_fase3.md`; pruebas históricas, no ejecución actual.
- `AppDatabase.kt`: versión 1 y exportSchema=true; `app/build.gradle.kts`: directorio de esquemas configurado, minSdk 26 y target/compileSdk 37. Verificar que el esquema v1 exista y esté versionado; LocalDate está disponible desde API 26.
- El manifiesto declara INTERNET y ACCESS_LOCAL_NETWORK; solicitud runtime y pruebas API 37 corresponden a fase 4.
- `designplans/figma-references.md` registra F01–F10 sincronizados y sin aprobación visual; `task.md` contiene estado V1 anterior contradictorio. Verificar el tablero actual antes de modificarlo.
- `design-decisions.md`: DD-16 edición de mantenimiento, DD-22/23 preferencias y unidades, DD-27 selección activa y DD-29 métricas compartidas están aprobadas funcionalmente, pero requieren implementación; no asumir que son capacidades existentes.
- Esta etapa no implementa Compose, permisos, cambios de Domain, migraciones especulativas ni soporte para odómetro OBD futuro. El adaptador físico se valida en fase 5 cuando esté disponible.

## Ajuste de alcance vigente

Por instrucción posterior del usuario, los puntos de las secciones históricas que exigen completar o aprobar diseño visual, declarar DESIGN_READY o preparar/aprobar un plan de fase 4 no son criterios de cierre de esta Pre-Fase. Esos trabajos siguen en el plan propio de diseño o una fase posterior. Aquí se concilian decisiones y detalles técnicos, se mantiene el handoff, se verifica la integración solicitada y se completa la revisión documental y QA. El borrador de fase 4 es una referencia no aprobada, no un entregable a completar aquí.

## 0. Arranque tras aprobación

- [ ] Actualizar `plans/task.md` con Pre-Fase 4, fases A/B/C y evidencia, preservando los cierres históricos; corregir la casilla obsoleta de fase 3 con referencia al reporte.
- [ ] Revisar `plans/designplans/implementation_plan.md`, `designtask.md`, `domain-contracts.md`, `screen-flows.md`, `ui-ux-specification.md`, `design-system.md`, `design-decisions.md` y `figma-references.md`; registrar discrepancias sin reemplazar aprobaciones.
- [ ] Consultar Figma MCP en modo lectura: verificar tablero, referencias Design y estado real; si faltan acceso o archivos, registrar bloqueo y no inventar diseños.
- [ ] Coder documental → Tester documental → QA Ponytail en secuencia; registrar evidencia y decisiones en los documentos existentes.

## 1. Decisiones y contratos de interfaz

- [ ] Resolver con el usuario los seis casos abiertos de `screen-flows.md`, incluidos salida durante lectura, coordinación con diagnóstico pendiente, precisión/redondeo de millas y momento de persistencia de ajustes. Registrar alternativa, decisión, alcance y fecha en `design-decisions.md`.
- [ ] Contrastar contratos reales y consumidores para DD-12, DD-15, DD-17, DD-18 y DD-24; aclarar opcionalidad de campos, guardias durante estados transitorios y semántica de códigos recurrentes. No cambiar contratos sin autorización explícita.
- [ ] Definir comportamiento de permiso de red local: antes de conectar, concesión, rechazo, reintento y recuperación desde ajustes cuando corresponda; verificar documentación oficial vigente y condiciones reales por versión. Prever prueba en API 37 y regresión API 36.
- [ ] Registrar captura original, fallo sin persistencia, guardar/descartar/seguir, guardado fallido, cambio de vehículo y recreación de Activity según DD-25–32. Incluir criterios Dado/Cuando/Entonces y evitar repetir operaciones.

## 2. Flujos y referencia visual

- [ ] Usar el plan existente de `plans/designplans/implementation_plan.md` y su checklist para auditar UI-01–UI-07, F01–F10, navegación, accesibilidad, estados vacíos/carga/error/confirmación y adaptación.
- [ ] Tras autorización aplicable, actualizar FigJam o Design solo para diferencias verificadas; preservar contenido existente y registrar enlaces/revisiones en `figma-references.md`.
- [ ] Resolver discrepancias funcionales antes de representar pantallas; obtener aprobación visual explícita por pantalla/variante. Marcar DESIGN_READY únicamente si cumple `agent-designer-instruction.md`.
- [ ] Elaborar o actualizar matriz de dependencias y entrega de diseño previstas en el plan de diseño. Identificar paquetes listos y bloqueados; no exigir que todas las pantallas estén listas para comenzar una independiente.

## 3. Persistencia y brechas técnicas

- [ ] Inspeccionar `app/schemas`, control de versiones y creación real de Room; registrar política: conservar v1 exportada, incrementar versión al cambiar esquema y proporcionar migración explícita y prueba con datos reales representativos, sin borrado destructivo. Si falta esquema o configuración, proponer corrección concreta para aprobación antes de implementarla.
- [ ] Auditar DAOs, repositorios y pruebas de mantenimiento: definir modificación mínima para editar el mismo ID, mantener hijos/datos y recalcular alertas; validar fecha y kilometraje conforme DD-30. Registrar archivos, contratos y pruebas de regresión en plan técnico separado antes de codificar.
- [ ] Auditar `AppPreferencesRepository` y DataStore: identificar ampliaciones necesarias para DD-22/23/29, defaults y compatibilidad de preferencias existentes; documentar conversión y redondeo acordados antes de implementarlos.
- [ ] Auditar selección activa y estados de conexión, incluyendo fallo parcial de DataStore y restricciones de cambio/borrado. Identificar punto compartido de coordinación para fase 4 sin duplicar reglas ni escribir desde un Flow de consulta.
- [ ] Revisar configuración declarada y resultados de build recientes; documentar incompatibilidades demostradas. No actualizar dependencias por novedad. Verificar compatibilidad con fuentes oficiales antes de cualquier cambio propuesto.

## 4. Plan de integración y validación

- [ ] Crear handoff con mapa requisito → DD → contrato/código → pantalla/frame → prueba, indicando dependencias y paquete listo/bloqueado.
- [ ] Preparar plan técnico de fase 4 por paquetes: composición de dependencias, Presentation/ciclo de vida, vehículos, conexión/permiso, diagnóstico, mantenimiento y ajustes. Incluir rutas concretas tras auditoría, casos de error y pruebas significativas.
- [ ] Prever pruebas unitarias de coordinación y conversión, instrumentadas para persistencia/permisos y pruebas UI de recorridos críticos; incluir rotación sin duplicar operaciones, API 37 concedido/denegado y dispositivo API 36. Si falta dispositivo, registrar bloqueo de validación y no declarar prueba aprobada.
- [ ] Validar que el plan conserva lectura explícita, null distinto de cero, historial offline, captura original, no persistencia de errores y sin escritura ECU.

## 5. QA y cierre

- [ ] Tester contrasta decisiones, matriz, referencias y criterios contra código y contexto; ejecuta pruebas Android solo si hay cambios técnicos aprobados.
- [ ] QA Ponytail revisa trazabilidad, ausencia de sobreingeniería y bloqueos reales; no declarar aprobación de diseño sin evidencia del usuario.
- [ ] Marcar checklist, crear `plans/reporte_prefase4.md` con evidencia y pendientes, y presentar qué paquetes pueden iniciar fase 4.
- [ ] Solicitar aprobación del plan técnico de fase 4 antes de codificar. Si una decisión, acceso a Figma o dispositivo necesario falta, registrar el bloqueo específico sin inventar resultados.

## Criterios de salida

Cierre de esta Pre-Fase: acuerdos y pendientes documentados, contratos y brechas técnicas trazables en el handoff para diseño, evidencia de validación disponible y QA de este alcance aprobado. Diseño visual, DESIGN_READY y plan técnico de fase 4 se gestionan fuera de este cierre, según instrucción posterior. Ningún pendiente se considera implementado por figurar en una decisión funcional.

---

# Fase 2 — Validación de Domain y Room

Estado: completada; validación final y QA global APROBADOS. Ver reporte_fase2.md.

## Objetivo
Completar cobertura del contexto maestro reutilizando JUnit, coroutines-test y Room en memoria. Mantener contratos, dependencias y esquema. Fase 1 cerrada: ver reporte_fase1.md.

## Fase A — Coder
- Obtener línea base JVM e instrumentada mediante Tester antes de editar.
- Exigir Connected en ScanDiagnosticCodesUseCase; otros estados retornan Failure sin lectura. Mantener firma.
- Corregir únicamente defectos demostrados por regresiones, sin ejecutar pruebas desde Coder.

## Fase B — Tester
- Diagnóstico: vehículo inexistente, conexión inválida, éxito vacío, duplicados, error sin escritura y captura original al guardar.
- Recurrencia: primera aparición, anteriores/posteriores, empate timestamp/ID, aislamiento y conteo por sesiones distintas, también mediante Room real.
- Mantenimiento: límites antes/igual/después, OR, UNSCHEDULED/UPCOMING/OVERDUE, OTHER excluido, tipos independientes, último por fecha/ID.
- Kilometraje: histórico sin confirmar no escribe, confirmado conserva, superior avanza, negativos rechazados, edición descendente permitida.
- Dashboard: vacío/listo, selección obsoleta, reactividad de vehículo/conexión/mantenimiento, deduplicación, limpio elimina alerta, emisiones anteriores no contaminan tras cambiar vehículo.
- Borrado: activo/no activo/último/inexistente/conectado; persistir reemplazo o null en DataStore antes de borrar el activo, para que un fallo de escritura conserve la fila y la selección válida.
- Room: base aislada por test, sesiones vacías, relaciones y cascada real de códigos, edición preserva hijos, orden/aislamiento, transacciones y rollback con fallo explícitamente comprobado y sin huérfanos.
- Verificar FK, unicidad, índices mediante esquema/metadatos SQLite y mappers completos con fechas/nulls.
- Mantener DataStore como regresión. Clock fijo, runTest, sin sleeps ni frameworks nuevos.
- Devolver defectos al Coder con reproducción antes de corregir.

## Fase C — QA Ponytail
Revisor secuencial aplica ponytail-review y verifica cobertura, aserciones, aislamiento y KISS/YAGNI. No modificar contratos sin autorización adicional.

## Validación y cierre
Ejecutar secuencialmente: ./gradlew :app:testDebugUnitTest; ./gradlew :app:connectedDebugAndroidTest; ./gradlew :app:lintDebug :app:assembleDebug.
Cero fallos en suites completas y QA APROBADO. Si falta dispositivo, registrar bloqueo sin afirmar cierre.
Finalizar task.md y reporte_fase2.md con evidencia. No avanzar fase3 ni implementar UI/TCP/migraciones.

---

# V1 — Flujos exploratorios de Bitácora OBD

Estado: plan aprobado por el usuario el 2026-11-07; ejecución documental en curso. El cierre de Fase 2 anterior se conserva como antecedente.

## Objetivo y destino

Construir un mapa editable de recorridos basado en el desarrollo disponible, sin ampliar funcionalidades ni definir la interfaz final.
Destino único: [FigJam — Flujos exploratorios](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6), archivo `1fsHY8ZUGVre2PrWrCi7z6`.

## Fase A — Creación (Coder documental)

- Inspeccionar el tablero y preservar contenido del usuario.
- Crear mediante MCP secciones, figuras y conectores nativos editables, bajo “V1 — Flujos exploratorios · Pendiente de revisión”.
- Organizar contexto/leyenda, mapa general, seis flujos y decisiones abiertas.
- Distinguir con texto y color: Base implementada (regla/persistencia, no pantalla terminada), Requisito pendiente y Propuesta UX.
- F01: Inicio vacío → agregar primer vehículo → guardar → Inicio con activo; campos existentes, cancelar y error. Selección inicial como propuesta UX.
- F02: listado → crear/editar/seleccionar/eliminar; kilometraje dentro de edición, bloqueo conectado, confirmación de cascada, restante o vacío tras borrar.
- F03: configurar IPv4/puerto → conectando → conectado/error → desconectar; métricas por acción explícita, captura y null distinto de cero. Transporte e integración pendientes.
- F04: escanear → lectura → fallo o éxito limpio/con códigos → pendiente → guardar/descartar. Antes de salir: Guardar/Descartar/Seguir aquí. Fallo de guardado conserva pendiente.
- F05: historial por vehículo → detalle → retorno; vacío, limpio, orientación conocida/desconocida y recurrencia solo con antecedentes. Alerta activa basada en último diagnóstico guardado.
- F06: historial → registro de mantenimiento → programación opcional → guardar; confirmación histórica sin reducir odómetro, vencimiento por fecha O kilometraje y Otro sin alertas.
- Formularios: cancelar vuelve sin persistir; error conserva datos como propuesta de presentación. No dibujar edición de mantenimiento como disponible.
- No modificar código, APIs, modelos, designplans, Make ni wireframes. Sin cuentas, nube, porcentaje de salud, categorías nuevas o borrado de DTC.

## Fase B — Validación documental (Tester)

- Verificar seis flujos completos, conectores rotulados y entradas/salidas/cancelaciones/errores.
- Comprobar distinción entre reglas disponibles y UI pendiente; no copiar defectos de implementación como reglas de negocio.
- Validar captura original, error sin historial, limpio válido, recurrencia y kilometraje histórico.
- Inspeccionar estructura mediante MCP y vista visual si está disponible; declarar cualquier límite.
- No ejecutar suites Android: esta intervención solo cambia diagramas y documentación.

## Fase C — QA Ponytail y cierre

Revisión secuencial de alcance, legibilidad, coherencia y KISS/YAGNI. No convertir propuestas de transiciones OBD, ubicación de métricas o navegación en contratos aprobados.
Tras aprobación QA, finalizar checklist y generar `plans/reporte_flujos_exploratorios_v1.md` con enlaces, evidencia y limitaciones. El tablero seguirá pendiente de revisión UX del usuario.


---

# Fase 3 — Subsistema OBD-II: plan ejecutado
Estado: completada; QA global APROBADO. Ver `plans/reporte_fase3.md`.
Fecha del plan: 2026-11-07.
Para ejecución: aplicar writing-plans, dispatching-parallel-agents y Ponytail; roles secuenciales Coder → Tester → QA, según AGENTS.md.

## Objetivo y arquitectura
Implementar lecturas puntuales OBD sobre TCP preservando ObdGateway y Domain.
Separar parser puro, intérprete puro, transporte y gateway. Kotlin, java.net.Socket, coroutines y JUnit existentes; sin bibliotecas nuevas.
Las rutas siguientes son relativas a app/src/main/java/cl/cristian/bitacoraobd/; pruebas equivalentes bajo app/src/test/java/cl/cristian/bitacoraobd/.

## Auditoría y decisiones propuestas para aprobación
- Existen ObdGateway, ObdResult, ObdConnectionState, ObdConnectionConfig y ObdLiveMetrics; no se encontró implementación concreta OBD.
- ScanDiagnosticCodesUseCase exige Connected y no persiste. Conservar ese límite; el gateway nunca escribe Room ni modifica kilometraje.
- Fase 2 tiene reporte APROBADO; sus resultados son evidencia histórica, no pruebas ejecutadas en esta revisión.
- Manifest no declara INTERNET; compileSdk/targetSdk son 37, minSdk 26.
- RPM es Int: usar división entera; 41 0C 14 5F produce 1303, sin cambiar el contrato a decimal.
- Hay conflicto documental: emuladorEML327.md equipara NO DATA a lista vacía. Propuesta conservadora: NO DATA en Modo 03 es Failure; solo una respuesta 43 válida sin códigos es éxito limpio. Actualizar esa guía tras aprobación.
- La guía también menciona DiagnosedDtc; el gateway real devuelve List<String>. Mantener el contrato real.
- IP 192.168.1.122:35000 es entorno documentado, no disponibilidad comprobada ni configuración fija de producción.
- Alternativas: fake de gateway solamente es rápido pero no prueba TCP; depender solo del emulador remoto vuelve las pruebas frágiles. Recomendada: servidor TCP local determinista de pruebas y validación adicional contra el emulador existente.

## Secuencia obligatoria
Tras aprobación, actualizar plans/task.md con etapas 3.1–3.6 y fases A/B/C.
En cada etapa: A Coder implementa exclusivamente el alcance; B Tester crea/ejecuta pruebas y devuelve defectos reproducibles a Coder; C QA aplica ponytail-review. No avanzar con fallos. Este orden solicitado es validación posterior, no TDD estricto; para correcciones Tester demuestra primero la regresión.

### 3.1 — Parser de respuestas
Crear data/obd/parser/ObdResponseParser.kt y su ObdResponseParserTest.kt.
- [x] Normalizar eco exacto del comando, CR/LF, espacios, prompt y SEARCHING...; conservar límites de respuestas, no eliminar caracteres arbitrarios.
- [x] Validar hexadecimal, cabecera 41 + PID esperado, o 43 para Modo 03; rechazar bytes impares, datos truncados y encabezados inesperados.
- [x] Distinguir NO DATA, ?, ERROR, UNABLE TO CONNECT y respuesta vacía de éxito.
- [x] Decodificar pares DTC a P/C/B/U + cuatro caracteres; ignorar relleno 0000 y deduplicar conservando orden.
- [x] Cubrir fixture real 43 01 43 01 96 02 34 → P0143/P0196/P0234; limpio 43 00 00; eco, compacto, minúsculas, múltiples líneas válidas y errores.
- [x] No afirmar soporte universal CAN/ISO-TP: rechazar formatos no reconocidos explícitamente, nunca convertirlos en limpio.
Salida: parser puro con pruebas verdes.

### 3.2 — Intérprete de PIDs
Crear data/obd/parser/ObdPidInterpreter.kt y ObdPidInterpreterTest.kt.
- [x] RPM ((A * 256) + B) / 4; temperatura A - 40; velocidad A.
- [x] Validar longitud de payload; ausencia/respuesta inválida produce null, no cero.
- [x] Probar RPM 1303, RPM cero, temperatura -40/215, velocidad 0/255 y payload incompleto.
Salida: fórmulas y límites comprobados, sin sockets.

### 3.3 — Cliente TCP y simulador determinista
Crear data/obd/connection/Elm327SocketClient.kt.
Crear en fuentes de test data/obd/simulator/Elm327TestServer.kt y data/obd/connection/Elm327SocketClientTest.kt.
- [x] Abrir Socket en Dispatchers.IO con timeout de conexión propuesto de 5 s.
- [x] Enviar ASCII terminado en CR y leer hasta >; admitir fragmentación TCP.
- [x] Limitar cada respuesta a 64 KiB y plazo total de comando a 10 s, incluido flujo lento; constantes internas, no opciones UI.
- [x] Serializar intercambios para no mezclar comandos; desconexión/cancelación debe cerrar el socket sin esperar el bloqueo de lectura.
- [x] Cerrar recursos ante EOF, timeout, exceso de tamaño y cancelación; preservar CancellationException.
- [x] Servidor local con puerto efímero y respuestas programadas: éxito, fragmentación, silencio, cierre abrupto, respuesta enorme y ausencia de prompt. Cerrar servidor e hilos al finalizar.
- [x] Probar reconexión y cancelación con esperas acotadas; evitar sleeps como mecanismo de sincronización.
Salida: transporte probado sin depender de LAN ni hardware.

### 3.4 — Inicialización y gateway concreto
Crear data/obd/Elm327ObdGateway.kt y data/obd/Elm327ObdGatewayTest.kt.
- [x] Implementar interfaz existente con StateFlow: Disconnected → Connecting → Connected únicamente tras inicialización válida; fallos → Error con recursos cerrados.
- [x] Secuencia propuesta ATZ, ATE0, ATH0, ATSP0, 0100. Validar identificación de reset, OK de configuración y respuesta 41 00. ATH0 fija el formato esperado sin cabeceras CAN.
- [x] Mantener espacios/saltos tolerados por parser; no necesitar ATL0/ATS0.
- [x] Serializar operaciones completas para evitar intercalado durante inicialización o lectura de métricas.
- [x] readLiveMetrics consulta 010C, 0105, 010D una sola vez; PID no disponible → null individual; pérdida TCP → Failure y Error.
- [x] readDiagnosticCodes envía solo 03 y devuelve códigos o Failure. Sin reintentos ni polling automático.
- [x] Desconexión idempotente; no publicar Connected después de una cancelación/desconexión concurrente.
- [x] Probar estados, fallo en cada paso AT, lectura desconectada, métricas parciales, diagnóstico limpio/con códigos, concurrencia y reconexión.
Salida: contrato completo sin cambios de Domain ni persistencia.

### 3.5 — Preparación Android y validación con emulador
Modificar app/src/main/AndroidManifest.xml y plans/emuladorEML327.md.
- [x] Declarar INTERNET; verificar requisitos de acceso a red local de targetSdk 37 con documentación oficial vigente antes de incorporar permisos adicionales.
- [x] Si hace falta consentimiento en runtime, documentar y planificar el mínimo puente de prueba; la pantalla definitiva pertenece a fase 4.
- [x] Documentar escenarios soportados, NO DATA, precisión RPM y configuración por ObdConnectionConfig.
- [x] Validar desde dispositivo/emulador Android contra el servidor configurado: inicialización, RPM, velocidad, temperatura disponible/no disponible y DTC documentados.
- [x] Registrar respuestas reales y entorno; no asumir que comandos documentados siguen funcionando.
- [x] No ejecutar 04, 07 ni 0A; no modificar configuración remota ni instalar herramientas sin necesidad.
Salida: evidencia de integración o bloqueo explícito si no hay acceso. No confundir test JVM con conectividad Android.

### 3.6 — Regresión, QA global y cierre
- [x] Tester ejecuta secuencialmente ./gradlew :app:testDebugUnitTest.
- [x] Ejecuta ./gradlew :app:connectedDebugAndroidTest con dispositivo disponible.
- [x] Ejecuta ./gradlew :app:lintDebug :app:assembleDebug.
- [x] Resultado exigido: cero fallos; si falta dispositivo/red, registrar pendiente y no declarar fase completa.
- [x] QA comprueba alcance completo, límites/cancelación, errores sin persistencia, null distinto de cero, ausencia de Modo 04 y de abstracciones/dependencias especulativas.
- [x] Tras QA APROBADO, finalizar plans/task.md y crear plans/reporte_fase3.md con comandos, resultados, evidencia y limitaciones.
- [x] Detenerse y consultar siguiente paso.

## Fuera de alcance
AppContainer, ViewModels, Compose, navegación y selección de vehículo: fase 4.
Sin migraciones Room, catálogo nuevo, telemetría continua, Bluetooth, nube, modos extendidos ni borrado ECU.
Hardware físico: validación adicional si está disponible; éxito con simulador no demuestra compatibilidad universal.

## Puerta de aprobación
Confirmación recibida para el plan, incluida política NO DATA como Failure en Modo 03 y uso del servidor TCP local determinista más validación remota adicional. Fase 3 se ejecutó en etapas secuenciales; el reporte final registra resultados y limitaciones.
