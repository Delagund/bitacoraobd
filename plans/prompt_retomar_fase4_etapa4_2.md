# Prompt archivado — Fase 4, etapa 4.2

> **Obsoleto: etapa 4.2 cerrada.** Conservado como registro del alcance inicial. No usar para retomar trabajo. Estado y evidencia vigentes: [reporte_fase4_etapa4_2.md](reporte_fase4_etapa4_2.md); siguiente etapa: planificación 4.3 en [prompt_retomar_fase4_etapa4_3.md](prompt_retomar_fase4_etapa4_3.md).

# Prompt original para retomar Fase 4 — Etapa 4.2

Actúa como agente Android del proyecto BitacoraOBD. Usa exclusivamente herramientas Android Studio prefijadas studio_ para inspeccionar, editar archivos y ejecutar comandos del workspace. Respeta ponytail full y KISS/YAGNI/SOLID.

## Instrucciones iniciales

Lee primero, sin volver a auditar ni modificar trabajo ajeno al alcance:

- AGENTS.md.
- plans/designplans/agent-designer-instruction.md.
- plans/designplans/implementation_plan_fase4.md.
- plans/designplans/implementation_plan_fase4_etapa4_1.md, especialmente la decisión de UUID/sentinel autorizada.
- plans/designplans/domain-contracts.md y design-handoff.md.
- plans/designplans/ui-ux-specification.md, screen-flows.md y design-decisions.md, solo en secciones relevantes a 4.2.
- plans/task.md y plans/reporte_fase4_etapa4_1.md.
- Código y pruebas reales de cada contrato que 4.2 toque.

El plan general de Fase 4 está aprobado. La etapa 4.1 ya fue implementada, pero su cierre técnico completo tiene pruebas instrumentadas Room pendientes. El usuario aclaró que disponer de dispositivos o emuladores **no bloquea la implementación de la interfaz**; registra esas pruebas pendientes para ejecutarlas cuando haya un dispositivo/emulador disponible. No marques su ejecución como exitosa.

## Estado técnico relevante de 4.1

- DiagnosticScanResult.Success recibe captureId UUID estable al crear la captura.
- La clave se propaga por DiagnosticSession, SaveDiagnosticSessionUseCase y mappers.
- Room v2 tiene índice UNIQUE y guardado idempotente dentro de transacción; conflictos de la misma clave con payload distinto fallan.
- MIGRATION_1_2 usa ALTER TABLE, default sentinel de esquema y asigna UUID Kotlin distintos a sesiones históricas antes de crear el índice único. Se autorizó esta estrategia. No rediseñarla salvo defecto demostrable.
- No hay builder Room de producción todavía; registrar MIGRATION_1_2 corresponde a la composición de 4.3.
- Pasaron 82 pruebas JVM, lint, assemble y compilación de AndroidTest. Pruebas instrumentadas de reintento/concurrencia/rollback/migración están añadidas, pero no ejecutadas porque adb no reportó dispositivo conectado.
- No se promete recuperar captura pendiente tras muerte del proceso. Documentación UI ahora requiere advertir: “Si cierras la app antes de guardar, perderás este diagnóstico.” La recuperación no forma parte del MVP.

## Alcance único: etapa 4.2 — backend

Trabaja únicamente preferencias y guardias identificadas en el plan. Antes de codificar:

1. Actualiza plans/task.md con fases A, B y C de etapa 4.2, preservando toda la historia.
2. Contrasta primero firmas, implementaciones, consumidores, schema DataStore y pruebas reales.
3. Si el contraste revela que el cambio modifica un contrato funcional o visual fuera de lo aprobado, documenta la diferencia y consulta al usuario antes de implementarla. No vuelvas a solicitar aprobación de wireframes existentes ni alteres Figma.
4. Si no hay discrepancia funcional nueva, procede bajo la aprobación del plan general sin volver a pedir permiso.

Implementa solo los elementos 4.2 aprobados en implementation_plan_fase4.md:

- Ampliar preferencias locales para nombre opcional, unidad de distancia (km/millas), unidad de temperatura (°C/°F) y visibilidad de RPM, refrigerante y velocidad; defaults acordados: nombre sin informar, km, °C y las tres métricas visibles.
- Mantener compatibilidad de preferencias existentes active_vehicle_id, obd_ip y obd_port.
- Diseñar la operación de guardar para el borrador completo de Ajustes conforme DD-36: guardado explícito, resultado real ante fallo y borrador conservado por Presentation. No afirmar atomicidad que DataStore no garantice. La conversión de distancia/redondeo debe seguir DD-37; guardar sin editar no reescribe distancia interna.
- Revisar guardias de cambios de vehículo/borrado para ConnectionState y capturas pendientes conforme DD-17/20/25/26/28/42. Respetar límites: Room y DataStore no comparten transacción. No crear lógica redundante ni cambiar interfaces de Domain sin autorización específica.
- Documentar el mecanismo mínimo de recálculo de vencimientos al activar Inicio conforme al plan, sin temporizadores innecesarios.
- Implementar solo pruebas correspondientes a reglas aprobadas. No modificar Compose ni navegación en 4.2.

## Protocolo secuencial obligatorio

### Fase A — Coder

Implementa lo aprobado y actualiza los checks de Fase A. El Coder no ejecuta pruebas. No agregues trabajo a 4.3 o etapas UI.

### Fase B — Tester

Añade/adapta pruebas y ejecútalas secuencialmente. Cubre como mínimo:

- Defaults de preferencias antiguas y actuales; guardar y volver a leer cada valor.
- Fallos de lectura/escritura DataStore, conservación del borrador/estado real cuando aplique y ausencia de afirmaciones de éxito parcial.
- Conversión/redondeo conforme DD-37 y sin reescribir datos internos por cambios de unidad.
- Bloqueo de selección/cambio/borrado en estados OBD aplicables y captura pendiente, conservando cancelación/errores existentes.
- Recalculo de vencimientos al entrar/reactivar Inicio si puede validarse en backend sin implementar UI.
- Regresión 4.1: JVM; compilar instrumentadas. Ejecutar instrumentadas si hay dispositivo; si no, anotar ese bloqueo sin frenar trabajo UI posterior.

Registrar comandos/salidas exactos y no afirmar 100% PASS si falta alguna suite requerida.

### Fase C — QA/Ponytail

Revisar código conectado, callers, tests, defaults/migración DataStore, errores parciales, cancelación y límites Room/DataStore. Aplicar KISS/YAGNI/SOLID. Resolver hallazgos en ciclo Tester → QA. Emitir estado claro: aprobado, aprobado con observaciones o bloqueado por evidencia. No cerrar como aprobado si falta una prueba crítica ejecutable sin documentar su límite.

## Límites y cierre

- No iniciar etapa 4.3, Compose, Figma ni etapa UI alguna.
- No implementar botón Demo, sondeo continuo ni recuperación tras muerte del proceso.
- Actualiza plans/task.md con evidencia y genera plans/reporte_fase4_etapa4_2.md con resultado, comandos, pruebas no ejecutadas y bloqueos.
- Si el QA de 4.2 queda validado, detenerse y comunicar el siguiente paso; no avanzar automáticamente.
