# Reporte — Saneamiento Domain/Data de Pre-Fase 4

**Estado del saneamiento:** QA APROBADO para Domain/Data. Al emitir este reporte la Pre-Fase 4 seguía abierta; su cierre posterior y alcance constan en [reporte de cierre](reporte_prefase4.md). La revisión de diseño continúa por separado.

## Cambios

- El resumen de diagnóstico expone los códigos analizados del último diagnóstico, conservando recurrencia sin asignar gravedad. Las alertas de mantenimiento se mantienen separadas.
- Se incorporan frenos, neumáticos, batería y refrigerante como tipos de mantenimiento programables; Otro conserva notas.
- La edición de mantenimiento actualiza el mismo registro y valida límites. Editar no modifica el odómetro; la inserción conserva su avance controlado.
- El borrado informa fallos, preservando la cancelación. El año 0 representa ausencia sin cambiar el esquema.
- Se añadieron regresiones para códigos, tipos, edición, validaciones, cancelación y fallos parciales, además de una consulta directa de códigos tras cascada.

## Validación

- `./gradlew :app:testDebugUnitTest`: BUILD SUCCESSFUL; UP-TO-DATE en la validación final (reutiliza resultado previo).
- `./gradlew :app:connectedDebugAndroidTest`: BUILD SUCCESSFUL en SM-A066M, Android 16; 13 pruebas aprobadas y una integración remota ELM327 omitida por falta de configuración de servidor. Las pruebas Room, incluida la regresión de odómetro y la cascada, pasaron.
- `./gradlew :app:lintDebug :app:assembleDebug`: BUILD SUCCESSFUL; tareas mayormente UP-TO-DATE.
- QA Ponytail revisó la corrección del odómetro y el alcance; APROBADO para Domain/Data.

## Pendientes fuera de este cierre

La integración remota omitida no se revalidó en esta ejecución de suite; posteriormente se ejecutó y aprobó de forma dirigida en API 36, como registra la [guía ELM327](emuladorEML327.md#revalidación-remota-de-pre-fase-4). Esa ejecución no valida el permiso runtime de API 37 ni adaptador físico. Las decisiones funcionales posteriores y los límites técnicos se concilian en [auditoría](auditoria_prefase4.md#conciliación-técnica-posterior--alcance-pre-fase-4). Presentation/Compose y permisos runtime permanecen para fase 4. Este reporte de saneamiento no declaraba DESIGN_READY ni cierre global de Pre-Fase 4; el estado posterior figura en el [reporte de cierre](reporte_prefase4.md).
