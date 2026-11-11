## 1. Fase de Diseño de Interfaz UI/UX

### 1.1. Objetivo

Coordinar el diseño, la implementación y la validación de la interfaz Android mediante un flujo incremental entre **Android Studio, Codex y Figma**, manteniendo la coherencia entre los contratos del dominio, las especificaciones UI/UX, los prototipos y el código Kotlin/Jetpack Compose.

El diseño y la implementación podrán avanzar en paralelo, siempre que se respeten las dependencias funcionales y se documenten las decisiones aprobadas.

### 1.2. Herramientas y responsabilidades

**Android Studio y repositorio Kotlin**
- Fuente de verdad para entidades, contratos, casos de uso y reglas de negocio.
- Implementación de Presentation mediante ViewModels, UiState y eventos.
- Implementación visual mediante Jetpack Compose y Material 3.
- Validación mediante Compose Preview, emulador y pruebas automatizadas.

**Figma Make**
- Exploración inicial de ideas de interfaz mediante IA.
- Generación de prototipos y flujos interactivos.
- Evaluación de alternativas de navegación y experiencia.
- No considerar su código web como implementación Android.

**Figma Design**
- Fuente de referencia visual de las pantallas aprobadas.
- Diseño de wireframes y mockups.
- Definición de componentes, variantes, estilos y variables.
- Organización del sistema de diseño.
- Preparación de especificaciones para implementación.

**Figma MCP**
- Utilizar el servidor MCP de Figma configurado en Codex.
- Comprobar su disponibilidad y las herramientas habilitadas antes de operar.
- Consultar los recursos de Make y Design cuando corresponda.
- Obtener información de pantallas, componentes, variables y propiedades.
- Crear o modificar diseños únicamente con autorización del usuario.
- No asumir sincronización automática entre Figma y Android Studio.


### 1.3. Fuentes de información

Antes de diseñar, modificar o implementar una funcionalidad UI/UX, consultar los documentos relevantes:

1. `plans/Contexto_Maestro_App_OBDII_v2.md`
    - Alcance, requisitos y decisiones generales del proyecto.

2. `plans/designplans/domain-contracts.md`
    - Modelos, interfaces, operaciones y restricciones del dominio.

3. `plans/designplans/ui-ux-specification.md`
    - Inventario de pantallas, información visible, acciones, estados y criterios de aceptación.

4. `plans/designplans/screen-flows.md`
    - Navegación, recorridos del usuario, decisiones y caminos alternativos.

5. `plans/designplans/design-system.md`
    - Colores, tipografías, espaciados, componentes, variantes y convenciones visuales.

6. `plans/designplans/design-decisions.md`
    - Decisiones aprobadas, justificación, cambios y elementos afectados.

7. `plans/designplans/figma-references.md`
    - Enlaces a archivos, páginas y frames de Figma, junto con su estado de aprobación.

Si algún documento no existe, informar de ello y proponer su creación. No asumir que su contenido está definido.

Verificar los contratos reales del código cuando exista una discrepancia entre la documentación y la implementación.

### 1.4. Flujo de trabajo incremental

Cuando el usuario solicite diseñar, revisar, modificar o implementar una pantalla:

**Fase A. Análisis funcional**

1. Leer el contexto general y los contratos de Domain.
2. Identificar entidades, casos de uso y operaciones relacionadas.
3. Determinar qué información necesita presentar la pantalla.
4. Identificar acciones, validaciones y restricciones.
5. Detectar dependencias con otras pantallas o módulos.

**Fase B. Especificación UI/UX**

1. Revisar o proponer la especificación de pantalla.
2. Definir el objetivo y los recorridos del usuario.
3. Identificar estados vacíos, de carga, éxito, error y confirmación aplicables.
4. Definir navegación y acciones.
5. Registrar los criterios de aceptación.
6. Presentar las decisiones funcionales pendientes de aprobación.

**Fase C. Revisión y diseño en Figma**

1. Conectar al servidor Figma MCP disponible.
2. Localizar los diseños y prototipos relacionados.
3. Determinar si se trata de un wireframe, mockup o prototipo funcional.
4. Comparar la representación visual con las especificaciones.
5. Detectar diferencias, elementos faltantes y comportamientos no definidos.
6. Proponer correcciones sin modificar todavía los diseños aprobados.
7. Tras la autorización, aplicar los cambios mediante las herramientas disponibles o proporcionar instrucciones precisas para realizarlos manualmente.

**Fase D. Preparación de Presentation**

1. Identificar los UiState necesarios.
2. Definir eventos e interacciones de usuario.
3. Establecer la relación entre ViewModel y casos de uso.
4. Identificar componentes Compose reutilizables.
5. Definir los datos de prueba necesarios para las previews.
6. Verificar que las reglas de negocio permanezcan fuera de los componentes visuales.

**Fase E. Implementación Jetpack Compose**

1. Implementar componentes según el sistema visual aprobado.
2. Construir las pantallas y sus estados.
3. Implementar navegación y manejo de eventos.
4. Conectar los ViewModels con los casos de uso existentes.
5. Evitar duplicar reglas de negocio.
6. Utilizar componentes Material 3 cuando corresponda.
7. Mantener la interfaz reactiva y consistente con el flujo unidireccional de datos.

**Fase F. Validación y retroalimentación**

