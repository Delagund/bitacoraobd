# Etapa 0 — Evidencia y límites
2026-11-12. Adjunto B02_aprobacion_plan_y_contratos.md leído íntegramente. Decisiones B-01/A, B-02 y B-03/A aprobadas; B-02 aún no implementada.

## Línea base
Git inicialmente limpio: feature/automotive-dark-ui...origin/feature/automotive-dark-ui. Inspeccionados app/build.gradle.kts y gradle/libs.versions.toml: AGP 9.5.1, Kotlin Compose 2.2.10, KSP 2.3.13, BOM 2026.02.01, Navigation Compose 2.10.2, Room 2.7.2, DataStore 1.1.7, coroutines 1.10.2; minSdk 26, compile/target 37, Java 11. Sin dependencias nuevas.

Ejecutado ./gradlew :app:assembleDebug :app:testDebugUnitTest: exit 0, BUILD SUCCESSFUL en 51 s, 49 tareas (9 ejecutadas/40 actualizadas). testDebugUnitTest ejecutada. Advertencia de características obsoletas incompatibles con Gradle 10; no investigada por estar fuera de alcance. No ejecutadas instrumentadas, hardware ELM327 ni QA visual. No se acredita suite completa Android.

## Tokens Figma comprobados mediante MCP Plugin API, solo lectura
Archivo y nodos: yUVaSHJDK7Y4bJHpzOQk5G; páginas 3:2, 3:3, 3:5.
Colors: colección VariableCollectionId:3:8, único modo Dark, 18 variables. Dimensions: 3:27, modo Default, 10 variables. Aliases semánticos resueltos a primitivas locales:

| Rol | Hex opaco | Variable semántica → primitiva |
|---|---|---|
| background/app | #0B1220 | 3:18 → 3:9 |
| surface/card | #172235 | 3:19 → 3:10 |
| surface/elevated | #223149 | 3:20 → 3:11 |
| text/primary | #F1F5F9 | 3:21 → 3:13 |
| text/secondary | #94A3B8 | 3:22 → 3:12 |
| accent/primary | #38BDF8 | 3:23 → 3:14 |
| status/success | #34D399 | 3:24 → 3:15 |
| status/warning | #FBBF24 | 3:25 → 3:16 |
| status/error | #FB7185 | 3:26 → 3:17 |

Dimensiones: space/1=4, /2=8, /3=12, /4=16, /6=24, /8=32. radius/sm=8, md=12, lg=16, xl=24. Son valores reales Figma; traducir medidas a dp/sp según propiedad y comprobar renderizado, no fijar alturas que corten texto.

## Componentes y estilos reales
- Primary 55:160: 344×44, radio enlazado md=12; accent/primary, texto background/app, Roboto Bold 14, lineHeight AUTO.
- Secondary 55:181: 344×44, radio 12; elevated, texto primary, Roboto Bold 14. Ambos sin strokes. Preferir estos canónicos frente a variantes antiguas de AppButton; área táctil Android accesible aunque dibujo mida 44.
- Card/Base 7:2 standard y 7:6 emphasis: 344×98, radio 16, padding 16, gap 8, sin borde; fondos card/elevated. Textos Roboto Bold 11 y 19, Regular 13, AUTO; textStyleId enlazados.
- StatusBadge 7:16/18/20: success/warning/error, 210×44, radio local 10, padding 10, fondo elevated; Roboto Bold 12 con color semántico. No deducir gravedad de DTC desde estos colores.
- VehicleTitle 50:2: 344×91, radio 16, padding 12 vertical/16 horizontal, gap 6, título Roboto Bold 25 (estilo compartido), secundaria 11. Máximo dos líneas por contrato. Compact 52:16: 344×55, padding 13/16, título 25.
- VehicleCard Selected 53:22: 344×104; ActiveEditable 55:42: 344×159, elevated, gap 9; OBDCompact 55:59: 344×96, título 18; HistoryWithContext 55:80: 344×104; HistoryCompact 55:88: 344×82. Radio 16, padding 16, gap 8 salvo variante indicada.
- Inicio 11:47: 390×839, fondo app, padding 22 vertical/23 horizontal, gap 12; heading Roboto Bold 32; secundaria Regular 13. El radio de frame 24 no implica redondear la ventana Android.
- Estilos de texto y colores enlazados comprobados en descendientes; existen overrides (por ejemplo botones antiguos a 15 frente a canónico 14). No convertir cada override en token nuevo.

No existe modo Light en colección: conservar selección según sistema y esquema claro existente; no afirmar fidelidad Automotive clara ni inventar paleta. Estados disabled/pressed/focus no están acreditados por variantes consultadas: usar comportamiento Material 3 y comprobar accesibilidad durante implementación. No se modificó Figma.

## Contratos de integración
Matriz conserva ViewModels reales: HomeState, VehicleDraft/AppSession, ConnectionViewModel, HistoryLoad/DiagnosticDetail, MaintenanceDraft/LoadState, SettingsViewModel. Formularios son estados internos, no requieren nuevas rutas. AppRoute mantiene home/vehicles/diagnostic/maintenance/settings, connection-flow/connection, history/{vehicleId}, diagnostic-detail/{vehicleId}/{sessionId}.
Mantener carga/vacío/éxito/error, lecturas y guardados en curso, fallos de persistencia, permiso LAN, captura pendiente hasta decisión, guardias de edición/selección/borrado conectado, confirmación de borradores, continuidad y retorno. Figma no sustituye estas reglas.
B-02 es única excepción Domain autorizada: política compartida sin cambiar MaintenanceStatus ni esquema; pruebas del adjunto previstas antes de integrar alertas.

## Puerta de salida
Decisiones cerradas y línea base de build/unitarias satisfactoria. Auditoría de tokens canónicos completada. Plan B-02 y secuencia de ocho etapas preparados. Límites claros: paleta Light sin referencia, estados interactivos a validar, suite instrumentada/QA visual/hardware pendientes para ejecución. No se ha reimplementado producción ni se afirma QA final.
