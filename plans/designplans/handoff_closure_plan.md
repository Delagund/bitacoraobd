# Plan de cierre del handoff de diseño

> **Para agentes:** ejecutar por tareas con la skill executing-plans. Aplicar el protocolo secuencial del repositorio al ejecutar las revisiones: editor documental → verificador de evidencia → QA/Ponytail. La creación de este plan no ejecuta esas tareas.

**Objetivo:** entregar paquetes UI-01–UI-07 trazables y revisados, con estado de preparación, contratos, criterios y pendientes suficientes para preparar la implementación Android.

**Arquitectura documental:** conservar los IDs DD, DEP y UI existentes. design-handoff.md es el índice de entrega; las reglas viven en decisiones, flujos y especificación; las evidencias visuales en figma-references.md; soporte y brechas en la matriz y contratos. Enlazar las fuentes para evitar copias contradictorias.

**Herramientas:** Markdown, herramientas studio_ de Android Studio, Figma MCP para verificar Design/FigJam, skills de ejecución y revisión. Sin dependencias nuevas.

**Fecha:** 2026-11-09.
**Estado:** EN EJECUCIÓN. El usuario indicó iniciar el paso 1 y, el 2026-11-10, continuar con el paso 2. Los resultados se registran sin marcar tareas anticipadamente. La validación de variantes y prototipo, y las observaciones de legibilidad de conectores F03 y navegación inferior, se posponen a la revisión final de cierre salvo que bloqueen un recorrido; no se consideran realizadas ni se declara DESIGN_READY sin evidencia; ver [reporte de saneamiento](../reporte_saneamiento_visual.md).
**Plan padre:** [implementation_plan.md](implementation_plan.md), especialmente §§7–9.
**Seguimiento existente:** [designtask.md](designtask.md).

## 1. Alcance y autorizaciones

- El usuario confirmó en chat la aprobación de los wireframes revisados UI-01–UI-07 y variantes. Conservar esta aprobación; no solicitarla nuevamente para los mismos contenidos.
- El usuario aprobó F03 tras añadir corregir/revalidar y cancelar al origen sin guardar, diferenciando formato inválido de fallo TCP/inicialización. Mantener la evidencia de esa condición y comprobar su representación final.
- Revisar trazabilidad, suficiencia y coherencia de la entrega. La aprobación visual no demuestra cobertura exhaustiva, un prototipo conectado, accesibilidad ejecutada ni backend integrado.
- Este plan permite proponer/documentar contratos pendientes de implementación; no cambiar contratos Kotlin, esquema, versiones, dependencias ni código de producción.
- Una discrepancia observable contra una regla aprobada no autoriza inventar otra regla. Presentar solo las diferencias reales que necesiten decisión; continuar con los paquetes independientes.
- No rediseñar pantallas aprobadas durante el cierre. Si se requiere una modificación visible adicional, identificar frame, motivo y alcance para obtener autorización aplicable.
- El odómetro ECU de F10/DD-21 continúa como capacidad futura. No convertirlo en prerrequisito del MVP.
- Mantener KISS, YAGNI y SOLID: reutilizar documentos e IDs; no crear un inventario duplicado ni un sistema de seguimiento nuevo.

## 2. Línea base y diferencias que motivan el plan

Inspección preparatoria local para redactar este plan:

1. MainActivity.kt sigue mostrando Greeting("Android"): existe soporte Domain/Data, pero las pantallas MVP no están integradas. No atribuir comportamiento ejecutable a Figma.
2. design-handoff.md registra la aprobación en su cabecera, pero varias filas todavía dicen aprobación visual, precisión/redondeo o permisos UX pendientes pese a decisiones posteriores.
3. designtask.md conserva sesiones A–F y aprobación visual sin marcar, y describe 21 DEP aunque el índice se amplió a DEP-023.
4. screen-dependencies.md recibió aprobación visual de wireframe en las fichas, pero conserva textos históricos que afirman que no existe aprobación. Reconciliar la lectura actual sin borrar evidencia histórica pertinente.
5. Las referencias del handoff mezclan frames originales y revisiones 10:2/11:2/13:2. Resolver por variante cuál es la referencia vigente.
6. La actualización de F03 quedó documentada en flujos y especificación; falta conciliar su registro DD, DEP-007 y referencias de FigJam.
7. Las variantes añadidas posteriormente en Design deben inspeccionarse en el lienzo: nombre o duplicación de un frame no demuestra que su contenido represente conectado, fallo parcial, carga o error.
8. Revisar especialmente el significado de “selección parcial”: DD-27 describe vehículo creado con fallo al persistir el activo. No asumir que permite seleccionar/guardar solo algunos DTC; contrastar cualquier representación semejante con el contrato de captura íntegra.

