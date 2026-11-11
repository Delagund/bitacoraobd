# Plan de implementación — Fidelidad visual del prototipo Bitácora OBD

**Estado: implementación visual previa cerrada; corrección de Ajustes y contraste planificada, pendiente de aprobación explícita.**

**Objetivo:** trasladar a Compose la apariencia visible del prototipo Figma Make aprobado, en UI-01–07 y los destinos secundarios presentes en el prototipo, conservando el comportamiento y los datos reales.

**Arquitectura:** mantener Compose/Material 3, AppNavigation, AppSession y ViewModels existentes. No copiar la lógica web ni añadir una segunda arquitectura. Componentes compartidos solo si los usos reales los justifican.

## 1. Fuentes y auditoría

El archivo `/Users/cristian/Downloads/Prototipo BitacoraOBD navegable.zip` contiene React/Vite, no Compose. `src/App.tsx` define estructura, pantallas, navegación simulada, componentes e iconos SVG; `src/index.css` define variables, color, tipografía, tamaños y layouts. El usuario confirmó que el prototipo completo es aprobado; por eso la referencia visual de esta implementación es el ZIP, incluyendo su paleta y estilo.

4.4 cerró revisión documental y R03, y transfirió la UI por etapas; no demuestra comparación visual del prototipo. 4.5 cerró UI-02 funcional y un tema fijo, con límites visuales/manuales expresos. La causa verificable de la brecha es que se validaron contratos y funcionalidad por paquete sin una comparación de fidelidad contra este prototipo. No hay evidencia suficiente para determinar si el ZIP exacto estaba disponible entonces.

En el código actual, `ui/navigation/AppNavigation.kt` usa `NavigationBarItem(icon = {})`, mientras el ZIP da iconos a los cuatro destinos. La integración actual no aplica consistentemente las tarjetas, cabeceras, espaciado, estados y agrupación del prototipo. El código del prototipo también incluye datos de demostración, selector de estado, botón Demo y “Notas de revisión”; esos elementos no son funciones de producto.

## 2. Decisiones visuales y funcionales

- **Referencia visual aprobada más reciente:** el ZIP aportado. Su paleta CSS declara tinta `#202124`, muted `#60646c`, líneas `#c9cbd0`, superficies `#fff/#f4f5f7/#e8ebef`, primary `#344b5f`, primary soft `#dce6ee`, semantic good/warn/bad `#275d46/#735315/#8a2f2c` y fondos suaves correspondientes. Trasladar roles equivalentes a Material 3 y comprobar contraste; no reproducir la aplicación web píxel a píxel.
- Esta aprobación visual explícita reemplaza para este alcance la paleta D4 verde/grafito previamente documentada. Actualizar DD, design-system y handoff para registrar el cambio y su fecha; no mantener dos fuentes visuales contradictorias. Mantener tema claro/oscuro solo donde el prototipo aprobado y la decisión funcional lo permiten; aclarar la variante oscura del prototipo desde sus reglas CSS antes de implementarla.
- Conservar contenido, rutas, restricciones y estados reales del producto. Los datos ficticios del prototipo sirven solo como contenido de referencia visual.
- No importar el código React/CSS, usar WebView, añadir una librería de iconos o modificar contratos/Domain/Data/Room/permisos por motivos estéticos.
- Mantener semántica accesible, textos de estado, objetivos táctiles ≥48 dp, scroll, tamaño de texto y compatibilidad con insets. No comunicar estados solo mediante color.
- Si un elemento visual del prototipo implica comportamiento nuevo o contradice una regla aprobada, no simularlo: documentar la diferencia en plan/reporte y solicitar la decisión funcional correspondiente.

## 3. Comparación de referencias similares

Se consultaron referencias públicas actuales para patrones de organización, sin copiar marca, contenido, funciones ni assets:

