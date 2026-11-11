# Fase 4 — Plan técnico de implementación Android

> Para agentes: ejecutar por etapas con `executing-plans` y el protocolo secuencial de AGENTS.md: Coder → Tester → QA/Ponytail. **Plan general aprobado y en ejecución por etapas.** Estado vigente al preparar 4.5: 4.1–4.4 cerradas según sus reportes; [plan específico 4.5](implementation_plan_fase4_etapa4_5.md) propuesto y pendiente de aprobación explícita antes de codificar. Las casillas y propuestas inferiores de 4.3/4.4 conservan su historia; tema/tokens/validación transferidos a 4.5. Mantener `plans/task.md` con Fases A/B/C y preservar historia. La aprobación no sustituye las confirmaciones visuales ni autoriza modificaciones de Figma sin autorización aplicable.

**Objetivo:** implementar el MVP Android por paquetes trazables, iniciando por los cambios y deudas Domain/Data necesarios para la UI; después integrar Presentation, navegación y Compose con referencias aprobadas.

**Arquitectura:** una app offline-first; Presentation coordina estados y eventos, Domain conserva reglas y contratos y Data implementa persistencia y OBD. Reutilizar Room, DataStore, gateway, casos de uso y Material 3 existentes. KISS, YAGNI y SOLID: dependencias por constructor, responsabilidades claras, sin capas, patrones o librerías especulativas.

**Tecnología actual:** Kotlin, coroutines/Flow, Room, DataStore, Compose y Material 3; minSdk 26, compile/target 37 según `app/build.gradle.kts`. Verificar configuración y documentación vigentes al ejecutar; no actualizar versiones por novedad.

## 0. Alcance, SSOT y reglas de ejecución

**Precedencia:** el código Kotlin y sus tests son fuente de verdad de lo efectivamente implementado; los requisitos y decisiones aprobadas definen lo que debe ocurrir. Una discrepancia genera una brecha, no sobrescritura silenciosa.

| Fuente de verdad por tema | Archivo |
|---|---|
| Requisitos y alcance MVP | [Contexto maestro](../Contexto_Maestro_App_OBDII_v2.md), con decisiones posteriores documentadas |
| Decisiones y aprobación funcional | [design-decisions.md](design-decisions.md) y confirmaciones posteriores expresas |
| Pantallas, estados y criterios | [ui-ux-specification.md](ui-ux-specification.md) |
| Navegación y recorridos | [screen-flows.md](screen-flows.md) |
| Contratos existentes y brechas | Kotlin bajo `app/src/main/java/cl/cristian/bitacoraobd/domain/` y `data/`; índice [domain-contracts.md](domain-contracts.md) se mantiene sincronizado |
| Paquetes y dependencias | [design-handoff.md](design-handoff.md), [screen-dependencies.md](screen-dependencies.md) |
| Evidencia y aprobación visual | [figma-references.md](figma-references.md), Figma aprobado y evidencia del prototipo revisado |
| Tokens aprobados | [design-system.md](design-system.md); tema Kotlin describe implementación real, no aprobación visual |
| Seguimiento de ejecución | [task.md](../task.md), después de aprobar este plan; reporte por etapa en `plans/reporte_fase4_etapaX.md` |

**Puertas obligatorias:**