Estas observaciones son entradas de revisión. No equivalen al veredicto de cierre ni certifican el estado remoto actual.

## 3. Archivos y responsabilidad

Rutas relativas a la raíz del repositorio.

| Archivo | Trabajo previsto |
|---|---|
| plans/designplans/design-handoff.md | Índice final y ficha de entrega por pantalla; alcance listo, bloqueos y orden de implementación. |
| plans/designplans/screen-dependencies.md | DEP-001–023, vínculos completos y estados consistentes; sin renumerar. |
| plans/designplans/design-decisions.md | Actualizar acuerdos vigentes, evidencia de aprobación y revisión F03; no reabrir DD confirmadas. |
| plans/designplans/screen-flows.md | Entradas, acciones, retornos y caminos de recuperación vigentes. |
| plans/designplans/ui-ux-specification.md | Datos, estados, acciones y criterios por UI. |
| plans/designplans/domain-contracts.md | Contratos existentes frente a extensiones aprobadas aún no implementadas. |
| plans/designplans/figma-references.md | Frames/nodos vigentes por variante, alcance de aprobación y evidencia de inspección. |
| plans/designplans/design-system.md | Identificar referencia visual/componentes utilizados y límites de accesibilidad o adaptación. |
| plans/designplans/design-review-report.md | Hallazgos, resolución, evidencia y revisiones de cierre. |
| plans/designplans/designtask.md | Seguimiento vivo de tareas y criterios cumplidos. |
| plans/designplans/implementation_plan.md | Enlace a este cierre y estado consolidado del plan padre. |
| plans/reporte_cierre_handoff.md | Crear al concluir: veredicto QA, listo/bloqueado, exclusiones, evidencia y paso siguiente. |

Fuentes de consulta: plans/Contexto_Maestro_App_OBDII_v2.md, plans/implementation_plan.md, plans/task.md, planes/reportes de fases y plans/plan_tecnico_fase4_borrador.md. Los históricos se conservan; una sección histórica superada debe indicar qué registro vigente la reemplaza.

Código que se contrasta, sin modificar: app/src/main/java/cl/cristian/bitacoraobd/{MainActivity.kt,domain/,data/,ui/theme/}; pruebas existentes en app/src/test/ y app/src/androidTest/. Resolver archivos con studio_find_files y leer los necesarios; no asumir firmas desde documentos.

## 4. Paso 1 — fijar la revisión aprobada

**Archivos:** figma-references.md, design-decisions.md, designtask.md.

- [ ] Registrar el texto de aprobación del usuario, fecha disponible y alcance UI-01–UI-07; distinguir wireframes de pantallas ejecutables.
- [ ] Localizar en Design las revisiones 10:2, 11:2, 13:2 y variantes posteriores. Inventariar únicamente lo necesario para la entrega: UI, variante, ID exacto, revisión vigente y fuente de aprobación.
- [ ] Identificar originales sustituidos sin eliminarlos; el implementador debe poder reconocer una sola referencia vigente por variante.
- [ ] Contrastar F03 2:220 en FigJam con la bifurcación aprobada y conservar su aprobación. La nota de aprobación no sustituye verificar los destinos de los conectores.
- [ ] Registrar qué elementos son estáticos, cuáles tienen interacción y cuáles no fueron comprobados.

**Salida:** tabla de referencias vigentes y alcance de aprobación verificable. No exigir aprobación de nuevo para la misma revisión.

## 5. Paso 2 — completar la trazabilidad por recorrido

**Archivos:** design-handoff.md y screen-dependencies.md; enlaces a decisiones, especificación, flujos y contratos.

Trabajar en este orden para revisar primero los contextos y guardias compartidos:

| Orden | Paquete | DEP a revisar | Escenarios obligatorios |
|---|---|---|---|
| 1 | UI-02 Vehículos | 001–003, 019 | Primer vehículo; guardar y seleccionar por separado; fallo parcial sin duplicación; activo; edición año ausente/km; borrado/cascada y recuperación; restricción OBD. |
| 2 | UI-03 Conexión | 006–009 | Permiso/concesión/denegación; IP/puerto inválidos; revalidación/cancelación; fallo TCP/init; handshake y fallo al guardar config; Connecting/Connected/Disconnecting; lectura explícita y null. |
| 3 | UI-04 Diagnóstico | 010–012 | Limpio/con DTC/desconocido; fallo sin historial; captura original; guardar/reintentar/descartar; nuevo escaneo; salida y desconexión. |
| 4 | UI-05 Historial | 013–014 | Lista/vacío/carga/error; orden; sesión limpia; detalle; recurrencia del mismo vehículo; retorno al origen. |
| 5 | UI-06 Mantenimiento | 015–017, 019 | Lista y programación vigente; siete tipos alertables y OTHER; alta/edición mismo ID; fechas/km; confirmación histórica; borrador y errores. |
| 6 | UI-07 Ajustes | 018–019 | Nombre opcional; unidades; tres interruptores; guardar/descarte/error parcial; km enteros y millas con un decimal; campos no editados conservan valor. |
| 7 | UI-01 Inicio y transversal | 004–005, 020–023 | Sin vehículos; jerarquía vehículo/mantenimiento/diagnóstico/métricas; recurrencia; carga/error por contexto; fecha al activar; cuatro destinos, retornos, recreación y fin de proceso. |

