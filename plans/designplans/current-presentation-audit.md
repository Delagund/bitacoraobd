# Auditoría de presentación Automotive Dark

2026-11-12. Alcance documental; sin cambios Kotlin ni Figma. Lectura de contexto maestro y seis documentos de diseño; contienen antecedentes superados, por lo que el código prevalece para contratos reales.

## Inventario actual

Base: app/src/main/java/cl/cristian/bitacoraobd/ui/.

- home/HomeScreen.kt: Dashboard; HomeViewModel, HomeState y activación/reintento.
- vehicles/VehiclesScreen.kt: lista y formulario; VehiclesViewModel, VehicleDraft; AppSession coordina selección, borrado y guardado.
- connection/ConnectionScreen.kt: configuración, permiso LAN, conexión y lectura; ConnectionViewModel conserva IP/puerto/borrador; AppSession coordina operaciones.
- diagnostic/DiagnosticScreen.kt: lectura, resultado pendiente y guardado; AppSession comparte estado y guardias.
- history/HistoryScreen.kt: lista y detalle; HistoryViewModel verifica pertenencia al vehículo y evita resultados obsoletos.
- maintenance/MaintenanceScreen.kt: lista/formulario; MaintenanceViewModel, MaintenanceDraft, MaintenanceRow y LoadState; confirma kilometraje histórico y conserva ID editado.
- settings/SettingsScreen.kt: preferencias, borrador, guardado y descarte; SettingsViewModel usa AppPreferencesRepository.
- navigation/AppNavigation.kt: cuatro destinos principales home, vehicles, diagnostic, maintenance; settings secundario; connection-flow/connection; history/{vehicleId}; diagnostic-detail/{vehicleId}/{sessionId}. Formularios son estados internos.
- components/GraphiteCard.kt y navigation/PrototypeIcons.kt: reutilización visual vigente. DistanceFormatting.kt y conversión de VehiclesViewModel: conservar unidades y valores originales.
- theme/Color.kt, Theme.kt, Type.kt: paleta Prototype, esquemas claro/oscuro según sistema y Typography. Theme no define Shapes propio. Radios y dimensiones exactas locales: PENDIENTE de inventario completo.

## Dependencias y preservación

MainActivity/AppContainer componen ViewModels; Presentation consume casos de uso y repositorios Domain. AppSession centraliza conexión, vehículo, métricas, diagnóstico pendiente y autorización de salida. Data implementa repositorios con Room/DataStore y gateway ELM327. Migrar únicamente consumidores visuales; conservar factories, alcance de ViewModels, recuperación, cancelaciones, guardias y ownership del contexto.

Home conserva datos durante actualización/error; HistoryLoad y LoadState de mantenimiento usan Loading/Ready/Failed sin datos previos en Failed: discrepancia con DD-40, no resolver silenciosamente como cambio visual. Mantener pruebas de recreación, reactivación, métricas, composición, contratos visuales y suites de ViewModels/casos de uso.

## Evidencia Figma

MCP confirmó Foundations 0:1, Base 3:2, Automotive 3:3, Service 3:4 (vacía), Screens 3:5 (31 frames), Prototype 3:6 y Documentation 3:7. Metadata confirma componentes y contratos textuales, no colores exactos, variables, interacción ni accesibilidad. Foundations contiene pantallas auxiliares en vez de tokens explícitos. Pendiente inspección visual y variables antes de implementar tokens.

La prioridad visual Automotive Dark queda establecida por la instrucción actual y reemplaza la dirección anterior. Decidir modo claro, rutas nuevas DTC/servicio/alertas, prioridad de detalles alternativos y cancelación de formularios. Ningún cambio Domain/Data justificado por ahora.
