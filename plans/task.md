# Pre-Fase 4 — Checklist

## Fase 4 — Etapa 4.6: Conexión y diagnóstico UI-03/04 (CERRADA)

Plan aprobado por el usuario: [implementation_plan_fase4_etapa4_6.md](designplans/implementation_plan_fase4_etapa4_6.md). Secuencia Coder → Tester → QA/Ponytail completada. [Reporte de cierre](reporte_fase4_etapa4_6.md).

### Fase A — Implementación de Código (Coder; COMPLETADA)
- [x] AppSession coordina configuración conectada, captura guardada, reescaneo, cancelación y salidas.
- [x] UI-03 integra formulario, permiso LAN por plataforma, métricas explícitas, borrador y navegación.
- [x] UI-04 integra diagnóstico, orientación, estados y confirmaciones de salida.

### Fase B — Pruebas (Tester; SUITE EJECUTABLE APROBADA)
- [x] Regresiones JVM para validación, persistencia parcial, reescaneo, aislamiento de vehículo y salida durante guardado.
- [x] JVM, compilación AndroidTest, 26 instrumentadas sin fallos, lint, assemble y diff check pasan en SM-A066M/API 36; 1 prueba ELM327 omitida sin host.
- [ ] Validar permiso runtime API 37, métricas y estados Compose en dispositivo, recreación durante operaciones y accesibilidad/IME manual; no hubo evidencia suficiente en esta etapa.

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)
- [x] Revisar plan, SOLID/KISS/YAGNI, datos, navegación y ciclo de vida; corregir hallazgos y repetir pruebas.
- [x] QA/Ponytail emitió APROBADO para implementación verificable; límites restantes documentados en reporte.



## Fase 4 — Etapa 4.5: Vehículos y formularios UI-02 (CERRADA)

Plan específico aprobado por el usuario: [implementation_plan_fase4_etapa4_5.md](designplans/implementation_plan_fase4_etapa4_5.md). Se ejecutó el flujo secuencial obligatorio Coder → Tester → QA/Ponytail. Evidencia formal: [reporte de cierre 4.5](reporte_fase4_etapa4_5.md).

### Planificación documental
- [x] Auditar contratos reales de vehículos, Room/DataStore, sesión/Mutex, navegación, tema, dependencias y pruebas/callers relevantes.
- [x] Definir D2 al abrir/guardar, carreras/transitorios, captura pendiente, alta parcial con ID retenido y refresco real antes de reintentar.
- [x] Definir formularios, unidades/validación, borrador, navegación mínima y tema/materiales/contraste.
- [x] Crear plan específico con archivos, firmas propuestas, tareas A/B/C, pruebas/comandos y límites; conciliar matriz/handoff.
- [x] Corregir hallazgos Ponytail del plan y obtener aprobación explícita antes de codificar.

### Fase A — Implementación de Código (Coder; COMPLETADA)
- [x] AppSession conserva el Mutex único y coordina D2, operaciones de vehículos, ID retenido, actualización y recuperación sin duplicar escritura.
- [x] VehiclesViewModel y componentes Material 3 implementan lista/formulario, validación, millas, borrador, errores, reintento y salida encolada durante guardado.
- [x] Integrar navegación UI-02 y vacíos secundarios mínimos; tema fijo claro/oscuro, callers y documentación reconciliados. Sin nuevas dependencias ni coordinadores.

### Fase B — Tests TDD (Tester; AUTOMATIZADA COMPLETADA)
- [x] JVM y AndroidTest cubren coordinación, cancelación/ID retenido, D2, reintentos y persistencia/migración/cascada relevante.
- [x] Pruebas de formulario y millas; Activity recreation y salida durante guardado con fallo conservando borrador, y retry exitoso continuando navegación.
- [x] :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, :app:connectedDebugAndroidTest, :app:lintDebug, :app:assembleDebug y git diff --check pasaron.
- [x] Instrumentadas ejecutadas en SM-A066M/API 36; ELM327 omitido porque no había elm327Host.

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)
- [x] Revisión conectada de navegación, VehiclesViewModel, pantalla, tema, tests y manejo de guardado concurrente; veredicto QA/Ponytail APROBADO, sin hallazgos pendientes.
- [x] Actualizar plan, checklist y reporte de cierre. Detener trabajo antes de 4.6.

**Límites de verificación:** no se realizó comprobación manual en dispositivo de TalkBack/orden de foco, fuente ampliada, contraste renderizado, IME/insets o barras; ratios calculados no sustituyen esas comprobaciones. No se probó hardware ELM327 ni permiso LAN runtime. Wireframes base siguen siendo referencia DESIGN_READY y no acreditan las variantes o pantallas de etapas posteriores.


## Fase 4 — Etapa 4.4: revisión de recorridos y DESIGN_READY (CERRADA)

Plan: [implementation_plan_fase4_etapa4_4.md](designplans/implementation_plan_fase4_etapa4_4.md). QA APROBADO para alcance documental y R03; [reporte de cierre](reporte_fase4_etapa4_4.md). Wireframes base DESIGN_READY; integración y validaciones transferidas a 4.5–4.8.

- [x] Revisar inventario UI-01–07, flujos/DD, dependencias, sistema visual y metadatos Figma de referencia.
- [x] Contrastar grafo/AppSession/HomeViewModel y contratos backend; separar implementación real de especificación histórica.
- [x] Registrar R01–R15, prioridades, D1–D4, límites de evidencia y puerta por paquete.
- [x] Crear plan concreto y registrar DESIGN_READY de wireframes base; las pantallas de producto no están implementadas.

### Fase A — Coder / diseñador (COMPLETADA CON TRANSFERENCIAS)
- [x] Confirmar ejecución del plan 4.4.
- [x] A1–A3: matriz y dependencias documentadas; D1–D3 aprobadas. D2 con coordinación/revalidación y pruebas de carreras asignada a UI-02/4.5.
- [x] A4: cuatro capturas verificadas y referenciadas; variantes y recorridos se validan en 4.5–4.8. Studio no expuso use_figma ni búsqueda de biblioteca.
- [x] A5: apariencia A, paleta fija, modo del sistema y captura de referencia documentados; ocho pares candidatos calculados. Muestras, tokens y validación de componentes transferidos a 4.5.
- [x] A6: corrección R03 implementada y verificada: rechazo previo, fallo recuperable y propagación de cancelación.
- [x] A7: tema y su validación transferidos a 4.5; pantallas posteriores no implementadas en 4.4.

### Fase B — Tester (COMPLETADA PARA EL ALCANCE 4.4)
- [x] B1–B5: contratos y soporte contrastados documentalmente; recorridos ejecutables y pruebas específicos asignados a 4.5–4.8.
- [x] B6: ocho pares candidatos recalculados; contraste de componentes, accesibilidad, texto/ventana/IME se validan durante implementación.
- [x] B7: para R03 pasaron testDebugUnitTest, compileDebugAndroidTestKotlin, connectedDebugAndroidTest (SM-A066M/API 36: 24, 0 fallos, 1 omitida por elm327Host), lintDebug, assembleDebug y git diff --check. Excepción real de NavController no inyectada; failExit/reintento cubiertos directamente. Falta validación del tema cuando corresponda.

