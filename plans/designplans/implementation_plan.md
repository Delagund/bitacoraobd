# Plan de matriz de dependencias y aprobación del diseño

**Plan técnico posterior:** [Fase 4](implementation_plan_fase4.md), aprobado por el usuario y aún no iniciado por instrucción expresa; este plan de diseño y su seguimiento continúan de forma independiente.

> Para agentes: usar writing-plans para mantener este plan y executing-plans al ejecutarlo. La implementación Android posterior debe seguir el protocolo secuencial Coder → Tester → QA de AGENTS.md; este documento no autoriza cambios de código.

**Objetivo:** construir trazabilidad verificable entre requisitos, decisiones, código, pruebas, flujos, frames y tareas futuras de implementación.
**Arquitectura documental:** una matriz indexa evidencias y dependencias; los acuerdos permanecen en design-decisions.md, los comportamientos en la especificación y los flujos, y las referencias visuales en figma-references.md. No duplicar las reglas en varias fuentes.
**Herramientas:** Markdown, herramientas MCP de Android Studio, integración Figma, revisión conjunta agente–usuario.
**Fecha:** 2026-11-08.
**Estado:** APROBADO por el usuario el 2026-11-08. Preparación iniciada; seguimiento y avance en designtask.md.
**Seguimiento:** [designtask.md](designtask.md).
**Continuación tras aprobación de wireframes:** [Plan de cierre del handoff](handoff_closure_plan.md), creado el 2026-11-09 para completar trazabilidad, revisiones y entrega. Su creación no marca el handoff cerrado ni autoriza implementación.

## 1. Alcance y autorización

Este plan prepara y ejecuta una auditoría documental/funcional y un proceso de decisión; no implementa backend ni Compose. El usuario ha solicitado crear este plan y checklist. Su aprobación autorizará la auditoría y preparación de propuestas; cada decisión funcional nueva y aprobación visual se registrará explícitamente.

No sustituye el plan técnico histórico de ../implementation_plan.md ni marca sus fases como terminadas. El backend pendiente se relacionará con su fase real después de leer el plan y reportes actuales; no se inventarán números de fase.

Los wireframes existentes son borradores. Las capturas de grupos y variantes se inspeccionaron estáticamente el 2026-11-08; interacciones, accesibilidad y aprobación visual permanecen sin verificar. Su existencia no acredita cobertura completa ni implementación. Los hallazgos de diseño se evaluarán después de crear la matriz de dependencias, según instrucción posterior del usuario.

## 2. Fuentes y responsabilidades

| Fuente | Información a contrastar |
|---|---|
| ../Contexto_Maestro_App_OBDII_v2.md | Alcance, requisitos y criterios del MVP. |
| agent-designer-instruction.md | Protocolo UI/UX, estados y criterios de aprobación. |
| domain-contracts.md | Contratos documentados; contrastarlos con código, no asumir vigencia. |
| ui-ux-specification.md / screen-flows.md | Pantallas, estados, acciones, destinos y pendientes. |
| design-decisions.md | Acuerdos DD, alcance, evidencia de aprobación y cuestiones abiertas. |
| design-system.md | Tema real frente a tokens/componentes todavía propuestos. |
| figma-references.md | Referencias y evidencia visual; incorporar los wireframes nuevos al ejecutar. |
| ../implementation_plan.md / ../task.md / ../reporte_fase0.md a ../reporte_fase3.md | Fases, dependencias y evidencia histórica; reconciliar contradicciones sin borrar historia. |
| ../ux_fase1.md / ../prompt_figma_make_ajustes_prototipo.md | Propuestas anteriores; no trasladarlas como acuerdos automáticamente. |

El agente recopila pruebas, explica alternativas e impactos, propone y documenta. El usuario decide sobre comportamiento, alcance y aprobación visual. Un revisor verifica coherencia y trazabilidad; no reemplaza la aprobación del usuario.

La realidad del código determina qué existe; el requisito aprobado determina qué debe existir. Si difieren, registrar una brecha. Ninguna fuente cancela silenciosamente a la otra.

## 3. Auditoría del proyecto

Rutas relativas a la raíz del repositorio. Inspeccionar archivos vigentes mediante Android Studio y buscar consumidores/tests de cada operación relevante.

