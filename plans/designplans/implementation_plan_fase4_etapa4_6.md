# Etapa 4.6 — Conexión y diagnóstico Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: usar executing-plans y dispatching-parallel-agents al ejecutar. Roles secuenciales Coder → Tester → QA/Ponytail, conforme AGENTS.md.

**Goal:** integrar UI-03/04 con conexión Wi-Fi/TCP explícita, métricas puntuales y diagnóstico guardable sin pérdida ni duplicados.
**Architecture:** reutilizar AppSession como propietario único de operaciones, captura y Mutex; ViewModels conservan presentación y borradores, Compose representa estados. Domain, gateway y persistencia existentes siguen siendo la base; no crear otro coordinador.
**Tech Stack:** Kotlin, StateFlow, coroutines, Navigation Compose, Material 3, Room/DataStore e infraestructura de pruebas instalada.
**Estado:** revisado tras auditoría Ponytail el 2026-11-10; requiere aprobación explícita antes de codificar. Se precisaron propietarios de estado, reescaneo fallido y responsabilidad del ViewModel de Diagnóstico.

## Fuentes y auditoría

Requisitos: Contexto_Maestro_App_OBDII_v2.md; plan general fase4 §8; DD-20/25/26/29/32–35/48; screen-flows; ui-ux-specification; domain-contracts; instrucciones del diseñador.
4.5 cerrada con QA APROBADO según reporte; sus pruebas son evidencia histórica, no ejecutadas en esta planificación.
Wireframes base UI-03/04 DESIGN_READY por aprobación previa; no volver a pedir esa aprobación. Variantes nuevas no se presumen aprobadas. Aplicar tema fijo claro/oscuro implementado en 4.5; no editar Figma.

Auditoría directa:
- MainActivity construye AppViewModel/HomeViewModel/VehiclesViewModel; AppNavigation conserva cuatro destinos principales y F03 anidado en Diagnóstico.
- UI-03 es Text("Connection"); UI-04 usa EmptyVehicleDestination. No hay formularios ni diálogo global de captura pendiente.
- AppSession conserva pendingDiagnostic, savedDiagnosticId, métricas por vehicleId, operación, errores, decisión y autorización de salida. Reutilizar requestExit/resolvePending.
- scanDiagnostic/readMetrics son explícitos; savePendingDiagnostic usa la captura estable e idempotencia existente.
- Guardar elimina pendingDiagnostic y conserva solo ID: falta conservar resultado completo para DD-25. Revisar también limpieza de savedDiagnosticId al iniciar otra captura o cambiar vehículo.
- requestExit cancela lecturas; runSensitive resuelve pendientes y esperas. Revisar disconnect durante lectura para que no espere innecesariamente el Mutex ni publique una captura incompleta.
- Barra y BackHandler actuales solo conocen borrador de vehículos: deben incorporar guardia del formulario de conexión sin duplicar navegación autorizada.

## Alcance y archivos

Rutas de producción relativas a app/src/main/java/cl/cristian/bitacoraobd/.
Modificar: ui/AppSession.kt (operaciones y resultados), ui/navigation/AppNavigation.kt (destinos y salidas), MainActivity.kt (factories por lifecycle).
Crear: ui/connection/ConnectionViewModel.kt y ConnectionScreen.kt; ui/diagnostic/DiagnosticViewModel.kt y DiagnosticScreen.kt. DiagnosticViewModel transforma el estado de AppSession y obtiene orientación mediante DiagnosticRepository; no copia capturas, operaciones, errores ni decisiones a otro estado mutable. Sus acciones usan AppSession. No crear interfaz, coordinador ni repositorio adicional.
AndroidManifest.xml y puente Activity de permisos: modificar solo lo exigido por plataforma tras comprobar SDK/documentación oficial. Reutilizar declaraciones existentes.
Inspeccionar AppContainer, AppPreferencesRepository, ObdConnectionConfig, gateway y catálogo DTC antes de conectar factories; mantener firmas Domain y esquema Room.
Pruebas: ampliar ui/AppSessionTest.kt y MainActivityRecreationTest.kt; crear ui/connection/ConnectionViewModelTest.kt, ui/diagnostic/DiagnosticViewModelTest.kt y androidTest/ui/ConnectionDiagnosticFlowTest.kt.
Actualizar al cierre design-handoff.md, screen-dependencies.md y referencias técnicas obsoletas de ui-ux-specification.md. No reescribir decisiones aprobadas.

## Fase A — Coder

