# Reporte de cierre — Fase 0: Domain y contratos

## Resultado
Se materializaron los modelos y contratos de Domain descritos en el contexto maestro. El módulo sigue siendo `:app`; no se añadieron módulos ni dependencias. Domain usa Kotlin puro, `LocalDate` y Flow/StateFlow, separado de Android, Room, Compose y sockets.

## Entregables
- Modelos persistibles: vehículo, sesión exitosa, mantenimiento y tipos de servicio.
- Modelos temporales/derivados: resultado de escaneo, orientación DTC, estado/alertas de mantenimiento y Dashboard.
- Contratos OBD de configuración, métricas, conexión y resultado tipado `ObdResult`; los errores exponen mensaje y no Throwable.
- Interfaces de preferencias, vehículos, diagnósticos y mantenimiento.
- Resultado de dominio para guardado de mantenimiento.
- Pruebas unitarias de captura de escaneo limpio, kilometraje no negativo, fechas calendario, alertas y validación IPv4/puerto.

## Validación
- `./gradlew :app:dependencies --configuration debugCompileClasspath`: BUILD SUCCESSFUL; coroutines 1.9.0 se resolvía transitivamente por Lifecycle.
- `./gradlew :app:testDebugUnitTest`: BUILD SUCCESSFUL.
- `./gradlew :app:assembleDebug --no-configuration-cache`: BUILD SUCCESSFUL.
- `./gradlew :app:lintDebug --no-configuration-cache`: en la ejecución inicial no completó porque Gradle no pudo resolver `androidx.espresso:espresso-core:3.7.0`. Posteriormente, el usuario actualizó con éxito la dependencia Espresso 3.7.0. El reporte registra esa corrección; lint no se volvió a ejecutar después del cambio.
- `connectedDebugAndroidTest`: no ejecutado, no se verificó dispositivo disponible.
- Ponytail QA: APROBADO para el alcance de fase 0. No encontró imports de Android, Room, Compose, sockets o Throwable en Domain; no hubo dependencias añadidas.

## Nota pendiente
La declaración directa de `kotlinx-coroutines-core` queda pendiente por instrucción del usuario. El build base y la compilación Kotlin funcionan con la dependencia transitiva observada; conviene decidir la declaración explícita antes de que el código deje de depender de Lifecycle para disponer de Flow.

## Fuera de alcance
No se implementaron casos de uso, persistencia, catálogo DTC, parser/socket OBD, UI ni simulador. No se actualizaron versiones preexistentes. Las reglas de Room, transacciones y casos de uso quedan para fase 1A y fases posteriores.
