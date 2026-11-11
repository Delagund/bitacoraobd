# Prompt para nueva sesión — Planificación de Fase 4.9

Copia este prompt en una sesión nueva de Codex abierta en el proyecto **Bitácora OBD**.

---

Actúa como ingeniero Android sénior en el repositorio `/Users/cristian/Desarrollo/kotlin/BitacoraOBD`. Esta sesión debe **planificar únicamente la Fase 4.9** conforme a `AGENTS.md`, las decisiones aprobadas y el estado real del repositorio.

## Objetivo de la sesión

Audita el alcance ya definido para 4.9, confirma su vigencia contra el código actual y crea o actualiza `plans/designplans/implementation_plan_fase4_etapa4_9.md`. Haz una revisión Ponytail del plan contra SOLID/KISS/YAGNI, incorpora las correcciones y presenta el resultado.

**Esta sesión es únicamente de planificación. No modifiques código, pruebas, Figma, checklist de ejecución ni reportes de cierre. No inicies roles Coder/Tester/QA ni ejecutes implementación. Detente y espera aprobación explícita del usuario antes de empezar a codificar.**

Si aparece una contradicción material entre el alcance de 4.9 y una decisión aprobada, descríbela e identifica su impacto; no cambies el comportamiento aprobado en silencio.

## Estado confirmado al preparar este prompt

- Fase 4.8 (UI-01 Inicio y UI-07 Ajustes) está cerrada con QA/Ponytail **APROBADO**. Consulta `plans/reporte_fase4_etapa4_8.md` y su plan aprobado `plans/designplans/implementation_plan_fase4_etapa4_8.md`.
- La verificación de 4.8 registrada: 137 pruebas JVM sin fallos; 27 instrumentadas en SM-A066M/API 36 sin fallos y una omitida por ausencia de `elm327Host`; compilación AndroidTest, lint, assemble y `git diff --check` exitosos.
- El reporte de 4.8 deja pendientes revisiones manuales de accesibilidad/visual (TalkBack, texto ampliado, IME, temas claro/oscuro y recorridos reales). A diferencia de 4.8, el §11 del plan global asigna explícitamente a 4.9 validación de accesibilidad Compose, texto grande, teclado, ventanas y recorridos críticos. Planifica cómo verificar esos criterios; no rediseñes ni cambies tokens aprobados sin autorización.
- El plan global `plans/designplans/implementation_plan_fase4.md` §11 define **Etapa 4.9 — Integración, validación y cierre**: suites Gradle secuenciales con resultados/omisiones/dispositivo; migración con datos v1; integración OBD simulada; permisos API 37 y regresión API 36; recreación; accesibilidad/Compose; texto grande, teclado, ventanas y recorridos críticos; QA/Ponytail; sincronización de SSOT, checklist y `plans/reporte_fase4.md`. El hardware ELM327 físico queda para Fase 5 salvo autorización y disponibilidad específicas. Verifica vigencia y el estado real de cada punto; no lo trates como una fase de implementación de paleta nueva.
- El plan global separa la decisión visual de la implementación: §6 (4.4) contiene referencias, dirección visual y aprobación/tokens; §7 (4.5) adapta `ui/theme/Color.kt`, `Theme.kt` y `Type.kt`, aplica Material 3 a la UI y valida contraste. Las etapas 4.5–4.8 ya están cerradas. 4.9 debe verificar uso/renderizado, accesibilidad y coherencia visual conforme al alcance de §11, no rehacer automáticamente la paleta.
- El usuario aceptó cambios recientes en `Entities.kt`, `AppDatabase.kt`, pantallas/ViewModels de Ajustes, Inicio, Historial y Mantenimiento, `MainActivity.kt`, `AppNavigation.kt`, `DistanceFormatting.kt` y pruebas asociadas. Léelos desde el IDE y trátalos como línea base aceptada; no reviertas ni reabras esos cambios sin una contradicción/requisito concreto y autorización aplicable.
- El árbol tenía cambios de trabajo de etapas previas al preparar este prompt, pero el usuario ha aceptado recientemente varios archivos; el `git status` histórico de este prompt no garantiza el estado actual. Inspecciona el status de nuevo y **preserva todos los cambios existentes**; no limpies, restaures ni resetees.
- `/plans` está excluido por la regla raíz de `.gitignore`. Los documentos siguen existiendo en el workspace, pero no aparecen en Git status. No alteres esa política ni fuerces archivos al índice sin autorización explícita.

