# Etapa 4.2 — Plan de implementación: preferencias y guardias

> Para agentes: ejecutar secuencialmente Coder → Tester → QA conforme AGENTS.md y al plan general aprobado.

**Objetivo:** preparar preferencias de Ajustes y precisar las guardias necesarias para gestionar vehículos, sin implementar Presentation.
**Arquitectura:** DataStore mantiene preferencias locales; Presentation conservará el borrador y coordinará captura pendiente. Room y DataStore son operaciones separadas.
**Tecnología:** Kotlin, Flow, DataStore y pruebas existentes; sin dependencias nuevas.

## Contraste inicial

- AppPreferencesRepository expone activeVehicleId, obdConnectionConfig y sus dos operaciones de escritura. DataStore usa active_vehicle_id, obd_ip y obd_port; las excepciones de lectura y escritura se propagan.
- DeleteVehicleUseCase bloquea Connected, cambia la selección antes de borrar el activo y propaga CancellationException. Si Room falla después de actualizar DataStore, devuelve Failed y la selección puede haber cambiado.
- La prueba instrumentada DataStore actual cubre activo y configuración OBD, pero no preferencias nuevas.
- La ampliación de AppPreferencesRepository está autorizada por el plan general. Cambios adicionales de contratos requieren autorización específica.
- DD-37 asigna conversión de entrada y conservación del valor original a Presentation; no crear conversión de formularios en backend. La coordinación de captura pendiente también pertenece a Presentation.

## Contraste de estados y pruebas

- `ObdConnectionState` define Disconnected, Connecting, Connected(config), Disconnecting y Error(message). Actualmente DeleteVehicleUseCase solo impide cualquier borrado en Connected; devuelve Connected también para vehículo no activo. La comprobación ocurre una vez antes de las operaciones suspendidas y no serializa una conexión concurrente.
- DD-28 exige bloquear selección y cualquier borrado en Connected, y mantener bloqueados los cambios incompatibles mientras termina cancelación o desconexión. Durante Connecting, Presentation debe cancelar el intento y esperar su finalización antes de permitir el cambio; durante Disconnecting debe esperar el cierre. Error y Disconnected no representan por sí solos una conexión activa; no atribuir a Error una desconexión efectiva sin comprobar el estado real del gateway.
- La selección usa directamente setActiveVehicleId, sin guardia; no hay consumidor de Presentation. La coordinación de selección y captura pendiente se implementa y prueba en Presentation, sin escribir desde un Flow. Resolver primero guardar, descartar con advertencia o seguir aquí; mientras guarda, impedir solicitudes duplicadas y esperar resultado.
- No reutilizar DeleteVehicleResult.Connected para estados transitorios como si ya estuvieran conectados. La representación de bloqueo transitorio en el contrato de borrado requiere decisión específica si se pretende cambiar Domain; hasta entonces documentar y coordinar la espera en Presentation.
- MaintenanceDashboardDeleteUseCasesTest cubre Connected, fallo al cambiar selección, fallo Room posterior con selección ya cambiada y cancelación. Ampliar regresiones solo para conducta backend aprobada; probar transiciones y captura en Presentation en su etapa. La lectura inicial del estado no protege carreras: la coordinación futura debe impedir conexión y cambios simultáneos; no afirmar garantía que el contrato actual no ofrece.
- GetDashboardDataUseCase recalcula mediante mapLatest cuando emiten sus fuentes; GetMaintenanceStatusUseCase consulta LocalDate.now(clock) en cada invocación. Al activar Inicio, Presentation debe iniciar una nueva colección del Flow para obtener una emisión inicial y recalcular; confirmar con prueba de reloj mutable y repositorios que emiten el estado actual. No hay emisión automática por cambio de fecha.

## Coordinación aprobada para 4.3 y Presentation

El usuario aprobó esta solución; 4.2 solo la documenta. En 4.3, un propietario compartido de Presentation será la entrada común para conectar, seleccionar y borrar vehículos. Usará un único Mutex de coroutines para excluir el inicio de conexión y las operaciones de selección/borrado; todos los caminos de UI relevantes deberán pasar por él.

