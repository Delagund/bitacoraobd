# Decisiones pendientes antes de Automotive Dark

Revisión 2026-11-12. Rama comprobada: `feature/automotive-dark-ui`. Alcance: documentación; sin migración ni cambios de producción.

Fuentes: [matriz](figma-compose-contract-map.md), [contratos](domain-contracts.md), [flujos](screen-flows.md), [decisiones](design-decisions.md), código vigente y consulta MCP de [Documentation Figma](https://www.figma.com/design/yUVaSHJDK7Y4bJHpzOQk5G?node-id=3-7). Los UI-xx siguientes son de Figma. Esta revisión corrige la interpretación de pendientes de la matriz; no modifica decisiones aprobadas.

## Estado actualizado — confirmación del usuario

B-01/A y B-03/A APROBADAS en chat. B-02 sigue abierta: el usuario solicita evaluar alertas solo a menos de 30 días en el backend; no aprobó B-02/A ni autorizó implementar una regla nueva. Los análisis siguientes conservan su trazabilidad; sus solicitudes históricas de aprobación para B-01/B-03 quedan resueltas.

## Hallazgos y resoluciones

### B-01 — YA APROBADO: conservar D2 y adaptar Figma
Pantallas: UI-02 conectado (15:46), UI-03 edición; contrato Figma 67:134.

1. Vigente: D2 prohíbe editar el vehículo activo mientras está conectado. AppSession.openVehicleEditor (línea 176) comprueba activo + Connected y devuelve error; no impide por esa condición editar otro vehículo. Figma describe conectado con acciones «editar, agregar», sin distinguir esa prohibición.
2. Diferencia: interpretar ese «editar» como habilitado para el activo contradice una guardia funcional aprobada. La matriz dice Compatible sin explicitar esta excepción.
3. Impacto: desbloquearlo alteraría coordinación de vehículo/conexión y revalidación del guardado; no es cambio gráfico. No requiere cambiar Domain/Data si se conserva D2.
4. Alternativas: A) conservar D2, deshabilitar o explicar la edición del activo conectado; otros vehículos mantienen sus restricciones actuales. B) autorizar expresamente edición conectada y preparar otro plan funcional con revisión de concurrencia.
5. Recomendación: A. Conserva asociación correcta de capturas y comportamiento vigente con el menor cambio.
6. Aprobación solicitada: aceptar la adaptación de la maqueta a D2; no reabrir D2 como si careciera de aprobación. Bloquea únicamente la interpretación contradictoria de esa variante.

### B-02 — Ventana de advertencia de mantenimiento
Pantallas: UI-01, UI-08, UI-10 detalle/estado calculado y UI-11. Figma Documentation 50:16–17.

1. Vigente: DD-08 y GetMaintenanceStatusUseCase consideran OVERDUE si fecha o km alcanzan su límite; UNSCHEDULED si no hay límites; UPCOMING en cualquier otro caso. No existe ventana de 30 días ni umbral de cercanía por kilómetros.
2. Figma: amarillo para fecha futura dentro de 0–30 días y verde para estado sin alertas.
3. Diferencia: UPCOMING puede significar fecha dentro de seis meses o límite exclusivamente kilométrico. No equivale a «vence en 30 días»; el día del límite ya es OVERDUE. Colorear o filtrar con esa ventana introduce una clasificación adicional.
4. Impacto: aplicar una nueva regla de proximidad como estado/alerta cambia semántica de dominio; duplicarla en Compose crea una segunda fuente de verdad. También debe definirse el tratamiento de límites exclusivamente kilométricos.
5. Alternativas: A) conservar estados del dominio y adaptar texto/color de Figma a ellos, sin filtro de 30 días. B) definir proximidad solo como énfasis visual derivado, sin ocultar ni renombrar estados, especificando kilómetros y precedencia. C) ampliar reglas de dominio mediante plan funcional separado y aprobación.
6. Recomendación: A para esta migración. Permite mantener todos los límites y alertas existentes sin inventar una nueva taxonomía. Aprobar explícitamente la excepción al texto Figma de 30 días.

### B-03 — YA APROBADO: confirmar Cancelar con cambios
Pantallas: UI-03 y UI-09; documentación Figma 32:60–61 y 50:23.

