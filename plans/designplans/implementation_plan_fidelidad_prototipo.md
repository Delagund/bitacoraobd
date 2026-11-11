# Plan de implementación — Fidelidad visual del prototipo Bitácora OBD

**Estado: implementación visual del prototipo cerrada; seguimiento de estilo Material 3 aprobado por el usuario y en implementación (Proceed, 2026-11-11).**

**Objetivo:** trasladar a Compose la apariencia visible del prototipo Figma Make aprobado, en UI-01–07 y los destinos secundarios presentes en el prototipo, conservando el comportamiento y los datos reales.

**Arquitectura:** mantener Compose/Material 3, AppNavigation, AppSession y ViewModels existentes. No copiar la lógica web ni añadir una segunda arquitectura. Componentes compartidos solo si los usos reales los justifican.

## 1. Fuentes y auditoría

El archivo `/Users/cristian/Downloads/Prototipo BitacoraOBD navegable.zip` contiene React/Vite, no Compose. `src/App.tsx` define estructura, pantallas, navegación simulada, componentes e iconos SVG; `src/index.css` define variables, color, tipografía, tamaños y layouts. El usuario confirmó el prototipo como referencia aprobada de pantallas, jerarquía, flujos y estilo; la paleta grafito de §11 se aprobó después con Proceed (2026-11-11) y sustituye únicamente los tokens de superficie del prototipo.

4.4 cerró revisión documental y R03, y transfirió la UI por etapas; no demuestra comparación visual del prototipo. 4.5 cerró UI-02 funcional y un tema fijo, con límites visuales/manuales expresos. La causa verificable de la brecha es que se validaron contratos y funcionalidad por paquete sin una comparación de fidelidad contra este prototipo. No hay evidencia suficiente para determinar si el ZIP exacto estaba disponible entonces.

En el código actual, `ui/navigation/AppNavigation.kt` usa `NavigationBarItem(icon = {})`, mientras el ZIP da iconos a los cuatro destinos. La integración actual no aplica consistentemente las tarjetas, cabeceras, espaciado, estados y agrupación del prototipo. El código del prototipo también incluye datos de demostración, selector de estado, botón Demo y “Notas de revisión”; esos elementos no son funciones de producto.

## 2. Decisiones visuales y funcionales

- **Referencia estructural y de interacción aprobada:** el ZIP aportado, incluidas sus pantallas, componentes, jerarquía y flujos. La paleta CSS original (`#fff/#f4f5f7/#e8ebef`, primary `#344b5f`, estados `#275d46/#735315/#8a2f2c`) queda registrada como origen; la dirección de tarjetas grafito y fondos blanco/azul profundo de §11 fue aprobada después mediante Proceed (2026-11-11) y reemplaza solo los tokens de superficie.
- Mantener una única fuente visual vigente: actualizar DD, design-system y handoff con los tokens aprobados y la fecha de decisión. La variante oscura queda definida en §11. No reproducir la aplicación web píxel a píxel.
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
2. **La paleta posterior es un cambio acotado aprobado.** El ZIP sigue gobernando estructura y flujos; la decisión aprobada de §11 sustituye solo superficies y estilos de acción, y sincroniza la fuente visual de diseño.
3. **La propuesta de iconos contradijo la preferencia del usuario.** Se eliminó Material Symbols/XML del plan. Los cuatro trazados SVG del prototipo se representarán como `ImageVector` local y se dibujarán con el `Icon` de Material 3, sin dependencia nueva.
4. **Las referencias externas podían ampliar funciones por accidente.** Se limitan a patrones de jerarquía visual; dashboard, DTC y sensores ilustran el dominio, pero se excluyen reset ECU, CAN debug, IA, gráficas y funciones no MVP.
5. **La comparación original solo nombraba cuatro rutas principales.** Ahora exige capturas de todas las interfaces de §4 en estados pareados y verificaciones de accesibilidad; no se declara fidelidad por build.

## 10. Límites de la revisión

Inspección estática del ZIP, CSS/React, reportes 4.4/4.5, tema Material 3, paleta, dependencias y `AppNavigation.kt`. No se ejecutó el prototipo web ni se compararon todos los estados visuales; esa matriz y las capturas se completan en implementación. No se editó código ni Figma.

**Ponytail:** propuesta mínima sin biblioteca nueva ni reescritura de Presentation. Tras aprobación del plan, seguir Coder → Tester → QA/Ponytail y registrar evidencia antes del cierre.

## 11. Seguimiento visual Material 3: tarjetas grafito, contornos y acciones (APROBADO; en implementación)

### Auditoría del estado actual