1. Resolver primero la captura pendiente y esperar el guardado si está en curso. No mantener el Mutex mientras se espera una decisión del usuario. Tras adquirirlo, volver a comprobar captura y estado: si apareció otra captura, liberar el Mutex y resolverla.
2. Si hay conexión en curso, solicitar su cancelación sin esperar por el Mutex que pudiera estar reteniendo la conexión; esperar la finalización y liberación de recursos antes de intentar el cambio. Si se desconecta, esperar su finalización. La espera no debe impedir que se complete la operación esperada.
3. Bajo el mismo Mutex, verificar nuevamente el estado real: Connected bloquea cambio y cualquier borrado; Connecting o Disconnecting no permiten ejecutarlos. Ejecutar selección o borrado solo cuando el cierre esté confirmado. Ante Error, comprobar que el gateway cerró recursos; no deducirlo solo del nombre del estado.
4. El inicio de conexión adquiere el mismo Mutex y valida el vehículo actual antes de comenzar. Mantener la exclusión hasta que el intento finalice o se cancele; cancelación debe seguir accesible sin adquirirlo. Conservar la guardia Connected existente de DeleteVehicleUseCase como defensa adicional.
5. Probar con sincronización determinista ambos órdenes: conexión antes de cambio/borrado y cambio/borrado antes de conexión; verificar cancelación durante Connecting, espera de Disconnecting, captura aparecida antes de adquirir el Mutex, errores y liberación tras cancelación. Ninguna operación debe cambiar vehículo durante conexión o cierre; no usar sleeps.

Room y DataStore siguen sin transacción común. El Mutex coordina únicamente operaciones que pasan por el propietario compartido dentro del proceso; no sustituye la consulta de estado real tras un fallo parcial ni garantiza exclusión frente a llamadas directas. La implementación concreta debe contrastar el ciclo de vida y cancelación actuales del gateway en 4.3.

## Fase A — Coder

- [x] Contrastar consumidores/fakes, DeleteVehicleUseCase, ObdConnectionState y pruebas existentes; evidencia registrada en el plan.
- [x] Ampliar contrato con AppSettings, Flow de ajustes y guardado del conjunto completo. Defaults: null, km, Celsius y métricas visibles.
- [x] DataStore lee claves nuevas con defaults para store antiguo; guarda el conjunto en una llamada edit sin tocar las claves anteriores. Errores/cancelación se propagan.
- [ ] Contrastar Connected, Connecting, Disconnecting y Error con las decisiones aprobadas antes de modificar la guardia de borrado. No añadir parámetro de captura pendiente al contrato: documentar que Presentation resuelve la captura antes de llamar selección, desconexión o borrado. Si se requiere modificar otro contrato Domain, detener ese cambio y presentar la propuesta.
- [x] Documentado en domain-contracts.md: reactivar Inicio reinicia colección del Flow; GetMaintenanceStatusUseCase calcula con Clock al evaluar. No hay emisión por cambio de fecha ni temporizador.
- [x] Fake JVM actualizado; sin consumidores de producción existentes que requirieran adaptación.

## Fase B — Tester

- [x] DataStore instrumentado vacío: defaults, guardar/leer todos los valores, nombre ausente y preservación de configuración OBD. Suite pasó Pixel_10 AVD/API 37.
- [x] Pruebas JVM inyectan IOException en lectura y escritura y verifican propagación sin éxito falso. El borrador pertenece a Presentation.
- [x] Backend no transforma ni escribe kilometrajes al guardar preferencias; conversión 10,0 millas → 16 km y preservación del campo no editado quedan asignadas a Presentation/4.7–4.8.
- [ ] Probar guardias efectivamente implementadas en cada estado OBD, error de DataStore, error de Room posterior a cambio de selección y cancelación. Captura pendiente y selección se prueban en Presentation cuando exista.
- [ ] Comprobar recálculo con reloj controlado si el backend permite provocar una nueva evaluación sin UI.
- [x] JVM dirigida y completa, compilación AndroidTest, instrumentadas en Pixel_10 AVD/API 37, lint y assemble pasaron; comandos/resultados en reporte 4.2. Validación Room/migración 4.1 completada después (ver reporte 4.1).

## Fase C — QA

- [x] QA revisó consumidores, claves, defaults, errores, cancelación y límites Room/DataStore.
- [x] QA revisó la cobertura; fallos inyectados añadidos y suite JVM repetida satisfactoriamente.
- [x] Checklist y reporte de cierre actualizados con comandos, resultados reales y alcance.
- [x] Cerrar 4.2 y detenerse; 4.3 requiere planificación y confirmación propias.

## Límites

Sin Figma, Compose, navegación, sondeo, Demo ni recuperación de captura tras muerte del proceso. El plan no cambia UUID/sentinel de 4.1. La coordinación aprobada se implementa en 4.3; no inferir semántica nueva del resultado Connected.
