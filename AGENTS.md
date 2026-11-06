# Flujo de Trabajo Estandarizado (Multi-Agente)

Este documento define el protocolo de ejecución obligatorio para todas las fases de desarrollo y refactorización en este repositorio.

## 1. Fase de Planificación (Planning Mode)

Antes de modificar cualquier código, el agente principal DEBE:

1. Leer los requerimientos de la fase correspondiente. El contexto del proyecto completo está en `/plans/Contexto_Maestro_App_OBDII_v2.md`
2. Auditar el código actual relevante para entender el contexto.
3. Crear o actualizar un artefacto `implementation_plan.md` con la propuesta técnica detallada en directorio `/plans` de la raiz del proyecto.
- **Restricciones de Diseño**: Respetar siempre los principios **KISS** (evitar sobreingeniería/dependencias de terceros), **YAGNI** y **SOLID**.
4. **BLOQUEO**: Detenerse y esperar la orden explícita del usuario (`Proceed` o confirmación en chat) antes de iniciar la codificación.

## 2. Fase de Seguimiento (Checklist)

Una vez aprobado el plan, el agente principal DEBE:

1. Generar/Actualizar el artefacto `task.md` que servirá como un To-Do list vivo y guárdalo en la carpeta 'plans' de la raíz del proyecto.
2. Dividir el trabajo en tres sub-etapas claramente marcadas:
    - **Fase A**: Implementación de Código (Coder)
    - **Fase B**: Tests TDD (Tester)
    - **Fase C**: Code Review & QA (Ponytail Reviewer)

## 3. Ejecución Delegada (Sistema Multi-Agente)

El trabajo se debe orquestar invocando subagentes secuenciales y especializados:

### A. Subagente Coder

- **Objetivo**: Implementar la lógica de negocio pura basada en el plan aprobado.
- **Restricción**: Solo escribe código. NO ejecuta pruebas. NO implementa funcionalidades no especificadas en el plan.

### B. Subagente Tester (TDD)

- **Objetivo**: Escribir pruebas unitarias/integración/regresión para los nuevos cambios y adaptar las pruebas antiguas (legacy) que se hayan roto.
- **Restricción**: NO se da por terminada su tarea hasta garantizar un **100% PASS** en la suite completa.

### C. Subagente QA / Reviewer (Ponytail)

- **Objetivo**: Auditar el código resultante bajo los lineamientos de la skill _Ponytail_.
- **Criterios de Evaluación**:
    - Legibilidad extrema y orden.
    - Validación de que no se introdujeron abstracciones especulativas ni dependencias pesadas (Anti-Sobreingeniería).
    - Confirmación de que el plan se ejecutó al 100%.

## 4. Cierre y Reporte

Tras el veredicto "APROBADO" del QA, el agente principal DEBE:

1. Marcar el `task.md` como finalizado.
2. Generar un reporte formal de cierre en la carpeta `/plans/reporte_faseX.md` con un resumen técnico de los logros.
3. Detenerse y consultar al usuario sobre los pasos a seguir.

## 5. Herramientas y Contexto Obligatorio (Tools & Skills)

Para orquestar este flujo, los agentes deben usar consistentemente el ecosistema de agentes:

- **Herramienta `writing-plans`**: Usalo para crear cada plan de trabajo de cada fase del proyecto.
- **Herramienta `dispatching-parallel-agents`**: Obligatoria para derivar el trabajo a los roles (Coder, Tester, QA) creando hilos secuenciales de trabajo (usualmente pasando `TypeName="self"` y el prompt con las directrices) Primero 'coder', luego 'tester' y finalmente 'QA'.
- **Herramienta `ponytail:ponytail: Lazy senior dev mode`**: junto con el agente 'Coder' como criterio para codificar la implementacion.
- **Herramientas de Artefactos**: Uso de la skill adecuada que permita la creación y gestión viva de `implementation_plan.md`, `task.md` y `reporte_faseX.md` (ubicados preferentemente en `/plans`).
- **Ejecución de Pruebas (Tester)**: Pendiente de determinar plan de pruebas.
- **Skill Ponytail (QA)**: El agente Reviewer debe invocar la mentalidad de la skill `ponytail-review` para asegurar que el código producido es "Lean", que cumple con el principio KISS y YAGNI, y que rechaza contundentemente la sobre-ingeniería (clases inútiles, abstracciones excesivas, dependencias de terceros superfluas).