1. Comparar la implementación Compose con el diseño aprobado.
2. Verificar navegación, acciones y estados.
3. Ejecutar las pruebas correspondientes.
4. Identificar diferencias funcionales, visuales y de accesibilidad.
5. Clasificar los problemas por prioridad.
6. Proponer correcciones en Figma, Presentation o Compose según corresponda.
7. Registrar las decisiones aprobadas.
8. Actualizar el estado de implementación de la pantalla.

Repetir este ciclo por pantalla o funcionalidad, evitando rediseñar toda la aplicación por cambios locales.

### 1.5. Gestión de cambios bidireccionales

**Cuando cambia el dominio:**
- Identificar pantallas y contratos de Presentation afectados.
- Revisar las especificaciones UI/UX.
- Comprobar si el diseño de Figma necesita ajustes.
- Proponer los cambios necesarios antes de implementarlos.

**Cuando cambia Figma:**
- Comparar el cambio con las especificaciones y contratos existentes.
- Clasificarlo como visual, UX, funcional o arquitectónico.
- Identificar componentes Compose y pruebas afectados.
- Solicitar aprobación si implica modificar funcionalidades o reglas.
- No trasladar automáticamente cualquier elemento nuevo al código.

**Cuando cambia Compose:**
- Comprobar que se mantienen las reglas funcionales.
- Comparar el resultado con el diseño aprobado.
- Registrar las desviaciones justificadas.
- Proponer actualizar Figma si el cambio visual debe convertirse en la nueva referencia.

### 1.6. Estados de desarrollo de cada pantalla

Utilizar los siguientes estados:

- `DRAFT`: propuesta inicial.
- `UX_REVIEW`: flujos y comportamientos en revisión.
- `DESIGN_READY`: diseño y estados aprobados.
- `IMPLEMENTING`: implementación Compose en curso.
- `VALIDATING`: validación funcional y visual.
- `DONE`: implementación validada y documentación actualizada.

Una pantalla puede avanzar de manera independiente siempre que sus dependencias estén definidas.

### 1.7. Restricciones arquitectónicas y funcionales

- No modificar los contratos de Domain sin autorización explícita.
- No introducir funcionalidades no contempladas sin presentar una propuesta.
- No implementar reglas de negocio dentro de componentes Compose.
- Reflejar visualmente las restricciones y resultados del dominio.
- Mantener separación entre Domain, Data y Presentation.
- No utilizar directamente el código generado por Figma Make como implementación Android.
- No asumir que un mockup contiene todos los estados funcionales.
- No considerar una interacción implementada sin comprobar su comportamiento.
- No crear una segunda fuente de verdad para las reglas de negocio.
- Priorizar claridad, consistencia, accesibilidad y facilidad de uso.
- Diseñar para Android y contemplar diferentes tamaños de pantalla.
- Favorecer componentes reutilizables y Material 3.
- No modificar Figma o el código de producción durante una auditoría de solo lectura.
- No realizar cambios arquitectónicos por razones exclusivamente estéticas sin aprobación.

### 1.8. Reglas específicas del proyecto OBD-II

Verificar que las pantallas respeten, cuando corresponda:

- Un usuario local puede registrar varios vehículos.
- No se puede cambiar el vehículo activo mientras exista una conexión OBD activa.
- El Dashboard debe contemplar el estado sin vehículos registrados.
- El kilometraje se edita como parte de los datos del vehículo.
- Los intentos fallidos de escaneo no deben persistirse.
- El escaneo OBD se ejecuta bajo demanda, no mediante sondeo continuo.
- Los resultados pendientes deben conservarse hasta guardar o descartar.
- Al abandonar una pantalla con resultados pendientes, solicitar confirmación cuando corresponda.
- Los códigos DTC desconocidos deben mostrar información genérica sin atribuir causas específicas.
- Las fechas de mantenimiento son fechas calendario sin hora.
- Las alertas derivadas deben calcularse a partir de los datos del dominio y no duplicarse como estado persistente innecesario.

Estas reglas deben comprobarse contra los contratos reales antes de generar código.

### 1.9. Criterios de aprobación

**Diseño listo para implementación (`DESIGN_READY`):**

- Objetivo de pantalla definido.
- Datos y acciones identificados.
- Navegación documentada.
- Estados relevantes definidos.
- Restricciones funcionales representadas.
- Diseño visual aprobado.
- Componentes reutilizables identificados.
- Referencia de Figma registrada.

**Implementación terminada (`DONE`):**

- Interfaz Compose implementada.
- Estados y eventos conectados correctamente.
- Navegación funcional.
- Pruebas relevantes superadas.
- Validación visual realizada.
- Diferencias documentadas y resueltas o aceptadas.
- Especificaciones y referencias actualizadas.

### 1.10. Comportamiento esperado del agente

Antes de realizar modificaciones importantes, presentar:

1. Estado actual de la funcionalidad.
2. Diferencias entre Domain, Figma y Compose.
3. Cambios propuestos.
4. Archivos, pantallas y componentes afectados.
5. Riesgos o decisiones pendientes.
6. Plan de implementación y validación.

Solicitar aprobación antes de cambios que alteren contratos, navegación principal, funcionalidades o diseños previamente aprobados.

Para cambios visuales locales ya autorizados, puede avanzar directamente y presentar los resultados.

Si Figma MCP no está disponible o no proporciona información suficiente, declarar la limitación. No inventar propiedades, componentes, interacciones ni estados del diseño.

El objetivo final es mantener una correspondencia verificable entre **requisitos funcionales → especificación UX → diseño Figma → contratos Presentation → implementación Compose → pruebas**.