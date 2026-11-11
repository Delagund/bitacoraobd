# Sistema visual existente

## Referencia vigente para implementación desde 4.5

El usuario eligió apariencia A con **paleta fija** y modos claro/oscuro según la configuración del sistema. La captura compartida en la conversación orienta el verde luminoso de detalles, botones principales e interfaz; el fondo azul puede convertirse en grafito. Modo claro usa superficies claras y texto oscuro, con verdes y colores semánticos adaptados. No copiar pantallas ni funciones de la aplicación de referencia.

Usar verde para acciones y estados favorables, ámbar para atención y rojo para errores o vencimientos; acompañar estados de texto o icono, sin atribuir gravedad a códigos DTC. Mantener la misma función y navegación en ambos modos. Como objetivos de contraste WCAG, comprobar texto normal ≥4,5:1, texto grande ≥3:1 y componentes/indicadores necesarios ≥3:1 frente a colores adyacentes; verificar estados y no depender solo del color. Verificar además tamaño de texto, controles y TalkBack durante implementación.

**Estado de 4.5:** esquema fijo claro/oscuro implementado en `ui/theme/Color.kt` y `Theme.kt`; no usa color dinámico. Pares de texto/superficie y botón/fondo calculados abajo corresponden a los hex instalados. Muestras renderizadas, alpha/estados de controles, TalkBack y escala de texto requieren comprobación visual durante QA; los ratios no acreditan por sí solos accesibilidad completa. La imagen de referencia sigue sin vínculo permanente. Esquemas y roles adicionales Material3 fijados en `ui/theme/Theme.kt`: containers, surfaceVariant, outline, inverse, surfaceContainer y errorContainer. La lista anterior de tokens de plantilla puede ser histórica.

### Contraste calculado de la paleta implementada

Contraste relativo WCAG para valores sRGB opacos actualmente asignados. Los ratios cubren texto y fondo; no miden alpha, bordes, foco, error, estados deshabilitados ni componentes renderizados.

| Uso candidato | Texto / icono | Fondo | Contraste |
|---|---|---|---|
| Texto oscuro sobre superficie clara | #161D1C | #E5E9E7 | 13,97:1 |
| Texto claro sobre tarjeta grafito | #E5E9E7 | #252E2C | 11,38:1 |
| Texto de variante oscuro | #C3CCC7 | #303A37 | 7,16:1 |
| Texto de variante claro | #404A45 | #DCE5E0 | 7,15:1 |
| Indicador outline oscuro sobre superficie | #89938E | #252E2C | 4,40:1 |
| Indicador outline claro sobre superficie | #707A74 | #FFFFFF | 4,44:1 |
| Texto error container oscuro | #FFDAD6 | #93000A | 7,24:1 |
| Texto error container claro | #410002 | #FFDAD6 | 13,26:1 |
| Texto onPrimary en botón verde luminoso (oscuro) | #161D1C | #55E6A5 | 10,80:1 |
| Verde oscuro sobre blanco | #006B47 | #FFFFFF | 6,57:1 |
| Ámbar oscuro sobre blanco | #7C2D00 | #FFFFFF | 9,41:1 |
| Ámbar claro sobre grafito | #FFB77D | #252E2C | 8,18:1 |
| Rojo oscuro sobre blanco | #B3261E | #FFFFFF | 6,54:1 |
| Rojo claro sobre grafito | #FFB4AB | #252E2C | 8,21:1 |

Las razones confirman los pares opacos de texto/superficie y botones de la paleta implementada. QA aún debe inspeccionar controles, alpha, foco/error/deshabilitado, TalkBack, fuente ampliada, IME y barras del sistema en dispositivo; esta tabla no afirma que esas verificaciones se hayan hecho.

Revisión documental: 2026-11-07. Línea base técnica, no diseño de producto aprobado. Fuentes bajo `app/src/main/`: `java/cl/cristian/bitacoraobd/ui/theme/{Color,Theme,Type}.kt`, `MainActivity.kt`, `res/values/{colors,strings,themes}.xml` y AndroidManifest.xml; dependencias en app/build.gradle.kts y gradle/libs.versions.toml.

