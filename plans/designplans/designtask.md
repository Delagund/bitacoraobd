# Seguimiento — matriz y aprobación de diseño

## Reconciliación y evaluación de cierre — en revisión

- [x] Editor comprobó 346 enlaces locales Markdown en 12 documentos de diseño: 49 destinos accesibles y anclas contrastadas con encabezados/IDs. La comprobación automatizada no acredita semántica de todos los destinos ni enlaces externos sin nodo.
- [x] Editor resolvió 107 referencias Figma/FigJam con ID extraídas de esos documentos y registró [alcance y límites](figma-references.md#revisión-de-cierre--evidencia-y-límites).
- [x] Editor evaluó [UI-01–UI-07](design-handoff.md#evaluación-de-preparación-por-paquete--revisión-de-cierre); ninguna se declara DESIGN_READY.
- [x] Verificador independiente contrastó 346 enlaces locales y 49 destinos, revisó semántica y detectó tres puntos de contexto histórico en matriz, flujos y seguimiento. Se corrigieron, incluidas las 21 menciones históricas de aprobación visual en la matriz, y se revalidaron. La existencia de enlaces no demuestra por sí sola la validez del contenido remoto.
- [x] QA/Ponytail: **APROBADO para el alcance documental revisado**, conservando los bloqueos y límites registrados. Este veredicto no afirma verificación independiente por QA de enlaces o Figma, ni validación visual, interactiva o de accesibilidad.
- [ ] Resolver requisito de prototipo navegable mediante validación o aceptación explícita de entrega estática; revisar variantes nuevas y accesibilidad. Cambios en Figma requieren autorización aplicable.
- [ ] Planificar al inicio de fase 4 idempotencia DEP-011 y demás backend necesario para la UI, según [seguimiento principal](../task.md#entrada-prioritaria-para-fase-4--backend-requerido-por-ui).



**Avance parcial paso 3, 2026-11-10:** editor documentó [firmas, consumidores y brechas por paquete](domain-contracts.md#paso-3--contraste-de-contratos-y-consumidores-2026-11-10) y [clasificación](design-review-report.md#paso-3--clasificación-parcial-2026-11-10). El verificador señaló estados DRAFT y falta de aprobación sin contexto histórico, y detalle insuficiente del contrato propuesto de Ajustes; se corrigieron y se atribuyó conversión y redondeo a DD-37. QA/Ponytail emitió **APROBADO para este alcance parcial**. Siguen pendientes la cobertura exhaustiva del paso 3 y la revisión final de variantes, interacciones, accesibilidad y prototipo; no se declara el paso completo, cierre ni DESIGN_READY. No se ejecutaron pruebas Android.

**Continuación paso 3:** se amplió el [contraste por operación](domain-contracts.md#ampliación-del-contraste-por-operación--paso-3), cubriendo UI y DEP-001–023, consumidores, entradas/salidas, errores, condiciones de cierre y F03. El verificador independiente revisó la ampliación y QA/Ponytail emitió **APROBADO para el alcance documental ampliado**. Esta revisión actualiza el estado parcial anterior; siguen pendientes la verificación exhaustiva de enlaces y el cierre visual, interactivo, de accesibilidad y prototipo. Android/Data debe resolver idempotencia ante resultado incierto y guardias en el plan técnico. No se declara DESIGN_READY ni cierre integral.

Plan: [implementation_plan.md](implementation_plan.md).
Estado: plan aprobado el 2026-11-08. El usuario aprobó en chat el 2026-11-09 los wireframes revisados de UI-01–UI-07 y variantes; evidencia en figma-references.md. Sigue pendiente cerrar la trazabilidad y preparar handoff técnico; la aprobación visual no acredita interacción, accesibilidad implementada ni integración.
Marcar cada casilla solo con evidencia enlazada en matriz, reporte o registro de decisiones.

## Preparación
- [x] Crear plan y checklist solicitados.
- [x] Usuario revisa y aprueba el alcance del plan (registro en implementation_plan.md, 2026-11-08).
- [x] Registrar línea base documental, código y referencias Figma con fecha: [reporte preparatorio](design-review-report.md#línea-base--2026-11-08).

## Auditoría
- [x] Revisar requisitos, decisiones y los seis casos abiertos; contradicciones y alcance residual en [reporte](design-review-report.md#auditoría-por-recorridos--2026-11-08).
- [x] Contrastar contratos y consumidores de vehículos/Inicio; evidencia en reporte.
- [x] Contrastar diagnóstico/historial y persistencia; evidencia en reporte.
- [x] Contrastar OBD, cancelación, permisos y configuración; evidencia y fuente Android en reporte.
- [x] Contrastar mantenimiento y actualización del mismo ID; evidencia en reporte.
- [x] Contrastar preferencias, unidades y métricas compartidas; evidencia en reporte.
- [x] Revisar Presentation, tema, configuración y pruebas existentes; evidencia en reporte.
- [x] Reconciliar alcance histórico y contradicciones documentales identificadas sin borrar historia; diferencias que requieren decisión conservadas en [reporte](design-review-report.md#reconciliación).
- [x] Inspeccionar visualmente F01–F10 y decisiones abiertas mediante MCP; hallazgos en [reporte](design-review-report.md#inspección-visual--2026-11-08).
- [x] Inspeccionar grupos de Design 3:2 y seis variantes 9:2; estados faltantes en [reporte](design-review-report.md#inspección-visual--2026-11-08). La validación interactiva y accesibilidad siguen pendientes.
- [ ] Separar notas técnicas de contenido visible al usuario durante la revisión de diseño posterior a la matriz; observaciones registradas.
- [x] Evaluar por recorrido los hallazgos registrados después de crear la matriz: [evaluación y decisiones abiertas](design-review-report.md#evaluación-de-hallazgos-posterior-a-la-matriz--2026-11-08). La validación interactiva y accesibilidad siguen pendientes.

## Matriz
- [x] Crear [screen-dependencies.md](screen-dependencies.md) con 21 IDs DEP estables.
- [x] Vincular capacidades con UI, acuerdos, código, pruebas inspeccionadas y referencias de grupos o frames; las variantes no identificadas quedan por verificar.
- [x] Clasificar brechas y separar estados funcional/visual/técnico.
- [x] Especificar criterios Dado/Cuando/Entonces, sin cerrar las decisiones abiertas.
- [x] Relacionar dependencias con tareas existentes y verificar ausencia de ciclos; sin asignar fases técnicas futuras no definidas.
- [x] Revisar cobertura UI-01–UI-07 y estados transversales a nivel de matriz; revisión detallada de variantes y hallazgos pendiente.

## Decisiones conjuntas
- [ ] Sesión A: vehículos/formularios y fallos parciales. DD-42 y [primeras variantes editables](figma-references.md#vehículos--revisión-editable-autorizada-2026-11-08); faltan estados conectado, fallos, confirmaciones, interacción, accesibilidad y aprobación visual.
- [ ] Sesión B: OBD/permisos/configuración. Salida durante lectura DD-33, recuperación LAN DD-34, salida con IP/puerto DD-35 y presentación de conexión/métricas DD-48 confirmadas; pendientes revisión visual, detalles de plataforma e implementación.
- [ ] Sesión C: resultados pendientes e historial. Presentación de códigos, limpio e historial definida en DD-47; diseño visual, interacción y validación pendientes.
- [ ] Sesión D: mantenimiento. Lista, acción principal y vacío definidos en DD-43; orden y programación en DD-44; presentación visual, variantes y validación pendientes.
- [ ] Sesión E: Ajustes y redondeo de entradas. Guardado explícito DD-36 y conversión de millas DD-37 confirmados; validación visual y pruebas de implementación pendientes.
- [ ] Sesión F: Inicio y requisitos transversales. Recurrencia DD-38, carga/error DD-39/40, actualización DD-41, jerarquía DD-45 y vacíos DD-46 confirmados; implementación y validación pendientes.
- [ ] Registrar alternativas, elección, alcance, fecha y evidencia.
- [ ] Registrar rechazos/diferimientos y bloqueos sin inferir aprobación.
- [ ] Actualizar DD, contratos propuestos, especificación, flujos y matriz.

## Diseño y prototipo
- [ ] Actualizar frames afectados con autorización aplicable. [Revisión editable ampliada](figma-references.md#revisión-de-wireframes-por-recorrido--2026-11-09) cubre estados principales; siguen faltando variantes, controles y revisión del usuario.
- [ ] Actualizar FigJam y figma-references.md. [Sincronización funcional parcial](figma-references.md#figjam--sincronización-funcional-parcial-2026-11-08): faltan contraste exhaustivo, variantes y corrección de conectores.
- [ ] Revisar accesos secundarios, cuatro destinos y retornos.
- [ ] Completar variantes de vacío/carga/error/confirmación.
- [ ] Revisar legibilidad, scroll, accesibilidad y adaptación.
- [ ] Decidir si aporta valor un prototipo en Make o continuar en Design.
- [ ] Si se usa Make, comparar su salida con DD/DEP sin ampliar alcance.
- [ ] Verificar interacciones reales del prototipo y señalar simulaciones.
- [ ] Revisar y obtener aprobación visual de las variantes posteriores o cambios que la requieran; conservar la aprobación de wireframes UI-01–UI-07 del 2026-11-09 sin solicitarla de nuevo para el mismo contenido.
- [ ] Marcar DESIGN_READY solo donde se cumplen todos los criterios.

## Entrega para implementación posterior

- [x] Crear el [plan de cierre del handoff](handoff_closure_plan.md) solicitado; tareas de ejecución y revisiones aún pendientes.
- [ ] Ejecutar pasos 1–5: revisión aprobada, paquetes UI-01–UI-07, brechas, conciliación y preparación. Paso 2: [fichas y criterios](design-handoff.md#paso-2--fichas-ui-02-ui-03-y-ui-04-en-revisión) y DEP-001–023 revisados; QA aprueba la trazabilidad documental. Se añadieron [estados visuales](figma-references.md#estados-añadidos--2026-11-10) para Conexión, Inicio y Ajustes; verificación estructural y QA acotado aprobados. Falta conciliación en pasos 3–5 y revisión final de variantes; no DESIGN_READY. Las observaciones visuales previas se posponen hasta el final salvo bloqueo.
- [ ] Ejecutar paso 6: revisión secuencial documental y QA con evidencia.
- [ ] Ejecutar paso 7: cierre total/parcial, reporte y presentación al usuario.
- [ ] Completar [design-handoff.md](design-handoff.md) existente, hoy borrador preliminar, con paquetes, dependencias y pruebas previstas.
- [ ] Verificar trazabilidad requisito → DD → DEP → frame → contrato → prueba.
- [ ] Revisar enlaces, contradicciones y pendientes.
- [ ] Presentar cierre de diseño y alcance listo/bloqueado al usuario.
- [ ] Preparar plan técnico posterior y solicitar aprobación antes de código.

## Fases de desarrollo posteriores — no ejecutadas por este plan
- [ ] Fase A: Coder implementa exclusivamente el plan técnico aprobado.
- [ ] Fase B: Tester aporta evidencia de pruebas según AGENTS.md.
- [ ] Fase C: QA/Ponytail revisa requisitos, simplicidad y validación visual.
- [ ] Actualizar task.md y reporte de fase; marcar DONE solo con evidencia.
