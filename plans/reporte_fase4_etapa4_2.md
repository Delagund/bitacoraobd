# Reporte de etapa 4.2 — Preferencias y guardias

**Estado: implementación y regresión instrumentada ejecutadas; QA aprobado con observaciones sobre cobertura de errores DataStore.**

## Cambios

- AppSettings define nombre opcional, unidades km/millas y °C/°F y visibilidad de RPM, refrigerante y velocidad, con defaults acordados.
- AppPreferencesRepository y DataStore admiten observar y guardar el conjunto de ajustes. Una llamada `DataStore.edit` escribe el conjunto; las claves existentes active_vehicle_id, obd_ip y obd_port quedan intactas. Fallos/cancelación siguen propagándose.
- Se adaptó el fake de preferencias de pruebas JVM y se extendió la prueba instrumentada para defaults, round-trip y preservación de config OBD.
- DeleteVehicleUseCase conserva su guardia Connected y pruebas previas. No se amplió el contrato para estados transitorios ni captura pendiente. La coordinación serializada aprobada se planificó para Presentation/4.3. El cambio de activo previo al borrado Room puede persistir si Room falla; Presentation debe consultar estado real, según DD-28.
- GetDashboardDataUseCase no emite por cambio de fecha por sí solo; GetMaintenanceStatusUseCase calcula con Clock al evaluarse. Presentation debe volver a iniciar la colección de Inicio al activarlo para obtener evaluación inicial con fecha actual; no se añade temporizador ni almacenamiento de alertas.

## Evidencia

- `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL, todas las tareas UP-TO-DATE en revalidación final. Una ejecución anterior de esta tarea compiló y pasó tras el cambio inicial, antes de añadir la prueba instrumentada.
- `./gradlew :app:compileDebugAndroidTestKotlin` — BUILD SUCCESSFUL.
- `adb devices -l` — `emulator-5554`, Pixel_10 (AVD), API 37.
- `./gradlew :app:connectedDebugAndroidTest` — BUILD SUCCESSFUL en Pixel_10 (AVD), 1m 1s; suite instrumentada ejecutada.
- `./gradlew :app:lintDebug` — BUILD SUCCESSFUL.
- `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL.

La prueba instrumentada de DataStore con defaults, round-trip y preservación de configuración OBD se ejecutó dentro de la suite instrumentada aprobada. Se añadieron pruebas JVM con DataStore falso que inyecta IOException en lectura y escritura; ambas verifican que la misma excepción se propaga y no se devuelve éxito. La prueba dirigida y la suite JVM completa pasaron.

## QA Ponytail

Revisión estática del cambio conectado: preferencias son un único valor observable y un guardado explícito; claves históricas no cambian; no se agregó dependencia. Compatibilidad de preferencias antiguas deriva de defaults por clave ausente. Los estados transitorios y captura pendiente permanecen fuera del backend 4.2 como corresponde a coordinación de Presentation.

**Observaciones de alcance:** Presentation deberá consultar el estado real después de un fallo parcial Room/DataStore y coordinar conexión, cambio de vehículo, borrado y captura pendiente a través del propietario compartido planificado en 4.3. Esos trabajos no pertenecen al backend 4.2.

**Veredicto:** APROBADO para el alcance backend de 4.2. La instrumentada DataStore y la cobertura JVM de errores pasaron. La validación Room/migración de 4.1 se completó posteriormente; ver [reporte de 4.1](reporte_fase4_etapa4_1.md).
