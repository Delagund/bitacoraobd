# Reporte de cierre — Fase 4, etapa 4.7

**Alcance:** UI-05 Historial y UI-06 Mantenimiento.  
**Estado:** implementación y QA/Ponytail completados; veredicto QA **APROBADO**.  
**Diseño:** el usuario confirmó el selector Material 3 de ocho tipos y prefirió entrega estática. No se modificó Figma ni se declara aprobado un prototipo o variante visual posterior.

## Resultado

Se implementaron lista de historial y detalle por vehículo, incluyendo sesiones limpias, fecha/hora, kilometraje, códigos, recurrencia y orientación existente; se conserva el código desconocido con orientación genérica. El acceso al historial está conectado desde Inicio y Diagnóstico y el detalle valida la pertenencia al vehículo de contexto.

Mantenimiento incluye lista por vehículo, estados de programación, alta y edición del mismo registro, selector de los ocho tipos y confirmación para kilometraje histórico. Los casos de uso y repositorios existentes siguen siendo la fuente de reglas. No se modificó Domain/Data, el esquema Room ni las dependencias.

La QA inicial detectó dos riesgos concretos y bloqueantes: una respuesta tardía de alta/edición podía volver a publicar un borrador después de salir o cambiar de vehículo; y la alerta podía quedar desactualizada al regresar si no había una nueva emisión de Room. Se corrigieron en Presentation y se añadieron tres pruebas deterministas con `CompletableDeferred`. La nueva revisión Ponytail confirmó las correcciones y no encontró más hallazgos.

## Verificación ejecutada

- `./gradlew :app:testDebugUnitTest --tests cl.cristian.bitacoraobd.ui.maintenance.MaintenanceViewModelTest` — BUILD SUCCESSFUL.
- `./gradlew :app:testDebugUnitTest --rerun-tasks` — BUILD SUCCESSFUL; resultado XML final: 128 pruebas, 0 fallos, 0 errores.
- `./gradlew :app:compileDebugAndroidTestKotlin --rerun-tasks` — BUILD SUCCESSFUL; 26 tareas ejecutadas.
- `./gradlew :app:connectedDebugAndroidTest --rerun-tasks` — BUILD SUCCESSFUL en SM-A066M, Android 16/API 36, serial R7AY40TDB1T: 26 pruebas, 0 fallos, 0 errores, 1 omitida.
- `./gradlew :app:lintDebug --rerun-tasks` — BUILD SUCCESSFUL; 28 tareas ejecutadas.
- `./gradlew :app:assembleDebug --rerun-tasks` — BUILD SUCCESSFUL; 37 tareas ejecutadas.
- `git diff --check` — sin errores.

La única prueba instrumentada omitida fue `Elm327RemoteIntegrationTest.connectsReadsMetricsAndReadsDocumentedDiagnosticCodes`, porque no se configuró `elm327Host`. No se reporta como ejecutada ni aprobada. Las primeras instrumentadas fallaron por aserciones que buscaban los marcadores antiguos “History” y “Maintenance”; se actualizaron a los títulos de las pantallas reales y se volvió a ejecutar la suite completa.

## QA / Ponytail

**Veredicto final: APROBADO.** QA releyó los ViewModels y callers conectados, examinó la invalidación de cargas pendientes, el refresco de alertas al volver, el aislamiento de vehículo/ID, integridad y reglas de mantenimiento y las regresiones añadidas. No quedan hallazgos de código pendientes para este alcance.

## Límites y pendientes explícitos

- No se hizo validación visual manual de formularios, tamaños táctiles, accesibilidad ni tema claro/oscuro. Las pruebas automatizadas no sustituyen esa revisión.
- La entrega es estática, según preferencia del usuario; no se construyó ni validó un prototipo navegable. No se modificó Figma.
- No se validó permiso de red local API 37 ni adaptador físico; esa validación de 4.6 queda fuera de esta etapa.
- El build reportó que `Modifier.menuAnchor()` está deprecado en `MaintenanceScreen`; no impidió compilación ni lint y no afecta el resultado funcional probado.
- Los cambios previos presentes en MainActivity, AppSession, navegación y pruebas se preservaron.
- Los artefactos dentro de `plans/` están ignorados por `.gitignore`; el checklist y este reporte existen en el workspace, aunque no aparecen en `git status`.

