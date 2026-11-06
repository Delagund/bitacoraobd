# Bitácora OBD

Aplicación Android para consultar información básica del vehículo mediante OBD-II y organizar su historial de diagnósticos y mantenimiento.

## Funciones principales

- Registrar varios vehículos y seleccionar uno activo.
- Conectar con adaptadores ELM327 Wi-Fi/TCP para consultar RPM, temperatura del refrigerante y velocidad.
- Leer códigos de diagnóstico (DTC), consultar orientación local y guardar escaneos exitosos en el historial.
- Registrar mantenimientos y programar próximas intervenciones por fecha, kilometraje o ambos.
- Consultar estado del vehículo y alertas de mantenimiento desde el Dashboard.

## Tecnología

Aplicación nativa para Android desarrollada en Kotlin y Jetpack Compose. Usa Room para datos locales, DataStore Preferences para preferencias y Coroutines/Flow para operaciones asíncronas y estado reactivo. La arquitectura separa presentación, dominio y datos, con inyección manual mediante `AppContainer`.

## Enfoque y alcance

El MVP prioriza el funcionamiento offline y la persistencia local. La comunicación OBD es de solo lectura y las consultas se realizan por acción explícita, sin telemetría continua.

La información de los DTC es orientativa: un código por sí solo no confirma una avería mecánica. El alcance no incluye cuentas, sincronización en la nube, borrado o reprogramación de ECU ni diagnóstico de sistemas propietarios como ABS o airbags.


