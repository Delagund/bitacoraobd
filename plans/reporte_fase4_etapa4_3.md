# Reporte de etapa 4.3 — Composición, ciclo de vida y navegación

**Estado: CERRADA — QA/Ponytail APROBADO.**

## Fase A — Coder

- Se agregó composición manual compartida por proceso con Application/AppContainer: una instancia de Room, DataStore, repositorios, gateway y AppSession.
- El builder Room de producción registra explícitamente `MIGRATION_1_2`; no se usa fallback destructivo. La migración se prueba mediante ese builder.
- AppSession coordina conexión, selección/borrado, lecturas y captura pendiente con un Mutex; cancelación/cierre se resuelven fuera del lock y se revalida el estado antes de mutar.
- Se conserva el error de borrado cuando Room falla después de actualizar DataStore, refrescando el activo real antes de permitir reintento.
- ViewModels adaptan la sesión y reinician la colección de Inicio al reactivarse. No hay temporizador ni recuperación de captura tras muerte del proceso.
- Se integró Navigation Compose `2.10.2` autorizada, con cuatro destinos principales, Ajustes/Historial secundarios y F03 retornando a Diagnóstico. Las acciones que no navegan no emiten autorización de Back; Back en raíz termina Activity y consume autorización.
- Se actualizaron contratos y matriz de dependencias. No se implementaron pantallas funcionales Compose de 4.4–4.8 ni se modificó Figma.

## Fase B — Tester

Validación secuencial final:

- `:app:testDebugUnitTest` — BUILD SUCCESSFUL.
- `:app:compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL.
- `:app:connectedDebugAndroidTest` — BUILD SUCCESSFUL en Pixel_10 AVD / Android 17 (API 37): **23 pruebas, 0 fallos, 1 omitida** por falta de `elm327Host`.
- `:app:lintDebug` — BUILD SUCCESSFUL.
- `:app:assembleDebug` — BUILD SUCCESSFUL.
- `git diff --check` — limpio.
- Resolución Gradle confirmó Navigation Compose 2.10.2 con Lifecycle 2.11.0 y Compose BOM 2026.02.01.

Las pruebas cubren identidad de composición, migración por builder de producción, conexión exitosa sin desconexión tardía, guardias Connected, cancelación/cierre, fallo parcial Room/DataStore, salida Save/Stay/fallo, recreación de MainActivity con captura pendiente y autorización de navegación, retorno F03 a Diagnóstico, Back en raíz y reactivación de Inicio con reloj controlado.

## Fase C — QA/Ponytail

QA revisó AppSession, AppNavigation, MainActivity, callers y pruebas. Se corrigieron los hallazgos de autorización de Back en acciones no navegacionales y autorización no consumida al Back en raíz. Se verificaron los escenarios añadidos de captura pendiente y recreación.

**Veredicto final: APROBADO.**

## Límites

La prueba de integración remota ELM327 se omitió porque no se configuró `elm327Host`; no se valida hardware físico. El permiso runtime de red local sigue sin validación. No se promete recuperar una captura tras terminar el proceso. Las pantallas UI-01–07 conservan sus puertas DESIGN_READY y corresponden a etapas posteriores.