### Fase C — QA/Ponytail (APROBADA PARA EL ALCANCE 4.4)
- [x] C1–C2: QA aprobó R03 y revisión documental; integración y trazabilidad visual se validan en etapas responsables.
- [x] C3: wireframes base DESIGN_READY por decisión del usuario; variantes/tokens y validación transferidos a implementación.
- [x] C4: reporte de cierre y checklist; detenerse antes de 4.5.


## Fase 4 — Etapa 4.3: composición, ciclo de vida y navegación (CERRADA)

Plan aprobado por el usuario durante la sesión de ejecución; implementación autorizada, incluida la dependencia exacta `androidx.navigation:navigation-compose:2.10.2`. Plan: [implementation_plan_fase4_etapa4_3.md](designplans/implementation_plan_fase4_etapa4_3.md). Cierre **QA APROBADO**; reporte [reporte_fase4_etapa4_3.md](reporte_fase4_etapa4_3.md). Validación final: JVM, AndroidTest compile, instrumentadas Pixel_10/API 37 (23 pruebas, 0 fallos, 1 omitida por ausencia de `elm327Host`), lint, assemble y diff check pasaron. Hardware ELM327 y permiso LAN runtime no se validaron.

### Planificación y condiciones de entrada
- [x] Leer AGENTS, contexto, planes/reportes 4.1/4.2, contratos, DD/flujos y DEP-001–023 pertinentes.
- [x] Contrastar Activity, Room v2/schema/exportación/migración, DAOs/repositorios, DataStore, gateway/socket, casos de uso, pruebas y callers.
- [x] Revisar reportes 4.1/4.2 preservando QA y limitaciones: remota omitida sin elm327Host; permiso LAN runtime aún pendiente.
- [x] Confirmar ausencia de builder Room de producción; planificar builder único con MIGRATION_1_2, sin fallback destructivo.
- [x] Inspeccionar dependencias: ViewModel/Lifecycle disponibles; Navigation no declarada/encontrada.
- [x] Definir propietario de proceso, adaptación mediante ViewModel, recreación y nuevo proceso sin recuperar pendiente.
- [x] Definir Mutex compartido no reentrante, cancelación fuera del lock, cierre confirmado, revalidación y pruebas deterministas.
- [x] Definir refresco real tras fallo parcial Room/DataStore y activación de Inicio sin temporizador.
- [x] Separar preparación técnica de puertas DESIGN_READY UI-01–07; conservar aprobación de wireframes.
- [x] Escribir plan y checklist; registrar discrepancias históricas sin cambiar reglas funcionales.
- [x] Auditoría documental Ponytail del plan 4.3: revisión principal y QA independiente; ver §13 del plan. Sin código ni pruebas ejecutadas.
- [x] Incorporar al plan la corrección del finally, ExitAction/AuthorizedExit con consumo único y fixture/runner para MainActivity real; auditoría histórica preservada en §13.
- [x] Segunda auditoría Ponytail: sin discrepancias funcionales nuevas; precisión B1/B2 incorporada. Resultado en §14 y prompt preparado en plans/prompt_ejecutar_fase4_etapa4_3.md. No autoriza ejecución por sí solo.
- [x] Implementar y verificar contratos A0 → B0 → C0; hallazgos QA corregidos y revalidados antes de aprobar.
- [x] Obtener aprobación explícita del plan 4.3 e iniciar ejecución.
- [x] Obtener autorización concreta para androidx.navigation:navigation-compose:2.10.2; resolución Gradle confirmada.
- [x] Builder real migración comprobado; dependencia resuelta y suites runtime ejecutadas con omisión remota documentada.

### Fase A — Implementación de Código (Coder; COMPLETADA)
- [x] A0: contratos corregidos de conexión/salida y punto de sustitución definidos antes de integrar Activity/grafo.
- [x] A1–A2: builder Room registra MIGRATION_1_2; Application/AppContainer y dependencias compartidas por constructor.
- [x] A3–A5: AppSession coordina estado, conexión/cancelación/cierre, selección/borrado y recuperación de fallo parcial.
- [x] A6: captura pendiente, guardado/reintento y lecturas explícitas; decisión fuera de Mutex, revalidación dentro.
- [x] A7: ViewModels y colección de Inicio reiniciada al activar; sin temporizador.
- [x] A8: Navigation Compose autorizado/resuelto; cuatro destinos principales, secundarios y retorno F03 a Diagnóstico; sin pantallas posteriores.
- [x] A9: contratos/matriz de dependencias actualizados y entregados a Tester.

### Fase B — Tests TDD (Tester; COMPLETADA)
- [x] B0: Success conserva conexión; Connected bloquea; cancelación cierra; fixture MainActivity real verifica captura Save/Stay/fallo, recreación, consumo único y retorno F03.
- [x] B1–B2: identidad de instancias y migración v1→v2 por builder de producción preservando relaciones.
- [x] B3–B5: coordinación, cancelación/cierre, estados Connected/Disconnecting/Error cubiertos por tests de sesión.
- [x] B6–B8: captura pendiente y decisiones Save/Stay/fallo; guardado diferido/recreación y lecturas coordinadas cubiertos.
- [x] B9–B10: fallo parcial Room/DataStore refresca el activo y conserva error; regresión añadida.
- [x] B11: reloj mutable confirma reevaluación UPCOMING→OVERDUE al reactivar Inicio, sin temporizador.
- [x] B12–B14: recreación, navegación/retornos/F03 y sesión del proceso verificadas.
- [x] B15: dependencias resueltas; JVM, compile AndroidTest, instrumentadas, lint, assemble y diff check secuenciales; resultados arriba.
- [x] Registrar omisión ELM327 sin elm327Host; hardware y permiso runtime LAN quedan no verificados.

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)
- [x] C0: revisión de evidencia B0 y cableado real de MainActivity.
- [x] C1–C2: Ponytail revisó builder/migración/propiedad/cierre y preservación de datos.
- [x] C3–C4: revisados Mutex/esperas/cancelación, propietario, KISS/YAGNI y dependencia autorizada.
- [x] C5: criterios técnicos aprobados, sin ampliar UI/contratos ni marcar pantallas DESIGN_READY; hallazgos resueltos.
- [x] C6: QA APROBADO; checklist finalizado y reporte_fase4_etapa4_3.md generado.
- [x] Detenerse al cierre y consultar siguiente paso; no iniciar 4.4 automáticamente.

---


## Fase 4 — Etapa 4.2: preferencias y guardias (CERRADA)

Plan aprobado: [implementation_plan_fase4_etapa4_2.md](designplans/implementation_plan_fase4_etapa4_2.md). Cierre QA: **APROBADO**; reporte [reporte_fase4_etapa4_2.md](reporte_fase4_etapa4_2.md).

