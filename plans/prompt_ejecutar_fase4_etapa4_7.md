# Prompt para nueva sesión — Fase 4.7: Historial y Mantenimiento (UI-05/06)

Copia este prompt en una sesión nueva de Codex abierta en el proyecto **Bitácora OBD**.

---

Actúa como ingeniero Android sénior en el repositorio `/Users/cristian/Desarrollo/kotlin/BitacoraOBD`. Aborda **únicamente la Fase 4.7: UI-05 Historial y UI-06 Mantenimiento**, siguiendo el protocolo local `AGENTS.md`, las decisiones aprobadas y el alcance ya definido en los planes.

## Objetivo de esta sesión

Primero planifica la etapa 4.7 de principio a fin. Audita requisitos, decisiones, diseño y código actual; crea/actualiza `plans/designplans/implementation_plan_fase4_etapa4_7.md`, y realiza una auditoría Ponytail del plan contra SOLID, KISS y YAGNI. Ajusta el plan a los hallazgos y preséntalo al usuario. **No modifiques código ni inicies las fases Coder/Tester/QA hasta recibir aprobación explícita (`Proceed` o confirmación inequívoca en chat).**

Después de la aprobación, ejecuta el trabajo completo en el orden obligatorio Coder → Tester → QA/Ponytail, actualizando `plans/task.md`. Cierra con un reporte solo tras veredicto QA APROBADO. Si un requisito está realmente bloqueado, completa el trabajo independiente y pide instrucciones únicamente para la parte dependiente.

## Fuentes obligatorias para auditar

Lee `AGENTS.md` y contrasta, como mínimo:

- `plans/Contexto_Maestro_App_OBDII_v2.md`, en particular alcance MVP, reglas de diagnóstico/mantenimiento y el plan general Fase 4 §9 Etapa 4.7.
- `plans/designplans/implementation_plan_fase4.md`.
- `plans/designplans/implementation_plan_fase4_etapa4_5.md` y `plans/reporte_fase4_etapa4_5.md` para los patrones de formularios, validación, borradores, errores y tema ya implementados.
- `plans/designplans/implementation_plan_fase4_etapa4_6.md` y `plans/reporte_fase4_etapa4_6.md` para el estado real de AppSession, navegación, estilo de pruebas y límites aún pendientes. El reporte actualizado registra una conexión a `192.168.1.122:35000` (1 prueba ELM327, 0 fallos) y señala que se debe validar más adelante permiso API 37 con Wi-Fi real y/o adaptador físico. Esta observación es contexto, no amplíes el alcance 4.7 para resolverla.
- `plans/designplans/design-decisions.md`, `ui-ux-specification.md`, `screen-flows.md`, `domain-contracts.md`, `screen-dependencies.md`, `design-handoff.md`, `design-system.md` y las referencias vigentes en `figma-references.md`.
- Código y pruebas actuales de DiagnosticRepository, MaintenanceRepository, casos de uso, DAOs, entidades/converters, AppSession, Navigation Compose, pantallas UI-02/03/04 y fixtures de Room/Compose.
- Configuración Gradle, dispositivos y herramientas de prueba existentes. No asumas que la documentación refleja el estado actual del código.

Usa Android Studio MCP `studio_*` para leer, buscar, editar y ejecutar comandos en el workspace. Respeta buffers de IDE: no leas ni edites archivos del proyecto desde shell.

## Alcance funcional que debe respetarse

Implementa UI-05 y UI-06 con los contratos y decisiones vigentes. No amplíes Domain/Data sin demostrar una brecha concreta, explicarla y obtener autorización si cambia comportamiento aprobado.

### UI-05 Historial

- Mostrar sesiones exitosas del vehículo/contexto elegido, incluidas sesiones limpias; los fallos no generan historial.
- Respetar el orden definido por el repositorio y presentar fecha/hora, kilometraje y cantidad de DTC, con estados de carga, vacío y error/reintento.
- Abrir detalle y retornar al origen correcto (Inicio o Diagnóstico según el flujo aprobado), conservando vehículo/contexto y evitando resultados de otro vehículo.
- Usar orientación de catálogo y recurrencia ya existentes; un código desconocido conserva el código original y recibe orientación genérica no taxativa. No inferir una pieza defectuosa ni alterar DTC.
- No implementar borrado, exportación, búsqueda, filtros o recuperación de capturas no guardadas salvo requisito vigente explícito.

### UI-06 Mantenimiento

