# Prompt para Figma Make — ajustes del prototipo Bitácora OBD

Fecha: 2026-11-07.
Estado: propuesta para prototipado, NO definición de interfaz ni aprobación visual.
Fuente revisada: código src/App.tsx del archivo “BitacoraOBD prototipo.zip”.
Contraste: contratos Android y documentación local de plans/designplans revisados en esta conversación.
Alcance de revisión: inspección estática; no se ejecutó ni evaluó visualmente el prototipo.

## Comparación que motiva el prompt

| Evidencia en el prototipo | Ajuste |
|---|---|
| HomeScreen solicita nombre y App mantiene userName | Retirar captura de nombre: no existe preferencia equivalente en el alcance actual; mantener saludo neutro. No implica que hoy exista autenticación. |
| Icono bluetooth y botón Configurar conexión alternan connected directamente | Usar Wi-Fi/TCP y abrir configuración IP/puerto con estados simulados. |
| onVehicleCreated solo cambia un booleano; selector y encabezados usan Toyota fijo | Compartir vehículos y activo en memoria, reflejar datos ingresados y selección en todas las vistas. |
| VehiclesScreen usa estado local active; agregar y menú no actúan | Completar crear/editar/eliminar/seleccionar como interacciones del prototipo. |
| DiagnosisScreen solo ofrece ready/scanning/result y un P0420 fijo | Añadir escenarios limpio, fallo y desconocido; guardado diferenciado de descarte e historial simulado. |
| Guardar y descartar hacen lo mismo; navegación desmonta DiagnosisScreen | Conservar captura pendiente y pedir decisión antes de salir. |
| Métricas fijas visibles sin lectura previa y sin actualizar | Mostrar ausencia de captura inicial y actualización explícita simulada. |
| Recurrencia dice “2 de los últimos 3 escaneos” | Evitar insinuar ventana de tres: contar sesiones guardadas y exigir presencia en el último escaneo guardado para alerta activa. |
| MaintenanceScreen muestra 82% y estado general Al día | Eliminar porcentaje sin fundamento y evitar equiparar mantenimiento con salud global. |
| Selector ofrece frenos, neumáticos y batería; faltan aceite y filtro por separado | Usar cuatro tipos reales; detalles de otros servicios en notas. |
| Formulario no permite próxima fecha/kilometraje; guardar solo cierra | Añadir programación opcional y confirmación de kilometraje histórico. |
| Fechas y distancias están fijadas en textos independientes | Usar un conjunto consistente de datos ficticios y fecha de referencia. |
| Ver todo, posibles causas, configuración y detalles no tienen acción | Darles destino dentro del alcance o retirar la apariencia interactiva. |

## Copiar en Figma Make desde aquí

Actualiza el prototipo existente de Bitácora OBD. Esto es una exploración interactiva para validar recorridos, NO una definición final de interfaz Android.

Conserva como punto de partida su composición, estilo visual, tarjetas y navegación Inicio/Vehículos/Diagnóstico/Mantención. No hagas un rediseño completo ni interpretes estos elementos como decisiones visuales aprobadas. Corrige únicamente las discrepancias funcionales que se describen abajo.

Todo funcionará con datos ficticios y estado local compartido en memoria. No conectes hardware, servicios remotos ni bases de datos; no agregues autenticación, nube, IA, exportaciones, notificaciones, borrado de DTC ni telemetría continua. No generes código Kotlin ni cambies contratos de la aplicación Android.

Las pantallas de negocio Android aún no están integradas y el transporte OBD es un contrato pendiente. Simular aquí un recorrido no demuestra que esté implementado en Android.

### 1. Inicio y vehículos

- Retira “¿Cómo te llamas?” y la captura de nombre; usa un saludo neutro.
- Conserva el estado sin vehículos y “Agregar vehículo”.
- El formulario debe capturar marca, modelo, año, combustible, descripción del motor y kilometraje actual. No inventes un catálogo exhaustivo ni reglas restrictivas nuevas de año o combustible.
- Al guardar, usa los datos introducidos en lugar del Toyota fijo. Para el recorrido de primer uso, selecciona el primer vehículo creado como propuesta de interacción del prototipo.
- Mantén una colección compartida y un único vehículo activo. Los encabezados, métricas, diagnósticos y mantenimientos deben corresponder a ese vehículo.
- Permite agregar, consultar, editar y eliminar vehículos en la simulación. El kilometraje se corrige dentro del formulario de edición, nunca en un flujo separado.
- Impide kilometrajes negativos. La corrección manual descendente es permitida y no es lo mismo que registrar mantenimiento histórico.
- Antes de borrar, confirma que se eliminarán también diagnósticos y mantenimientos del vehículo. Al borrar el último, vuelve al estado vacío; al borrar el activo, elige un restante.
- Con OBD conectado, bloquea cambio de activo y borrado, explicando que debe desconectarse primero.
- No crees datos de otro vehículo al navegar ni reinicies la selección al volver a la pestaña.