1. Vigente documental: DD-30 y screen-flows distinguen Cancelar explícito (descarta y vuelve directamente) de Atrás/flecha/cambio de destino (confirma si hay cambios).
2. Vigente ejecutable: VehiclesScreen líneas 69–85 y MaintenanceScreen, botón Cancelar, ya comprueban dirty y muestran confirmación. Coinciden con Figma, que exige confirmación en Cancelar/Atrás.
3. Diferencia: contradicción entre contrato aprobado escrito y código/diseño aprobado; el código por sí solo no demuestra sustitución de DD-30.
4. Impacto: únicamente Presentation y pruebas de formularios; sin cambio de modelos ni persistencia. No afecta Guardar, cambios inexistentes ni las guardias específicas de diagnóstico/conexión.
5. Alternativas: A) ratificar confirmación también en Cancelar cuando dirty, manteniendo código actual y Figma, y actualizar DD-30/flujos. B) conservar DD-30 literal y adaptar Cancelar en código y referencia visual para descarte directo.
6. Recomendación: A, por protección del borrador y coincidencia del comportamiento actual con Figma. Requiere aprobación porque modifica el contrato escrito; no se puede cerrar por analogía ni por aprobación visual implícita.

## NO BLOQUEANTES — resolver técnicamente sin cambiar contratos

| ID | Correspondencias afectadas | Resolución dentro del alcance |
|---|---|---|
| N-01 | UI-06 DTC; UI-08/10/11 servicio, historial y alertas | No añadir rutas por separación visual. Presentar datos existentes mediante contenido/estado interno del destino propietario, conservar origen, vehículo y guardias. No interpretar abrir detalle como abandonar un diagnóstico pendiente. Si apareciera una necesidad funcional real de otra ruta, documentarla antes; hoy no se demuestra. |
| N-02 | Formularios UI-03/UI-09 | Ocultar barra según diseño aprobado conservando destino interno, Atrás, borrador y alcance de ViewModels. No exige reemplazar grafo ni factories. |
| N-03 | Fechas UI-09/UI-10 | DD-MM-AAAA numérico con separadores en Presentation; convertir a LocalDate antes de guardar y conservar ISO interno. Validaciones Domain intactas. |
| N-04 | Ajustes, métricas, loading/error y estados de guardado sin frame completo | Conservar controles y eventos vigentes; adaptar componentes Automotive ya aprobados. Ausencia de maqueta no autoriza eliminación ni funcionalidad nueva. |
| N-05 | Modo claro | Mantener selección según sistema ya aprobada; Automotive Dark reemplaza dirección gráfica anterior. No inferir autorización para forzar oscuro o retirar modo claro. La adaptación de roles necesita contraste y QA, no una nueva decisión funcional. |
| N-06 | Historial/mantenimiento ante error posterior | DD-40 ya exige conservar datos del mismo contexto. HistoryLoad/LoadState no los preservan en Failed: brecha de implementación Presentation, no decisión abierta. Planificar ajuste mínimo y pruebas sin cambiar Domain/Data ni prometer que hoy funciona. |
| N-07 | 35:104 frente a alternativa 41:2; selector 32:2 | Screens es referencia base de las 31 pantallas; Foundations contiene alternativas fuera de ese inventario. No incorporar automáticamente esas alternativas ni pedir otra aprobación para implementar la referencia Screens. |
| N-08 | Tokens, variables, dimensiones y accesibilidad | Completar inspección real antes de asignar valores. Lectura pendiente es trabajo técnico, no bloqueo de producto; no inventar bindings. Área táctil accesible puede superar altura dibujada sin modificar navegación. |

## YA APROBADO — no reabrir

