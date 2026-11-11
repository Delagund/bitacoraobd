# Reporte de cierre — Fase 4, etapa 4.6

**Fecha:** 2026-11-11  
**Estado:** QA/Ponytail **APROBADO para la implementación verificable**.  
**Plan:** [implementation_plan_fase4_etapa4_6.md](designplans/implementation_plan_fase4_etapa4_6.md).

## Alcance entregado

- UI-03 Conexión permite configurar IPv4 y puerto, solicitar el permiso LAN cuando corresponde, conectar mediante acción explícita, cancelar, desconectar y leer métricas sin sondeo.
- UI-04 Diagnóstico permite escanear, distinguir resultado limpio o con DTC, guardar/descartar, reintentar el guardado y resolver salidas con captura pendiente.
- AppSession conserva la captura y su identidad original, coordina operaciones con el Mutex existente, guarda la configuración después del handshake y reintenta solo su persistencia. Un escaneo posterior fallido o cancelado se muestra separado del diagnóstico anterior guardado.
- El cambio de vehículo limpia el resultado y las métricas del contexto anterior. La navegación y las pantallas observan el estado compartido sin crear otro coordinador ni dependencia.

## Verificación

El Tester ejecutó secuencialmente:

1. `./gradlew :app:testDebugUnitTest` — PASS.
2. `./gradlew :app:compileDebugAndroidTestKotlin` — PASS.
3. `./gradlew :app:connectedDebugAndroidTest` — PASS en SM-A066M/API 36: 26 pruebas ejecutables, 0 fallos; 1 prueba ELM327 omitida por ausencia de `elm327Host`.
4. `./gradlew :app:lintDebug :app:assembleDebug` — PASS.
5. `git diff --check` — PASS.

Se ampliaron regresiones de AppSession para validación del formulario, fallo de persistencia del endpoint y reintento sin reconectar, identidad de capturas A/B, aislamiento entre vehículos, cancelación y salida durante guardado. Se adaptó una prueba instrumentada de recreación a las pantallas nuevas. La prueba UI adicional de guardado se retiró porque el fixture instrumentado compartía un gate con la prueba heredada; no se interpreta su retiro como cobertura.

## Revisión QA/Ponytail

El QA revisó código conectado, pruebas y plan. Sus hallazgos de aislamiento de vehículo, identidad de escaneo cancelado/fallido, salida durante guardado, metadatos de métricas y estados visibles de diagnóstico se corrigieron y verificaron en las sucesivas pasadas. El veredicto final fue **APROBADO** para lo verificable. No observó abstracciones o dependencias nuevas superfluas.

## Límites y seguimiento

- En la prueba manual actual, la app conectó a `192.168.1.122:35000` sin mostrar un diálogo de permiso. La captura compartida muestra estado Conectado y métricas recibidas. Esto no confirma por qué no apareció el diálogo ni sustituye la validación completa del permiso runtime API 37.
- Repetir la comprobación posteriormente con una conexión Wi-Fi real y/o un adaptador ELM327 físico. Verificar entonces concesión, denegación, recuperación desde Ajustes y revocación; registrar la versión Android y `targetSdk` usados.
- La prueba instrumentada ELM327 se ejecutó contra `192.168.1.122:35000` en SM-A066M/API 36: 1 prueba, 0 fallos, 0 omitidas. Esto valida el handshake y las lecturas con ese host, no el comportamiento del permiso API 37 ni compatibilidad universal con adaptadores físicos.
- No hubo prueba específica de la presentación de métricas/unidades, todos los estados Compose UI-03/04 ni recreación durante conectar, leer o guardar. Las suites verdes no acreditan esos recorridos.
- Quedan comprobaciones manuales de TalkBack, foco, texto grande, IME, barras y contraste renderizado.
- No se ejecutaron etapas 4.7 ni 4.8 ni se editaron variantes Figma.
