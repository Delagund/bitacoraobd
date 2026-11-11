# Brief UX/UI — Bitácora OBD, Fase 1B

## Propósito

Diseñar en Figma un prototipo navegable para una app Android nativa que ayuda a conductores particulares a llevar sus vehículos, revisar lecturas OBD-II básicas y registrar diagnósticos y mantenimientos. La app funciona localmente. El tono debe ser claro y útil: los códigos OBD orientan y no confirman por sí solos una avería mecánica.

## Usuario y límites

- Una instalación corresponde a una persona local, sin cuentas ni perfiles.
- Puede registrar varios vehículos y tener uno activo a la vez.
- La conexión es a un adaptador ELM327 Wi-Fi/TCP.
- El usuario inicia cada lectura o escaneo explícitamente; no hay sondeo continuo.
- Solo se consultan RPM, temperatura del refrigerante y velocidad.
- El diagnóstico lee DTC almacenados. No borra códigos ni escribe en la ECU.
- La información se guarda localmente; el diseño no debe sugerir nube o sincronización.
- Las métricas no disponibles deben mostrarse como “No disponible”, nunca como 0.

## Navegación propuesta

Usar una navegación principal sencilla entre **Inicio**, **Vehículos**, **Diagnóstico** y **Mantenimiento**. Mantener visible el vehículo activo y su selector cuando sea válido. Un estado de conexión global puede estar en Inicio y Diagnóstico. Si cambiar el vehículo afecta una conexión activa, bloquear el cambio y explicar cómo desconectar primero.

## Pantallas y especificación

### 1. Inicio / Dashboard

**Sin vehículos**
- Mensaje: aún no hay vehículos registrados.
- Acción principal: “Agregar vehículo”.
- No mostrar métricas, historial, alertas ni placeholders de un vehículo.

**Con vehículo activo**
- Encabezado: marca, modelo, año y selector de vehículo.
- Estado de conexión: conectado/desconectado y acceso a configuración.
- Tarjetas de lecturas recientes: RPM, temperatura y velocidad; distinguir sin lectura de valor cero.
- Resumen del último diagnóstico exitoso: fecha, cantidad de códigos o estado “Sin códigos”.
- Alertas de DTC recurrentes y mantenimiento vencido/próximo; evitar duplicar una alerta para la misma condición.
- Acciones: “Conectar”, “Escanear códigos”, “Registrar mantenimiento”, “Ver historial”.
- Alerta DTC debe orientar a revisar el vehículo, nunca afirmar una pieza defectuosa.

### 2. Vehículos

- Lista de vehículos con marca/modelo/año, combustible, motor y kilometraje actual.
- Indicar claramente cuál está activo.
- Acciones: agregar, editar, activar y eliminar.
- Formulario: marca, modelo, año, combustible, descripción del motor y kilometraje actual.
- Validación visible para campos requeridos y kilometraje no negativo.
- Confirmar antes de eliminar e indicar que se borrarán su historial de diagnósticos y mantenimientos.
- Permitir corregir kilometraje editando el vehículo; si baja, presentar una advertencia comprensible.
- Si está conectado el OBD, deshabilitar activación/cambio y explicar que primero debe desconectarse.

### 3. Conexión OBD

- Estado: desconectado, conectando, conectado o error.
- Campos de IP y puerto TCP editables/guardables.
- Acciones: conectar y desconectar.
- Mostrar errores de conexión en lenguaje sencillo y permitir reintento.
- No simular lecturas automáticas ni una conexión activa persistente sin indicación.
- Incluir una nota breve sobre compatibilidad variable de adaptadores ELM327.

### 4. Diagnóstico

Estados que deben aparecer como variantes explícitas del diseño:

1. **Desconectado:** explicar que requiere conectar un adaptador; acción “Conectar”.
2. **Listo:** mostrar vehículo activo y acción explícita “Escanear códigos”.
3. **Leyendo:** progreso y acción no repetible mientras la solicitud está activa.
4. **Error:** explicar que no se pudo completar; permitir reintentar. No crear sesión de historial.
5. **Resultado limpio:** lectura exitosa sin DTC. Debe diferenciarse de un error. Resultado pendiente con acciones “Guardar resultado” y “Descartar”.
6. **Resultado con DTC:** lista de códigos, descripción, sistema, posibles causas y orientación local cuando exista; fallback para código desconocido. Mostrar aviso de que es orientación no taxativa. Acciones “Guardar resultado” y “Descartar”.
7. **Pendiente al salir:** confirmación “Guardar”, “Descartar” o “Seguir aquí” antes de navegar fuera de un resultado exitoso no guardado.