| Área | Archivos/directorios a revisar | Preguntas |
|---|---|---|
| Vehículos | app/src/main/java/cl/cristian/bitacoraobd/domain/model/Vehicle.kt; domain/repository/VehicleRepository.kt y AppPreferencesRepository.kt; domain/usecase/DeleteVehicleUseCase.kt; data/repository/RoomVehicleRepository.kt | Campos y ausencias, selección activa, borrado/cascada, guardias, éxito parcial Room/DataStore. |
| Inicio | domain/model/DashboardState.kt y DashboardSummary.kt; domain/usecase/GetDashboardDataUseCase.kt, AnalyzeDiagnosticHistoryUseCase.kt y GetMaintenanceStatusUseCase.kt | Vacío, recurrencia real, alertas, cambio de día, vehículo de cada dato. |
| Diagnóstico | domain/model/DiagnosticScanResult.kt y DiagnosticSession.kt; domain/usecase/ScanDiagnosticCodesUseCase.kt y SaveDiagnosticSessionUseCase.kt; data/repository/RoomDiagnosticRepository.kt y DtcCatalog.kt | Captura original, limpio frente a fallo, guardado explícito, reintento, historial y desconocidos. |
| OBD | domain/obd/; data/obd/Elm327ObdGateway.kt; data/obd/connection/Elm327SocketClient.kt | Conexión/cancelación/desconexión reales, exclusión de operaciones, métricas parciales, timeout, permisos. |
| Mantenimiento | domain/model/MaintenanceRecord.kt y MaintenanceType.kt; domain/usecase/SaveMaintenanceUseCase.kt y SaveMaintenanceResult.kt; domain/repository/MaintenanceRepository.kt; data/repository/RoomMaintenanceRepository.kt | Insertar frente a actualizar mismo ID, fechas, próximos límites, confirmación histórica, transacciones. |
| Preferencias | data/local/datastore/DataStoreAppPreferencesRepository.kt y contrato de preferencias | IP/puerto actuales frente a nombre, unidades y visibilidad propuestas; errores y persistencia. |
| Persistencia | data/local/dao/, data/local/entity/, data/local/database/; app/schemas | Consultas, rollback, cascadas y posibles migraciones derivadas de decisiones. |
| Presentation y configuración | MainActivity.kt; ui/; AndroidManifest.xml; app/build.gradle.kts; gradle/libs.versions.toml | Qué pantallas/estados existen realmente, navegación, DI, ciclo de vida, tema y soporte de plataforma. |
| Pruebas | app/src/test/ y app/src/androidTest/ | Aserciones reales por requisito; distinguir prueba existente, ejecución histórica y ejecución actual. |

Las rutas abreviadas de la tabla parten de app/src/main/java/cl/cristian/bitacoraobd/.
No ejecutar toda la suite para demostrar una revisión documental. Si es necesario resolver una duda mediante prueba, registrar comando, entorno, fecha y resultado. No declarar 100% PASS por informes históricos.
Para reglas de plataforma, especialmente permiso de red local, verificar documentación oficial actual y configuración real; conservar URL y fecha.

## 4. Auditoría de Figma

Referencias iniciales:
- Flujos: https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=0-1
- Wireframes: https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2

Pasos:
1. Abrir sesión Figma de auditoría y enumerar páginas/nodos actuales; reutilizar sesión si corresponde al mismo trabajo.
2. Inventariar los frames reales, títulos, IDs, variante, estado y fecha de inspección. No asumir que siguen intactos.
3. Comparar FigJam F01–F10 con DD y recorridos locales, incluyendo los seis casos abiertos.
4. Comparar cada wireframe con UI-01–UI-07 y código: entrada, datos, acciones, destino, errores, vacío, carga, guardado, permisos y confirmaciones.
5. Separar pantallas y variantes que estén mezcladas: por ejemplo lista y detalle del historial; revisar alta frente a edición de vehículo.
6. Verificar cuatro destinos principales, Ajustes secundario, acceso a Conexión e Historial y retornos al origen. Revisar navegación inferior de los borradores, no aceptarla por estar dibujada.
7. Revisar campos, tipos reales de mantenimiento, unidades, null frente a cero, antigüedad/vehículo de lecturas y ausencia de sondeo.
8. Identificar anotaciones técnicas dentro de pantallas y proponer trasladarlas a notas externas; el usuario final no debe ver estados de desarrollo.
9. Capturar evidencia visual por grupo y comprobar texto recortado, jerarquía, controles, scroll, accesibilidad y tamaños de ventana. No certificar TalkBack con una captura.
10. Registrar si hay interacciones de prototipo realmente conectadas. Un botón dibujado no demuestra navegación.
11. Reportar faltantes y propuestas antes de modificar diseños aprobados. Si falla acceso, marcar NO VERIFICADO y continuar la auditoría local; no inventar contenido remoto.