### 2. Conexión Wi-Fi/TCP

- Sustituye iconos y alusiones a Bluetooth por Wi-Fi o conexión de red.
- “Configurar conexión” debe abrir IP y puerto, no establecer conexión inmediatamente.
- Usa como valores iniciales editables 192.168.0.10 y 35000, sin prometer compatibilidad.
- Valida IPv4 y puerto entero entre 1 y 65535.
- Simula desconectado, conectando, conectado, desconectando y error/reintento.
- No equipares conexión al adaptador con acceso a Internet.
- Evita acciones duplicadas durante transiciones. Como propuesta conservadora del prototipo, bloquea cambio de vehículo durante conexión/desconexión y documenta que esta política está pendiente de ratificación.
- Conserva el acceso de retorno a la pantalla desde la que se abrió conexión.

### 3. Métricas puntuales

- Conserva RPM, temperatura de refrigerante y velocidad como exploración de ubicación en Inicio; esa ubicación no queda aprobada y no forma parte del DashboardSummary actual.
- Antes de una lectura, muestra “Sin lectura” en lugar de cifras ficticias que parezcan capturadas.
- Añade “Actualizar lectura” mediante acción explícita estando conectado.
- Muestra hora de captura, valores de ejemplo coherentes y unidades rpm, °C y km/h.
- Diferencia un cero válido de “— / No disponible”.
- Tras desconectar, una captura conservada debe identificarse como última lectura, nunca como dato en vivo.
- No actualices automáticamente al entrar, cambiar de pestaña o por temporizador. No obtengas ni modifiques el odómetro a partir de OBD.

### 4. Diagnóstico pendiente y guardado

- Mantén lectura explícita y el aviso de solo lectura.
- Permite demostrar escenarios deterministas: éxito con P0420, éxito sin códigos, fallo de lectura y código desconocido. Usa un control discreto separado del flujo de producto, rotulado “Escenarios de demostración”.
- Al comenzar la captura, conserva vehículo, kilometraje y fecha/hora asociados. No los sustituyas por los del vehículo que se seleccione después.
- Bloquea repetición durante lectura.
- Un éxito queda pendiente: “Guardar resultado” agrega una sesión al historial simulado; “Descartar” elimina solo el pendiente. Ambas acciones deben tener resultados distintos.
- Un éxito sin códigos también puede guardarse. Usa “Sin códigos detectados”, no “Vehículo sin averías”.
- Un fallo no crea sesión ni cambia odómetro, alertas o datos guardados; permite reintentar.
- Antes de cambiar pestaña, volver atrás, cambiar vehículo o iniciar otro escaneo con resultado pendiente, ofrece Guardar, Descartar y Seguir aquí.
- Si guardar falla en el escenario de demostración, conserva el pendiente y el destino solicitado; muestra error y permite reintentar. No navegues como si hubiese guardado.
- La captura pendiente debe sobrevivir a cambios internos de componentes. No prometas recuperación después de cerrar o recargar el prototipo.
- Después de guardar o descartar, vuelve a diagnóstico listo, salvo que exista una navegación solicitada: en ese caso complétala tras resolver el pendiente.
- Conserva orientación no taxativa. “Ver posibles causas” debe abrir contenido y permitir volver.
- Usa P0300 y P0420 como ejemplos conocidos. Para un código desconocido, conserva el código, indica que no hay descripción local y orienta a consultar un técnico; no inventes causas específicas.

### 5. Historial y recurrencia

- Añade acceso al historial de diagnósticos dentro del recorrido existente, sin nueva pestaña principal.
- Muestra únicamente sesiones guardadas del vehículo activo, con fecha/hora, kilometraje y cantidad de códigos. Incluye sesiones limpias y estado vacío.
- Permite abrir detalle y volver.
- Un código es recurrente si aparece en la sesión consultada y en otra sesión anterior guardada del mismo vehículo. No requiere sesiones consecutivas ni ventana temporal.
- Para alerta activa en Inicio, además debe estar en el último diagnóstico guardado.
- Sustituye “2 de los últimos 3 escaneos” por un texto como “Detectado en 2 escaneos guardados”, con datos consistentes.
- Guardar un escaneo limpio elimina la alerta recurrente activa, pero conserva el historial.
- No etiquetes todos los códigos como recurrentes. Existe una discrepancia en el campo Android recurrentCodesInLastSession: contiene todos los analizados; no copies ese defecto a la simulación.
- No muestres alertas duplicadas por el mismo código.