- La etiqueta de sección “Vehículo” en Ajustes es incorrecta: el campo ya dice “Nombre (opcional)” y SettingsViewModel persiste displayName. UI-07 ya especifica nombre para mostrar; la corrección sigue siendo solo el texto del encabezado.
- Color.kt ya contiene PrototypeSurface (#FFFFFF), PrototypeDarkSurface (#2B2E33), PrototypeDarkBackground (#202124), PrototypeGood (#275D46) y PrototypeBad (#8A2F2C). Theme.kt asigna el fondo claro actual #EDF0F2, el oscuro #202124 y tarjetas neutrales mediante surfaceContainerLow. Type.kt solo personaliza bodyLarge; los títulos siguen la escala estándar Material 3.
- Hay Cards distribuidas por Home, Vehículos, Diagnóstico, Conexión, Historial, Mantenimiento y Ajustes. La barra inferior usa NavigationBarItem con indicador de selección Material 3 y los colores actuales del tema.
- No se modificará Domain, Room/DataStore, lógica de navegación, Figma ni se añadirá dependencia externa.

### Comparación con las guías oficiales

- Material 3 reserva Surface para el fondo y Surface Container para tarjetas y otras áreas elevadas; el sistema usa colores emparejados “on” para que texto/iconos contrasten. Aplicar la misma tarjeta grafito en ambos temas es una decisión de marca válida, pero deja de ser un Surface Container estándar: el plan la modela como color fijo de tarjeta con su propio color de contenido, no alterando onSurface globalmente. Las guías advierten que colores fijos pueden causar problemas de contraste, por lo que se medirán texto y borde en ambos temas. [Color system](https://m3.material.io/styles/color/the-color-system)
- Compose Material 3 ofrece OutlinedCard para límites visibles, con color, borde y forma configurables. Se usará ese componente para la tarjeta grafito en vez de inventar un componente gráfico o añadir librerías. [Compose Cards](https://developer.android.com/develop/ui/compose/components/card)
- Los botones Material 3 se jerarquizan por importancia: Filled para acción primaria, Outlined para importancia media y Text para baja. Su guía usa el rol Error para comunicar urgencia; aquí se propone rojo en Cancelar porque es una preferencia explícita del usuario, manteniendo Volver/navegación neutral y reservando el rojo más prominente para acciones destructivas. [Compose Buttons](https://developer.android.com/develop/ui/compose/components/button), [color roles](https://m3.material.io/styles/color/the-color-system)
- NavigationBarItem ya resalta la selección. Se conservarán sus colores actuales y su indicador; no se añadirá un contorno alrededor de todos los destinos porque duplicaría ese énfasis. [Compose Navigation bar](https://developer.android.com/develop/ui/compose/components/navigation-bar)
- Material 3 organiza la tipografía en roles display, headline, title, body y label. Se marcarán los roles de encabezado y título, no el texto de cuerpo completo. [Typography](https://m3.material.io/styles/typography/applying-type)

### Propuesta recomendada

- Fondo del tema claro: blanco #FFFFFF.
- Fondo del tema oscuro: azul profundo #0C1420 (preferido sobre negro puro para que el contorno y las superficies tengan un punto de referencia cromático).
- Relleno único de tarjeta, en ambos temas: grafito #2B2E33, reutilizando el valor ya existente PrototypeDarkSurface.
- Contenido normal de tarjeta: #F4F5F7; mensajes de error sobre grafito usan rojo suave #F6DFDD para conservar contraste. Contorno de 1 dp: token compartido #87919B. Cálculo sRGB preliminar: contenido/grafito 12,49:1; contorno/grafito 4,25:1; contorno/fondo claro 3,27:1; contorno/fondo oscuro 5,77:1. El relleno grafito/fondo oscuro queda en 1,36:1, por lo que el contorno es necesario. Confirmar contraste visual al renderizar.
- Tarjetas de estado también conservan grafito; error, vencimiento y éxito se comunican con texto/icono y etiquetas semánticas rojas/verdes/ámbar, no rellenando tarjetas completas con colores de estado.
- Títulos de pantalla con peso Bold y títulos de sección/tarjeta con SemiBold usando roles existentes de Typography; el cuerpo conserva peso Normal.
- Las acciones operativas positivas usan verde #275D46; los botones Filled fijan texto blanco. Outlined sobre superficies generales usan `secondary` adaptable al tema; dentro de tarjetas grafito usan texto #F4F5F7 y borde #87919B para mantener contraste. Cancelar usa rojo `error` en superficies generales y #F6DFDD sobre tarjetas grafito; Descartar/Eliminar usa Filled rojo #8A2F2C con texto blanco. Volver/navegar sin cancelar permanece neutral.
- Navegación inferior conserva los colores actuales y el indicador seleccionado Material 3; se aplica un contorno fino alrededor de la barra completa para separarla del fondo, sin borde individual por destino.

### Decisiones visuales aprobadas por el usuario (Proceed, 2026-11-11; implementadas y verificadas en dispositivo)

1. Aprobar la combinación de fondo claro blanco, fondo oscuro azul profundo #0C1420 y tarjetas grafito #2B2E33 compartidas entre temas, que sustituye únicamente los colores de superficie de la referencia ZIP.
2. Aplicar verde a acciones operativas positivas con texto blanco en Filled; Outlined sobre fondo general usa `secondary` adaptable y sobre tarjeta grafito texto #F4F5F7/borde #87919B. Cancelar usa rojo `error` en fondos generales y #F6DFDD sobre grafito. Descartar/Eliminar conserva mayor énfasis rojo con texto blanco. Volver/navegar sin cancelar permanece neutral.
3. Conservar colores e indicador seleccionado de NavigationBarItem. Se propone un contorno fino solo alrededor de la barra inferior completa, no alrededor de cada destino.

### Archivos y trabajo de implementación

- SettingsScreen.kt: cambiar el encabezado Vehículo por Nombre; conservar el campo, valor displayName y guardado.
- Color.kt y Theme.kt: definir fondo claro/blanco, fondo oscuro azul profundo, tokens fijos de tarjeta grafito, contenido y contorno; mantener correctamente el color de texto de las superficies generales y las acciones semánticas.
- Crear un único componente pequeño GraphiteCard basado en Material 3 OutlinedCard y reemplazar las Cards neutrales en todas las pantallas; mantener interacciones/callbacks en Modifier y etiquetas de estado. Esto evita repetir colores y bordes en los siete destinos. Las superficies de diálogo/sheets no se convierten en tarjetas.
- Type.kt: asignar peso Bold a headline y SemiBold a title, sin modificar tamaños, interlineado ni pesos de body/label.
- Mantener el rol primary actual de navegación; usar verde en acciones operativas positivas, usando texto blanco en Filled y `secondary` en Outlined; adaptar Cancelar al fondo y usar rojo Filled para acciones destructivas.
- Actualizar design-system.md y design-decisions.md con la nueva dirección visual y la aprobación del usuario; no modificar Figma ni .gitignore.
- Añadir pruebas Compose mínimas para el encabezado Nombre, estilo de GraphiteCard, acciones y selección de navegación. Reutilizar dependencias e infraestructura existentes.

### Aceptación y verificación

- Ajustes identifica el nombre de la aplicación como “Nombre” y mantiene “Nombre (opcional)” enlazado a displayName; guardar, cancelar y descartar no cambian sus contratos.
- Todas las tarjetas neutrales de las siete pantallas usan el mismo relleno grafito y contorno; textos/iconos de tarjeta son legibles en claro y oscuro. Estados mantienen mensaje/texto además del color.
- Las acciones operativas positivas (Agregar, Guardar, Conectar, Actualizar lectura) aparecen en verde; Cancelar aparece en rojo con variante Outlined/Text; eliminar/descartar con pérdida tiene mayor énfasis rojo; volver/navegar sin cancelar permanece neutral. Navegación conserva colores e indicador y usa un contorno fino alrededor de la barra completa.
- Revisar capturas de Inicio, Ajustes, formularios, error/vencimiento y navegación en claro/oscuro. Verificar texto normal ≥4,5:1, texto grande ≥3:1 y contorno de componente ≥3:1; probar escala de fuente, foco y TalkBack disponible.
- Gradle secuencial: :app:testDebugUnitTest, :app:compileDebugAndroidTestKotlin, :app:connectedDebugAndroidTest, :app:lintDebug, :app:assembleDebug y git diff --check. No agregar screenshot-testing framework.

### Skill y revisión Ponytail del plan

- Se revisó la skill adaptive disponible: su alcance es navegación/layouts adaptativos y conservación de estado en ventanas/dispositivos; no define estética, paleta ni componentes Material 3. No se aplicará porque esta propuesta no cambia layouts responsivos.
- No hay una skill dedicada a la estética Material 3 en las skills disponibles. Se usan brainstorming para comparar opciones, Ponytail para limitar código, y las guías oficiales de Material 3 como referencia técnica. No se necesita plugin ni dependencia.
- Un GraphiteCard común se justifica porque el mismo estilo se aplica a muchas tarjetas y el API Material 3 no ofrece un tema global de borde de Card. Mantenerlo fino y sin API opcional para excepciones especulativas.
- Riesgo principal: el relleno grafito constante en tema claro se aparta del tratamiento de superficie M3 habitual; se mitiga con el token de contenido emparejado, borde medido y capturas en ambos temas.
- **Estado:** aprobación explícita recibida mediante Proceed (2026-11-11); implementación terminada y QA/Ponytail **APROBADO**. La revisión encontró y corrigió contraste insuficiente en campos de Mantenimiento y acciones Outlined dentro de tarjetas. JVM (137), AndroidTest (34, 0 fallos, 1 omitido sin `elm327Host`), compilación AndroidTest, lint, assemble y diff-check pasaron. En SM-A066M/API 36 se inspeccionaron Inicio y Ajustes en claro y el campo enfocado; pruebas de píxeles cubren contraste ≥4,5:1 de elementos clave en ambos temas. La escala 1.3 parte “Mantenimiento” en dos líneas; TalkBack y rutas que requieren vehículo quedaron sin revisión manual. Figma no se modificó.
