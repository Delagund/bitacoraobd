# Etapa 1 Automotive Dark — informe de implementación
Fecha: 2026-11-12. Rama feature/automotive-dark-ui. Autorización explícita del usuario; ejecución secuencial Coder → Tester → QA.

## Resultado y alcance
Implementado Design System oscuro opt-in, sin sustituir pantallas. Revisión de código Ponytail: APROBADA sin bloqueantes. QA integral: PENDIENTE de ejecución en dispositivo; no se declara cierre de accesibilidad/runtime.
La etapa se detiene aquí, sin comenzar migración de Dashboard/Vehículos ni B-02.

## Archivos Kotlin creados
- app/src/main/java/cl/cristian/bitacoraobd/ui/theme/AutomotiveTheme.kt: 9 colores semánticos, 10 dimensiones/radios, 14 estilos, ColorScheme/Typography/Shapes y AutomotiveTheme.
- app/src/main/java/cl/cristian/bitacoraobd/ui/components/AutomotiveComponents.kt: AutomotiveButton Primary/Secondary; AutomotiveCard standard/emphasis; StatusBadge Success/Warning/Error; VehicleTitle; VehicleCard Selected.
- app/src/main/java/cl/cristian/bitacoraobd/ui/components/AutomotivePreviews.kt: catálogo puro sin repositorios/hardware.
- app/src/test/java/cl/cristian/bitacoraobd/ui/AutomotiveContrastTest.kt: contraste de 8 pares ≥4,5.
- app/src/androidTest/java/cl/cristian/bitacoraobd/AutomotiveComponentsTest.kt: 3 pruebas de clic/disabled/tamaño táctil, fuente ampliada y semántica de badges.

Ningún Kotlin existente modificado. Theme.kt, Type.kt, Color.kt, GraphiteCard y consumidores legacy conservados. Sin cambios navegación, ViewModels, Domain, Data, persistencia, dependencias ni OBD. AutomotiveTheme requiere adopción explícita futura.

## Correspondencia Figma
Referencia: https://www.figma.com/design/yUVaSHJDK7Y4bJHpzOQk5G
- Primary 55:160 / Secondary 55:181 → AutomotiveButton radio12 y Action/Label Bold14.
- Card/Base 7:2 / 7:6 → AutomotiveCard radio16, padding16, gap8, sin borde.
- StatusBadge 7:16/18/20 → fondo elevated, radio10, padding10, Status/Label Bold12 y colores semánticos con texto.
- Vehicle/Title estilo 25 Bold → VehicleTitle, máximo2 líneas, ellipsis y heading.
- VehicleCard Selected 53:22 → VehicleCard con tres textos y altura adaptable. Otras variantes VehicleCard no implementadas para evitar opciones todavía sin consumidor.

## Evidencia visual y referencias reproducibles
En Android Studio abrir AutomotivePreviews.kt y renderizar AutomotiveComponentsPreview:
1. índice0 “Automotive · 390”, 390×1100 dp.
2. índice1 “Automotive · narrow · large font”, 320×1400 dp, fuente1,5.
3. índice2 “Automotive · wide”, 600×1100 dp.

MCP renderizó los tres anchos. Capturas inline en la conversación; no hay PNG persistente en repositorio. Se inspeccionaron capturas reales Figma 55:160,7:2,50:2,7:18 mediante MCP, además de tokens/bindings auditados.
Comparación cualitativa: acento cyan, fondos, jerarquía Bold, radios12/16 y ausencia de borde coherentes. No se afirma igualdad pixel a pixel. Botón48 dp frente a44 del maestro es adaptación táctil explícita; texto multilínea aumenta altura. Badge ajusta ancho al contenido en vez de fijar210. Alturas VehicleCard no se fijan para admitir texto escalado.
Se amplió viewport de catálogo tras observar recorte inferior en Preview default: nueva renderización estrecha muestra VehicleCard y título largo completos dentro del viewport; texto largo termina en ellipsis a2 líneas conforme al contrato.

Primera renderización devolvía ClassNotFound AutomotiveThemeKt aun tras build shell; studio_gradle_build(:app:assembleDebug) actualizó las clases del IDE y resolvió el problema. No se cambió producción para sortearlo.

## Pruebas ejecutadas y límites
Tester ejecutó ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest: BUILD SUCCESSFUL, exit0,52s,80 tareas (16 ejecutadas/64 actualizadas).
21 informes XML:138 unitarias,0 fallos,0 errores,0 omitidas. La prueba nueva de contraste verifica8 pares opacos activos; no prueba contraste renderizado de todos los estados.
Las3 instrumentadas nuevas se compilaron, NO ejecutaron: adb devices -l sin dispositivos. No se ejecutó connectedDebugAndroidTest ni se atribuye PASS a la suite Android.
Verificación final tras ajustar altura de previews: ./gradlew :app:assembleDebug :app:testDebugUnitTest, BUILD SUCCESSFUL en31s, exit0 (49 tareas,9 ejecutadas/40 actualizadas). git diff --check sin errores. Advertencia de obsolescencias Gradle10 permanece fuera de alcance.

## Diferencias conocidas y riesgos
Disabled delega a Material3 con alpha derivada: adaptación de plataforma, no token/estado Figma verificado. No se inventó paleta clara ni se alteró preferencia de tema. Roboto se representa mediante sans-serif Android; sustitución OEM y métricas reales requieren dispositivo. AUTO no se convierte en altura arbitraria.
TalkBack, foco/teclado, runtime de los3 tests instrumentados, escalas adicionales y comparación física permanecen pendientes. La revisión estática no acredita accesibilidad integral.
Se siguió el orden Coder → Tester → QA del usuario; las pruebas se añadieron después del código conforme a ese flujo, sin afirmar ciclo TDD rojo/verde.

## Seguimiento
plans/task.md y ui-migration-plan.md actualizados. Código y unitarias entregados; QA integral queda abierto con casillas explícitas. No se hizo commit ni publicación.
Próximo paso: validar instrumentadas/accesibilidad cuando haya dispositivo y recibir aprobación antes de comenzar cualquier sustitución de pantallas.
