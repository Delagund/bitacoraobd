# Reporte de cierre — Fidelidad visual del prototipo Bitácora OBD

## Resultado

Se implementó el alcance visual aprobado en Compose, siguiendo Material 3 y los trazados locales de iconos. Ponytail QA emitió **APROBADO** después de revisar la tarjeta OBD de Inicio y el estado vacío de Mantenimiento; ambos hallazgos se corrigieron y verificaron.

## Cambios

- `Color.kt` y `Theme.kt`: tokens y esquemas Material 3 basados en la paleta aprobada del prototipo.
- `PrototypeIcons.kt` y `AppNavigation.kt`: cuatro iconos locales de navegación con icono y etiqueta. Se conservó seleccionada la ruta Diagnóstico durante Conexión. No se añadió Material Symbols ni otra dependencia.
- Inicio y Vehículos/formulario: jerarquía con tarjetas, estados, métricas configurables, lista y formulario con sus flujos existentes.
- Diagnóstico y Conexión: tarjetas y agrupación visual de estado, métricas, resultados y acciones, preservando permisos, errores y navegación.
- Mantenimiento, Historial/detalle y Ajustes: listas, formularios, estados y secciones Material 3 con callbacks, validación, borradores y confirmaciones existentes.
- Mantenimiento sin vehículo: el destino vacío usa 16 dp de padding y título `headlineSmall`.
- Inicio: la tarjeta de métricas desaparece cuando las tres opciones (`showRpm`, `showCoolant`, `showSpeed`) están desactivadas.

No se hicieron cambios intencionales a contratos, Domain, Data o esquema Room; no se añadieron dependencias ni contenido de demostración.

## Verificación

- `:app:testDebugUnitTest`: 137 pruebas, 0 fallos, 0 omitidas.
- `:app:compileDebugAndroidTestKotlin`: BUILD SUCCESSFUL.
- `:app:connectedDebugAndroidTest`: 29 pruebas, 0 fallos, 1 omitida (`Elm327RemoteIntegrationTest`, no había `elm327Host`).
- `:app:lintDebug`, `:app:assembleDebug` y `git diff --check`: exitosos.
- `HomeMetricsVisibilityTest`: cubre que los tres flags apagados ocultan la tarjeta y que activar RPM la muestra.
- `HomeTabReactivationTest`: conserva la regresión de recálculo de estado de mantenimiento al volver a Inicio.
- Inspección manual en `SM-A066M`, API 36, pantalla 720×1600: Inicio, Vehículos, Diagnóstico, Mantenimiento sin vehículo y Ajustes. Se confirmó visualmente el arreglo de padding de Mantenimiento.

## Revisión Ponytail

QA revisó la implementación conectada, confirmó la corrección de ambos hallazgos, los iconos vectoriales locales y el uso de Material 3 sin nuevas dependencias. Veredicto: **APROBADO**.

## Límites conocidos

El dispositivo no tenía un vehículo configurado. Por eso no se pudieron abrir manualmente Historial, detalle de diagnóstico ni Conexión contextual. Tampoco se hizo revisión manual de ambos temas, TalkBack, foco, texto ampliado o todos los estados. La única omisión automatizada fue la prueba ELM327 por falta de host; estas limitaciones no se declaran verificadas por inferencia.

El árbol ya contenía cambios anteriores de Fase 4.8 en sesión, navegación y pruebas; se conservaron.

## Seguimiento visual Material 3: paleta grafito y acciones — 2026-11-11

El usuario aprobó el ajuste de la paleta y la implementación se cerró con QA/Ponytail **APROBADO** tras corregir los hallazgos de contraste.

- Modo claro: fondo blanco `#FFFFFF`; modo oscuro: azul profundo `#0C1420`; ambas variantes conservan tarjetas grafito `#2B2E33`, texto `#F4F5F7` y borde `#87919B` de 1 dp. Títulos headline Bold y títulos de sección SemiBold.
- Las acciones positivas usan verde; Cancelar y las acciones destructivas conservan los tratamientos rojos aprobados. Los botones Outlined dentro de tarjetas usan texto claro y borde contrastante. Los formularios de Mantenimiento aplican color explícito a texto, etiquetas, placeholders, iconos, cursor y bordes de los cinco campos y del selector.
- La barra inferior conserva destinos, color e indicador y recibe un borde perimetral. Ajustes muestra “Nombre” para el nombre de usuario.
- La inspección Ponytail detectó texto de formulario y botones secundarios poco legibles sobre grafito; se corrigieron y la revisión final aprobó. No se alteró comportamiento, datos, contratos, Domain/Data, esquema Room ni dependencias.

### Verificación del seguimiento

- `:app:testDebugUnitTest`: 137 pruebas sin fallos.
- `:app:compileDebugAndroidTestKotlin`: exitoso.
- `:app:connectedDebugAndroidTest`: 34 casos, 0 fallos y 1 omitido en SM-A066M/API 36 (Android 16); el omitido es ELM327 remoto por falta de `elm327Host`.
- `:app:lintDebug`, `:app:assembleDebug` y `git diff --check`: exitosos. `VisualStyleContractTest`: 5/5. La compilación informó la deprecación de `Modifier.menuAnchor()` en Mantenimiento; lint pasó.
- Capturas en dispositivo 720×1600: Inicio y Ajustes en tema claro; Ajustes con el campo Nombre enfocado; se observaron insets. La prueba Compose confirmó el contraste de tarjeta, opción “Millas” y etiqueta de Mantenimiento ≥4,5:1 en ambos temas.
- A escala de fuente 1.3, “Mantenimiento” ocupa dos líneas en la barra inferior; se restauró el ajuste del dispositivo a 1.0. El screenshot completo de Ajustes oscuro no fue concluyente y TalkBack no se inspeccionó manualmente.

### Límites conocidos tras el seguimiento

El dispositivo seguía sin vehículo configurado, por lo que no se inspeccionaron manualmente el formulario de Mantenimiento, Historial, detalle de diagnóstico ni Conexión contextual. Tampoco se revisó TalkBack ni hardware ELM327. La navegación inferior puede partir “Mantenimiento” a escala 1.3; debe revisarse si se requiere soporte visual robusto a tamaños de fuente grandes.