### A1. Completar coordinación compartida
- [ ] Leer todos los consumidores de AppSession y fixtures antes de cambiar estado.
- [ ] Conservar captura completa guardada y su ID en memoria; separar pendiente de guardada. Tras éxito retirar Guardar/Descartar, mostrar fecha original y etiqueta “Diagnóstico guardado”.
- [ ] Nuevo escaneo no borra sesión persistida. Secuencia A guardado → escaneo B: durante B, A permanece identificado como resultado anterior guardado, con su fecha original. Si B falla o se cancela, mostrar el fallo/cancelación de B separado de A; no presentar A como resultado de B. Si B tiene éxito, sustituir A por la captura B pendiente, sin ID ni etiqueta de guardado heredados. Al cambiar vehículo retirar resultados del contexto anterior.
- [ ] Mantener captureId, vehicleId, timestamp, kilometraje y códigos originales en reintento; doble pulsación no inicia otra escritura.
- [ ] AppSession es el único propietario del guardado de configuración: tras handshake ejecuta saveObdConnectionConfig y expone estado de persistencia y configuración exacta pendiente de guardar. Fallo conserva Connected; reintento escribe exclusivamente ese valor, nunca el borrador del formulario, y no vuelve a conectar. Una desconexión no convierte el fallo en éxito ni elimina silenciosamente ese valor pendiente; resolver el guardado pendiente antes de sustituirlo por otro handshake. No duplicar estos datos como estado mutable del ViewModel.
- [ ] Serializar con Mutex existente y validar contexto al ejecutar; cancelar lectura y cerrar recursos antes de desconectar, sin publicar parcial. Conservar última métrica completa.
- [ ] Error de lectura no escribe historial ni kilometraje. Preservar CancellationException y mensajes comprensibles sin detalles internos.

### A2. UI-03
- [ ] ConnectionViewModel conserva solo IP/puerto textual, originales y presentación de carga/validación del formulario. Derivar cambios comparando textos con originales. Observar el resultado de persistencia de AppSession, sin ejecutar escrituras ni mantener otra copia mutable de su estado. Error de carga no se convierte silenciosamente en defaults guardados.
- [ ] Aviso Wi-Fi con Siguiente/Cancelar; abrir pantalla no conecta. Cancelar y todas las salidas finales F03 vuelven a Diagnóstico.
- [ ] Validar IPv4 y puerto 1..65535 al pulsar Conectar; error junto al campo, conservar ambos textos, cero sockets con entrada inválida.
- [ ] Antes de conectar comprobar permiso real según versión. Verificar documentación Android oficial vigente y SDK instalado para nombre/API exactos, solicitud, denegación y ajustes; no inferir concesión por volver de ajustes. Registrar evidencia en reporte.
- [ ] Denegación permite reintentar cuando Android lo admite; en otro caso abrir ajustes de la aplicación. Volver solo revalida, no conecta automáticamente.
- [ ] Connecting muestra progreso y Cancelar; cancelar permanece, salir cancela y vuelve a Diagnóstico. Connected permite salir sin desconectar; cambiar endpoint exige desconexión explícita.
- [ ] Con configuración modificada sin intento, todas las salidas muestran Descartar/Seguir editando. Restaurar originales elimina dirty; cerrar IME no sale.
- [ ] Métricas: Leer/Actualizar explícitos, fecha y vehículo de captura; null “No disponible”, cero numérico. Aplicar preferencias de unidades existentes sin alterar valores almacenados; RPM intacta, velocidad km/h o mph y temperatura °C o °F.

### A3. UI-04 e integración
- [ ] Representar sin vehículos, carga/error del contexto, desconectado, listo, leyendo, fallo, pendiente limpio/con códigos, guardando, fallo de guardado y guardado.
- [ ] Mostrar orientación del catálogo existente, fallback genérico con código original; no inventar causas ni afirmar pieza defectuosa.
- [ ] Guardar/Descartar/Seguir aquí resuelve pendingDecision global, también desde pestañas, Atrás, desconexión y selección de vehículo. Mientras guarda esperar resultado; fallo conserva captura y cancela salida.
- [ ] Fallo de guardado ofrece Reintentar y Descartar y volver a Inicio. Éxito mantiene resultados con Volver a Inicio/Nuevo escaneo si conectado.
- [ ] Informar pérdida de captura sin guardar al terminar proceso; no persistir borrador ni socket en SavedState.
- [ ] Salida durante lectura cancela captura incompleta y conserva última completa; recreación conserva operación y no la reinicia.
- [ ] Integrar destinos con factories existentes, recolección por lifecycle y padding/insets/IME. Un solo responsable de cada Back y confirmación; navegación autorizada se consume una vez.
- [ ] Usar controles Material 3, etiquetas, errores textuales, objetivos táctiles accesibles y listas desplazables; ninguna abstracción compartida por anticipación.
Coder no ejecuta pruebas.

## Fase B — Tester