1. Tras aprobar el plan, crear/actualizar el checklist vivo en `plans/task.md` con Fase A Coder, B Tester y C QA para cada etapa. El Coder implementa solo lo aprobado y no ejecuta pruebas; Tester escribe/adapta pruebas y ejecuta la suite; QA aplica `ponytail-review`, verifica plan y KISS/YAGNI. Si una suite no puede ejecutarse, documentar bloqueo y no afirmar 100 % PASS. Resolver observaciones antes del cierre de etapa y registrar reporte. Pedir indicación para continuar tras cada cierre conforme AGENTS.md.
2. El tester prepara pruebas significativas de comportamiento; respetar el orden Coder → Tester → QA exigido por el repositorio. Para regresiones críticas acordar antes con Coder los casos observables; Tester verifica que cubren el defecto y la solución, sin afirmar ejecución roja previa si no ocurrió.
3. Antes de codificar cada paquete Compose, comprobar en handoff/referencias `DESIGN_READY`, estados y versión aprobada. Si no existe, **preguntar al usuario por la decisión o confirmación visual concreta**, presentar referencias y efectos, detener ese paquete y continuar trabajo independiente autorizado. La aprobación de wireframes UI-01–07 de 2026-11-09 se conserva; no solicitarla otra vez para el mismo contenido. Prototipo nuevo y variantes posteriores requieren su revisión, no se presumen aprobados.
4. Cuando un cambio de backend afecte datos visibles, estados, errores, navegación o alcance, documentar la diferencia y presentar opciones e impacto al usuario antes de cambiar reglas de diseño o comportamiento; actualizar decisiones/especificación/contratos solo tras confirmación. No modificar Figma sin autorización aplicable.
5. Antes de cada etapa, releer código y consumidores, revisar estado del handoff y confirmar que la aprobación del plan cubre los contratos concretos; si se descubre una modificación de Domain distinta, solicitar autorización específica. No marcar `DONE` sin validación.
6. Herramientas `studio_` para workspace y comandos; ejecutar Gradle y dispositivo secuencialmente. No introducir control ni botón «Demo» en la aplicación. Prototipo Make es referencia, no código Android.
7. El trabajo técnico necesario para la UI —incluida deuda que se descubra después— se aborda al inicio de fase 4, antes del paquete Compose dependiente. Diseñar en paralelo no convierte una dependencia técnica en completada.

**Reglas funcionales que no se reabren:** F03 inicia y termina en Diagnóstico; lectura explícita, no sondeo; captura íntegra, fallo sin historial; sin temperatura de admisión ni odómetro ECU MVP; vacío de vehículos también tras borrar el último; siete tipos alertables y OTHER; Room/DataStore no comparten transacción; conservar aprobación de wireframes previa.

## 1. Mapa de archivos y dependencias

Rutas bajo `app/src/main/java/cl/cristian/bitacoraobd/` salvo indicación. Los nombres nuevos de Presentation son propuestos, deben contrastarse con el proyecto antes de crear archivos; no crear componentes vacíos por anticipación.