| ID | Decisión y fundamento |
|---|---|
| A-01 | Automotive Dark es referencia visual definitiva por instrucción actual; quedan superadas superficies anteriores incompatibles. Botones principales/secundarios a 12 dp y detalle diagnóstico con scroll vertical, explícitos del usuario. |
| A-02 | DD-13 y actualización F03: cuatro destinos principales; Conexión desde Diagnóstico y retorno final allí; historial conserva origen. AppRoute mantiene history/{vehicleId} y diagnostic-detail/{vehicleId}/{sessionId}. |
| A-03 | DD-04/05/25: escaneo explícito, sin polling, limpio guardable, error no persistido, captura pendiente protegida y guardado con continuidad/reintento. Estados ausentes en Figma se conservan. |
| A-04 | DD-33/34/35: cancelar lectura al salir voluntariamente, permiso LAN contextual y recuperación según estado real, confirmación al salir con IP/puerto modificados. Pendientes históricos de screen-flows no los reabren. |
| A-05 | DD-22/29/31/36/37: unidades, métricas compartidas, cierre de formularios antes de Ajustes, guardado explícito y redondeo de millas. Sin reescritura de registros al cambiar preferencias. |
| A-06 | DD-16/24/27/28/30/32: campos/opcionalidad, selección y borrado, mismo ID al editar mantenimiento, validaciones y continuidad por configuración. Excepción documental concreta: Cancelar se eleva únicamente como B-03. |
| A-07 | DD-08/44/47: OTHER sin alertas, estados derivados, límites absolutos y orden; DTC desconocido conserva código y orientación genérica. No atribuir gravedad por color. |
| A-08 | Domain/Data/Room/DataStore/gateway y casos de uso se preservan por alcance explícito. No hay autorización para cambiar reglas con finalidad estética. |

## Resolución solicitada

B-01/A y B-03/A aprobadas: mantener bloqueo de edición del activo conectado y confirmar Cancelar solo con cambios. B-03 actualiza DD-30 y screen-flows donde indicaban descarte explícito directo; sin cambios se vuelve directamente. B-02 permanece BLOQUEANTE para modificar semántica de alertas.

### Evaluación solicitada B-02 — menos de 30 días en backend

El backend de esta aplicación es Domain local, no un servidor. GetMaintenanceStatusUseCase determina estados; GetDashboardDataUseCase actualmente convierte todo estado distinto de UNSCHEDULED en alerta. Por eso filtrar solo en Compose produciría discrepancias entre Inicio y Servicio.

Propuesta para evaluar, NO aprobada para implementación: conservar MaintenanceStatus y sus tres valores para describir la programación, pero derivar centralmente en Domain la elegibilidad de alerta. Siempre alertar OVERDUE por fecha o kilómetros; alertar anticipadamente por fecha solo cuando falten entre 1 y 29 días calendario. A 30 días exactos no alertar: «menos de 30» es estricto. Hoy y fechas pasadas son vencimiento, no aviso futuro. OTHER continúa excluido. Una programación a 60 días sigue existiendo y visible en Servicio aunque no genere alerta; ausencia de alerta no significa ausencia de programación.

Límite kilométrico: mantener alerta cuando se alcanza o supera el límite, incluso sin fecha. No inferir días restantes desde kilómetros ni añadir un umbral preventivo no solicitado. Si coexisten fecha y km, el vencimiento de cualquiera prevalece sobre la ventana temporal.

Alternativa: añadir un nuevo estado de programación lejana a MaintenanceStatus. Tiene mayor impacto en modelos, consumidores y pruebas; no es necesario si solo cambia cuándo se genera una alerta. No modificar esquema Room/DataStore ni persistir alertas derivadas.

Impacto previsto: Domain (política compartida de elegibilidad y composición Dashboard); Presentation (consumir misma decisión en Inicio/Servicio/Alertas y distinguir programación de alerta); pruebas de dominio y regresión. Reutilizar Clock y actualización al reactivar Inicio (DD-41), sin temporizador ni polling nuevo.

Criterios para el futuro plan: probar 31/30/29/1/0 días, fecha pasada, límite km por debajo/igual/superado, solo km, límites combinados, OTHER y registros históricos no vigentes. Comprobar que alertas e indicadores de todas las pantallas usan la misma política.

Decisión pendiente: ratificar esta definición de alerta temporal (1–29 días, vencidas siempre, km al alcanzar límite) antes de elaborar el cambio ejecutable. La solicitud actual autoriza evaluar, no cambiar contratos ni código.

No se modificó la matriz ni la documentación contractual histórica en esta ejecución: su actualización debe reflejar las resoluciones explícitas. No se ejecutaron compilación, pruebas, prototipo interactivo ni validación en hardware; el contraste fue estático y documental, con lectura MCP de contratos textuales Figma.
