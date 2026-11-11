# Prompt para ejecutar etapa 4.3 en una nueva sesión

> Texto para enviar como instrucción de usuario al agente de la nueva sesión. Al enviarlo, se aprueba el plan específico y la dependencia citada. Guardar o leer este archivo por sí solo no autoriza ejecutar código.

---

Actúa como agente Android del proyecto BitacoraOBD, raíz /Users/cristian/Desarrollo/kotlin/BitacoraOBD. Usa exclusivamente herramientas Android Studio prefijadas studio_ para inspeccionar/editar archivos y ejecutar comandos del workspace.

## Autorización de esta nueva sesión

**Apruebo y autorizo ejecutar** plans/designplans/implementation_plan_fase4_etapa4_3.md, incluida su actualización de contratos y tareas A0/B0/C0 tras las auditorías Ponytail. Autorizo añadir únicamente androidx.navigation:navigation-compose:2.10.2, con su alias en el catálogo, y el runner de pruebas descrito por el plan. No añadas otra dependencia ni cambies versiones sin justificar y obtener autorización concreta.

Esta autorización corresponde solo a 4.3. No repitas la planificación ni vuelvas a pedir aprobación de este mismo alcance. Relee el estado real para detectar cambios desde el traspaso; ante una discrepancia nueva que requiera otro contrato o regla, presenta evidencia y continúa lo independiente.

## Estado de entrada

- 4.1 y 4.2 cerradas QA APROBADO. Reportes: plans/reporte_fase4_etapa4_1.md y reporte_fase4_etapa4_2.md.
- Room v2 y MIGRATION_1_2 existen; falta el builder de producción y registrar explícitamente la migración. Sin fallback destructivo.
- AppSettings/DataStore implementados, claves antiguas conservadas y errores IOException probados.
- La última revisión documental del plan no encontró discrepancias funcionales nuevas; no es QA de implementación ni evidencia runtime. §13 es auditoría histórica; §§4/5/7 y A0/B0/C0 son el contrato actualizado.
- En esta preparación no se ejecutó código ni Gradle. Navigation fue consultada, todavía no resuelta/compilada.
- Reporte 4.1: Pixel_10 AVD/API37, 19 pruebas, 0 fallos, 1 omitida por falta de elm327Host. No presentar esa omisión como validación remota ni asumir permiso LAN runtime probado.
- /plans está excluido de Git. Conserva los artefactos allí, verifica su contenido con lectura IDE y no modifiques .gitignore ni fuerces versionado.

## Lectura inicial enfocada

Lee AGENTS.md y las skills aplicables: executing-plans, dispatching-parallel-agents, Ponytail full, ponytail-review y verificación antes de cierre. Respeta el orden del repositorio si una skill sugiere otro.

Lee:
1. plans/designplans/implementation_plan_fase4_etapa4_3.md completo, especialmente §§4/5/7/9/12/14.
2. plans/task.md, preservando todos los antecedentes.
3. Plan general plans/designplans/implementation_plan_fase4.md: puertas, 4.3 y dependencias 4.4–4.8.
4. Plan 4.2: protocolo de coordinación aprobado, y reportes 4.1/4.2.
5. plans/Contexto_Maestro_App_OBDII_v2.md y secciones relevantes de domain-contracts.md, design-decisions.md, screen-flows.md, screen-dependencies.md, design-handoff.md y agent-designer-instruction.md bajo plans/designplans.
6. Código y pruebas reales de Activity, Room/migración, DataStore/repositorios, gateway/socket y casos de uso; catálogo/Gradle. Buscar callers antes de cambiar una API.

No audites ni refactorices áreas ajenas a 4.3.

## Inicio y ejecución secuencial obligatoria

Registra esta aprobación en plans/task.md y el estado del plan, conserva historial y marca ejecución 4.3 iniciada. Antes de editar, revisa cambios del usuario y buffers IDE.

Orquesta subagentes especializados **secuenciales**, nunca Coder y Tester en paralelo:
- **Coder:** aplica A0 primero como criterios de implementación, después A1–A9. Solo código aprobado; no ejecuta tests. No implementa pantallas posteriores.
- **Tester:** prepara fixture/runner y empieza por B0; después B1–B15 y suite requerida. No afirmar RED previo si no ocurrió. Usa sincronización determinista, sin sleeps.
- **QA/Ponytail:** empieza por C0 y revisa después todo el cambio conectado. Devuelve defectos a Coder → Tester → QA hasta resolverlos. No declares completado por haber escrito código o pruebas.

## Invariantes que deben guiar el primer cambio

- Application/AppContainer con inyección manual y un único propietario AppSession por proceso; una instancia de Room, DataStore y gateway. Sin Hilt ni servicios.
- El finally de connect nunca desconecta después de Success. Connected bloquea selección y cualquier borrado sin cerrar el socket.
- Un Mutex de Presentation coordina conexión/selección/borrado. Resolver pendiente antes de tomarlo y revalidar después; nunca esperar decisiones de usuario dentro del lock.
- Cancelación accesible fuera del Mutex, cierre externo espera al intento, el intento no espera al cierre. No habilitar acciones incompatibles hasta limpieza confirmada; Error no demuestra cierre por su nombre.
- Captura pendiente y operación sobreviven recreación con proceso vivo. Guardado/reintento conserva payload y captureId; sin comandos ni guardados duplicados.
- ExitAction/AuthorizedExit conservan una sola intención. Un adaptador RESUMED comprueba ID, navega y confirma consumo en Main sin suspensión. Recreación no duplica navegación; fallo/Stay no navegan. F03 inicia y termina en Diagnóstico.
- Pruebas de recreación sobre MainActivity real con TestBitacoraApplication/BitacoraTestRunner y fixture de test; sin setters productivos, localizador global ni controles de depuración.
- B1 comprueba identidad sin abrir/cerrar recursos de proceso. B2 usa base temporal propia y valida migración mediante el builder real.
- Room/DataStore no son una transacción común. Tras fallo parcial o cancelación, refrescar estado real antes del reintento.
- Inicio reinicia colección al activarse con fecha actual; no temporizador.
- Nuevo proceso: desconectado y sin pendiente; no prometer recuperación tras muerte de proceso ni reanudar comandos automáticamente.

## Límites y puertas

Preparar composición/estado y grafo oficial mínimo con slots técnicos; no construir pantallas UI-01–07, tema o formularios de 4.4–4.8. Wireframes ya aprobados conservan aprobación; no solicitarla de nuevo ni modificar Figma. DESIGN_READY sigue siendo puerta de cada paquete visual.

Sin Demo, polling, reconexión automática, escritura ECU, hardware adicional ni funciones fuera del MVP. Permiso LAN/formulario de producto siguen en 4.6; no exponer conexión sin comprobación real del permiso.

## Verificación y cierre

Usa studio_run_shell_command y studio_interact_shell, secuencialmente. Ejecuta comandos de §10: resolución debugRuntimeClasspath, JVM dirigida/completa, compileDebugAndroidTestKotlin, connectedDebugAndroidTest, lintDebug, assembleDebug y comprobación de diff.

Registra comandos/resultados/conteos/dispositivo reales; BUILD SUCCESSFUL no significa que una prueba omitida pasó. Si no hay dispositivo para una prueba requerida, conserva ese pendiente, completa trabajo independiente y no declares 100% PASS ni cierre de etapa.

Solo tras QA APROBADO y evidencia requerida: finalizar task.md, generar plans/reporte_fase4_etapa4_3.md con límites y detenerse. No iniciar 4.4; consultar siguiente paso.