## Fuentes que debes auditar

Usa Android Studio MCP `studio_*` para leer y buscar el workspace. Para comandos Git, Gradle o dispositivo, utiliza las herramientas `studio_*` apropiadas. No uses shell para leer, buscar o editar archivos del proyecto.

Lee y contrasta, como mínimo:

1. `AGENTS.md` y `plans/Contexto_Maestro_App_OBDII_v2.md`: alcance MVP, Fase 4 y Fase 5; identifica cualquier validación del MVP que el plan global deje en Fase 5.
2. `plans/designplans/implementation_plan_fase4.md` §6 (Etapa 4.4 visual), §7 (Etapa 4.5: implementación tema/formularios), §10 (4.8) y §11 (4.9: integración, validación y cierre). Contrasta los checkboxes antiguos con reportes específicos posteriores.
3. Plan y reporte de 4.8; reportes/planes de 4.5–4.7 donde aporten evidencia del tema, UI, decisiones o validaciones. Revisa en especial el reporte 4.5 y la revisión vigente de `design-system.md`.
4. `design-decisions.md`, `domain-contracts.md`, `screen-flows.md`, `screen-dependencies.md`, `design-handoff.md`, especificación UI y referencias vigentes.
5. `Entities.kt`, esquema Room, migraciones, DAOs/repositorios/casos de uso afectados, callers, ViewModels, pantallas y pruebas/fixtures relacionados con los requisitos reales de 4.9.
6. Git status, configuración Gradle, herramientas/dispositivo disponibles y estado de ejecución actual. Distingue resultados históricos de verificaciones realizadas en esta sesión.

## Decisiones y límites heredados

- Conserva las decisiones aprobadas de UI-01–07, navegación, AppSession, unidades, conversiones, persistencia y manejo de errores. Reabre una decisión solo si una fuente vigente demuestra una contradicción, y describe el impacto en vez de cambiar el contrato silenciosamente.
- La preferencia de 4.7 por una entrega estática y las aprobaciones de wireframes base no acreditan interacción o aprobación de variantes nuevas. La revisión visual en 4.9 verifica implementación contra tokens/referencias ya aprobados; no autoriza rediseño ni cambios Figma.
- El cierre funcional de 4.8 no acredita revisión visual/manual. El §11 del plan global sí asigna a 4.9 validaciones concretas de accesibilidad Compose, texto grande, teclado, ventanas y recorridos críticos. Diseña criterios/evidencia para estas verificaciones, distinguiéndolas de la aprobación de nuevas variantes visuales.
- No añadas capas, dependencias, cambios de esquema, migraciones ni contratos por previsión. Reutiliza el código existente y demuestra cualquier brecha antes de proponer ampliaciones.
- No pruebes hardware o red real sin necesidad del requisito; registra límites y recursos disponibles sin prometer verificaciones no ejecutadas.

## Trabajo de planificación

1. Desglosa cada criterio de §11 del plan global en hecho, pendiente y evidencia actual. Distingue pruebas automatizadas, validación de simulador, dispositivo/API, accesibilidad manual, evidencia visual y obligaciones de cierre documental.
2. Inspecciona callers, datos, navegación, estado, persistencia, fixtures y regresiones relevantes. Identifica qué validaciones se cubrieron en 4.5–4.8, qué falta realmente en 4.9 y qué pertenece a Fase 5 (hardware ELM327 físico) u otra etapa.
3. Si el alcance está definido, propone la solución mínima compatible con arquitectura y contratos existentes. Incluye archivos/callers probables, criterios de aceptación verificables, estrategia y comandos de pruebas, dependencias, riesgos y límites.
4. Si hay puerta visual o una discrepancia funcional real, describe la opción concreta y su impacto; no solicites de nuevo decisiones ya aprobadas ni edites Figma.
5. Guarda el plan en `plans/designplans/implementation_plan_fase4_etapa4_9.md`. Haz una revisión Ponytail del plan: elimina alcance especulativo, duplicación, pérdida potencial de datos, abstracciones innecesarias y verificaciones imposibles; incorpora y registra los ajustes.
6. Presenta el plan enlazado, la evidencia revisada, las decisiones cerradas, dudas reales y lo que no pudiste comprobar. Después **espera aprobación explícita**.

## Forma de trabajo

Comunícate en español y ofrece actualizaciones breves. No confundas una propuesta de plan con aprobación para implementar. No ejecutes Coder, Tester o QA ni modifiques el `task.md` durante esta sesión de planificación.