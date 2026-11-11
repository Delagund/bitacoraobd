# Guía de validación ELM327 Wi-Fi/TCP

## Entorno

- Emulador de referencia: [ELM327-emulator de ircama](https://github.com/ircama/ELM327-emulator).
- Servidor de pruebas documentado: `192.168.1.122:35000`.
- Transporte: TCP con comandos ASCII terminados en CR y respuestas hasta el prompt `>`.
- El gateway usa `ObdConnectionConfig`; la IP y el puerto no están fijados en producción.

## Permisos Android

El manifiesto declara `INTERNET` y `ACCESS_LOCAL_NETWORK`. Para aplicaciones que apuntan a Android 17/API 37 o superior, Android exige declarar y solicitar `ACCESS_LOCAL_NETWORK` en runtime antes de acceder a la LAN. La solicitud en runtime queda para la fase 4 junto al flujo de conexión; esta fase no agrega UI de permisos.

La validación disponible se hizo en un SM_A066M con Android 16/API 36, mientras la aplicación apunta a targetSdk 37. Android 16/API 36 aún no aplica la enforcement obligatoria de acceso local de Android 17/API 37; por tanto, esta ejecución no valida el diálogo ni el bloqueo del permiso en API 37. Ver [Local network permission](https://developer.android.com/privacy-and-security/local-network-permission) y [cambios para apps que apuntan a Android 17](https://developer.android.com/about/versions/17/behavior-changes-17).

## Evidencia de alcance TCP

En la revisión de esta etapa se comprobó que el puerto respondía desde el host y desde el dispositivo Android:

```text
Host:
$ nc -vz -w 5 192.168.1.122 35000
succeeded

SM_A066M / Android 16 (API 36):
$ adb shell toybox nc -z -w 5 192.168.1.122 35000
exit code: 0
```

Estos comandos solo demuestran que hay conectividad TCP al puerto. La validación OBD de extremo a extremo se completó por separado con la prueba instrumentada `Elm327RemoteIntegrationTest`: **BUILD SUCCESSFUL** en SM_A066M / Android 16 (API 36), conectado a `192.168.1.122:35000`.

Las lecturas reflejaron el estado dinámico del simulador: una corrida registró RPM=4208, temperatura=28 °C y velocidad=55 km/h; la corrida final registró RPM=3442, temperatura=36 °C y velocidad=39 km/h. Ambas obtuvieron los DTC `P0143`, `P0196` y `P0234`. La prueba comprueba disponibilidad y rango de las métricas, no valores fijos. Una corrida inicial que esperaba RPM=1303 y velocidad=10 falló al recibir otro estado del simulador; esto no fue un defecto del parser.

## Revalidación remota de Pre-Fase 4

Se ejecutó la prueba instrumentada dirigida `Elm327RemoteIntegrationTest` con `elm327Host=192.168.1.122` y `elm327Port=35000` desde SM-A066M / Android 16: **BUILD SUCCESSFUL**. El registro de la ejecución mostró 1297 RPM, 60 °C, 51 km/h y los códigos P0143, P0196 y P0234. El simulador produce métricas variables; estos valores no son valores esperados fijos. La prueba solo ejercita comandos de lectura y no valida un adaptador físico ni el permiso runtime de API 37.

## Protocolo implementado en fase 3

Al conectar, el gateway ejecuta secuencialmente:

1. `ATZ` — reinicio; la respuesta debe identificar ELM327.
2. `ATE0` — desactivar eco; se espera `OK`.
3. `ATH0` — desactivar cabeceras; se espera `OK`.
4. `ATSP0` — protocolo automático; se espera `OK`.
5. `0100` — consulta de PIDs soportados; se requiere respuesta `41 00` con bitmap completo de cuatro bytes.

Solo entonces el estado pasa a `Connected`. Las lecturas se ejecutan por solicitud y no hay polling automático:

| Comando | Dato | Respuesta útil | Interpretación |
| --- | --- | --- | --- |
| `010C` | RPM | `41 0C A B` | `((A * 256) + B) / 4`, como `Int` |
| `0105` | Temperatura refrigerante | `41 05 A` | `A - 40` °C |
| `010D` | Velocidad | `41 0D A` | `A` km/h |
| `03` | DTC almacenados | `43 ...` | Lista de códigos o fallo |

Para RPM `14 5F`, el contrato actual usa división entera y devuelve **1303 RPM**; no conserva el decimal `1303.75`. Si un PID individual no tiene datos o su respuesta no se puede interpretar, esa métrica se representa como `null`. Un error de transporte hace fallar la lectura.

Para modo 03, `NO DATA` es un fallo de protocolo, no una lista vacía. Un escaneo limpio válido requiere una respuesta modo 03 completa como `43 00 00`. La respuesta de referencia `43 01 43 01 96 02 34` se decodifica como `P0143`, `P0196` y `P0234`.

En esta fase se implementan solo handshake, PIDs indicados y modo 03, todos de lectura. No se envían comandos que borren datos de la ECU ni se consultan modos de DTC fuera del alcance.

## Pendiente

- En Android 17/API 37+, implementar y validar el consentimiento runtime de red local en la fase 4. La prueba en API 36 no valida el permiso de API 37.
- Validar compatibilidad con un adaptador ELM327 físico; la prueba con el emulador no la demuestra.
