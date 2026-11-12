# Fase 4.9 — Integración, validación y cierre: plan de implementación

> Para agentes: ejecutar por tareas con `executing-plans` o `subagent-driven-development` y el protocolo de AGENTS.md. Estado: propuesta; requiere aprobación explícita antes de ejecutar cambios de código.

**Objetivo:** consolidar la evidencia de integración de UI-01–07, verificar los criterios pendientes y cerrar Fase 4 únicamente con resultados trazables.

**Arquitectura:** reutilizar AppSession, Navigation Compose, Room/DataStore y pruebas existentes. Los defectos se corrigen en su propietario actual; no se anticipan capas, dependencias ni cambios de datos.

**Tecnología:** Kotlin, Compose Material 3, JUnit, AndroidTest, Room, Gradle y adb.

## Alcance y evidencia disponible

Fuente principal: [plan global §11](implementation_plan_fase4.md), [checklist](../task.md), reportes 4.1–4.8 y [reporte de fidelidad](../reporte_fidelidad_prototipo.md). Fase 4.9 exige suites, migración v1, OBD simulado, permiso API 37 y regresión API 36, recreación, accesibilidad, texto grande, teclado, ventanas, recorridos, QA y reporte global. Hardware físico ELM327 corresponde a Fase 5 salvo autorización y disponibilidad específicas.

| Criterio | Evidencia histórica | Pendiente para 4.9 |
|---|---|---|
| Suites | Seguimiento visual: 137 JVM; 34 AndroidTest, 0 fallos, 1 omitida; lint/assemble y diff-check exitosos. | Repetir y registrar resultados del estado ejecutado; una tarea UP-TO-DATE no acredita ejecución nueva. |
| Migración | Reportes 4.1/4.3: v1 con relaciones y builder de producción. Existe `app/src/androidTest/java/cl/cristian/bitacoraobd/data/local/DiagnosticMigrationTest.kt`. | Confirmar fixture y ejecutar prueba con datos v1, preservación de relaciones e índice. |
| OBD | Reporte 4.6 registra prueba remota con host en API 36; suites posteriores omitieron `elm327Host`. Existe `app/src/androidTest/java/cl/cristian/bitacoraobd/data/obd/Elm327RemoteIntegrationTest.kt`. | Identificar y ejecutar simulador disponible, documentar host, comandos y resultados; sin host, registrar bloqueo y no atribuir PASS. |
| Permiso LAN | API 37 runtime no validada; conexión previa en API 36 no demuestra diálogo ni revocación. | Verificar concesión, denegación, retorno desde Ajustes y revocación en API 37; comprobar regresión API 36. |
| Recreación/navegación | `MainActivityRecreationTest.kt` y pruebas de sesión/navegación cubren casos anteriores. | Mapear casos existentes y cubrir solo brechas demostradas durante conexión, lectura y guardado. |
| Visual/accesibilidad | Inicio y Ajustes claros inspeccionados; pruebas de píxeles cubren algunos elementos en ambos temas. A fuente 1.3 la etiqueta Mantenimiento ocupa dos líneas. | Revisar siete pantallas y estados alcanzables, ambos temas, TalkBack, foco, fuente grande, teclado/insets y ventanas; registrar evidencias y defectos. Sin vehículo no se inspeccionaron formularios de mantenimiento, historial/detalle ni conexión contextual. |
| Cierre | Reportes de etapas y seguimiento visual aprobados. | QA global, reconciliar §10 del plan global con 4.8 cerrada, actualizar checklist y crear `plans/reporte_fase4.md`. |

Auditoría actual: `main...origin/main` sin cambios Git; `adb devices -l` no muestra dispositivos. `app/build.gradle.kts` declara minSdk 26 y compileSdk/targetSdk 37. La disponibilidad anterior del SM-A066M/API 36 y de Pixel_10/API 37 no garantiza disponibilidad actual. Las cifras anteriores son históricas; no se ejecutaron suites en esta planificación. No se revisaron exhaustivamente todos los callers, fixtures ni el entorno del simulador en esta sesión: la primera tarea debe hacerlo antes de atribuir cobertura.

## Archivos y responsabilidades