### Fase A — Implementación de Código (Coder)
- [x] Preferencias de Ajustes en Domain/DataStore con defaults aprobados y compatibilidad de active_vehicle_id, obd_ip y obd_port.
- [x] Fallos y cancelación se propagan; se documentó el límite Room/DataStore y el fallo parcial de selección/borrado, sin ampliar guardias fuera del contrato aprobado.
- [x] Documentado recálculo al activar Inicio mediante nueva evaluación del Flow; sin temporizador ni persistencia de alertas.
- [x] Fake de preferencias actualizado; no se detectaron consumidores de producción pendientes.

### Fase B — Tests TDD (Tester)
- [x] Instrumentadas de DataStore: defaults, round-trip y preservación OBD; suite completa pasó en Pixel_10 AVD/API 37.
- [x] JVM inyecta IOException en lectura y escritura; ambas pruebas verifican propagación sin éxito falso.
- [x] Guardias backend existentes, cancelación y fallo parcial de borrado revisados; coordinación de conexión/cambio/captura pendiente asignada a Presentation/4.3.
- [x] Conversión de millas y conservación de campos sin editar asignadas a Presentation/4.7–4.8 conforme DD-37.
- [x] `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, `:app:connectedDebugAndroidTest`, `:app:lintDebug`, `:app:assembleDebug` — BUILD SUCCESSFUL. Instrumentadas Pixel_10 API 37.

### Fase C — Code Review & QA (Ponytail Reviewer)
- [x] Revisados contratos, callers, claves, defaults, errores, cancelación y límites transaccionales; sin hallazgos pendientes dentro del alcance 4.2.
- [x] Reporte de cierre generado; etapa cerrada. Siguiente paso requiere indicación del usuario.

---

## Entrada prioritaria para fase 4 — backend requerido por UI

**Estado actualizado:** backend requerido para UI planificado e implementado en etapas 4.1 y 4.2. Idempotencia/migración: [reporte 4.1](reporte_fase4_etapa4_1.md). Preferencias/guardias backend: [reporte 4.2](reporte_fase4_etapa4_2.md). Pendientes de integración Presentation, Room builder y ciclo de vida pasan a 4.3 y etapas UI; no reabrir la deuda ya cerrada.

- [x] Plan de Fase 4 aprobado y ejecución iniciada por instrucción del usuario.
- [x] DEP-011: captureId estable, índice único, transacción idempotente y migración v1→v2; instrumentadas ejecutadas y QA aprobado.
- [x] Preferencias locales requeridas por Ajustes implementadas y verificadas en 4.2.
- [x] Registrar evidencia y límites reales en reportes por etapa.

## Saneamiento visual y documental — 2026-11-10

Plan: [saneamiento visual](implementation_plan.md#saneamiento-documental-y-visual--2026-11-10). Sin cambios de código Android.

- [x] **Fase A — editor:** actualizar referencias, F03 y variantes autorizadas; separar históricos y conservar [evidencia](designplans/figma-references.md#saneamiento-de-variantes--2026-11-10). La verificación independiente se registró posteriormente en Fase B.
- [x] **Fase B — verificador:** variantes y correcciones contrastadas en Figma; F03 conserva una etiqueta parcialmente tapada y líneas visualmente cercanas. Interacciones y accesibilidad sin probar.
- [x] **Fase C — QA/Ponytail:** APROBADO CON OBSERVACIONES para el saneamiento; pendientes de claridad visual y validación en [reporte](reporte_saneamiento_visual.md). No implica cierre integral ni DESIGN_READY.

## Saneamiento Domain/Data — ampliación aprobada

- [x] Usuario aprobó recomendaciones y ejecución.
- [x] **Fase A — Coder:** códigos DTC, año ausente, tipos y edición de mantenimiento, errores parciales; corrección QA evita avanzar odómetro al editar.
- [x] **Fase B — Tester:** JVM, instrumentadas, lint y assemble BUILD SUCCESSFUL. SM-A066M Android 16: 13 instrumentadas aprobadas; una integración remota ELM327 omitida por no configurar servidor. JVM UP-TO-DATE en validación final.
- [x] **Fase C — QA Ponytail:** saneamiento Domain/Data APROBADO tras corregir odómetro y verificar regresiones; ver [reporte_saneamiento_prefase4.md](reporte_saneamiento_prefase4.md). Al emitir ese reporte la Pre-Fase 4 seguía abierta; el cierre posterior consta en [reporte de cierre](reporte_prefase4.md). La revisión de diseño continúa por separado.

## Fase A — Coder documental

- [x] Revisar documentación y contratos principales; contrastar DD-12/15/17/18/24 con el código y registrar decisiones posteriores al saneamiento.
- [x] Auditar Room, mantenimiento, preferencias, selección activa y configuración; conservar los hallazgos y la política de migración en [auditoría](auditoria_prefase4.md) y [contratos](designplans/domain-contracts.md).
- [x] Inspeccionar parcialmente FigJam y Design; actualizar en Design, con autorización, la línea de códigos de Inicio, variantes limpio/sin diagnóstico y notas técnicas. Registrar evidencia en [referencias](designplans/figma-references.md).
- [x] Preparar borradores de [handoff](designplans/design-handoff.md) y [plan técnico de fase 4](plan_tecnico_fase4_borrador.md).
- [x] Registrar acuerdos de cancelación de lectura, permiso LAN, conexión, descarte/reescaneo y retorno al origen en [flujos](designplans/screen-flows.md); conservar km enteros en el MVP.
- [x] Conciliar detalles funcionales: salida con IP/puerto modificados, guardado de Ajustes y redondeo de millas definidos en DD-35/36/37; ver [decisiones](designplans/design-decisions.md). Implementación y pruebas corresponden a fase 4.
- [x] Documentar handoff técnico requisito → decisión → contrato → referencia y prueba prevista, con diferencias y dependencias en [handoff](designplans/design-handoff.md) y [contratos](designplans/domain-contracts.md). La verificación independiente por muestreo estático se registra en Fase B; la revisión y aprobación visual pertenecen a [su plan propio](designplans/implementation_plan.md).
- [x] Excluir de esta Pre-Fase la elaboración o finalización del plan de fase 4 por instrucción del usuario; el borrador existente queda como referencia no aprobada.

## Fase B — Tester

- [x] Contrastar parcialmente contratos, decisiones y documentación con el código; identificar y corregir contradicciones en flujos, decisiones y especificación UI.
- [x] Validar saneamiento Domain/Data: JVM, lint y assemble exitosos; instrumentadas en SM-A066M/Android 16 con 13 aprobadas y una integración remota omitida. Ver [reporte](reporte_saneamiento_prefase4.md).
- [x] Verificador independiente contrastó por muestreo estático el [handoff](designplans/design-handoff.md), acuerdos, referencias y [contratos](designplans/domain-contracts.md) con fuentes disponibles; vacíos y responsables constan en esos documentos. No validó Figma ni aprobación visual.
- [x] Verificador contrastó por muestreo la trazabilidad de detalles técnicos y pruebas previstas; API 37 sigue sin validar. La integración remota dirigida fue revalidada en SM-A066M / Android 16; ver [guía](emuladorEML327.md#revalidación-remota-de-pre-fase-4). No ejecutó pruebas Android durante esta revisión.
- [x] La validación visual y accesibilidad continúan en el plan propio de diseño; la elaboración del plan de fase 4 queda fuera de esta Pre-Fase.

## Fase C — QA Ponytail

**Verificación y conciliación documental técnica:** QA/Ponytail APROBADO para el alcance estático por muestreo, sin validar Figma ni ejecutar pruebas Android. Este veredicto no sustituye el QA global ni el cierre formal de Pre-Fase 4.

- [x] QA global/Ponytail: APROBADO para el alcance vigente de Pre-Fase 4, con contraste selectivo de contratos y evidencia; sin ejecución nueva de pruebas ni revisión de Figma.
- [x] Checklist consolidado y [reporte de cierre](reporte_prefase4.md) generado; los pendientes posteriores conservan su propio seguimiento.

---

# Fase 4 — Seguimiento por etapa

## Etapa 4.1 — Idempotencia de capturas diagnósticas

Plan específico aprobado por el usuario; ejecución 4.1 iniciada: [implementation_plan_fase4_etapa4_1.md](designplans/implementation_plan_fase4_etapa4_1.md).

### Fase A — Implementación de Código (Coder)
- [x] Identidad UUID estable generada al crear la captura exitosa y conservada al guardar/reintentar; sin recuperación tras muerte de proceso.
- [x] Unicidad persistente e inserción idempotente implementada con índice UNIQUE/INSERT IGNORE; retorno del mismo ID y efectos solo para sesión nueva dentro de transacción.
- [x] Migración Room v1→v2 aditiva con sentinel autorizado, UUID Kotlin por sesión e índice UNIQUE; schema v2 exportado. Sin builder de producción que registrar hasta 4.3.

### Fase B — Tests TDD (Tester)
- [x] JVM: `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL (82 pruebas); ajustados asserts afectados por UUID no deterministas.
- [x] `./gradlew :app:lintDebug` y `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL.
- [x] Misma captura guardada dos veces retorna mismo ID y una sesión; instrumentada ejecutada en Pixel_10 AVD/API 37.
- [x] Pruebas instrumentadas añadidas para captura repetida, payload contradictorio, dos captureId con payload igual, concurrencia, rollback/retry con el mismo objeto y migración desde fixture SQLite v1 preservando datos.
- [x] Validación Room/migración diferida ejecutada en Pixel_10 AVD/API 37; UI no estuvo bloqueada mientras se esperaba disponibilidad.
- [x] Compilación de pruebas instrumentadas: `./gradlew :app:compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL.
- [x] Capturas distintas generan sesiones distintas; instrumentada ejecutada.
- [x] Guardados concurrentes convergen a una fila/ID; instrumentada ejecutada.
- [x] Migración v1→v2 conserva y permite consultar vehículo, sesiones, códigos y mantenimientos; instrumentada ejecutada.
- [x] Rollback de escritura conserva datos y permite reintento; instrumentada ejecutada.
- [x] Suite instrumentada final: 19 pruebas, 0 fallos y 1 omitida; `:app:connectedDebugAndroidTest` BUILD SUCCESSFUL. Ver reporte 4.1.