- Mostrar historial por vehículo y estados de carga, vacío, error, programación próxima/vencida/sin programar.
- Implementar alta y edición del mismo registro por ID; cancelar edición conserva el registro original y fallos mantienen el borrador para reintento.
- Incluir los ocho tipos reales: OIL_AND_FILTER, OIL_ONLY, FILTER_ONLY, BRAKES, TIRES, BATTERY, COOLANT y OTHER. OTHER no genera alertas automáticas.
- Usar fecha calendario LocalDate, kilometraje, notas opcionales y límites próximos opcionales. No inventar campos, obligatoriedades, frecuencias ni catálogos.
- Aplicar validaciones y reglas Domain existentes. Si el kilometraje histórico requiere confirmación, no persistir antes de confirmar; al confirmar guardar el historial sin retroceder el odómetro. No duplicar lógica de negocio en Compose.
- Al editar fecha o programación, volver a leer el estado real; no simular alertas ni desplazar automáticamente límites absolutos.
- Respetar DD-22/DD-31/DD-37 en unidades: almacenar cantidades canónicas, convertir solo presentación/entrada de campos editados y nunca regrabar campos no editados por una conversión visual. Aclarar en el plan las reglas de redondeo ya aprobadas y verificar implementaciones existentes antes de proponer otras.
- El selector visual completo es una puerta de diseño indicada por el plan general. Verifica las referencias actuales: la aprobación de wireframes base no acredita por sí sola un selector/variante posterior. Si falta una confirmación visual requerida, durante la planificación presenta una decisión concreta y su impacto; no vuelvas a pedir aprobaciones ya otorgadas para los wireframes base.

## Restricciones y exclusiones

- KISS, YAGNI y SOLID; reutiliza Material 3, repositorios, casos de uso, AppSession y componentes existentes donde encajen.
- Sin dependencias, coordinadores, interfaces, capas o componentes compartidos especulativos.
- Sin cambios de esquema Room/migraciones salvo una brecha real aprobada. Sin nube, modo Demo, automatizaciones, notificaciones, borrado de ECU, historial de métricas o funciones 4.8.
- No modificar decisiones funcionales ni Figma silenciosamente.
- Usa el tema fijo claro/oscuro de 4.5 y conserva accesibilidad: etiquetas, errores textuales, objetivos táctiles y contenido desplazable.
- La continuidad tras muerte del proceso de borradores no persistidos sigue fuera del MVP.

## Flujo obligatorio

### Planning, antes de código

1. Inspecciona el árbol real, callers, fixtures y estado de git; identifica riesgos de pérdida de datos/contexto y posibles duplicaciones.
2. Consulta requisitos y diseño de UI-05/06, decisiones DD pertinentes (en especial DD-16, DD-30, DD-43/44 y las actualizaciones vigentes de unidades/navegación).
3. Crea/actualiza el plan de implementación 4.7 con alcance, archivos/callers, contratos reales, criterios de aceptación, estrategia de pruebas, comandos y límites.
4. Haz una revisión Ponytail del plan buscando sobreingeniería y problemas SOLID/KISS/YAGNI. Incorpora las correcciones al plan y registra hallazgos.
5. Presenta resumen del plan, decisiones ya cerradas, decisiones de diseño que realmente requieran al usuario y evidencia/límites. **Detente y espera aprobación antes de modificar código.**

### Después de recibir aprobación

1. Actualiza `plans/task.md` con Fase A Coder, Fase B Tester y Fase C QA/Ponytail, preservando los registros anteriores.
2. Delegación secuencial obligatoria conforme `AGENTS.md`: Coder implementa solo el plan y no ejecuta tests; luego Tester crea/adapta pruebas y ejecuta la suite completa; finalmente QA/Ponytail revisa código conectado, consumidores y cobertura. Corrige hallazgos y repite las verificaciones afectadas.
3. Ejecuta Gradle/dispositivo secuencialmente. Como mínimo seguir el plan aprobado y verificar unit tests, compilación AndroidTest, instrumentadas disponibles, lint, assemble y `git diff --check`. No declares una prueba PASS si fue omitida, UP-TO-DATE sin ejecución o no se pudo ejecutar.
4. QA debe comprobar aislamiento por vehículo, integridad de registros, fechas y kilometraje, edición en el mismo ID, confirmación histórica, navegación de retorno, estados de error y cobertura real.
5. Solo tras QA **APROBADO**: marca el checklist completo, sincroniza handoff/dependencias/documentación que la implementación haya vuelto obsoleta y crea `plans/reporte_fase4_etapa4_7.md` con resultados, comandos, dispositivo, omitidas, límites y decisión QA. Detente y consulta el siguiente paso.

## Forma de trabajo

Comunícate en español. Da actualizaciones breves. En la fase de planificación no hagas cambios de producción. Sé explícito sobre qué se inspeccionó y qué no. No confundas aprobación del plan, aprobación de wireframes, pruebas automatizadas y validación visual manual.