## 5. Construcción de screen-dependencies.md

Crear una fila por interacción o capacidad y variante relevante, con ID estable DEP-001, DEP-002… No renumerar IDs publicados.

Columnas:
- ID; UI y variante; acción/dato; requisito/DD; frame Figma exacto.
- Soporte real: símbolo/archivo y evidencia con fecha.
- Brecha y clasificación: documentación, visual, Presentation, Domain, Data, OBD, plataforma o prueba.
- Dependencias por ID y fase/tarea existente; bloqueo concreto y responsable.
- Criterio de aceptación en formato Dado/Cuando/Entonces.
- Estado funcional: PROPUESTA / ABIERTA / APROBADA / DESCARTADA / DIFERIDA.
- Estado visual: DRAFT / UX_REVIEW / DESIGN_READY / IMPLEMENTING / VALIDATING / DONE.
- Estado técnico: NO_VERIFICADO / PENDIENTE / PARCIAL / IMPLEMENTADO / VERIFICADO.
- Evidencia de aprobación, fecha/revisión y próximo paso.

Para no crear una tabla ilegible, usar una tabla índice compacta y fichas por DEP con los campos extensos. Ausencia de evidencia significa NO_VERIFICADO, no “no existe”.
Cada dependencia debe apuntar a una tarea o a otra DEP; comprobar ciclos y ordenar por prerrequisitos.

Ejemplo de criterio, no aprobación nueva:
Dado un resultado original pendiente, cuando falla su guardado, entonces sigue disponible y Reintentar guardado usa la misma captura sin emitir otra lectura OBD.

Cobertura mínima: primer vehículo/selección/borrado; Inicio vacío y métricas; conexión/permisos/lectura; diagnóstico limpio/con DTC y persistencia; historial lista/detalle; mantenimiento alta/edición/validaciones; ajustes/unidades; salida de formularios y continuidad por recreación.

## 6. Sesiones de decisión agente–usuario

Por indicación del usuario, agrupar las decisiones relacionadas en conjuntos completos, incluso cuando superen tres, sin forzar preguntas adicionales ni inferir aprobaciones.

Por instrucción del usuario del 2026-11-08, tras cada decisión confirmada de este plan se actualizan en la misma sesión la matriz, el registro de decisiones, los flujos y la especificación afectados, además del reporte y seguimiento cuando corresponda. Contrastar los recorridos similares para mantener coherencia UX, respetando las reglas específicas y sin resolver por analogía decisiones aún abiertas. Mantener evidencia, alcance y pendientes sin inferir aprobación visual; Figma se actualiza solo con autorización aplicable.

Trabajar por recorrido, no por una lista indiscriminada de preguntas.

1. Agente presenta una ficha de comparación con ID DEP: acuerdo vigente, evidencia de código, frame actual y diferencia observable.
2. Clasifica: defecto respecto de acuerdo, decisión abierta, propuesta nueva o dependencia técnica ya aprobada. No reabrir acuerdos sin motivo.
3. Explica un escenario concreto y sus consecuencias para el usuario.
4. Cuando haya decisión real, presenta 2–3 alternativas viables, recomendación KISS y coste relativo: solo visual, Presentation, contrato, persistencia/migración o hardware.
5. Usuario elige, pide ajustes, rechaza o difiere. La falta de respuesta no aprueba nada; diferir deja explícito qué variante queda bloqueada.
6. Agente devuelve el acuerdo preciso con alcance, excepciones y criterios Dado/Cuando/Entonces. Si la elección fue ambigua, confirma solo esa ambigüedad.
7. Registra en design-decisions.md: ID DD nuevo o revisión del existente, fecha, evidencia textual de confirmación, alternativas descartadas, razón, DEP/UI/frames afectados e impacto técnico. No inventar fechas o aprobadores.
8. Actualiza especificación, flujos y matriz mediante enlaces a la decisión. Si cambia contrato propuesto, distingue versión propuesta de contrato implementado.
9. Con autorización visual aplicable, actualiza wireframes y FigJam afectados, conserva una referencia de la revisión anterior y registra nodos/fecha en figma-references.md.
10. Presenta al usuario el recorrido actualizado y solicita aprobación visual explícita de frames/variantes concretos. Aprobar la regla no aprueba automáticamente el aspecto.
11. Actualiza estados solo dentro del alcance confirmado; registrar aprobaciones parciales y lo excluido.