### Fase C — Code Review & QA (Ponytail Reviewer)
- [x] QA final APROBADO tras inspección estática y ejecución de instrumentadas; ver reporte 4.1.
- [x] Ajustar UUID por defecto de entidad para que fixtures/creaciones directas no colisionen bajo índice UNIQUE; sentinel queda solo como default de esquema de migración.
- [x] Registrar informe parcial: [reporte_fase4_etapa4_1.md](reporte_fase4_etapa4_1.md).
- [x] Ejecutar `./gradlew :app:connectedDebugAndroidTest` en Pixel_10 AVD/API 37: 19 pruebas, 0 fallos, 1 omitida (Elm327 remoto sin elm327Host). Corregir assert de índices SQLite y repetir; segunda corrida BUILD SUCCESSFUL. QA final APROBADO; ver [reporte_fase4_etapa4_1.md](reporte_fase4_etapa4_1.md).

**Límite histórico:** al cierre original de 4.1 no iniciar Compose, Figma ni etapa 4.2. La validación pendiente quedó resuelta posteriormente; el estado actual consta en el reporte.

---

# Fase 2 — Checklist de avance

Fase 1 completada: ver [reporte_fase1.md](reporte_fase1.md). Fase 2 completada según [implementation_plan.md](implementation_plan.md); ver [reporte_fase2.md](reporte_fase2.md).

## Fase A — Implementación de Código (Coder)

- [x] Registrar línea base JVM de 27/27 y el bloqueo inicial de instrumentadas por falta de dispositivo.
- [x] Exigir estado OBD `Connected` antes de leer códigos; los demás estados fallan sin invocar la lectura.
- [x] Actualizar regresiones existentes para cubrir conexión requerida, confirmar excepción de rollback y comprobar que no queden sesiones huérfanas tras cascada.
- [x] Cambios de producción limitados a los casos de uso de escaneo y borrado; no se cambiaron contratos ni esquema.

## Fase B — Tests TDD (Tester)

### Diagnóstico

- [x] Escaneo conectado exitoso sin códigos.
- [x] Escaneo desconectado devuelve fallo sin leer códigos.
- [x] Error del gateway se representa como fallo.
- [x] No se guarda un resultado de escaneo fallido.
- [x] Al guardar se conserva el vehículo y los códigos capturados originalmente.
- [x] Vehículo inexistente devuelve fallo y no provoca lectura OBD; `:app:testDebugUnitTest` BUILD SUCCESSFUL.
- [x] `Connecting`, `Disconnecting` y `Error` devuelven fallo sin lectura OBD; `:app:testDebugUnitTest` BUILD SUCCESSFUL.
- [x] El escaneo deduplica códigos; el error del gateway no escribe sesión (verificación de llamada al repositorio); `:app:testDebugUnitTest` BUILD SUCCESSFUL.

### Recurrencia

- [x] Cobertura básica: código recurrente si aparece en el conjunto de códigos previos; código nuevo no recurrente.
- [x] Primera aparición no recurrente; sesión posterior no se consulta como anterior y se excluye; `:app:testDebugUnitTest` BUILD SUCCESSFUL.
- [x] Empate de timestamp resuelto por ID en la consulta Room: sesiones anteriores incluyen menor ID y excluyen mayor ID.
- [x] Aislamiento por vehículo y conteo por sesiones distintas verificados contra Room; códigos del otro vehículo no contaminan el conteo.
- [x] Reglas de recurrencia, orden, igualdad de timestamp/ID y aislamiento repetidas contra Room real; `:app:connectedDebugAndroidTest` BUILD SUCCESSFUL.

### Mantenimiento y kilometraje

- [x] Caso vencido por fecha.
- [x] Caso próximo y no programado.
- [x] El repositorio de prueba excluye `OTHER`; prueba confirma que no se evalúa como alerta.
- [x] Límites de fecha y kilometraje antes/igual/después; vencimiento OR cubierto en pruebas JVM.
- [x] `OIL_AND_FILTER`, `OIL_ONLY` y `FILTER_ONLY` se evalúan independientemente en pruebas JVM.
- [x] Room selecciona el último registro por tipo mediante fecha e ID; prueba instrumentada existente pasa.
- [x] Kilometraje histórico requiere confirmación y no escribe antes de confirmarse.
- [x] Confirmación guarda historial; Room conserva odómetro al guardar valor inferior, avanza al guardar superior y permite edición manual descendente; modelos rechazan kilometrajes negativos.