## Compose y Material

Compose habilitado; Material 3 declarado con BOM 2026.02.01. BitacoraOBDTheme aplica MaterialTheme(colorScheme, typography). MainActivity usa enableEdgeToEdge, Scaffold fillMaxSize y pasa innerPadding a Greeting/Text. GreetingPreview solo muestra saludo; no acredita diseño adaptable ni estados MVP.

Tema oscuro según isSystemInDarkTheme. dynamicColor=true por defecto: Android 12+ elige dynamicDarkColorScheme/dynamicLightColorScheme; en otro caso usa esquemas locales. Por ello los colores estáticos siguientes no garantizan el resultado visual en todos los dispositivos.

## Paleta Compose explícita

| Token | ARGB | Rol |
|---|---|---|
| Purple80 | #FFD0BCFF | primary oscuro |
| PurpleGrey80 | #FFCCC2DC | secondary oscuro |
| Pink80 | #FFEFB8C8 | tertiary oscuro |
| Purple40 | #FF6650A4 | primary claro |
| PurpleGrey40 | #FF625B71 | secondary claro |
| Pink40 | #FF7D5260 | tertiary claro |

Otros roles usan valores predeterminados de Material 3; ejemplos comentados de Theme.kt no son configuración activa. No hay tokens propios para advertencia OBD, recurrencia o mantenimiento.

colors.xml define además purple_200=#FFBB86FC, purple_500=#FF6200EE, purple_700=#FF3700B3, teal_200=#FF03DAC5, teal_700=#FF018786, black=#FF000000 y white=#FFFFFFFF. Son recursos XML distintos: el tema Compose inspeccionado no los referencia. No equiparar ambas paletas.

## Tipografía, forma y espaciado

Type.kt personaliza solo bodyLarge: FontFamily.Default, FontWeight.Normal, 16.sp, lineHeight 24.sp, letterSpacing 0.5.sp. titleLarge y labelSmall mostrados en comentarios no están activos; otros estilos vienen de Typography predeterminado.

No hay escala de espaciado, radios, bordes ni Shapes propios definidos en los archivos de tema. El padding actual viene de Scaffold; no constituye escala del producto. Tamaños, elevaciones y variantes de componentes MVP: PENDIENTE DE DEFINICIÓN.

## Componentes e iconografía

Implementados: tema, Typography, Greeting y GreetingPreview, apoyados en Scaffold/Text. No tarjetas, formularios, navegación inferior ni diálogos funcionales propios.

El inventario de recursos contiene ic_launcher_background, ic_launcher_foreground e iconos adaptativos ic_launcher/ic_launcher_round; son recursos de lanzamiento, no un sistema de iconos funcional OBD. Geometría visual no evaluada. Iconografía del producto y mapeo Figma↔Compose: NO VERIFICADO.

Componentes propuestos por ux_fase1.md (tarjetas de conexión/métrica/diagnóstico, filas, etiquetas, diálogos, estados y navegación) permanecen DRAFT; ver [especificación](ui-ux-specification.md).

## Recursos Android y accesibilidad

Theme.BitacoraOBD XML hereda android:Theme.Material.Light.NoActionBar; es envoltorio de Activity y no reemplaza el tema Compose oscuro/dinámico. Manifiesto declara supportsRtl=true y adjustResize. strings.xml solo contiene app_name; saludo está literal en Kotlin. Textos de producto/localización pendientes.

Accesibilidad y diferentes tamaños son requisitos de AGENTS.md. Contraste, TalkBack, escalado de fuente, objetivos táctiles, teclado, tamaño de ventana y legibilidad de barras: NO VERIFICADO, sin ejecución visual. No atribuir accesibilidad validada al solo uso de Material 3.

## Referencia visual pendiente

No existe evidencia de paleta, tipografía, componentes, variables o variantes aprobadas en Figma. [figma-references](figma-references.md) registra el límite. El tema actual se documenta como código de plantilla existente, no como decisión visual aprobada. Definir tokens y estados de producto requiere diseño y aprobación posterior.
