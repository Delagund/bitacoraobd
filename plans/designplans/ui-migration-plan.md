# Automotive Dark — UI Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: usar subagent-driven-development o executing-plans y el flujo secuencial Coder → Tester → QA exigido por AGENTS.md, tras aprobación.

**Goal:** reemplazar Presentation visual incrementalmente preservando toda funcionalidad actual.

**Architecture:** Compose y Material 3 sobre ViewModels/AppSession existentes. Domain admite únicamente la excepción funcional B-02 aprobada; Data, Room, DataStore y gateway se conservan. Política de elegibilidad compartida, sin duplicación en Compose.

**Tech Stack:** Kotlin, Compose, Material 3, Navigation Compose y pruebas existentes.

## Etapa 1 — implementación autorizada y entregada

Tokens y componentes base de etapas 1/2 consolidados en esta entrega por autorización explícita del usuario. Nuevos AutomotiveTheme.kt, AutomotiveComponents.kt y AutomotivePreviews.kt; tema oscuro opt-in para evitar migrar consumidores legacy. No se cambiaron Theme.kt/Type.kt/Color.kt/GraphiteCard ni pantallas. Revisión código APROBADA, 138 unitarias PASS y APK instrumentado compilado; QA integral pendiente sin dispositivo. Previews renderizadas 390/320 fuente1.5/600; altura de catálogo ampliada para mostrar vehículo completo. Informe: ../reporte_automotive_dark_etapa1.md. No comenzar sustitución de pantallas sin aprobación posterior.

## Estado y puertas de entrada

Preparación documental completada para decisiones y línea base; sin cambios de producción. Evidencia de tokens y build en [etapa 0](automotive-dark-stage0.md). B-01/B-02/B-03 aprobadas. La colección Figma solo define Dark: conservar tema según sistema y esquema claro actual hasta contar con referencia clara verificable, sin inventar tokens. Ese límite impide afirmar fidelidad Automotive del modo claro, pero no impide implementar el oscuro.

### Etapa 0 — Cierre y línea base

- [x] Leer adjunto completo y sincronizar cinco documentos conservando antecedentes.
- [x] Verificar rama, Git inicialmente limpio y dependencias declaradas.
- [x] Inspeccionar variables, aliases, componentes, radios y tipografía reales de Figma.
- [x] Ejecutar assembleDebug y testDebugUnitTest: BUILD SUCCESSFUL, exit 0.
- [x] Definir contrato y pruebas B-02 antes de consumidores de alertas.
- [ ] Validación visual Android y suite instrumentada: corresponden a etapas de implementación; no ejecutadas en esta preparación.

### Tarea Domain B-02 — antes de etapa 3

Archivos: domain/usecase/GetDashboardDataUseCase.kt; política mínima compartida bajo domain/usecase solo si evita duplicación; consumidores MaintenanceViewModel/HomeViewModel según necesidad comprobada; tests bajo app/src/test/java/cl/cristian/bitacoraobd/domain/usecase/. GetMaintenanceStatusUseCase conserva semántica. No tocar repositorios ni esquema.

- [ ] Preparar pruebas unitarias de tabla con Clock fijo: 31/30 días sin alerta, 29/1 UPCOMING, 0/pasado OVERDUE; km bajo sin preventiva, igual/sobre OVERDUE; combinaciones vencidas prevalecen; OTHER/sin límites/históricos excluidos.
- [ ] Implementar elegibilidad una vez en Domain y adaptar ensamblado Dashboard. Mantener futuras programaciones visibles y compartir política con Servicio/Alertas.
- [ ] Verificar reactivación DD-41 y coherencia entre consumidores; ejecutar suite base y regresión de programación sin alterar estados.
- [ ] QA confirma contrato del adjunto y ausencia de filtros duplicados en UI antes de integrar Dashboard.

## Estructura propuesta

Conservar carpetas ui/theme, ui/components y carpetas funcionales actuales. En theme mapear colores semánticos, Typography y Shapes al sistema Automotive; no crear framework de tokens. En components añadir solo los elementos usados: AutomotiveButton, tarjeta base y StatusBadge. VehicleTitle/VehicleCard compartidos solo donde se repitan. Mantener filas DTC, sesiones y mantenimiento en su carpeta funcional. Pantallas reciben estado y callbacks; reglas siguen en ViewModels/casos de uso. Reemplazar GraphiteCard/PrototypeIcons únicamente después de localizar todos sus consumidores y comprobar equivalencia.

## Etapas incrementales

Cada etapa sigue Coder (solo código) → Tester (pruebas y correcciones hasta PASS) → QA Ponytail (verificar alcance, contratos, legibilidad y ausencia de abstracciones especulativas). Actualizar plans/task.md al aprobar el plan. No eliminar pantallas antiguas sin reemplazo comprobado.