- Consultar `app/src/main/java/cl/cristian/bitacoraobd/ui/navigation/AppNavigation.kt`, `ui/AppSession.kt`, pantallas y ViewModels UI-01–07 para recorridos, permisos y estados.
- Consultar `app/src/main/java/cl/cristian/bitacoraobd/data/local/database/AppDatabase.kt`, `app/schemas/`, `DiagnosticMigrationTest.kt`, `MainActivityRecreationTest.kt`, `Elm327RemoteIntegrationTest.kt` y `VisualStyleContractTest.kt` para cobertura real y fixtures.
- Actualizar durante ejecución `plans/task.md`, `plans/designplans/implementation_plan_fase4.md` y crear `plans/reporte_fase4.md`. Modificar producción o pruebas solo ante un fallo reproducido y dentro del plan aprobado; si exige cambiar comportamiento, contratos o diseño aprobado, detener esa parte y presentar evidencia y opciones.

## Ejecución propuesta

### Preparación y seguimiento
- [ ] Registrar Git status, versiones Gradle/JDK, dispositivos autorizados y API. Preservar los datos existentes; usar emulador o fixture aislado para altas/borrados y restaurar ajustes del dispositivo.
- [ ] Actualizar `plans/task.md` con Fases A Coder, B Tester y C QA; reconciliar las casillas históricas de 4.8 en el plan general con su reporte.
- [ ] Inspeccionar pruebas y callers indicados; construir matriz por requisito con prueba, dispositivo, fecha, resultado y limitación. Confirmar cómo iniciar el simulador y configurar `elm327Host` consultando sus fuentes actuales, sin inventar comandos.

### Fase A — Coder, solo si aparece un defecto reproducible
- [ ] Registrar pasos, resultado observado, resultado esperado y archivo responsable. Corregir la causa mínima en el componente existente; conservar persistencia, cancelación y navegación.
- [ ] La etiqueta Mantenimiento en dos líneas a escala 1.3 requiere evaluación con criterios de legibilidad y zona táctil; cualquier cambio de texto o diseño aprobado requiere decisión del usuario.
- [ ] No modificar Figma ni añadir dependencias o cambios de esquema sin una brecha demostrada y autorización aplicable.

### Fase B — Tester
- [ ] Ejecutar secuencialmente `./gradlew :app:testDebugUnitTest --rerun-tasks`, `./gradlew :app:compileDebugAndroidTestKotlin`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew :app:lintDebug`, `./gradlew :app:assembleDebug` y `git diff --check`. Registrar número de pruebas, fallos, omisiones, dispositivo/API y si Gradle ejecutó tareas o usó caché.
- [ ] Ejecutar migración v1 con fixture y builder de producción; documentar datos e índices preservados. Ejecutar OBD contra simulador configurado; si falta host o dispositivo, dejar criterio bloqueado.
- [ ] En API 37 validar permiso LAN: conceder, denegar, volver desde Ajustes y revocar; en API 36 repetir conexión y manejo de error. No inferir permiso probado de un handshake exitoso.
- [ ] Con datos de prueba aislados recorrer alta/selección de vehículo, conectar/leer, escaneo limpio/con códigos/error, guardar/descartar, historial/detalle, mantenimiento y ajustes; comprobar aislamiento y persistencia. Registrar vacíos, errores y recreación durante operaciones.
- [ ] En ambos temas inspeccionar las rutas y estados disponibles; verificar contraste de texto/controles, TalkBack y orden de foco, fuente 1.3 o mayor, teclado/insets y cambio de tamaño de ventana. Capturar dispositivo, escala y estado; documentar rutas inaccesibles y restaurar ajustes.
- [ ] Añadir o adaptar solamente pruebas que reproduzcan una brecha real; repetir pruebas afectadas y suite necesaria tras cada corrección.

### Fase C — QA/Ponytail y cierre
- [ ] Revisar matriz, código conectado, datos, permisos, navegación, accesibilidad y KISS/SOLID/YAGNI. Resolver hallazgos y repetir verificaciones afectadas.
- [ ] Emitir veredicto con evidencia y límites; crear `plans/reporte_fase4.md`, actualizar SSOT y checklist. No declarar cierre global mientras un criterio obligatorio no tenga evidencia o una decisión explícita sobre su bloqueo.

## Revisión Ponytail del plan

Se reutilizan fixtures y pruebas existentes; no se plantea framework de screenshots, dependencia, migración ni nuevo coordinador. La prueba física ELM327 queda en Fase 5. Los datos reales del dispositivo se protegen mediante fixture o emulador aislado; no se prescribe borrar datos. Los criterios dependientes de API 37, simulador o dispositivo quedan bloqueados si el recurso no está disponible. Los resultados históricos no sustituyen la ejecución final. La cobertura de callers y la configuración del simulador requieren auditoría previa durante preparación: no se presentan como comprobadas.

**Puerta:** AGENTS.md exige aprobación explícita del plan antes de iniciar codificación. Esta propuesta no autoriza modificar código, pruebas ni ejecutar la etapa.