- [ ] Para cada paquete, enlazar RF/DD, DEP, frame exacto, símbolo/archivo real y evidencia de prueba existente o caso futuro.
- [ ] Comprobar en el lienzo el contenido de las variantes citadas. Contrastar textos, acciones y conexiones con reglas aprobadas; no validar solo el nombre del frame.
- [ ] Revisar éxito, vacío, carga inicial, actualización con datos, error y recuperación cuando apliquen. Marcar “no aplica” con motivo concreto.
- [ ] Documentar entradas/origen, datos y unidades, acciones/eventos, estados, guardias, efectos persistentes y destino de cada salida.
- [ ] Vincular componentes/estilos al inventario visual existente; identificar límites concretos de contraste, tamaño táctil, scroll y adaptación para la implementación.
- [ ] Registrar criterios Dado/Cuando/Entonces observables por escenario, con datos ficticios y resultado esperado.

**Formato de ficha por pantalla:** alcance aprobado; referencias; RF/DD/DEP; datos; eventos y estados; navegación/guardias; contratos reales/propuestos; criterios y evidencia; dependencias; exclusiones; estado/próximo paso.

**Salida:** siete paquetes y cobertura transversal, sin variantes confundidas ni referencias a grupos presentadas como prueba de un estado específico.

## 6. Paso 3 — resolver huecos y delimitar el backend pendiente

**Archivos:** domain-contracts.md, design-handoff.md, screen-dependencies.md y design-review-report.md.

- [ ] Leer las firmas y consumidores relevantes de cada operación citada; confirmar tipo, resultado, nulabilidad, persistencia y límites. Ampliar lectura solo ante discrepancia o dependencia.
- [ ] Separar soporte actual de trabajo pendiente: Presentation/navegación, ampliación de preferencias, permiso runtime, coordinación de captura/guardado y recálculo al activar Inicio.
- [ ] Para contratos pendientes, documentar datos de entrada/salida, defaults, errores y responsabilidad necesaria; señalar que son propuestos/aprobados para implementar y no API existente.
- [ ] Registrar brechas con UI/DEP, hecho comprobado, efecto para el usuario, resolución, responsable y condición de cierre.
- [ ] Clasificar cada hallazgo: documental corregible; discrepancia visual contra acuerdo; decisión funcional nueva; dependencia de implementación; validación futura.
- [ ] Corregir documentación que contradiga decisiones ya confirmadas. Para decisiones nuevas o cambios de diseño aprobado, presentar un grupo de cuestiones concretas al usuario y continuar con paquetes independientes.
- [ ] F03: verificar que formato inválido no abra socket, corregir permita revalidar, cancelar descarte y vuelva al origen; un fallo TCP/init permita reintentar, y fallo de persistencia tras handshake reintente solo guardado.
- [ ] Mantener identificadas las operaciones separadas Room/DataStore y evitar prometer atomicidad común o reintentos que creen duplicados.

**Salida:** cero decisiones funcionales sin resolver dentro de un paquete listo; dependencias técnicas descritas con suficiente precisión para planificarlas.

## 7. Paso 4 — reconciliar los artefactos

**Archivos:** todos los documentos de diseño de §3 afectados.

- [ ] Revisar evidencia entre decisiones, flujos, especificación, matriz, referencias y handoff; aplicar las reglas vigentes en todos los documentos afectados.
- [ ] Incorporar F03 al acuerdo DD vigente y DEP-007 sin inventar una decisión distinta a la aprobada.
- [ ] Corregir el conteo a 23 DEP y separar aprobación visual, preparación de diseño y estado técnico.
- [ ] Retirar de los bloqueos actuales los acuerdos ya cerrados: guardado de Ajustes DD-36, redondeo DD-37, carga/error DD-39/40, LAN DD-34 y aprobación de wireframes. Conservar la implementación pendiente.
- [ ] Señalar como históricas las afirmaciones superadas, evitando secciones de estado actual que se contradigan.
- [ ] Revisar enlaces locales, anclas DD/DEP y referencias Figma; resolver todo enlace roto o registrar el acceso no verificable.
- [ ] Confirmar cobertura DEP-001–023 sin duplicados/ciclos y un próximo paso para cada brecha.