El resultado exitoso solo entra al historial después de “Guardar resultado”. Descartar no persiste datos. No mostrar botón de borrar códigos ni prometer diagnóstico definitivo.

### 5. Historial de diagnósticos

- Lista solo de escaneos exitosos guardados, incluidos resultados limpios.
- Cada fila: fecha/hora, vehículo, kilometraje al escanear y cantidad de DTC.
- Detalle con códigos, orientación disponible y recurrencia respecto de sesiones anteriores del mismo vehículo.
- Errores de lectura no aparecen como sesiones.

### 6. Mantenimiento

**Historial**
- Registros por vehículo, tipo de servicio, fecha, kilometraje y notas.
- Distinguir mantenimientos con próxima fecha, kilometraje, ambos o ninguno.
- Estado visible: próximo, vencido o sin programación.

**Formulario**
- Tipo de servicio: aceite, filtros, frenos, neumáticos, batería, refrigerante u otro.
- Fecha realizada, kilometraje al servicio, notas opcionales, próxima fecha opcional y próximo kilometraje opcional.
- Para “Otro”, permitir una descripción simple si el contrato/UI actual lo soporta; no inventar catálogo adicional.
- Si ambos próximos límites están vacíos, el registro no programa alerta.
- Si fecha o kilometraje límite se alcanza, estado vencido.
- Validar kilometrajes no negativos.

**Kilometraje histórico**
- Si el kilometraje del mantenimiento es menor que el actual, pedir confirmación antes de guardar.
- Explicar que el registro conservará el valor histórico, pero el odómetro actual no retrocederá.
- Si el valor supera el odómetro actual, al guardar el nuevo mantenimiento puede avanzar el kilometraje del vehículo.

## Recorridos del prototipo

1. Primer inicio → estado vacío → agregar vehículo → formulario → Dashboard con vehículo.
2. Dashboard → Vehículos → activar otro vehículo.
3. Dashboard → conexión → introducir IP/puerto → conectar → desconectar.
4. Diagnóstico desconectado → conectar → escanear → resultado limpio → guardar; repetir con DTC.
5. Diagnóstico → resultado pendiente → descartar; comprobar que no aparece en historial.
6. Diagnóstico → resultado pendiente → intentar salir → elegir seguir aquí, guardar o descartar.
7. Dashboard → mantenimiento → crear registro normal → aparece en historial/estado.
8. Crear mantenimiento con kilometraje inferior → confirmar histórico → guardar sin retroceder odómetro.
9. Intentar cambiar vehículo con OBD conectado → cambio bloqueado con explicación.
10. Eliminar vehículo → confirmación de cascada de datos → lista restante o estado vacío.

## Componentes a definir en Figma

- Barra superior con vehículo activo.
- Navegación inferior.
- Tarjeta de estado de conexión.
- Tarjeta de métrica OBD y estado no disponible.
- Tarjeta/resumen de diagnóstico y alerta.
- Fila de vehículo y distintivo “Activo”.
- Fila de historial.
- Etiquetas de estado de mantenimiento.
- Diálogo de confirmación y banner/snackbar de éxito/error.
- Estados vacíos, carga y error.

## Entregable esperado

Prototipo navegable para formato Android móvil que conecte los recorridos descritos. Incluir variantes de estado para Dashboard, conexión, diagnóstico y mantenimiento. Usar datos ficticios claramente reconocibles como ejemplo. El diseño debe permitir identificar acciones principales, estados y confirmaciones; la fidelidad visual final puede definirse en Figma.

## Revisión antes de implementar Compose

- Cada pantalla y acción corresponde al alcance funcional descrito.
- Los resultados limpios y los errores son visualmente distintos.
- Solo se guarda el resultado de escaneo cuando el usuario lo solicita.
- No se confunde PID no disponible con cero.
- El historial excluye errores y mantiene resultados limpios guardados.
- Los mantenimientos históricos no reducen el odómetro actual.
- Cambio de vehículo bloqueado mientras OBD está conectado.
- Sin controles de cuentas, nube, borrar DTC, telemetría continua o diagnóstico concluyente.
