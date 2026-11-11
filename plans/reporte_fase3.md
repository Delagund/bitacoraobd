# Reporte de cierre — Fase 3: Subsistema OBD-II

**Estado:** Completada  
**QA global:** APROBADO  
**Aprobación del plan:** usuario  
**Alcance:** parser, intérprete de PIDs, cliente TCP, gateway ELM327 y validación Android contra emulador.

## Resultado

Se completaron las etapas 3.1–3.6 manteniendo el contrato de Domain y sin agregar dependencias. El gateway ejecuta el handshake `ATZ`, `ATE0`, `ATH0`, `ATSP0`, `0100`; lee `010C`, `0105`, `010D` y DTC modo 03 bajo demanda. No hay polling ni comandos de escritura ECU.

- El parser valida cabeceras y respuestas; para modo 03 `NO DATA` es fallo. Un resultado limpio requiere trama válida como `43 00 00`.
- El intérprete devuelve RPM como `Int`; PID no disponible queda como `null`, distinto del valor cero.
- El cliente TCP cubre timeout, EOF, tamaño de respuesta, serialización y cancelación.
- El gateway solo publica `Connected` tras inicialización válida, limpia el socket ante fallo/cancelación y serializa lecturas.
- El manifiesto declara `INTERNET` y `ACCESS_LOCAL_NETWORK`; la solicitud de permiso runtime queda para el flujo de conexión de Fase 4.

## Validación con emulador

Servidor documentado: `192.168.1.122:35000`.

Alcance TCP comprobado desde ambos entornos:

- Host: `nc -vz -w 5 192.168.1.122 35000` — conexión exitosa.
- SM_A066M / Android 16 (API 36): `adb shell toybox nc -z -w 5 192.168.1.122 35000` — exit code 0.

La prueba instrumentada dirigida `Elm327RemoteIntegrationTest` conectó desde el proceso de la app, completó el handshake, leyó métricas y DTCs, y desconectó al finalizar. Comando:

```text
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=cl.cristian.bitacoraobd.data.obd.Elm327RemoteIntegrationTest \
  -Pandroid.testInstrumentationRunnerArguments.elm327Host=192.168.1.122 \
  -Pandroid.testInstrumentationRunnerArguments.elm327Port=35000
```

DTCs observados: `P0143`, `P0196`, `P0234`. El simulador entrega métricas variables: se observaron RPM/temperatura/velocidad de `4208/28/55` y, en la corrida final, `3442/36/39`. Por esto, la prueba valida que RPM y velocidad estén disponibles, que una temperatura disponible esté dentro de rango y que los DTC coincidan, en vez de fijar muestras cambiantes.

Una corrida inicial falló porque esperaba valores históricos fijos (`1303` RPM y `10` km/h), mientras el emulador devolvió otro estado. El test se ajustó a las respuestas dinámicas y pasó; no fue un fallo del parser.

## Regresión final

Los comandos se ejecutaron secuencialmente:

1. `./gradlew :app:testDebugUnitTest` — **BUILD SUCCESSFUL** (26 s).
2. `./gradlew :app:connectedDebugAndroidTest` — **BUILD SUCCESSFUL** (16 s), dispositivo SM_A066M / Android 16 (API 36): 10 pruebas pasaron y la integración remota opcional se omitió porque esta corrida no recibió `elm327Host`. La integración remota pasó por separado con los argumentos anteriores.
3. `./gradlew :app:lintDebug :app:assembleDebug` — ambos **BUILD SUCCESSFUL** (40 s).

QA global revisó el plan y el código de 3.1–3.6; veredicto **APROBADO**, sin hallazgos pendientes. No se ejecutaron `04`, `07` ni `0A`; no se alteró configuración remota.

## Límites conocidos

- La prueba usó Android 16/API 36. No valida el diálogo ni el bloqueo de `ACCESS_LOCAL_NETWORK` runtime de Android 17/API 37, que deberá probarse al implementar el flujo de permisos en Fase 4. Ver [documentación oficial de permiso de red local](https://developer.android.com/privacy-and-security/local-network-permission).
- No se probó un adaptador ELM327 físico; el resultado con emulador no demuestra compatibilidad universal.
- La corrida final completa de instrumentadas omitió la prueba remota por ausencia de argumento `elm327Host`; la prueba targeted se ejecutó y pasó en el mismo dispositivo.