### 6. Mantenimiento

- Elimina el anillo “82%”: no existe regla que calcule ese porcentaje.
- Sustituye “Estado general / Al día” por resumen factual de programaciones, sin afirmar salud mecánica.
- El selector debe ofrecer exactamente: Aceite y filtro, Solo aceite, Solo filtro y Otro.
- Frenos, neumáticos y batería pueden describirse en notas de Otro, sin categorías o alertas propias. En registros de ejemplo, muestra tipo Otro y la nota “Rotación de neumáticos”.
- Campos: fecha realizada sin hora, kilometraje, notas opcionales, próxima fecha opcional y próximo kilometraje opcional.
- Guardar agrega un registro al historial simulado; no se limita a cerrar el formulario.
- Si el kilometraje realizado es menor al actual, pregunta si se desea registrar mantenimiento histórico. Cancelar conserva formulario sin guardar; confirmar guarda sin reducir odómetro.
- Si es superior, avanza el odómetro simulado; si es igual, lo conserva.
- Los estados son Sin programación, Programado y Vencido. Usa Programado para todo límite futuro, sin inventar un umbral de “pronto”.
- Está vencido al alcanzar cualquiera de los límites configurados, fecha O kilometraje. Sin ambos límites, queda sin programación.
- Evalúa el registro más reciente por fecha e ID de cada tipo alertable; Aceite y filtro, Solo aceite y Solo filtro son independientes.
- Otro conserva historial y campos opcionales, pero no genera alerta automática.
- Separa el servicio realizado de su próxima programación; evita presentar una programación como un servicio ya ejecutado.
- Conserva consulta y detalle. No añadas edición de un mantenimiento existente: sigue pendiente definir su semántica y el repositorio Android hoy inserta nuevos registros.
- Mantén filtros solo si funcionan y tienen significado claro; no inventes un estado contractual “Completado” para una programación.

### 7. Coherencia y alcance visual

- Usa un conjunto pequeño de datos ficticios compartidos y una fecha de referencia coherente para fechas, kilometrajes y textos como “en 9 días”.
- Incluye estados vacíos de historial y mantenimiento, y feedback de guardado o error.
- Todo botón visible debe realizar una interacción del alcance. Si “Ver todo” o el engranaje no tienen destino definido, retíralos en vez de crear un módulo nuevo.
- Mantén etiquetas accesibles en botones de icono, foco visible, texto junto al color de estado y formularios que puedan desplazarse con el teclado.
- No adoptes la paleta de plantilla Android como identidad obligatoria ni declares aprobado un sistema visual.
- No agregues dependencias o infraestructura para hacer una demostración local.

### Recorridos para comprobar en el prototipo

1. Primer uso → agregar vehículo con datos propios → Inicio refleja esos datos.
2. Crear segundo vehículo → cambiar activo → todas las vistas muestran los datos correspondientes.
3. Conectar simulado → cambio de activo bloqueado → desconectar → cambio permitido.
4. Configuración inválida → mensaje; configuración válida → conectando → éxito o error con reintento.
5. Actualizar métricas → captura con cero válido y un dato no disponible; desconectar conserva su fecha.
6. Escanear → pendiente → intentar salir → Seguir aquí conserva resultado.
7. Guardar pendiente → aparece una sola sesión; descartar → no aparece ninguna nueva.
8. Fallo de guardado → pendiente conservado; fallo de escaneo → historial sin cambios.
9. Guardar limpio → figura en historial y retira alerta recurrente activa.
10. Registrar mantenimiento histórico → cancelar no guarda; confirmar guarda sin bajar odómetro.
11. Programación por fecha, por kilometraje y por ambos → vencimiento al alcanzar cualquiera.
12. Eliminar último vehículo tras confirmación → estado inicial vacío.

Al terminar, resume qué corregiste y qué decisiones siguen siendo exploratorias: ubicación de métricas, navegación de retorno, política durante transiciones y estilo visual. No declares que la interfaz está definida, aprobada o implementada en Android.
