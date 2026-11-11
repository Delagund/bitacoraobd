# Reporte de cierre — Fase 4.8: Inicio y Ajustes

**Estado:** CERRADA — QA/Ponytail **APROBADO**  
**Plan aprobado:** [implementation_plan_fase4_etapa4_8.md](designplans/implementation_plan_fase4_etapa4_8.md)  
**Fecha:** 2026-11-11

## Resultado

Se completó UI-01/UI-07 respetando las decisiones aprobadas y los contratos existentes. Inicio presenta el dashboard y sus vacíos, diagnóstico reciente y métricas de la última captura disponible. Ajustes mantiene un borrador local y persiste sus preferencias mediante el repositorio DataStore existente. Las unidades modifican la representación; no se reescriben datos almacenados.

La activación de Inicio depende de la ruta HOME y el ciclo RESUMED desde AppNavigation. Se eliminó la activación duplicada desde MainActivity. Las métricas no generan lecturas automáticas y conservan su asociación con el vehículo/contexto.

## Hallazgo corregido y auditoría Ponytail

QA detectó que una solicitud de salida durante un guardado podía descartar el borrador antes de saber si la escritura terminaría. La corrección mantiene la ruta pendiente mientras se guarda; solo continúa tras éxito. Si falla, conserva borrador y error para reintentar.

Se agregaron regresiones para ese caso, el reintento seguido de navegación, error de lectura inicial, actualización fallida conservando el dashboard actual y cambio de vehículo. La prueba instrumentada de AppNavigation cubre HOME + RESUMED y retorno por pestaña con reloj mutable. QA re-revisó los cambios conectados y no encontró hallazgos bloqueantes de navegación, persistencia, aislamiento por vehículo, SOLID, KISS o YAGNI.

No se añadieron dependencias, cambios de esquema ni ampliaciones de Domain/Data.

## Verificación ejecutada

- `:app:testDebugUnitTest --rerun-tasks`: **137 pruebas, 0 fallos, 0 omitidas**.
- `:app:compileDebugAndroidTestKotlin`: compilación exitosa.
- `:app:connectedDebugAndroidTest` en SM-A066M / API 36: **27 pruebas, 0 fallos, 1 omitida** por falta de `elm327Host`.
- `:app:lintDebug`: BUILD SUCCESSFUL.
- `:app:assembleDebug`: BUILD SUCCESSFUL.
- `git diff --check`: exitoso.

## Límites pendientes

No se realizó revisión visual manual de variantes, accesibilidad, TalkBack, texto ampliado, IME o temas claro/oscuro. El veredicto aprueba el alcance funcional ejecutable y no certifica esos aspectos visuales. La integración ELM327 quedó omitida por no configurar `elm327Host`; no se validó adaptador físico. No se modificó Figma.

## Cierre

Checklist de Fase 4.8 finalizado en [task.md](task.md). Detener la ejecución aquí y esperar indicación del usuario para el siguiente paso.