| Etapa / dependencia | Archivos afectados bajo app/src/main/java/cl/cristian/bitacoraobd/ui | Trabajo y aceptación |
|---|---|---|
| 1 Sistema visual / decisiones de tema | theme/Color.kt, Theme.kt, Type.kt | Extraer valores reales y variables Figma; mapear roles M3; botones primario/secundario radio 12 dp; conservar modo según sistema; no inventar paleta clara; contraste, escala de fuente e insets comprobados |
| 2 Componentes / 1 | components/GraphiteCard.kt y componentes compartidos necesarios | Card sin contorno normal, estados semánticos con texto; títulos 25 sp hasta dos líneas y altura flexible; controles con área táctil accesible aunque maqueta tenga 44 px |
| 3 Dashboard y vehículos / 2 | home/HomeScreen.kt, vehicles/VehiclesScreen.kt, navigation/AppNavigation.kt | Preservar vacío, selección, edición, borrado, kilometraje, unidades, guardias y métricas; formulario sin barra si se confirma integración; no perder acciones ausentes en mockup |
| 4 Conexión y diagnóstico / 3 | connection/ConnectionScreen.kt, diagnostic/DiagnosticScreen.kt | Conservar permiso LAN, IP/puerto editables, cancelación, desconexión, lectura explícita y estados de guardado; detalle DTC mediante estado interno conservando origen y pendiente, sin nuevas rutas por motivos visuales |
| 5 Historial / 4 | history/HistoryScreen.kt | Conservar IDs y contexto; lista vacía distinta de error; detalle vertical desplazable incluso con muchos códigos y fuente ampliada; retorno al origen |
| 6 Mantenimiento y alertas / 5 | maintenance/MaintenanceScreen.kt; navigation/AppNavigation.kt solo si se aprueba | Fechas DD-MM-AAAA con entrada numérica convertida a LocalDate; ocho etiquetas españolas; preservar próximos límites, edición y confirmación histórica; no inventar alertas de OTHER; componer resumen/historial/detalle/alertas dentro del flujo existente y consumir B-02 compartida |
| 7 Excepciones y Ajustes / 6 | settings/SettingsScreen.kt y pantallas anteriores | Mantener todos los estados transitorios, reintentos, borradores y preferencias aunque no haya maqueta; resolver discrepancia DD-40 mediante plan Presentation separado si se aprueba |
| 8 QA / 7 | app/src/test, app/src/androidTest | Regresión completa, comparación visual por las 31 referencias y estados adicionales; recreación, cambio de pestaña/contexto, teclado, texto ampliado, TalkBack, scroll y guardias |

## Pasos de validación por etapa

- [ ] Registrar referencia Figma por node-id, captura, viewport, estado y datos de prueba antes del cambio. No equiparar píxeles Figma a tamaños táctiles Android.
- [ ] Revisar consumidores y pruebas del componente que se reemplaza; conservar semántica accesible y callbacks.
- [ ] Ejecutar `./gradlew :app:assembleDebug :app:testDebugUnitTest`; aceptación: BUILD SUCCESSFUL y ninguna prueba fallida.
- [ ] Ejecutar `./gradlew :app:connectedDebugAndroidTest` con emulador disponible; registrar dispositivo/API, resultados y cualquier exclusión. Hardware ELM327 requiere validación específica y no se acredita mediante emulador.
- [ ] Comparar capturas Android con nodos de matriz para cada estado migrado, documentar diferencias y resolverlas o pedir aceptación explícita.
- [ ] QA revisa diff, confirma preservación funcional y cobertura; solo entonces avanzar y retirar reemplazos antiguos sin consumidores.

Línea base ejecutada: ./gradlew :app:assembleDebug :app:testDebugUnitTest, BUILD SUCCESSFUL en 51 s, exit 0; testDebugUnitTest ejecutada. Suite instrumentada y hardware no ejecutados.

## Riesgos y límites técnicos vigentes

B-01/A, B-02 y B-03/A cerradas; no reabrirlas. Conservar rutas y guardias; formularios sin barra según diseño; referencia Screens frente a alternativas externas. DD-40 es brecha Presentation a resolver con pruebas, no decisión funcional nueva. Mantener acciones y estados sin maqueta. Actualizar VisualStyleContractTest a valores aprobados sin debilitar pruebas funcionales. Tokens oscuros y límites del claro documentados en automotive-dark-stage0.md.

## Cierre de cada etapa

Actualizar task.md; tras QA aprobado generar reporte técnico en plans con archivos, comandos/resultados, comparación visual y limitaciones. Solicitar siguiente paso según AGENTS.md.
