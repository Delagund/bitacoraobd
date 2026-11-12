# Automotive Dark — UI Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: usar subagent-driven-development o executing-plans y el flujo secuencial Coder → Tester → QA exigido por AGENTS.md, tras aprobación.

**Goal:** reemplazar Presentation visual incrementalmente preservando toda funcionalidad actual.

**Architecture:** Compose y Material 3 sobre ViewModels/AppSession existentes. Domain, Data, Room, DataStore y gateway permanecen sin cambios; incompatibilidades se documentan antes de proponer excepciones.

**Tech Stack:** Kotlin, Compose, Material 3, Navigation Compose y pruebas existentes.

## Estado y puertas de entrada

Preparación documental, NO autorización de codificación. Consultar current-presentation-audit.md y figma-compose-contract-map.md. La matriz registra 31 variantes, rutas comprobadas y destinos PENDIENTES. La inspección MCP fue estructural y textual: extracción de tokens, variables y revisión de capturas aún necesaria. No iniciar sustitución hasta cerrar las decisiones que afecten la etapa.

## Estructura propuesta

Conservar carpetas ui/theme, ui/components y carpetas funcionales actuales. En theme mapear colores semánticos, Typography y Shapes al sistema Automotive; no crear framework de tokens. En components añadir solo los elementos usados: AutomotiveButton, tarjeta base y StatusBadge. VehicleTitle/VehicleCard compartidos solo donde se repitan. Mantener filas DTC, sesiones y mantenimiento en su carpeta funcional. Pantallas reciben estado y callbacks; reglas siguen en ViewModels/casos de uso. Reemplazar GraphiteCard/PrototypeIcons únicamente después de localizar todos sus consumidores y comprobar equivalencia.

## Etapas incrementales

Cada etapa sigue Coder (solo código) → Tester (pruebas y correcciones hasta PASS) → QA Ponytail (verificar alcance, contratos, legibilidad y ausencia de abstracciones especulativas). Actualizar plans/task.md al aprobar el plan. No eliminar pantallas antiguas sin reemplazo comprobado.

| Etapa / dependencia | Archivos afectados bajo app/src/main/java/cl/cristian/bitacoraobd/ui | Trabajo y aceptación |
|---|---|---|
| 1 Sistema visual / decisiones de tema | theme/Color.kt, Theme.kt, Type.kt | Extraer valores reales y variables Figma; mapear roles M3; botones primario/secundario radio 12 dp; aprobar política de modo claro; contraste, escala de fuente e insets comprobados |
| 2 Componentes / 1 | components/GraphiteCard.kt y componentes compartidos necesarios | Card sin contorno normal, estados semánticos con texto; títulos 25 sp hasta dos líneas y altura flexible; controles con área táctil accesible aunque maqueta tenga 44 px |
| 3 Dashboard y vehículos / 2 | home/HomeScreen.kt, vehicles/VehiclesScreen.kt, navigation/AppNavigation.kt | Preservar vacío, selección, edición, borrado, kilometraje, unidades, guardias y métricas; formulario sin barra si se confirma integración; no perder acciones ausentes en mockup |
| 4 Conexión y diagnóstico / 3 | connection/ConnectionScreen.kt, diagnostic/DiagnosticScreen.kt | Conservar permiso LAN, IP/puerto editables, cancelación, desconexión, lectura explícita y estados de guardado; detalle DTC requiere decisión de navegación |
| 5 Historial / 4 | history/HistoryScreen.kt | Conservar IDs y contexto; lista vacía distinta de error; detalle vertical desplazable incluso con muchos códigos y fuente ampliada; retorno al origen |
| 6 Mantenimiento y alertas / 5 | maintenance/MaintenanceScreen.kt; navigation/AppNavigation.kt solo si se aprueba | Fechas DD-MM-AAAA con entrada numérica convertida a LocalDate; ocho etiquetas españolas; preservar próximos límites, edición y confirmación histórica; no inventar alertas de OTHER; decidir resumen/historial/detalle/alertas |
| 7 Excepciones y Ajustes / 6 | settings/SettingsScreen.kt y pantallas anteriores | Mantener todos los estados transitorios, reintentos, borradores y preferencias aunque no haya maqueta; resolver discrepancia DD-40 mediante plan Presentation separado si se aprueba |
| 8 QA / 7 | app/src/test, app/src/androidTest | Regresión completa, comparación visual por las 31 referencias y estados adicionales; recreación, cambio de pestaña/contexto, teclado, texto ampliado, TalkBack, scroll y guardias |

## Pasos de validación por etapa

- [ ] Registrar referencia Figma por node-id, captura, viewport, estado y datos de prueba antes del cambio. No equiparar píxeles Figma a tamaños táctiles Android.
- [ ] Revisar consumidores y pruebas del componente que se reemplaza; conservar semántica accesible y callbacks.
- [ ] Ejecutar `./gradlew :app:assembleDebug :app:testDebugUnitTest`; aceptación: BUILD SUCCESSFUL y ninguna prueba fallida.
- [ ] Ejecutar `./gradlew :app:connectedDebugAndroidTest` con emulador disponible; registrar dispositivo/API, resultados y cualquier exclusión. Hardware ELM327 requiere validación específica y no se acredita mediante emulador.
- [ ] Comparar capturas Android con nodos de matriz para cada estado migrado, documentar diferencias y resolverlas o pedir aceptación explícita.
- [ ] QA revisa diff, confirma preservación funcional y cobertura; solo entonces avanzar y retirar reemplazos antiguos sin consumidores.

No se ejecutaron compilaciones ni pruebas durante esta preparación documental; no hay línea base PASS establecida en esta ejecución.

## Decisiones y riesgos antes de codificar

1. Política de modo claro: hoy sigue sistema; Automotive Dark no define su sustitución completa.
2. Elegir estados internos o nuevas rutas para DTC, historial/detalle de mantenimiento y alertas, conservando retorno y guardias actuales.
3. Resolver Cancelar con cambios: Figma pide confirmar; antecedentes Android distinguen cancelar explícito de Atrás.
4. Elegir detalle de intervención 35:104 o alternativa 41:2; selector 32:2 está fuera de Screens.
5. Completar extracción de tokens/variables y referencia de Ajustes, métricas y estados sin maqueta.
6. DD-40 pide conservar datos ante error de actualización, pero HistoryLoad/LoadState no los transportan: propuesta Presentation separada, sin alterar Domain/Data.

Riesgos: pérdida de acciones al copiar maquetas; pérdida de contexto/pendientes al cambiar rutas; fechas ambiguas si se cambia parser sin adaptar presentación; pruebas VisualStyleContractTest ligadas al estilo anterior. Actualizar expectativas visuales solo para decisiones aprobadas, nunca debilitar pruebas funcionales.

## Cierre de cada etapa

Actualizar task.md; tras QA aprobado generar reporte técnico en plans con archivos, comandos/resultados, comparación visual y limitaciones. Solicitar siguiente paso según AGENTS.md.