### Dashboard y borrado

- [x] Dashboard sin vehículos produce estado vacío.
- [x] Selección obsoleta usa un vehículo existente.
- [x] Cambiar vehículo activo cambia la observación.
- [x] Alerta recurrente se deduplica y desaparece tras sesión limpia.
- [x] Dashboard reactivo ante cambios de vehículo, conexión y mantenimiento; aislamiento al cambiar vehículo.
- [x] Borrar vehículo activo, no activo, último e inexistente; comprobar reemplazo/limpieza de selección.
- [x] Si falla la actualización de DataStore al borrar el activo, el borrado no ocurre; la fila y la selección siguen válidas; prueba JVM BUILD SUCCESSFUL.
- [x] Revisión Ponytail del bloque dashboard/borrado: APROBADO.
- [x] Borrado bloqueado mientras OBD está conectado.

### Room, SQLite y DataStore

- [x] Prueba usa Room en memoria y cierra la base en cada caso.
- [x] Cascada de vehículo a sesiones y mantenimientos; verifica que la sesión borrada no quede huérfana.
- [x] Actualización del vehículo conserva sesiones relacionadas.
- [x] Fallos forzados de avance de odómetro se afirman explícitamente; las transacciones revierten sesión/códigos y mantenimiento y conservan el kilometraje.
- [x] Sesión limpia vacía y relación sesión-códigos verificadas con consultas Room reales.
- [x] Consultas Room de orden/aislamiento, recurrencia y mantenimiento vigente cubiertas en pruebas instrumentadas.
- [x] Claves foráneas, columnas, cascadas, PK compuesta e índices verificados con metadatos SQLite; JVM e instrumentadas pasan.
- [x] Mappers verificados en ambas direcciones con fechas calendario, campos nullable y colecciones vacías/con datos.
- [x] Regresión DataStore verifica guardar y limpiar `activeVehicleId`; además, el fallo posterior al borrado está cubierto en el caso de uso.
- [x] Revisión Ponytail del bloque Room/SQLite/DataStore: APROBADO.

### Ejecución de pruebas

- [x] Línea base registrada: 27 pruebas JVM; instrumentadas inicialmente sin dispositivo disponible.
- [x] `./gradlew :app:testDebugUnitTest` BUILD SUCCESSFUL tras la cobertura Room/DataStore.
- [x] `./gradlew :app:connectedDebugAndroidTest` BUILD SUCCESSFUL en SM-A066M / Android 16 tras la cobertura Room/DataStore.
- [x] Las pruebas usan Clock fijo y no añaden sleeps ni frameworks.
- [x] Revalidación final secuencial: `:app:testDebugUnitTest`, `:app:connectedDebugAndroidTest`, `:app:lintDebug` y `:app:assembleDebug`; todos BUILD SUCCESSFUL tras la corrección de borrado.

## Fase C — Code Review & QA (Ponytail Reviewer)

- [x] Revisión contra el plan realizada; encontró cobertura incompleta.
- [x] Resolver los casos pendientes y revisar cobertura, aserciones, aislamiento y KISS/YAGNI.
- [x] Veredicto QA global Ponytail: APROBADO.
- [x] Checklist finalizado y reporte de cierre generado: [reporte_fase2.md](reporte_fase2.md).

## Estado

**Completada.** Cobertura de Fase 2, suites, lint, assemble y QA global aprobados. Ver [reporte_fase2.md](reporte_fase2.md).

---

# V1 — Checklist de flujos exploratorios

Estado: plan aprobado el 2026-11-07; en ejecución. El cierre de Fase 2 anterior se conserva.
Destino: [FigJam — Flujos exploratorios](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6).

# Fase 3 — Checklist de avance (etapa 3.6)

Etapas 3.1–3.5 completadas; QA APROBADO. El usuario autorizó iniciar regresión y cierre 3.6.

## Etapas 3.1 y 3.2
Coder, Tester y QA completados; pruebas dirigidas y suite JVM pasan.

## Etapa 3.3 — Cliente TCP y servidor determinista

### Fase A — Coder
- [x] Cliente con Dispatcher IO, connect timeout 5s, plazo por comando 10s, límite 64 KiB.
- [x] Envío ASCII + CR; lectura hasta `>` con fragmentación TCP.
- [x] Mutex para comandos; limpieza ante EOF, error, timeout y cancelación; CancellationException se propaga.
- [x] Revisado hallazgo QA: `ensureActive()` tras connect bloqueante para cerrar socket si la coroutine quedó cancelada.
- [x] Fixture local de pruebas con ServerSocket en puerto efímero.

### Fase B — Tester
- [x] Respuesta válida/fragmentada, CR, comandos secuenciales, timeout, EOF, prompt ausente, tamaño >64 KiB, cancelación en lectura y reconexión.
- [x] `./gradlew :app:testDebugUnitTest --tests 'cl.cristian.bitacoraobd.data.obd.connection.Elm327SocketClientTest'` y `./gradlew :app:testDebugUnitTest`: ambos BUILD SUCCESSFUL tras fix.
- [x] No se contactó emulador externo; no corresponde en esta etapa.
- [x] Sin test determinista de cancelación durante connect: loopback completa rápido y saturación de backlog depende del SO; evitar abstracción especulativa.

### Fase C — QA Ponytail
- [x] QA encontró y el Coder corrigió fuga potencial de socket al cancelar durante connect.
- [x] Revisión final de límites, errores, cancelación y KISS: APROBADO.
- [x] Detenerse y esperar confirmación antes de 3.4.

## Etapa 3.4 — Inicialización y gateway concreto

### Fase A — Coder
- [x] Implementar handshake ATZ, ATE0, ATH0, ATSP0, 0100 con validación de banner, OK y bitmap completo 41 00.
- [x] StateFlow publica Connected solo tras inicialización válida; fallos cierran socket y publican Error.
- [x] Lecturas puntuales 010C/0105/010D, PID inválido → null individual; 03 devuelve códigos o Failure.
- [x] Operaciones serializadas; limpieza por cancelación y desconexión idempotente, con protección contra Connected tardío.
- [x] Correcciones QA: bitmap PID 00 completo; cancelación en métricas/DTC desconecta y actualiza estado sin ocultar CancellationException.

### Fase B — Tester
- [x] Diez pruebas iniciales cubren estados, respuestas de handshake, métricas parciales, pérdida TCP y DTC limpio/con códigos.
- [x] Concurrencia, desconexión durante handshake, desconexión idempotente y reconexión normal cubiertas; sin comandos extra/polling.
- [x] Regresiones de PID 00 truncado y cancelación en métricas/DTC fallaron antes del fix y pasaron después.
- [x] `:app:testDebugUnitTest --tests 'cl.cristian.bitacoraobd.data.obd.Elm327ObdGatewayTest'` y `:app:testDebugUnitTest`: BUILD SUCCESSFUL tras los fixes.
- [x] Emulador externo no probado; no aplica a 3.4.