| Etapa | Archivos principales previstos | Responsabilidad |
|---|---|---|
| 4.1 Idempotencia | `domain/model/DiagnosticScanResult.kt`, `DiagnosticSession.kt`; `domain/usecase/ScanDiagnosticCodesUseCase.kt`, `SaveDiagnosticSessionUseCase.kt`; `domain/repository/DiagnosticRepository.kt`; `data/repository/RoomDiagnosticRepository.kt`; `data/local/entity/Entities.kt`, mappers, `dao/DiagnosticDao.kt`, `database/AppDatabase.kt`, `app/schemas/` | Identidad por captura y transacción idempotente, migración no destructiva |
| 4.2 Preferencias y guardias | `domain/repository/AppPreferencesRepository.kt`, `data/local/datastore/DataStoreAppPreferencesRepository.kt`, `domain/usecase/DeleteVehicleUseCase.kt`; coordinación mínima en Presentation cuando se introduzca | Defaults, persistencia y errores; preservar límites de transacción |
| 4.3 Composición/estado | `MainActivity.kt`, `data/local/database/AppDatabase.kt`, `ui/` nuevo punto de composición y ViewModels mínimos, `app/build.gradle.kts`, `gradle/libs.versions.toml` solo si necesario | Construcción única Room/DataStore/gateway, ciclo de vida y navegación |
| 4.4 Tema y componentes | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`; `ui/` componentes compartidos solo cuando dos usos lo justifiquen | Esquema aprobado, Material 3, accesibilidad |
| 4.5–4.8 UI | `ui/` por paquete Vehículos, Conexión/Diagnóstico, Historial/Mantenimiento, Inicio/Ajustes; `MainActivity.kt` navegación | Estados/eventos y representación de cada contrato |
| Pruebas | `app/src/test/java/cl/cristian/bitacoraobd/`; `app/src/androidTest/java/cl/cristian/bitacoraobd/`; `app/schemas/` | JVM, Room/migración, DataStore, Compose, permisos y recreación |

## 2. Consultas y aprobaciones antes o durante ejecución

| Tema | Hecho / decisión existente | Pregunta que el agente debe presentar cuando proceda | Quién y cuándo |
|---|---|---|---|
| Idempotencia | Identificador estable aceptado; no recuperación tras muerte del proceso | Si una misma clave llega con contenido distinto, proponer fallo explícito sin sobrescribir; consultar solo si se pretende otra experiencia o cambio funcional. Aclarar mensaje para resultado incierto si el diseño aún no lo representa. | Android + diseño, antes de UI-04; solución técnica en 4.1 |
| Room/DataStore | No hay transacción común; selección o borrado puede fallar parcialmente | Si el prototipo no comunica qué se guardó o cómo recuperar, mostrar caso y opciones al usuario sin suponer atomicidad. | Diseño/Android antes de UI-02 |
| Preferencias | DD-36/37 aprobadas; falta API y persistencia | Ante un resultado parcial incierto, confirmar texto y recuperación solo si la variante vigente no los especifica; no volver a preguntar por guardado explícito o redondeo. | Diseño/Android antes de UI-07 |
| Permiso LAN | DD-34 define reintento/ajustes según plataforma | Si API y prototipo difieren, presentar límites de la versión y propuesta textual sin inventar comportamiento del sistema. | Android/diseño antes de UI-03 |
| Tema | Wireframes aprobados, tokens de producto no aprobados; tema actual usa color dinámico por defecto | Elegir alternativa 4.4, tokens y comportamiento claro/oscuro/dinámico; presentar ejemplos de Inicio, formulario y estados. | Usuario antes de integrar tema visual |
| Barras y selector | Barras inferiores observadas partidas/pequeñas; selector completo no representado | Presentar corrección concreta y solicitar confirmación del diseño afectado; siete tipos + OTHER no se renegocian. | Usuario/diseño antes de UI-01/02/06 |
| Estados y accesibilidad | Interacción y accesibilidad de prototipo aún no acreditadas | Para cada paquete no DESIGN_READY, pedir revisión de variantes nuevas y comportamiento faltante, sin repetir aprobación previa. | Usuario/diseño antes de Compose de ese paquete |

## 3. Etapa 4.1 — Backend: captura idempotente (prioridad 1)

**Cerrada:** 4.1 implementó UUID estable por captura, índice UNIQUE, guardado idempotente transaccional y migración aditiva v1→v2. Instrumentadas en Pixel_10 AVD/API 37: 19 tests, 0 fallos, 1 omitida por elm327Host no configurado. QA APROBADO; evidencia en `plans/reporte_fase4_etapa4_1.md`. El registro de migración en builder de producción sigue en 4.3.

## 4. Etapa 4.2 — Backend: preferencias y guardias (CERRADA)

`AppPreferencesRepository`/DataStore implementan AppSettings con defaults aprobados; conservan las claves previas. QA APROBADO. JVM incluye fallos inyectados de lectura/escritura; instrumentadas DataStore, lint, assemble y compilación AndroidTest pasan. Evidencia en `plans/reporte_fase4_etapa4_2.md`.

Se conserva la guardia backend existente de borrado Connected. La coordinación transitoria/captura pendiente se implementa en Presentation con el propietario compartido aprobado en 4.3; no existe atomicidad Room/DataStore. Al activar Inicio, Presentation debe iniciar/recolectar su Flow para recalcular con fecha actual, sin temporizador. Conversión de millas y protección de campos no editados se validan en etapas UI.

## 5. Etapa 4.3 — Composición, ciclo de vida y navegación

- [x] Coder: construir Room con migración explícita y una instancia compartida de DataStore, repositorios y gateway por proceso; inyectar por constructor. Integrar Activity, ViewModel y navegación de cuatro destinos con Ajustes/Historial secundarios y F03 desde/hacia Diagnóstico. Usar capacidades ya instaladas; si falta navegación/ViewModel adecuada, justificar y pedir aprobación de dependencia concreta en vez de inventar una plataforma propia.
- [x] Coder: mantener operaciones y captura en propietario de ciclo de vida adecuado; una recreación con proceso vivo no duplica conexión, lectura ni guardado. Guardar solo estado serializable apropiado, nunca socket; muerte de proceso no promete recuperar captura.
- [x] Tester: recreación durante operación, cancelación y navegación, cambio de vehículo sin mostrar datos anteriores, desconexión y retorno, sin operaciones duplicadas. Probar con fakes limitados y una prueba de integración donde aporte evidencia.
- [x] QA: revisar responsabilidades, cancelación, liberación de recursos y ausencia de dependencia innecesaria. La estructura no convierte pantallas en DESIGN_READY.

## 6. Etapa 4.4 — Referencias y decisiones visuales Material 3

**Referencias investigadas, no plantillas de producto aprobadas ni dependencias:**

- [JetNews, ejemplo oficial Compose](https://github.com/android/compose-samples/tree/main/JetNews): referencia de organización de contenido, temas claro/oscuro y componentes; no es una app de diagnóstico.
- [Reply, ejemplo oficial Compose](https://github.com/android/compose-samples/tree/main/Reply): referencia de Material 3, navegación y estados adaptables; no es una plantilla OBD.
- [Codelab oficial de tema Material 3](https://developer.android.com/codelabs/jetpack-compose-theming): roles, componentes y esquema oscuro aplicables a Compose.
- [Material 3: roles de color](https://m3.material.io/m3/pages/color-roles?s=m3): pares de roles y restricciones de contraste.
- [AAOS Monitor](https://github.com/KukhtoSlava/aaos-monitor): ejemplo de dashboard vehicular Compose/M3 para Android Automotive, inspiración de lectura de métricas; no es plantilla para teléfono ni se propone copiarlo. Revisar licencia antes de reutilizar código o recursos.
- [OBD2 Dashboard](https://github.com/mguyco/obd2-dashboard): por decisión del usuario se toma como **referencia cromática, no como diseño a reproducir**: tonos oscuros, verdes y blancos, con alertas en rojo. Sus funciones Bluetooth y demás capacidades fuera del MVP no se incorporan. No se han verificado valores exactos ni se propone copiar código o recursos; revisar licencia antes de reutilizar.
- [OBD Scanner Android](https://github.com/ETSoftwareStudio/obd-scanner-android): muestra Compose/M3 de conexión y diagnóstico vehicular; es referencia funcional y de componentes, no una plantilla visual oscura validada. Su Bluetooth, sondeo, borrado DTC y dependencias no forman parte del alcance. Revisar licencia antes de reutilizar.

**Dirección cromática acordada:** inspirarse en OBD2 Dashboard con tonos oscuros, verdes, blancos y alertas rojas. Los valores exactos, contraste, aplicación a cada estado, reglas de color dinámico y propuesta visual final requieren revisión y aprobación; esta referencia no aprueba la apariencia de las pantallas ni DESIGN_READY.

**Variantes a presentar al usuario sobre los mismos wireframes y dentro de esa dirección cromática:**

A. **Grafito + semáforo semántico:** superficies oscuras neutras, texto claro; verde para estado favorable, ámbar para atención y rojo para error/vencido, siempre con texto/icono. No asignar gravedad mecánica a DTC ni inventar estados del dominio.
B. **Verde, blanco y negro:** superficies grafito/negro, texto blanco roto, verde como acento de marca y acciones; advertencias y errores usan roles semánticos distinguibles y etiquetas, no solo verde.
Ambas son propuestas, no colores, licencia ni componentes aprobados. No exigir compra ni añadir librería por una plantilla.

- [x] Diseño/agente: presentar muestras comparables de Inicio, formulario, botón primario/secundario y estados normal/advertencia/error/deshabilitado para A y B, con referencias y valores de color propuestos; consultar elección y alcance claro/oscuro, color dinámico frente a identidad fija. El tema actual usa `dynamicColor=true` en Android 12+, por lo que una paleta fija debe acordarse expresamente.
- [x] Comprobar pares texto/fondo, controles, foco y estados con herramientas de contraste; etiquetar con texto y forma, verificar tamaños táctiles, texto ampliado y navegación. No atribuir accesibilidad automática a una paleta o a Material 3.
- [x] Registrar aprobación, tokens y fuentes en design-decisions/design-system y referencias; no cambiar diseños aprobados sin autorización. Si el usuario no aprueba, detener solo esta etapa visual y avanzar backend independiente.
- [x] Tras aprobación, Coder adapta Color/Theme/Type y utiliza botones, tarjetas, diálogos, barras y formularios Material 3; no abstraer componentes de uso único. Tester verifica contraste/semántica y previews/estado visual; QA compara referencias aprobadas.

## 7. Etapa 4.5 — Vehículos y formularios (UI-02)

**Puerta:** UI-02 DESIGN_READY y dependencias 4.2–4.4 pertinentes aprobadas. Archivos `ui/vehicles/` propuestos y `VehicleRepository`, `DeleteVehicleUseCase`, preferencias existentes.

- [x] Coder: lista con selección por fila, alta/edición, año 0 como ausencia, eliminación confirmada/cascada y errores de carga; borrador y descarte. Guardar vehículo y selección por separado; reintentar selección con ID existente.
- [x] Tester: primer vehículo y fallo parcial sin duplicado, adicional conserva activo, Connected/transitorios y captura pendiente bloquean según reglas, error borrado consulta estado real, último eliminado lleva a vacío, cambio de contexto, rotación y accesibilidad.
- [x] QA: contrastar UI-02 con referencias y DD-24/27/28/30/40/42; evidenciar estados y guardias.

## 8. Etapa 4.6 — Conexión y diagnóstico (UI-03/04)

**Puerta:** implementación 4.1 disponible y validada con `:app:connectedDebugAndroidTest` en Pixel_10 AVD/API 37 (19 pruebas, 0 fallos, 1 integración remota omitida por falta de `elm327Host`; ver reporte 4.1). La prueba omitida no bloquea UI. Composición, referencias y aprobación UI-03/04 siguen siendo puertas visuales. Archivos `ui/connection/`, `ui/diagnostic/` propuestos; `AndroidManifest.xml`, gateway y casos de uso existentes.

- [x] Coder: aviso Wi-Fi, permiso LAN según plataforma, validar IP/puerto antes de socket, conectar explícitamente y persistir config tras handshake. Fallo de persistencia reintenta solo guardado; cancelación y desconexión muestran estado real; no reconectar ni leer automáticamente.
- [x] Coder: ejecutar escaneo bajo demanda, conservar captura íntegra y su identidad; guardar/reintentar/descartar/seguir, resolver pendiente antes de salida, cambio de vehículo o desconexión. Mostrar DTC reales/desconocidos sin causas inventadas; null distinto de 0.
- [x] Tester: entrada inválida sin socket, permiso denegado/revocado y retorno de ajustes, conexión cancelada, error TCP, handshake/guardado parcial, doble guardar/reintento incierto, lectura fallida no persiste, limpia, código desconocido, rotación sin duplicados.
- [x] QA: F03 salidas finales Diagnóstico; validar permiso en API 37 y regresión API 36 cuando exista entorno; no declarar hardware probado sin dispositivo.

## 9. Etapa 4.7 — Historial y mantenimiento (UI-05/06)

**Puerta:** UI-05/06 DESIGN_READY, selector visual confirmado. Archivos `ui/history/`, `ui/maintenance/` propuestos; repositorios y casos de uso existentes.

- [x] Coder: historial del vehículo con orden, limpio y detalle/retorno; mantenimiento con lista, alta/edición mismo ID, fechas calendario, campos de próximos límites, notas y ocho tipos; OTHER sin alertas. Vacío/carga/error y descarte.
- [x] Tester: aislamiento y retorno desde origen, historial limpio, fecha futura/inválida y km histórico confirmado, edición del mismo ID sin avance de odómetro, programación por tipo y rollback; validación de conversión y no reescritura de campo no editado.
- [x] QA: comprobar representación de estado textual de programación vigente y selector completo; contraste con DD-16/30/43/44.

## 10. Etapa 4.8 — Inicio y Ajustes (UI-01/07)

**Puerta:** UI-01/07 DESIGN_READY y dependencias anteriores. Archivos `ui/home/`, `ui/settings/` propuestos; `GetDashboardDataUseCase` y preferencias ampliadas.

- [ ] Coder: Inicio orden vehículo → mantenimiento → diagnóstico → métricas; vacío sin vehículos incluso tras borrar último; distinguir ausencia de diagnóstico de limpio; recurrencia solo etiqueta códigos analizados. Última lectura identificada por vehículo/fecha; null y cero distintos; recálculo al activar.
- [ ] Coder: Ajustes con borrador común, Guardar, descarte, recuperación y defaults; convertir millas con un decimal a km entero más cercano solo cuando se edita, sin reescribir históricos.
- [ ] Tester: cambio de vehículo aísla datos, carga/error y actualización con datos del contexto, cambio de día al reactivar, 10,0 millas → 16 km, unidad/nombre sin editar distancia conserva entero, error parcial conserva borrador.
- [ ] QA: contrastar DD-29/36–41/45/46/48 y referencias; no atribuir persistencia tras muerte de proceso a captura temporal.

## 11. Etapa 4.9 — Integración, validación y cierre

- [ ] Tester ejecuta secuencialmente `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew :app:lintDebug :app:assembleDebug`. Registrar comando, fecha, dispositivo, número de pruebas, omitidas y resultado real; UP-TO-DATE no equivale a nueva ejecución. Si falta dispositivo o API requerida, registrar bloqueo; no declarar suite completa.
- [ ] Validar migración con datos v1, integración OBD simulada, permisos API 37 y regresión API 36, recreación, accesibilidad Compose, texto grande, teclado, ventanas y recorridos críticos. Hardware ELM327 físico se valida en fase 5 salvo autorización y disponibilidad específicas.
- [ ] QA/Ponytail revisa cambios conectados, regresiones, datos, seguridad, accesibilidad, complejidad y cobertura. Resolver hallazgos y repetir solo comprobaciones afectadas.
- [ ] Actualizar SSOT, checklist y `plans/reporte_fase4.md`, registrar evidencia por pantalla y diferencias aceptadas. `DONE` exige implementación y validación, no solo prototipo; consultar al usuario siguiente paso tras cierre.

## 12. Criterios globales y exclusiones

Cada etapa termina con Coder → Tester → QA, evidencia y reporte; un fallo bloquea solo el trabajo dependiente. Cierre global exige suites disponibles aprobadas, bloqueos explícitos, migración segura, decisiones visuales aprobadas, UI-01–07 verificadas y QA global. No considerar una simulación del prototipo prueba de hardware.

Quedan fuera: botón Demo, telemetría continua, escritura ECU/borrado DTC, temperatura de admisión, odómetro ECU, nube y recuperación de captura tras muerte del proceso. No hay código ni paleta aprobada por la creación de este plan.
