# Prompt para retomar Fase 4 — planificación de etapa 4.3

Actúa como agente Android del proyecto BitacoraOBD. Usa exclusivamente herramientas Android Studio prefijadas `studio_` para inspeccionar, editar documentos/código y ejecutar comandos del workspace. Respeta Ponytail full, KISS/YAGNI/SOLID y `AGENTS.md`.

## Objetivo de esta sesión

Inicia por la **planificación de etapa 4.3 — Composición, ciclo de vida y navegación**. En esta sesión contrasta requisitos, decisiones, código y pruebas, identifica dependencias/condiciones de entrada, y crea o actualiza un plan específico de etapa 4.3 y el checklist de ejecución. No implementes código hasta completar el plan y obtener confirmación explícita del usuario para ejecutarlo.

## Estado confirmado al retomar

- Etapa 4.1: implementada y cerrada; QA APROBADO. `./gradlew :app:connectedDebugAndroidTest` pasó en Pixel_10 AVD / Android 17 (API 37): 19 pruebas, 0 fallos, 1 omitida por falta de `elm327Host`. Reporte: `plans/reporte_fase4_etapa4_1.md`.
- Etapa 4.2: implementada y cerrada; QA APROBADO. Preferencias AppSettings/DataStore incluyen nombre opcional, unidades y visibilidad; compatibilidad con claves antiguas conservada. Pruebas instrumentadas y JVM de DataStore pasan; lectura/escritura fallida inyectada con IOException propaga el error. Reporte: `plans/reporte_fase4_etapa4_2.md`.
- Sigue pendiente registrar `MIGRATION_1_2` en el builder Room de producción: el repositorio todavía no tiene composición/builders de producción. Esto pertenece a 4.3.
- El usuario aprobó para 4.3 un coordinador compartido en Presentation que serialice con un `Mutex` las operaciones de conectar, seleccionar vehículo y borrar vehículo. Consultar el plan 4.2 para el protocolo detallado y los límites.
- No prometer recuperación de captura diagnóstica tras muerte del proceso. Captura pendiente debe conservarse durante el proceso vivo y resolverse antes de acciones incompatibles conforme DD-17/20/25/26/28.
- No introducir botón Demo, sondeo continuo ni hardware/funciones fuera del MVP.
- Wireframes UI-01–07 previamente aprobados no requieren aprobación repetida; no modificar Figma.

## Lecturas iniciales obligatorias

Leer, sin auditar trabajo ajeno al alcance:

- `AGENTS.md`.
- `plans/designplans/implementation_plan_fase4.md`, secciones de puertas, mapa, 4.3 y dependencias 4.4–4.8.
- `plans/designplans/implementation_plan_fase4_etapa4_1.md` y `plans/designplans/implementation_plan_fase4_etapa4_2.md`.
- `plans/designplans/domain-contracts.md`, `design-handoff.md`, `screen-dependencies.md`.
- Secciones relevantes de `design-decisions.md`, `screen-flows.md`, `ui-ux-specification.md`: DD-13, DD-17, DD-20, DD-25–28, DD-32–34, DD-36–42, DEP-001–023; flujo de conexión/cancelación y navegación.
- `plans/task.md`, `plans/reporte_fase4_etapa4_1.md`, `plans/reporte_fase4_etapa4_2.md`.
- Código real y tests de Activity, Room entities/database/migrations/DAOs, repositorios, DataStore, gateway/socket y casos de uso; configuración Gradle y dependencias de lifecycle/navigation/ViewModel. Buscar todos los callers de APIs que se proponga cambiar.

Usa los contratos y tests Kotlin como evidencia de implementación actual. Registra diferencias documentales sin asumir que una especificación ya está implementada.

## Preguntas técnicas que debe resolver el plan