### Fase C — QA Ponytail
- [x] Revisión inicial encontró dos defectos reproducibles; Coder corrigió ambos y Tester confirmó regresiones GREEN.
- [x] Revisión final de protocolo, errores, concurrencia, cancelación, cobertura y KISS/YAGNI: APROBADO.
- [x] Confirmación del usuario recibida para continuar a 3.5 tras prueba TCP satisfactoria.

## Etapa 3.5 — Preparación Android y validación con emulador

### Fase A — Coder
- [x] Manifest declara INTERNET y ACCESS_LOCAL_NETWORK tras verificar documentación oficial para targetSdk 37; solicitud runtime se reserva a fase 4.
- [x] Guía actualizada con NO DATA como Failure, RPM Int, protocolo soportado y distinción API36/API37.
- [x] Evidencia TCP host y dispositivo: `192.168.1.122:35000`; SM_A066M / Android 16 (API36), ambos conectan.

### Fase B — Tester
- [x] Test instrumentado desde proceso de app valida handshake, estado Connected, lecturas de métricas y DTCs.
- [x] Métricas observadas variables; corrida final RPM=3442, temperatura=36 °C, velocidad=39 km/h; DTCs P0143/P0196/P0234.
- [x] No se ejecutaron 04/07/0A ni se modificó el servidor remoto; `connectedDebugAndroidTest` dirigido BUILD SUCCESSFUL.
- [x] Corrida inicial con valores métricos fijos falló debido a valores variables del simulador; test final solo comprueba disponibilidad/rangos y DTCs.

### Fase C — QA Ponytail
- [x] Revisión de manifest, guía, test instrumentado, permisos y evidencia: APROBADO.
- [x] Registrar limitación: ACCESS_LOCAL_NETWORK runtime/API37 no probado; solicitud queda para fase 4.
- [x] Confirmación del usuario recibida para ejecutar cierre 3.6.

## Etapa 3.6 — Regresión, QA global y cierre

### Fase A — Coder
- [x] Sin cambios de producción previstos; subsistema completo en etapas 3.1–3.5.