**Salida:** lectura consistente del estado actual desde cualquier documento principal.

## 8. Paso 5 — evaluar preparación por paquete

- [ ] Aplicar los criterios DESIGN_READY del plan padre §7 a cada paquete: datos/eventos/estados/guardias definidos; comportamiento cerrado; contratos definidos; referencia y aprobación; criterios y dependencias trazables.
- [ ] Marcar DESIGN_READY solo con evidencia de esos criterios. Registrar bloqueado cuando falte una decisión o referencia que impida implementar con certeza; no inferir preparación desde aprobación global.
- [ ] Mantener por separado estado técnico PENDIENTE/PARCIAL/IMPLEMENTADO/VERIFICADO. Backend pendiente puede coexistir con diseño listo si contrato y comportamiento están definidos.
- [ ] Evaluar el requisito de prototipo navegable del contexto §16.4: comprobar evidencia existente. Si falta, registrar el bloqueo y proponer prototipo mínimo en Design o aceptación explícita del usuario de una entrega estática; Make es opcional. No dar por dispensado el requisito por la aprobación del wireframe.
- [ ] Vincular las comprobaciones futuras de Compose, accesibilidad, ventanas, permisos y hardware a responsables/casos de prueba; no inventar resultados.

**Salida:** tabla por paquete con estado, evidencia y bloqueo. Si subsiste un bloqueo, entregar “cierre parcial” con pendientes concretos.

## 9. Paso 6 — revisiones y presentación del handoff

Ejecutar los roles secuencialmente, siguiendo el protocolo del repositorio y la skill dispatching-parallel-agents para la delegación, respetando su orden:

- [ ] **Fase A — Editor documental (Coder):** completar fichas y conciliación autorizada; trabajar solo en documentación. No ejecutar pruebas Android ni implementar funcionalidades.
- [ ] **Fase B — Verificador (Tester):** revisar cobertura RF/DD/DEP/UI, enlaces, firmas y evidencia remota; comprobar casos previstos y estados. Al tratarse de documentos, el resultado exigible es consistencia documental, no “100% PASS” de una suite Android que no se ejecuta.
- [ ] **Fase C — QA/Ponytail Reviewer:** usar ponytail-review y revisar KISS/YAGNI, alcance, veracidad de evidencia, aprobación preservada y cumplimiento del plan. No sustituir la decisión del usuario.
- [ ] Resolver observaciones y devolver al verificador/revisor afectado hasta tener veredicto APROBADO o bloqueos concretos.
- [ ] Presentar al usuario el handoff con paquetes listos, bloqueados, contratos pendientes, exclusiones y validaciones futuras.

**Validación documental mínima:** 7 UI cubiertas; DEP-001–023 cubiertas; cada capacidad incluida enlaza un criterio, contrato y referencia; ningún hecho técnico nuevo sin fuente; ningún pendiente sin responsable/próximo paso; ninguna afirmación de implementación/pruebas basada solo en Figma.

No crear ni ejecutar pruebas de aplicación por esta revisión documental. Si una duda técnica exige ejecución para resolverse, explicar su necesidad y registrar alcance/entorno/resultado por separado.

## 10. Paso 7 — cierre y preparación de implementación

- [ ] Consolidar design-handoff.md como entrega final o parcial según el veredicto, con revisión y alcance.
- [ ] Actualizar designtask.md y el estado del plan padre solo en las tareas acreditadas.
- [ ] Crear plans/reporte_cierre_handoff.md con resultado de las revisiones, referencias, aprobación del usuario, bloqueos y exclusiones.
- [ ] Presentar el cierre documental al usuario y consultar el siguiente paso conforme a AGENTS.md.
- [ ] Tras autorización para preparar la implementación, usar el handoff cerrado como entrada de un plan técnico separado en plans/implementation_plan.md, preservando historia y contrastando el borrador de fase 4.
- [ ] Ese plan técnico especificará archivos, cambios de contratos necesarios, orden de dependencias y validación; actualizará plans/task.md con Coder → Tester → QA tras aprobarse.
- [ ] Esperar aprobación explícita del plan técnico antes de codificar, según AGENTS.md. La aprobación visual existente se conserva como entrada.

**Cierre total:** siete paquetes y DEP transversales trazables, criterios DESIGN_READY acreditados para el alcance entregado, requisito de prototipo resuelto o excepción explícita registrada, cero decisiones abiertas dentro del alcance listo, QA aprobado y reporte/checklist consistentes.

**Cierre parcial:** entregar el mismo inventario señalando exactamente qué paquetes no pasan y qué acción/evidencia los libera. No declarar cerrada la etapa completa mientras queden bloqueos de entrega.