1. **Composición Room:** definir un único builder Room de producción y registrar explícitamente `MIGRATION_1_2`; verificar schema v2 y configuración de exportación. No usar fallback destructivo. Definir quién posee la instancia compartida y cómo se cierra si aplica.
2. **DataStore/repositorios/gateway:** identificar la forma mínima coherente con el proyecto para crear una instancia compartida por proceso e inyectar dependencias por constructor. Sin Hilt ni nueva librería salvo dependencia ya instalada y justificación.
3. **Estado y ciclo de vida:** escoger propietario compartido de Presentation para estado de conexión, captura pendiente y coordinación de acciones; evitar duplicar socket/gateway, lecturas o guardados en recreación. No guardar socket en saved state. La muerte del proceso no promete recuperación de la captura.
4. **Coordinación aprobada:** diseñar operación no reentrante y sin deadlock con un único Mutex para conectar, seleccionar y borrar. Resolver captura pendiente antes del Mutex y revalidar estado/captura luego de adquirirlo. Cancelación de conexión debe poder interrumpir un connect que retenga el Mutex, esperar limpieza del gateway y luego liberar; Disconnecting requiere esperar cierre. Connected bloquea cambio/borrado. Comprobar estado real también ante Error. Todas las entradas de UI deben pasar por el mismo propietario; llamadas directas quedan fuera del alcance de esa garantía.
5. **Room y DataStore:** el Mutex no crea transacción conjunta. Preservar y explicar fallo parcial al actualizar activo en DataStore antes de borrar en Room; ante fallo, consultar/refrescar el estado real antes de permitir reintento. No afirmar atomicidad.
6. **Inicio/fecha:** `GetDashboardDataUseCase` no emite solo por cambio de fecha; acordar en qué evento de ciclo de vida se crea/reinicia la colección para recalcular vencimientos. No temporizador.
7. **Navegación:** verificar dependencias actuales y definir cómo cumplir cuatro destinos principales, Ajustes/Historial secundarios y flujo F03 desde/hacia Diagnóstico. Reutilizar dependencias instaladas; no crear navegación propia ni añadir dependencia automáticamente.
8. **Puertas de diseño:** 4.3 puede preparar composición/estado/navegación mínima, pero no debe implementar pantallas Compose de etapas posteriores sin confirmar DESIGN_READY y dependencias visuales. Conservar aprobaciones existentes y listar bloqueos visuales para 4.4+.

## Entregables de planificación

1. Auditar contratos/código/test actuales solo para 4.3 y registrar la línea base con paths exactos.
2. Crear `plans/designplans/implementation_plan_fase4_etapa4_3.md` con meta, arquitectura mínima, archivos a modificar, firmas propuestas, dependencias, tareas pequeñas de Fase A/B/C, comandos de verificación, riesgos, criterios de aceptación y exclusiones. Separar explícitamente prerequisitos ya cumplidos de preguntas que aún requieren evidencia.
3. Actualizar `plans/task.md` con checklist 4.3 en Fases A (Coder), B (Tester), C (QA), preservando todos los estados/reportes históricos. Marcar que esta sesión solo planifica.
4. No modificar código, Figma, Compose ni navegación durante la planificación.
5. Presentar al usuario las discrepancias funcionales nuevas, si aparecen, con evidencia y efecto; solicitar autorización únicamente para cambios de contrato/regla fuera de lo ya aprobado. Para decisiones técnicas dentro del alcance aprobado, resolver con KISS y criterio técnico.
6. Detenerse tras entregar el plan concreto y esperar aprobación del plan 4.3 antes de codificar. La aprobación general de Fase 4 no sustituye esta puerta de planificación por etapa.

## Condiciones de entrada a ejecución (registrar como checklist verificable)

- 4.1 y 4.2 con reportes revisados; actualización de checklist preserva limitaciones reales.
- Builder Room de producción identificado (o planificado en 4.3) y migración 1→2 registrada explícitamente.
- Versiones/dependencias realmente disponibles inspeccionadas; no añadir dependencia no aprobada.
- Ciclo de conexión y cancelación del gateway entendido con código y pruebas existentes.
- Propietario de proceso y políticas de recreación definidas.
- Protocolo del Mutex evita deadlock y tiene pruebas deterministas planificadas.
- Dependencias visuales de UI-01–07 separadas; no bloquear preparación técnica por emulador, ni marcar prueba runtime ausente como exitosa.
- Plan 4.3 y checklist escritos y sometidos a confirmación del usuario.

No ejecutar Gradle ni pruebas durante la fase de planificación documental, salvo que el contraste requiera una comprobación puntual para establecer una dependencia; si se ejecuta, registrar comando y resultado. Tras la aprobación, seguir protocolo secuencial Coder → Tester → QA y parar al cierre de 4.3.