### Fase B — Tester
- [x] `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL (26 s).
- [x] `./gradlew :app:connectedDebugAndroidTest` — BUILD SUCCESSFUL (16 s), SM_A066M Android 16/API36: 10 instrumentadas pasaron; Elm327RemoteIntegrationTest omitida sin `elm327Host` (ya pasó targeted en 3.5).
- [x] `./gradlew :app:lintDebug :app:assembleDebug` — ambos BUILD SUCCESSFUL (40 s).
- [x] Registrar ejecución remota targeted de 3.5 y limitación: API37 runtime permission no probado.

### Fase C — QA Ponytail
- [x] QA global revisó plan 3.1–3.6, cobertura, permisos, errores, cancelación, null/0 y comandos de solo lectura: APROBADO.
- [x] Sin hallazgos pendientes.
- [x] Checklist finalizado y reporte `plans/reporte_fase3.md` generado.
- [x] Detenerse y consultar siguiente paso.

## Estado Fase 3

**Completada.** Pruebas JVM, instrumentadas, lint, assemble, validación remota dirigida y QA global aprobados. Límites pendientes: permiso runtime API37 y adaptador físico.

## Etapa 3.1 — Parser

Coder, Tester y QA completados; suites dirigidas y JVM pasan. QA APROBADO. Emulador no aplica.

## Etapa 3.2 — Intérprete de PIDs

### Fase A — Coder
- [x] Crear `ObdPidInterpreter.kt` puro reutilizando el parser existente.
- [x] Interpretar RPM `((A * 256) + B) / 4` como Int, temperatura `A - 40`, velocidad `A`.
- [x] Validar longitud exacta y bytes en rango; conservar null distinto de cero.
- [x] Sin sockets, cambios de Domain ni dependencias.

### Fase B — Tester
- [x] RPM 1303 para bytes `14 5F` y cero; temperaturas -40/215; velocidades 0/255.
- [x] Payloads null, truncados, extra y bytes fuera de rango devuelven null.
- [x] `./gradlew :app:testDebugUnitTest --tests 'cl.cristian.bitacoraobd.data.obd.parser.ObdPidInterpreterTest'` y `./gradlew :app:testDebugUnitTest`: ambos BUILD SUCCESSFUL.
- [x] Emulador no aplica a esta etapa; no se ejecutó conectividad.

### Fase C — QA Ponytail
- [x] Revisión de fórmulas, límites, null versus cero, alcance y KISS/YAGNI: APROBADO, sin hallazgos.
- [x] Detenerse a esperar confirmación antes de 3.3.

No generar reporte_fase3 hasta completar toda Fase 3.

## Fase A — Implementación de Código (Coder)
- [x] Implementar parser puro para respuestas OBD, sin transporte ni cambios de Domain.
- [x] Validar respuestas 41 + PID esperado y 43 para Modo 03; manejar errores/protocolo desconocido como fallo.

## Fase B — Tests TDD (Tester)
- [x] Probar fixture DTC 43 01 43 01 96 02 34 → P0143/P0196/P0234 y limpio explícito 43 00 00; `43` sin payload falla.
- [x] Probar eco exacto, CR/LF, espacios, prompt, SEARCHING..., hexadecimal inválido, tramas incompletas y NO DATA como Failure.
- [x] `./gradlew :app:testDebugUnitTest --tests 'cl.cristian.bitacoraobd.data.obd.parser.ObdResponseParserTest'` y `./gradlew :app:testDebugUnitTest`: ambos BUILD SUCCESSFUL.
- [x] Comunicación con emulador no aplica a esta etapa; no se intentó. No corresponde informar indisponibilidad en esta etapa.

## Fase C — Code Review & QA (Ponytail Reviewer)
- [x] QA Ponytail re-revisó respuesta `43` vacía y cobertura: APROBADO, sin hallazgos bloqueantes.
- [ ] Registrar veredicto QA y resultados de pruebas en reporte_fase3.md tras completar toda fase 3.
- [x] Detenerse y esperar confirmación del usuario antes de etapa 3.2.

---

## Fase A — Creación (Coder documental)

- [x] Registrar plan aprobado preservando los antecedentes de Fase 2.
- [ ] Inspeccionar tablero y crear contexto, leyenda y mapa general mediante MCP.
- [ ] Crear F01 primer vehículo y F02 gestión de vehículos.
- [ ] Crear F03 conexión/métricas y F04 diagnóstico pendiente.
- [ ] Crear F05 historial y F06 mantenimiento.
- [ ] Añadir decisiones abiertas y diferenciar base implementada, requisito pendiente y propuesta UX.

## Fase B — Validación documental (Tester)

- [ ] Verificar los seis recorridos y sus conectores, cancelaciones y errores.
- [ ] Comprobar captura original, guardado/descarte, limpio/error, recurrencia y mantenimiento histórico.
- [ ] Comprobar estructura editable y revisión visual MCP, declarando límites.
- [ ] Confirmar que no se modificaron código, designplans, Make ni wireframes.
- [ ] Registrar que pruebas Android/hardware no aplican a esta intervención documental.

## Fase C — QA (Ponytail Reviewer)

- [ ] Revisar alcance, legibilidad y coherencia con el plan aprobado.
- [ ] Emitir veredicto QA y resolver hallazgos.
- [ ] Generar reporte_flujos_exploratorios_v1.md y finalizar este checklist.
- [ ] Entregar enlace al usuario; mantener aprobación visual/UX pendiente.

---

# Fase 4 — Etapa 4.7: UI-05 Historial y UI-06 Mantenimiento

Plan aprobado por el usuario; selector de ocho tipos confirmado y entrega estática preferida. Figma no se modifica. Ver [plan de implementación](designplans/implementation_plan_fase4_etapa4_7.md).

## Fase A — Implementación de Código (Coder)
- [x] Implementar historial, detalle y navegación por vehículo, reutilizando repositorios/casos de uso.
- [x] Implementar historial, alta/edición y confirmación histórica de mantenimiento con borrador seguro.
- [x] Integrar casos de uso y ViewModels en la composición actual; preservar cambios preexistentes.
- [x] Mantener estado de error/reintento, aislamiento de ID/vehículo, unidades canónicas y accesibilidad.
- [x] Sin dependencias nuevas ni cambios de contratos o esquema Room.
- [x] Corregir respuestas tardías de add/edit al salir o cambiar de vehículo; proteger navegación mientras se carga el formulario.
- [x] Refrescar estados de mantenimiento al reabrir la pantalla manteniendo observación reactiva de Room.

## Fase B — Tests TDD (Tester; automatizada completada)
- [x] JVM existente: HistoryViewModel cubre sesiones limpias, cambio de vehículo, error/reintento, ownership del detalle, orientación genérica y recurrencia; MaintenanceViewModel cubre ocho tipos, estado vigente/OTHER, edición con mismo ID y kilometraje canónico, validaciones, confirmación histórica, error/reintento, cancelación y salida encolada.
- [x] Regresiones deterministas QA: Deferred controla carga de ID/preferencias; requestLeave y cambio a vehículo B descartan respuestas tardías, sin draft de A ni posibilidad de guardarlo desde B. UPCOMING→OVERDUE al cambiar odómetro sin nueva emisión Room y reingresar.
- [x] Room existente: 15 pruebas instrumentadas cubren aislamiento/orden, edición y recomputación, OTHER, odómetro, transacción y rollback.
- [x] Prueba dirigida MaintenanceViewModel, `:app:testDebugUnitTest --rerun-tasks` y `:app:compileDebugAndroidTestKotlin`: BUILD SUCCESSFUL.
- [x] `:app:connectedDebugAndroidTest --rerun-tasks`: BUILD SUCCESSFUL en SM-A066M / Android 16 (R7AY40TDB1T), 26 pruebas, 0 fallos, 1 omitida (`Elm327RemoteIntegrationTest`, sin `elm327Host`).
- [x] Verificación final del coordinador tras QA: JVM 128 pruebas/0 fallos; AndroidTest compilado con `--rerun-tasks` (26 tareas ejecutadas); instrumentadas 26/0 fallos/1 omitida; `lintDebug --rerun-tasks` y `assembleDebug --rerun-tasks` ejecutados; `git diff --check` sin errores.
- [ ] Validación manual en claro/oscuro, accesibilidad, formularios Compose y recorridos visuales no ejecutada; automatización no la sustituye.

## Fase C — QA / Ponytail Reviewer (APROBADO)
- [x] Revisión inicial encontró dos riesgos de datos en estado asíncrono de mantenimiento; ambos se corrigieron.
- [x] QA re-revisó código conectado y tres regresiones deterministas; no encontró hallazgos nuevos. Veredicto: **APROBADO**.
- [x] Checklist de implementación/QA finalizado; la comprobación manual visual/accesibilidad queda pendiente y declarada como límite.
- [x] Reporte formal generado: [reporte_fase4_etapa4_7.md](reporte_fase4_etapa4_7.md).



## Fase 4 — Etapa 4.8: Inicio y Ajustes (UI-01/07; CERRADA)
Plan aprobado por el usuario: [implementation_plan_fase4_etapa4_8.md](designplans/implementation_plan_fase4_etapa4_8.md). Cierre QA/Ponytail **APROBADO**; [reporte de cierre](reporte_fase4_etapa4_8.md).

### Fase A — Implementación de Código (Coder; COMPLETADA)
- [x] Inicio y Ajustes integrados con preferencias actuales, navegación existente y unidades solo de representación.
- [x] Activación de Inicio coordinada por destino HOME + Lifecycle RESUMED; se retiró el propietario duplicado de MainActivity.
- [x] Carrera de salida durante guardado de Ajustes corregida: navegación espera éxito; el error conserva el borrador y permite reintentar.
- [x] Revisión de callers, datos y cambios conectados; sin ampliación de Domain/Data, esquema o dependencias.

### Fase B — Tests TDD (Tester; COMPLETADA)
- [x] JVM: 137 pruebas, 0 fallos/omisiones; incluye guardado fallido→reintento→ruta, error de lectura de Ajustes, actualización fallida del mismo contexto y cambio de vehículo.
- [x] AndroidTest compilado; instrumentadas SM-A066M/API 36: 27 pruebas, 0 fallos y 1 omitida por falta de elm327Host.
- [x] HomeTabReactivationTest integra AppNavigation con Activity RESUMED y reloj mutable: fecha sin navegación no recalcula Inicio; volver Vehículos→Inicio sí recalcula.
- [x] :app:lintDebug, :app:assembleDebug y git diff --check pasan.

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)
- [x] Revisión de navegación, guardado, borrador, aislamiento por vehículo y KISS/YAGNI; sin hallazgos bloqueantes.
- [x] La cobertura integrada cerró el blocker HOME+RESUMED/retorno sin seam de producción ni dependencia adicional.
- [x] Reporte formal generado; se mantienen explícitos los límites visuales/manuales y la omisión ELM327.

---

## Fidelidad visual del prototipo Figma Make (CERRADA; implementación aprobada)

Plan aprobado por el usuario: Material 3 solamente, sin Material Symbols. Se autorizó una excepción acotada de tres Coders paralelos en paquetes de archivos excluyentes tras integrar la base compartida.

### Fase A — Coder (COMPLETADA)
- [x] Tema/tokens del prototipo y cuatro ImageVector locales conectados a Material 3; sin dependencia nueva.
- [x] Inicio, Vehículos/formulario, Diagnóstico, Conexión, Mantenimiento/lista/formulario, Historial/detalle y Ajustes recibieron la implementación visual.
- [x] Se respetaron rutas, ViewModels, datos, validación, borradores, callbacks y estados funcionales; no hubo cambios intencionales a contratos, Domain/Data/Room ni dependencias.
- [x] Fix de Mantenimiento sin vehículo: padding 16 dp y título headlineSmall, preservando su condición, texto y navegación.
- [x] Fix de visibilidad de Inicio: la tarjeta OBD se oculta si RPM, refrigerante y velocidad están desactivados; la conexión mantiene su propia tarjeta.

### Fase B — Tester (COMPLETADA)
- [x] JVM: 137 pruebas, 0 fallos y 0 omitidas.
- [x] AndroidTest en SM-A066M/API 36: 29 pruebas, 0 fallos y 1 omitida: Elm327RemoteIntegrationTest, por ausencia de elm327Host.
- [x] HomeTabReactivationTest mantiene la regresión de recálculo Próximo→Vencido con aserciones para los nodos de texto separados.
- [x] HomeMetricsVisibilityTest cubre tres flags desactivados (tarjeta ausente) y RPM habilitado (tarjeta visible).
- [x] compileDebugAndroidTestKotlin, connectedDebugAndroidTest, lintDebug, assembleDebug y git diff --check pasaron tras los cambios finales.
- [x] Inspección manual en 720×1600: Inicio, Vehículos, Diagnóstico, Mantenimiento vacío y Ajustes; fix de padding confirmado. No se alteraron datos persistentes.
- [x] Límites documentados: Historial, detalle y Conexión contextual no se inspeccionaron manualmente porque el dispositivo no tenía vehículo; tampoco se hizo revisión manual de ambos temas, TalkBack, foco ni texto ampliado en todos los estados.

### Fase C — QA/Ponytail (APROBADA)
- [x] Revisión conectada: visibilidad de métricas y estado vacío de Mantenimiento; no se hallaron otros defectos bloqueantes.
- [x] Sin Material Symbols/dependencias nuevas ni expansión de funciones fuera del prototipo.
- [x] Cierre y reporte formal: plans/reporte_fidelidad_prototipo.md.

---

## Seguimiento visual Material 3: paleta grafito y acciones (CERRADA; QA APROBADO)

Extensión del plan aprobada por el usuario con “Proceed” el 2026-11-11. La implementación de fidelidad del prototipo ya cerrada se conserva; este checklist cubre el cambio visual posterior de §11.

### Fase A — Implementación de Código (Coder; COMPLETADA)
- [x] A1. Actualizar tokens de tema claro/oscuro y tipografía según §11; mantener tarjetas grafito y color de contenido legible en ambos temas.
- [x] A2. Añadir `GraphiteCard` basado en Material 3 OutlinedCard y migrar las tarjetas de las siete áreas UI sin cambiar callbacks, estado ni semántica.
- [x] A3. Aplicar verde a acciones operativas positivas, rojo Outlined/Text a Cancelar y mayor énfasis rojo solo a las acciones destructivas existentes; acciones Outlined sobre tarjetas y campos de formularios usan pares legibles. Volver y navegación sin cancelar permanecen neutrales.
- [x] A4. Cambiar el encabezado “Vehículo” de Ajustes a “Nombre” y añadir el contorno perimetral fino de la barra inferior, manteniendo color e indicador seleccionado.
- [x] A5. Sincronizar design-system.md y design-decisions.md con los tokens aprobados; no modificar Figma, Domain/Data, esquema ni dependencias.

### Fase B — Tests TDD (Tester; COMPLETADA)
- [x] B1. Compose: `settingsNamesTheSectionNombreWithoutVehiculo`, `graphiteCardUsesFixedFillAndOutlineInBothThemes`, `unselectedMillasTextContrastsWithGraphiteInBothThemes`, `vehicleFormUsesGreenSaveAndRedCancelar`, `maintenanceFieldLabelContrastsWithGraphiteInBothThemes`; acciones inspeccionadas dentro de sus límites semánticos. La prueba existente `HomeTabReactivationTest` conserva la comprobación de destino seleccionado.
- [x] B2. Secuencia completa en orden: `:app:testDebugUnitTest` (137 sin fallos), `:app:compileDebugAndroidTestKotlin`, `:app:connectedDebugAndroidTest` (34 casos, 0 fallos, 1 omitido), `:app:lintDebug`, `:app:assembleDebug` y `git diff --check`; SM-A066M/API 36 (Android 16). La omisión es ELM327 remoto por falta de `elm327Host`. La compilación informó una advertencia deprecada para `Modifier.menuAnchor()` en Mantenimiento; lint pasó.
- [x] B3. Capturas manuales de Inicio/Ajustes en claro, enfoque del campo Nombre e insets; pruebas de píxeles de GraphiteCard, “Millas” y campo de Mantenimiento verifican contraste ≥4,5:1 en ambos temas. La captura completa de Ajustes oscuro no fue concluyente. Con escala de fuente 1.3, “Mantenimiento” se parte en dos líneas en la navegación inferior; se restauró 1.0. TalkBack no se verificó manualmente. Sin vehículo disponible, no se inspeccionaron Mantenimiento con formulario, Historial/detalle ni Conexión contextual.

### Fase C — Code Review & QA (Ponytail Reviewer; APROBADA)
- [x] C1. Revisión conectada encontró campos de Mantenimiento y acciones Outlined de bajo contraste sobre tarjetas; se corrigieron los campos y controles afectados, y la segunda revisión emitió APROBADO.
- [x] C2. Confirmado que no se cambiaron contratos, persistencia, interacciones funcionales ni datos; sin dependencias nuevas, Domain/Data ni esquema Room.
- [x] C3. Reporte formal de seguimiento actualizado en `reporte_fidelidad_prototipo.md`; límites y hallazgo de escala de fuente documentados.

---

## Fase 4.9 — Integración, validación y cierre (EN CURSO)

Plan aprobado: [implementation_plan_fase4_etapa4_9.md](designplans/implementation_plan_fase4_etapa4_9.md).

### Fase A — Coder
- [x] Auditoría de cobertura: migración v1 por builder real, captura pendiente/recreación y simulación OBD JVM existentes; no se detectó defecto reproducido que requiera cambios.
- [x] QA detectó contraste insuficiente en «Abrir ajustes de la aplicación» tras denegar LAN; Coder aplicó `PrototypeCardContent` en ConnectionScreen sin cambiar el Intent. `:app:compileDebugKotlin` y `:app:lintDebug` pasaron después del cambio.
- [ ] Verificar la corrección en dispositivo y comprobar recreación durante conexión/lectura; resolver brechas demostradas durante validación integrada.

### Fase B — Tester
- [ ] Suite completa pendiente: `:app:testDebugUnitTest --rerun-tasks` 137/137, compilación AndroidTest exitosa; `:app:connectedDebugAndroidTest` en Pixel_10/API 37 agotó 300 s con ANR de System UI, sin resultado de suite. Lint y assemble exitosos con mayoría de tareas UP-TO-DATE; `git diff --check` exitoso.
- [ ] Validar migración instrumentada, OBD remoto, permiso API 37 y regresión API 36. El fixture OBD existente solo corre en JVM; falta host compatible para `elm327Host`.
- [ ] Inspeccionar recorridos, recreación, accesibilidad, temas, texto ampliado, teclado y ventanas: AVD dejó de aceptar adb; API 36 no disponible.

### Fase C — QA/Ponytail
- [ ] Revisar evidencia, código conectado y límites; resolver hallazgos.
- [ ] Reconciliar plan general, completar checklist y generar reporte global solo al cumplir los criterios de cierre.