- [Car Scanner ELM OBD2](https://www.carscanner.info/): comunica datos en vivo, DTC y conexión. Útil para comparar jerarquía entre estado de conexión, lectura explícita y métricas. La propia web describe múltiples sensores/funciones fuera de este MVP; no incorporarlas.
- [OBD Auto Doctor](https://www.obdautodoctor.com/) y [Google Play](https://play.google.com/store/apps/details?id=com.obdautodoctor): presenta DTC, estado del vehículo y datos numéricos/gráficos. Comparar claridad de listas, métricas y estado; no importar gráficas, reset ECU, selección de PIDs ni funciones de pago.
- [OBD Pro](https://obd-pro.app/): muestra dashboard live data y DTC en páginas distintas. Útil para evaluar densidad y separación entre datos y diagnóstico. Sus vistas CAN y asistencia IA están fuera del producto.

La referencia que gobierna la implementación visual sigue siendo el prototipo aprobado del usuario. Las apps externas solo informan patrones; no autorizan expansión de producto.

## 4. Inventario completo de interfaces

Rutas verificadas en `AppNavigation.kt`: HOME, Vehículos, Diagnóstico, Conexión (flujo secundario F03), Mantenimiento, Ajustes, Historial por vehículo y Detalle de diagnóstico. Formularios/menús/confirmaciones son estados de esas pantallas; `revision` del prototipo es soporte de revisión y se excluye de producto.

| Interfaz/estado | Referencia del prototipo | Compose real | Elementos que cubre el plan |
|---|---|---|---|
| Inicio | `HomeScreen`, tarjetas, `metrics-grid`, acciones | `ui/home/HomeScreen.kt` | Cabecera, vehículo activo, tarjetas de mantenimiento/último diagnóstico, códigos/estado, métricas, vacío sin vehículos, carga/error/refresco y acceso a ajustes/historial |
| Vehículos lista | `VehiclesScreen`, `vehicle-card`, menú | `ui/vehicles/VehiclesScreen.kt` | Cabecera, activo/selección, tarjetas, agregar, menú editar/eliminar, vehículo conectado, vacío/carga/error |
| Vehículo alta/edición | `VehicleForm`, `Field`, `SelectField` | formulario usado por VehiclesScreen | Título/modo, inputs y selectores, kilometraje, validación, guardar/cancelar, borrador y diálogo de descarte |
| Diagnóstico | `DiagnosticScreen`, `hero-card`, `progress-card` | `ui/diagnostic/DiagnosticScreen.kt` | Estado OBD, lectura explícita, progreso, resultado limpio/DTC, orientación, guardar/descartar/reintentar y captura pendiente |
| Conexión | `ConnectionScreen`, `metric-list` | `ui/connection/ConnectionScreen.kt` | Cabecera/volver, campos IP/puerto, estado/conectar/desconectar, permiso/error, métricas y retorno F03 a Diagnóstico |
| Mantenimiento lista | `MaintenanceScreen`, `schedule-card` | `ui/maintenance/MaintenanceScreen.kt` | Calendario/lista, etiquetas próximo/vencido/sin programar, vacío/carga/error, agregar/editar |
| Mantenimiento alta/edición | `Field`, `SelectField`, botones | formulario de MaintenanceScreen | Ocho tipos, fecha/km/notas/límites, validación, historial, confirmación de kilometraje histórico, guardar/cancelar/descarte |
| Historial | `HistoryScreen` | `ui/history/HistoryScreen.kt` | Sesiones limpias/DTC, fecha, kilometraje, conteo, vacío/error/reintento, abrir detalle |
| Detalle diagnóstico | `detail-historial` | `DiagnosticDetailScreen` registrado en AppNavigation | Resumen, códigos/orientación y navegación atrás; verificar si el ZIP tiene una vista equivalente y mapear diferencias sin inventar campos |
| Ajustes | `SettingsScreen` | `ui/settings/SettingsScreen.kt` | Nombre, unidades, visibilidad de métricas, guardar, error/reintento y descarte |
| Superficies globales | `Modal`, `Banner`, `StatusPill`, `toast`, `topbar`, `bottom-nav` | AppNavigation y pantallas actuales | Confirmaciones, errores/info, estados, Snackbar/feedback, barras e iconos en todas las rutas |

**Criterio de cobertura:** cada fila requiere referencia fuente concreta (componente y clase CSS), caller Android, datos/eventos del ViewModel, estados aplicables y evidencia visual. Cuando el prototipo no tenga pantalla/estado equivalente, anotarlo como extensión funcional existente y preservarlo, sin fingir que está representado. Completar el mapa desde todo `App.tsx`/`index.css` antes de implementación.

## 5. Librerías y recursos

El usuario eligió mantener la implementación dentro de Material 3 y descartó Material Symbols. Compose Material 3 ya cubre Cards, Buttons, NavigationBar, TextFields, dialogs y surfaces; no añadir otra dependencia de interfaz o iconografía.

Para los cuatro iconos SVG del prototipo, trasladar sus trazados a `ImageVector` locales y mostrarlos con `androidx.compose.material3.Icon`, que ya está disponible. Mantener solo esos iconos aprobados y los que el inventario completo del ZIP confirme. No importar `material-icons-extended`, Material Symbols, FontAwesome ni iconos de las apps de referencia.

Fuentes de API: [Compose icons](https://developer.android.com/develop/ui/compose/graphics/images/material). No descargar assets de apps similares ni reutilizar sus marcas.

## 6. Archivos y alcance probable

- `ui/navigation/AppNavigation.kt` y `MainActivity.kt` para estructura común solo si son los propietarios adecuados.
- Siete pantallas del grafo: `ui/home/HomeScreen.kt`, `ui/vehicles/VehiclesScreen.kt` y formulario, `ui/diagnostic/DiagnosticScreen.kt`, `ui/connection/ConnectionScreen.kt`, `ui/history/HistoryScreen.kt` y detalle, `ui/maintenance/MaintenanceScreen.kt`, `ui/settings/SettingsScreen.kt`.
- Tema: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`; `ImageVector` locales para los iconos aprobados, renderizados con Material 3.
- Adaptar pruebas Compose/navegación por ruta y estado; preservar tests de ViewModel, flujo, guardias y datos. Confirmar callers reales antes de modificar firmas.

## 7. Secuencia de implementación después de aprobación

1. Completar matriz de todas las interfaces y estados de §4 desde el ZIP y el grafo Compose; asociar capturas/viewport.
2. Implementar tokens del prototipo aprobados en MaterialTheme y vectores necesarios; aplicar estructura común de barras, botones y tipografía.
3. Coder implementa el alcance visual aprobado completo; al terminar, Tester adapta y ejecuta pruebas; después QA/Ponytail revisa el cambio conectado, como exige AGENTS.md. Dentro de A/B/C, priorizar por ruta: Inicio; Vehículos/lista/formulario; Diagnóstico/Conexión; Mantenimiento/lista/formulario; Historial/detalle; Ajustes; superficies globales. Conservar comportamiento.
4. Comparar estado visual equivalente del ZIP/app y registrar diferencias inevitables (sistema Android, contenido real, scroll/insets).
5. Verificar estados operables y accesibilidad en claro/oscuro y viewport de teléfono; cubrir layouts cuando cambia tamaño/teclado.
6. Sincronizar documentación visual y reporte con tokens finales y capturas; cerrar solo después de revisión QA.

## 8. Verificación propuesta

- Reutilizar pruebas/fixtures; probar navegación, semantics/content descriptions, acciones y estado real por ruta. No añadir infraestructura de screenshot sin demostrar necesidad.
- Gradle secuencial: `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, `:app:connectedDebugAndroidTest`, `:app:lintDebug`, `:app:assembleDebug`, `git diff --check`; registrar dispositivo, conteos y omisiones.
- Capturas pareadas por todas las rutas de §4, luz/oscuro y estados de aceptación. Comprobar accesibilidad, texto grande, foco, teclado/IME, barras, scroll e insets en dispositivo disponible. Preview/compilación no sustituyen comparación renderizada.
- ELM327 físico y exactitud calibrada de hardware no se acreditan con un prototipo web ni simulador.

## 9. Revisión Ponytail del plan

1. **Cobertura no era completa ni comprobable.** Antes varias pantallas aparecían agrupadas; el plan ahora enumera todas las rutas, formularios, overlays y estados, más requisito de rastrear cada componente al ZIP y a Compose.
2. **La solución anterior descartaba la paleta del prototipo explícitamente aprobado.** El plan alinea tokens con CSS del ZIP y registra la actualización de D4/SSOT como parte del trabajo aprobado.
3. **La propuesta de iconos contradijo la preferencia del usuario.** Se eliminó Material Symbols/XML del plan. Los cuatro trazados SVG del prototipo se representarán como `ImageVector` local y se dibujarán con el `Icon` de Material 3, sin dependencia nueva.
4. **Las referencias externas podían ampliar funciones por accidente.** Se limitan a patrones de jerarquía visual; dashboard, DTC y sensores ilustran el dominio, pero se excluyen reset ECU, CAN debug, IA, gráficas y funciones no MVP.
5. **La comparación original solo nombraba cuatro rutas principales.** Ahora exige capturas de todas las interfaces de §4 en estados pareados y verificaciones de accesibilidad; no se declara fidelidad por build.

## 10. Límites de la revisión

Inspección estática del ZIP, CSS/React, reportes 4.4/4.5, tema Material 3, paleta, dependencias y `AppNavigation.kt`. No se ejecutó el prototipo web ni se compararon todos los estados visuales; esa matriz y las capturas se completan en implementación. No se editó código ni Figma.

**Ponytail:** propuesta mínima sin biblioteca nueva ni reescritura de Presentation. Tras aprobación del plan, seguir Coder → Tester → QA/Ponytail y registrar evidencia antes del cierre.

## 11. Seguimiento de Ajustes y contraste de tarjetas (pendiente de aprobación)

### Hallazgos y fuente de verdad

- La captura actual de Ajustes muestra el grupo que contiene el nombre de la aplicación titulado “Vehículo”, aunque su campo es Nombre (opcional) y SettingsViewModel persiste displayName. La especificación UI-07 ya define “Nombre para mostrar opcional”. Cambiar solo el título de la sección a “Nombre”; conservar campo, opcionalidad, guardado y modelo.
- Color.kt define PrototypeBackground = #EDF0F2, PrototypeSurface2 = #F4F5F7 y PrototypeSurface = #FFFFFF. Theme.kt usa PrototypeBackground para el fondo claro y PrototypeSurface2 como surfaceContainerLow; las Cards estándar/Material 3 usan esa superficie baja. El prototipo aprobado adjunto muestra fondo blanco y tarjetas gris claro.
- No cambiar Domain, DataStore, tema oscuro, controles semánticos ni navegación; no modificar Figma ni añadir dependencias.

### Propuesta visual recomendada y alternativas

1. **Recomendada:** asignar el fondo claro a PrototypeSurface (#FFFFFF) y conservar las tarjetas en PrototypeSurface2 (#F4F5F7). Esto separa más las tarjetas del fondo y sigue la captura aprobada. Es un cambio de un token que propaga a las rutas Material 3.
2. Mantener el fondo y poner todas las Cards en blanco. La separación aumentaría, pero invertiría la relación fondo/tarjeta que muestra el prototipo.
3. Mantener ambos tokens y añadir bordes a cada Card. Requiere editar muchos call sites para un resultado que ya ofrecen los roles existentes, así que se descarta por alcance y mantenimiento.

### Archivos y trabajo tras aprobación

- Modificar app/src/main/java/cl/cristian/bitacoraobd/ui/settings/SettingsScreen.kt: texto de encabezado Vehículo → Nombre.
- Modificar app/src/main/java/cl/cristian/bitacoraobd/ui/theme/Color.kt: asignar PrototypeBackground a PrototypeSurface; conservar PrototypeSurface2 y los roles actuales de Cards en Theme.kt.
- Actualizar plans/designplans/design-system.md y design-decisions.md para registrar que la referencia aprobada usa fondo blanco/tarjetas #F4F5F7 y que el título de Ajustes representa nombre para mostrar. No tocar archivos Figma.
- Añadir una prueba Compose mínima que compruebe el encabezado Nombre y el campo opcional, reutilizando infraestructura existente; no crear harness ni dependencia nuevos.

### Aceptación y verificación

- Ajustes muestra el grupo Nombre y conserva el campo Nombre (opcional) enlazado a displayName; guardar, cancelar y descartar mantienen el comportamiento aprobado.
- En tema claro, fondo de pantalla blanco y Cards estándar gris claro; el tema oscuro, tarjetas semánticas de error/estado y colores de controles se mantienen.
- Comparar captura de Ajustes e Inicio en dispositivo con la referencia y comprobar al menos tema claro/oscuro; verificar que no cambian los estados, navegación ni datos.
- Tester ejecutará en serie :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, :app:connectedDebugAndroidTest, :app:lintDebug, :app:assembleDebug y git diff --check.
- Después, QA/Ponytail revisará el cambio conectado, KISS, consistencia visual y evidencia de accesibilidad disponible.

### Revisión Ponytail del anexo

- El encabezado se corrige en el único componente que presenta displayName; no se renombra propiedad ni dato persistido.
- El contraste se resuelve con un token global ya existente y superficies M3 instaladas, sin recorrer todas las Cards ni crear una abstracción.
- La diferencia solicitada es visual y no introduce cambios de contrato; el tema oscuro se mantiene intacto.
- **Puerta AGENTS.md:** este anexo queda propuesto. No modificar código ni pruebas hasta que el usuario confirme explícitamente la aprobación (por ejemplo, “Proceed”).