Orden sugerido de sesiones:
A. Vehículos y formularios: ausencias de campos, activo y fallos parciales.
B. OBD: permiso, IP/puerto, salida durante lectura y coordinación con pendientes.
C. Diagnóstico: descarte/reescaneo/desconexión, historial y errores.
D. Mantenimiento: mismo ID, validaciones, tipos y programación.
E. Ajustes: momento de guardado, fallo, precisión/redondeo de millas.
F. Inicio y revisión transversal: recurrencia, métricas, vacíos, accesibilidad y recreación.

Priorizar primero decisiones compartidas que bloquean varias pantallas. No convertir odómetro ECU futuro en requisito de esta entrega.

## 7. De wireframe a diseño aprobado y prototipo

DESIGN_READY requiere:
- Objetivo, datos, acciones, estados, navegación y restricciones definidos.
- Sin decisiones abiertas que alteren el comportamiento del alcance aprobado.
- Contratos necesarios definidos aunque su implementación esté pendiente.
- Frames y componentes visuales identificados, revisión visual explícita del usuario registrada.
- Criterios de aceptación y dependencias técnicas vinculados.
- Referencia exacta de Figma y alcance de aprobación, con variantes excluidas si aplica.

Backend pendiente no impide DESIGN_READY si el comportamiento y contrato están cerrados; sí impide declarar integración o DONE. Una anotación “pendiente” no resuelve una decisión abierta.

Design es la referencia visual aprobada. Make es opcional para explorar interacción; verificar capacidades de integración disponibles cuando se use, sin prometer sincronización. Antes de generar en Make preparar un brief con IDs DD/DEP, pantallas, estados, datos ficticios y restricciones. Comparar su salida con la matriz; toda nueva sugerencia vuelve al paso 6. No importar código web como implementación Android.

Un prototipo navegable debe comprobar recorridos principales y alternativos, retorno, cancelación y fallos simulados. Registrar qué interacciones son simuladas y cuáles no están conectadas. La aprobación del prototipo no prueba backend ni hardware.

Si cambia una decisión después de aprobar, marcar las variantes afectadas para nueva revisión; no invalidar pantallas independientes. Conservar historial y referencias anteriores.

## 8. Entrega a implementación Android

Producir por pantalla un paquete enlazado desde la matriz:
- Frames/revisión aprobados y criterios.
- Datos y eventos, estados de carga/éxito/error, guardias, navegación y ciclo de vida.
- Contratos existentes frente a cambios aprobados pendientes.
- Dependencias técnicas ordenadas y tareas de fase.
- Casos de prueba para lógica, persistencia, UI, recreación y permisos según alcance.
- Riesgos y exclusiones explícitos.

Preparar después un plan técnico separado en ../implementation_plan.md conforme a AGENTS.md, preservando la historia pertinente. Solicitar aprobación antes de codificar. Actualizar ../task.md con Fase A Coder, Fase B Tester y Fase C QA; usar subagentes secuenciales y skills requeridas por el repositorio. El checklist de diseño no sustituye el checklist de desarrollo.

Tester debe acreditar la suite completa exigida por AGENTS.md; si faltan dispositivo/permisos/entorno, documentar el bloqueo y no declarar PASS. QA usa ponytail-review y comprueba ejecución del plan. Solo tras pruebas, comparación visual y resolución de diferencias se marca DONE. No confundir revisión documental con QA de aplicación.

## 9. Artefactos y criterio de cierre de esta planificación

Crear al ejecutar:
- screen-dependencies.md: matriz/fichas y orden de dependencias.
- design-review-report.md: auditoría, evidencia, diferencias y limitaciones.
- design-handoff.md: ya existe como borrador preliminar; actualizarlo como índice de paquetes listos para implementar y pendientes después de completar la matriz y las decisiones.

Actualizar según decisiones:
- design-decisions.md, domain-contracts.md, ui-ux-specification.md, screen-flows.md, design-system.md y figma-references.md.
- designtask.md: seguimiento vivo sin marcar acuerdos por anticipado.

Cierre de la etapa de diseño: todas las capacidades previstas tienen DEP, evidencia, estado y próximo paso; cada variante aprobada tiene confirmación verificable; cada pendiente tiene bloqueo/dependencia; no hay promesas de implementación inferidas de Figma. Revisar enlaces, IDs, contradicciones y cobertura antes de entregar.

No exigir que todas las propuestas se implementen: una decisión descartada o diferida queda trazada y fuera del alcance aprobado. No declarar el MVP completo al cerrar esta etapa.