Orden solicitado Coder → Tester: no afirmar TDD rojo previo si no ocurrió. Para defectos encontrados, demostrar regresión y devolver corrección al Coder.
- [ ] JVM conexión: IP inválida y puertos vacío/0/65536 no llaman gateway; válidos sí. Denegación no conecta; retorno de ajustes revalida sin iniciar operación.
- [ ] Handshake correcto + fallo DataStore: Connected permanece; retry incrementa escrituras y no connect. Modificar el borrador antes de reintentar no cambia el endpoint escrito. Desconectar no marca el fallo como resuelto ni pierde el valor pendiente; otro handshake no lo sobrescribe sin resolverlo. Cambio de endpoint conectado no reconecta.
- [ ] Borrador: salida sin cambios directa; cambios abre confirmación, Stay conserva textos, Discard conserva preferencias anteriores.
- [ ] Cancelar conexión y salir durante lectura liberan recursos, no publican parcial ni reconectan; repetir acción no duplica trabajo.
- [ ] Diagnóstico: éxito limpio guardable; fallo sin escrituras; desconocido conserva código; guardar dos veces/reintentar tras resultado incierto produce una sesión con captureId original.
- [ ] Guardado muestra captura original y etiqueta. Cubrir A guardado → B leyendo → B fallido/cancelado: A conserva identidad y fecha como resultado anterior, y el error corresponde a B. Cubrir B exitoso: B pendiente sustituye A sin ID/etiqueta heredados. Cambio de vehículo no muestra datos previos como actuales.
- [ ] DiagnosticViewModel transforma limpio/conocido/desconocido mediante DiagnosticRepository y refleja emisiones de AppSession; no conserva una segunda captura mutable ni ejecuta persistencia.
- [ ] Pendiente: Save/Discard/Stay en salidas, desconexión y selección; fallo conserva pendiente; salida durante guardado espera y continúa solo con éxito.
- [ ] Métricas: null distinto de 0, conversión visual sin mutación y conservación de última captura completa.
- [ ] Instrumentadas Compose: recorrido Diagnóstico → Conexión → Diagnóstico; invalidación de campos; limpio/con DTC; fallo/reintento guardado y diálogos. Fakes controlados sin red externa.
- [ ] Recrear durante conectar/leer/guardar: contadores permanecen en uno, borrador y resultado sobreviven con proceso vivo.
- [ ] Permisos reales en API 37 y regresión API 36: concedido, denegado, recuperación por ajustes y revocación. Registrar entorno; si no está disponible, declarar bloqueo de esa validación.
- [ ] Ejecutar secuencialmente:
  1. ./gradlew :app:testDebugUnitTest
  2. ./gradlew :app:compileDebugAndroidTestKotlin
  3. ./gradlew :app:connectedDebugAndroidTest
  4. ./gradlew :app:lintDebug :app:assembleDebug
  5. git diff --check
Resultado exigido: cero fallos; registrar conteos/omisiones y dispositivo. Integración ELM327 remota omitida por elm327Host ausente se declara, no se presenta como hardware probado.
- [ ] Comprobar manualmente claro/oscuro, texto grande, TalkBack, teclado/IME y barras. Separar comprobaciones automatizadas y manuales.

## Fase C — QA/Ponytail

- [ ] Invocar ponytail-review; inspeccionar consumidores y diff conectado.
- [ ] Verificar DD-25/26/33/34/35, F03 termina en Diagnóstico, aislamiento por vehículo, idempotencia y recuperación parcial.
- [ ] Confirmar reutilización de Mutex/operaciones y ausencia de coordinadores, capas, dependencias o campos persistidos especulativos. Verificar propietario único de persistencia OBD en AppSession y responsabilidad exclusivamente de presentación/orientación de DiagnosticViewModel.
- [ ] Revisar accesibilidad y cobertura real; devolver hallazgos al Coder y repetir solo verificaciones afectadas.
- [ ] Emitir APROBADO únicamente con evidencia suficiente; no confundir wireframes aprobados con validación de implementación.

## Seguimiento, cierre y exclusiones

Tras aprobación actualizar plans/task.md con A/B/C antes de delegar. Mantener historial de etapas anteriores.
Tras QA aprobado, finalizar checklist, sincronizar handoff/dependencias y generar plans/reporte_fase4_etapa4_6.md con resultados, comandos, entorno y límites; detenerse y consultar siguiente paso.
No ejecutar 4.7/4.8, desarrollar historial/ajustes/dashboard completos, añadir Demo, Bluetooth, polling, borrado ECU, odómetro ECU, temperatura admisión, nube, dependencias ni migraciones.
No se ejecutan suites en esta planificación documental ni se promete compatibilidad universal ELM327.
