# Referencias Figma y límites de inspección

## Etapa 4.4 — contraste visual inicial

Capturas estáticas revisadas en ejecución 4.4: [Inicio 11:4](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-4), [Inicio sin diagnóstico 11:42](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-42), [formulario 11:286](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-286) y [permiso 13:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-75). El texto renderizado de 11:42 dice «Aún no hay diagnósticos»: la sospecha basada en el nombre de una capa no constituye un error visible. La barra inferior de 11:4 es texto compacto; 11:286 muestra «Aceite» como ejemplo sin el selector completo; 13:75 muestra Abrir ajustes y Cancelar. Esto no verifica interacción, tamaños táctiles ni accesibilidad; no concede DESIGN_READY ni sustituye la revisión de todos los estados.

## Revisión de cierre — evidencia y límites

Inspección remota de solo lectura: las 107 referencias con ID de nodo extraídas de 12 documentos de diseño se resolvieron (93 en Design y 14 en FigJam). Esto acredita existencia y nombre de nodo, no que cada enlace represente correctamente un estado, ni interacción o aprobación. Las referencias históricas se conservan como tales.

Se contaron cero reacciones declaradas en 10:2, 11:2, 13:2, 27:2, 28:2, 28:77, 28:169, 29:3, 29:20 y las variantes 36:3, 36:22, 36:36, 36:49, 36:66, 36:84, 36:103 y 38:2, incluidos sus descendientes. Esto limita la evidencia a esos subárboles; no descarta un prototipo en otro archivo. No se ejecutaron recorridos interactivos.

Capturas estáticas revisadas: [36:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-3), [36:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-22), [36:36](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-36), [36:49](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-49), [36:66](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-66), [36:84](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-84), [36:103](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-103), [38:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=38-2), [Vehículos 10:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-3), [Mantenimiento 11:267](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-267), [formulario 11:286](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-286) y [F03 2:220](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220). Persisten barra inferior en dos líneas en Inicio y Vehículos, barra pequeña en Mantenimiento y etiqueta TCP parcialmente tapada en F03; la rama de persistencia se referencia por texto. El formulario presenta Aceite como ejemplo, no un selector de ocho tipos.

Estas capturas no prueban dimensiones táctiles, contraste medido, TalkBack, foco, teclado, scroll, adaptación o interacciones. Las variantes nuevas no quedan aprobadas por su inspección; ver la [evaluación por paquete](design-handoff.md#evaluación-de-preparación-por-paquete--revisión-de-cierre).



## Estados añadidos — 2026-11-10

Nuevas referencias editables, propuestas y pendientes de revisión del usuario; no acreditan navegación, accesibilidad ni Android:

- Conexión: [IP/puerto inválido 36:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-3) presenta corrección/revalidación y cancelar a Diagnóstico; [desconectando 36:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-22) muestra progreso y salida permitida.
- Inicio: [sin vehículos 36:36](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-36) es referencia propuesta para primer alta y tras borrar el último; reemplaza como referencia vigente al original [3:8](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-8), conservado con nombre HISTÓRICO. [Carga inicial 36:49](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-49) y [error inicial 36:66](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-66) representan estado completo y reintento.
- Ajustes: [carga inicial 36:84](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-84) y [error inicial 36:103](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=36-103) presentan carga y recuperación sin suponer valores persistidos.
- [Lectura con dato no disponible 38:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=38-2): variante editable de 11:94 que muestra RPM «No disponible», refrigerante 89 °C y velocidad 0 km/h; captura estática revisada sin recortes. No demuestra lectura real ni interacción.
- En [lectura 11:94](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-94), la tarjeta de temperatura de admisión 11:107 quedó oculta y etiquetada histórica; la captura visible mantiene RPM, refrigerante y velocidad. No se borra el nodo.
- Mantenimiento [formulario 11:286](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-286): «Aceite» es solo ejemplo; el selector final debe ofrecer los siete tipos alertables y OTHER definidos en el dominio. El selector real aún no está representado ni implementado.

Se verificaron capturas estáticas de los tres grupos y lectura OBD; no se observaron recortes de los nuevos mensajes. La navegación inferior heredada de Inicio sigue partida en dos líneas; revisión visual/accesibilidad aplazada según instrucción. No se comprobaron interacciones ni accesibilidad.

## Saneamiento de variantes — 2026-11-10

El usuario autorizó el saneamiento y fijó Diagnóstico como inicio y destino de salida final de F03 en el MVP. Se conserva la aprobación anterior de wireframes; las variantes nuevas requieren revisión y no acreditan implementación.

- Vehículos: [conectado 27:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-3), [fallo de selección 27:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-20); grupo [27:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=27-2).
- Vehículos, carga/error/actualización: [28:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-2), variantes 28:3, 28:20, 28:37 y 28:56.
- Historial, vacío y carga/error/actualización: [28:77](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-77), variantes 28:78, 28:95, 28:112, 28:131 y 28:152.
- Mantenimiento, carga/error/actualización: [28:169](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=28-169), variantes 28:170, 28:189, 28:208 y 28:229.
- Ajustes: [descarte 29:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-3) y [fallo de guardado 29:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-20).
- La variante de selección parcial de DTC 23:61 se retiró de la entrega vigente: está oculta y marcada OBSOLETO en [archivo visual 29:37](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=29-37), con otras copias obsoletas. La captura diagnóstica se guarda íntegra según DD-05. Las capas 13:13 y 11:37 se renombraron conforme a su texto real.
- [F03 2:220](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220): origen, aviso y salida actualizados; cancelar el intento permanece en configuración como estado intermedio y la salida final va a Diagnóstico. Se reanclaron dos conectores que atravesaban cuadros. [Rama de persistencia 49:724](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=49-724): handshake, fallo con conexión activa y reintento exclusivo del guardado; F03 la referencia mediante texto, sin conector físico entre secciones. Los accesos de Inicio 11:4, 11:23, 11:42 y 13:3 se cambiaron para abrir Diagnóstico.

Editor, verificador y QA revisaron capturas estáticas y conectores. QA aprobó el saneamiento con observaciones: una etiqueta TCP está parcialmente tapada, una línea de salida es visualmente cercana a la de éxito y la rama de persistencia se vincula por texto. La navegación inferior de Vehículos está partida en dos líneas y la de Mantenimiento es pequeña; quedan pendientes legibilidad y accesibilidad. Los textos anteriores de este archivo describen la inspección previa y quedan superados en lo relativo a estas variantes, al destino F03 y a la separación de obsoletos.


Inspección visual estática 2026-11-08: capturas de grupos Design 3:2 y 9:2, F01–F10 y panel de decisiones revisadas. Se detectaron discrepancias y conectores superpuestos; ver [reporte preparatorio](design-review-report.md#inspección-visual--2026-11-08). Interacciones, accesibilidad y aprobación visual siguen sin verificar.

Sincronización autorizada: 2026-11-07. FigJam actualizado mediante Figma MCP tomando design-decisions.md, screen-flows.md y ui-ux-specification.md como fuente de verdad funcional. Se conservaron los casos abiertos de esos archivos; no se añadieron aprobaciones.

## Paso 1 del cierre — inspección estructural 2026-11-09

**Histórico:** los estados, destinos y referencias identificados como pendientes abajo fueron revisados el 2026-11-10; consultar primero [saneamiento vigente](#saneamiento-de-variantes--2026-11-10).

La aprobación registrada en chat el 2026-11-09 comprende los wireframes revisados UI-01–UI-07 y variantes vinculadas; el texto literal del mensaje original no está disponible en este registro. Se conserva también la aprobación de F03 tras añadir corrección/revalidación y cancelación al origen sin guardar. Es aprobación visual/funcional del diseño, no de pantallas Android ejecutables ni de un prototipo conectado.

| UI / variante | Referencia candidata de la revisión aprobada | Resultado de inspección |
|---|---|---|
| UI-02 lista, alta, edición | [10:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-3), [10:39](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-39), [10:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-20) | Metadatos consultados; lista desconectada, aviso conectado oculto. Originales 3:38 y 3:55 conservados. |
| UI-01 sin mantenimiento | [13:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-3) | El texto real es «Aún no hay mantenimientos / Registrar mantenimiento». El nombre obsoleto de la capa causó la observación anterior; no fue necesario editar el lienzo. |
| UI-06 vacío | [13:22](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-22) | Texto de vacío y acción de registro visibles en metadatos. |
| UI-05 detalle | [13:41](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-41) | Contiene sesión, código y volver al origen. |
| UI-04 limpio guardado | [13:58](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-58) | Contiene resultado limpio guardado y acciones de retorno y nuevo escaneo. |
| UI-03 permiso, abrir ajustes | [13:75](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-75) | Contiene explicación y acciones representadas. |

Los grupos [11:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-2) y [13:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-2) son índices, no pruebas de cada estado. En [11:23](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-23) el texto real sí dice «Sin códigos detectados»; la observación anterior confundió el nombre obsoleto de la capa con su contenido.

En [F03 2:220](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220), lectura estructural de conectores: 2:231 → 2:243 (inválido), 2:243 → 2:227 (corregir y revalidar), 2:243 → 2:223 (cancelar y descartar); 2:235 → 18:442 (fallo TCP/inicialización) y 18:442 → 2:227 (reintentar). El texto del nodo 2:227 dice «Cancelar a Inicio», mientras el destino 2:223 contempla Inicio / Diagnóstico: requiere conciliación con retorno al origen. La nota 31:785 conserva la aprobación pero no reemplaza la comprobación de conectores.

### Inventario por variante — contenido real

Inspección mediante Plugin API de textos visibles de los descendientes de 10:2, 11:2 y 13:2. Los IDs siguientes se abren agregando `?node-id=ID` a [Design](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt). La aprobación general del 2026-11-09 se conserva; las copias posteriores no se declaran aprobadas individualmente por el solo hecho de existir.

| UI | Referencia por estado | Contraste |
|---|---|---|
| UI-01 | 11:4 resumen; 11:23 limpio; 11:42 sin diagnóstico; 13:3 sin mantenimiento | Textos corresponden al estado; 11:4 muestra recurrencia. |
| UI-02 | 10:3 lista; 10:39 alta; 10:20 edición | Lista desconectada; menú anotado. Falta variante verificada de lista con OBD conectado y fallo parcial de selección de vehículo. |
| UI-03 | 11:62 configuración; 11:81 conectando; 11:94 lectura; 11:115 error; 11:415 aviso; 11:432 permiso denegado; 11:449 fallo al guardar; 11:466 cambio IP; 11:483 lectura interrumpida; 13:75 ajustes de permiso | Lectura muestra dato no disponible; fallo de persistencia ofrece reintentar guardado. |
| UI-04 | 11:133 listo; 11:150 pendiente; 11:167 limpio pendiente; 11:184 guardado; 11:202 escaneando; 11:217 fallo; 11:232 fallo de guardado; 11:500 desconectado; 13:58 limpio guardado | Representan captura, guardado y recuperación; no acreditan interacción. |
| UI-05 | 11:249 lista; 13:41 detalle | Sesiones separadas, detalle y retorno representados; vacío, carga y error pendientes. |
| UI-06 | 11:267 lista; 11:286 alta; 11:307 edición; 11:326 confirmación histórica; 13:22 vacío | Representan registro, edición y confirmación; carga/error pendientes. |
| UI-07 | 11:344 preferencias; 11:380 descarte de formulario | Guardado explícito; falta contrastar confirmación de descarte específica y errores. |
| Transversal | 11:363 salida diagnóstico; 11:397 borrado de vehículo | Las opciones y la cascada están representadas como textos. |

**Copias posteriores en 11:2:** 23:40 reproduce lectura de Conexión, no la lista de Vehículos conectada; 23:61 ofrece «Guardar selección parcial» de DTC, incompatible con la captura íntegra DD-05 y no debe usarse como referencia vigente. 23:79 muestra carga del estado de conexión con datos anteriores y 23:137 error/reintento; las otras copias 23:98, 23:117, 23:156, 23:175, 23:195, 23:214, 23:235, 23:254, 23:272, 23:291, 23:312 y 23:331 reproducen textos de estados existentes; sus nombres o duplicación no acreditan nuevas variantes de carga/error. Conservar los originales sin borrarlos y usar los IDs de la tabla hasta resolver las diferencias.

**F03:** la bifurcación aprobada está definida en [screen-flows.md](screen-flows.md#bifurcación-f03--entrada-inválida-flujo-aprobado-tras-revisión-del-usuario). El conector de cancelación desde datos inválidos llega al origen y descarta borrador. La etiqueta «Cancelar a Inicio» de 2:227 sigue siendo texto real del tablero; también hay una propuesta histórica de cancelar el aviso hacia Inicio, por lo que no se modifica ni se infiere una nueva decisión. Conciliar el alcance de esa etiqueta con las reglas de retorno en la revisión de recorridos.

La inspección es estructural: textos, visibilidad y extremos de conectores. No se comprobó interacción de prototipo, navegación ejecutable, accesibilidad ni hardware; los botones son representaciones estáticas hasta disponer de evidencia de interacción. Faltan revisión visual detallada, estados ausentes y resolución de discrepancias; este inventario no certifica DESIGN_READY.

## Revisión de wireframes por recorrido — 2026-11-09

[Conjunto editable 11:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-2), copias de los borradores anteriores; originales conservados:

- [Inicio y estados diagnósticos](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-3): jerarquía de mantenimiento, diagnóstico y métricas, recurrencia y vacíos.
- [Conexión y lectura](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-61), [casos de conexión y permisos](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-414).
- [Diagnóstico](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-132), [errores e historial](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-201): texto de salida, códigos reales y lista por sesión.
- [Mantenimiento](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-266): lista y formulario, edición con tipo/notas y estados.
- [Ajustes y confirmaciones](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=11-343): botón Guardar y confirmaciones.
- [Variantes adicionales](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=13-2): sin mantenimiento en Inicio, mantenimiento vacío, detalle de historial, diagnóstico limpio guardado y permiso con acceso a ajustes.
- [Vehículos 10:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-2) sigue separado, como se registró anteriormente.

Evidencia: 604 descendientes (289 frames y 315 textos), sin rellenos de imagen, tipografía Roboto; capturas estáticas revisadas en grupos principales y variantes. Son wireframes DRAFT: textos, botones y navegación son representaciones, no prototipo conectado. La navegación reducida a 11 px evita el salto en estas copias, pero no constituye solución accesible aprobada.

**Pendientes para revisión y trabajo adicional:** (1) usuario revisa jerarquía, textos y aspecto por recorrido y aprueba o solicita cambios; (2) construir/validar controles reales, menú por fila, navegación, estados de Vehículos conectado, selección parcial, carga/error y confirmaciones; (3) revisar diferencias de FigJam, cruces de conectores y conexión de ramas; (4) validar contraste, tamaños táctiles, TalkBack, scroll, teclado y tamaños de ventana; (5) confirmar recuperación LAN por versión de Android y probar con hardware. No se han inspeccionado todas las variantes ni se garantiza que las existentes cubran todas las combinaciones. Aprobación visual de wireframes: el usuario confirmó en chat el 2026-11-09 la aprobación de los wireframes revisados de UI-01–UI-07 y variantes vinculadas. Se registra como aprobación visual de wireframe; no acredita interacción de prototipo, accesibilidad instrumentada, integración Android ni pruebas con hardware. La rama F03 aprobada tras la revisión está en FigJam 2:220 y se documenta en screen-flows.md.

## FigJam — sincronización funcional parcial, 2026-11-08

A petición del usuario se actualizaron nodos editables del [tablero](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=0-1): mapa y F01–F08, y el [panel de seis temas históricos](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-616). Se corrigieron en particular selección y año, cancelación OBD, diagnóstico pendiente, recurrencia, mantenimiento del mismo ID, guardado de Ajustes, millas y permiso LAN; referencias a DD-33–48 son resúmenes, no representación exhaustiva de cada variante.

Se inspeccionaron capturas de F04, F07 y el panel. Persisten cruces y superposiciones de conectores en F04 y otros recorridos; faltan un contraste exhaustivo nodo por nodo y la actualización estructural de variantes para todas las decisiones recientes. F09/F10 se conservaron sin cambios. La sincronización es parcial y no acredita prototipo interactivo, aprobación visual ni implementación Android.

## Vehículos — revisión editable autorizada, 2026-11-08

[Revisión de Vehículos 10:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-2): [lista 10:3](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-3), [alta 10:39](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-39) y [edición 10:20](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=10-20). Copias editables basadas en 3:38 y 3:55, conservando originales. Se representa DD-42: datos por fila, acción de selección en la fila, menú textual por vehículo y variantes de formulario; se retira del formulario texto técnico sobre ECU. La lista representa estado desconectado; la variante conectada y guardias, confirmaciones, fallo parcial, navegación e interacciones aún no están representadas/validadas. El menú es una anotación de wireframe, no un control interactivo implementado.

Captura estática revisada tras ajustar alturas recortadas: Roboto y estructura editable existente. La navegación inferior heredada sigue partida en dos líneas. No se verificaron prototipo, contraste, TalkBack, adaptación ni aprobación visual. Sigue DRAFT. FigJam y otras pantallas no se actualizaron en esta intervención.

## Actualización de conexión — autorizada por usuario

[Design 9:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=9-2) contiene seis variantes editables: aviso Wi-Fi previo, permiso denegado, fallo al guardar configuración, desconexión antes de editar IP, lectura interrumpida y diagnóstico desconectado. Inicio 3:29 ofrece Conectar. [FigJam F03](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-220) se actualizó en nodos 2:222, 2:227, 2:239, 18:442, 3:412 y 24:544. Capturas revisadas; F03 conserva cruces de conectores preexistentes. Otros flujos no se actualizaron en esta intervención; revisar concordancia con decisiones recientes. Design sigue DRAFT, sin validación de interacción/accesibilidad ni aprobación visual.

## Actualización Design — autorizada por usuario

En [Design, página 3:2](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2), Inicio 3:21 separa alertas de códigos (8:2/8:3); variantes editables 8:5 y 8:24 representan diagnóstico limpio y ausencia de diagnóstico. Las notas técnicas obsoletas quedaron ocultas sin borrar mediante 3:329 y 3:375. El detalle 3:279 conserva «Volver al origen»; se aprobó retornar a Inicio o a la lista de historial según origen, pendiente de validar la interacción. Se inspeccionaron capturas sin recortes visibles. Los diseños siguen DRAFT: no se validaron interacción ni accesibilidad ni existe aprobación visual. FigJam no se modificó en esa intervención; F03 se actualizó posteriormente como se indica arriba.

## Resultado verificable

- Tablero proporcionado por el usuario: [Bitácora OBD — Flujos exploratorios](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=0-1). Tipo FigJam; fileKey 1fsHY8ZUGVre2PrWrCi7z6, raíz 0:1 y sección general 2:2.
- Inspección mediante Figma MCP get_figjam. El tablero incluye mapa y F01–F06; la lectura no valida wireframes, componentes de Figma Design, tokens o navegación Android implementada.
- Las afirmaciones anteriores de falta de acceso y ausencia de URL quedaron obsoletas. Design fue inspeccionado parcialmente; Make no fue inspeccionado.
- Las aprobaciones funcionales están en design-decisions.md. En la sincronización histórica se actualizaron F01–F06 y el mapa y se añadieron F07–F10. La inspección posterior encontró diferencias con acuerdos y backend actuales; ver [reporte](design-review-report.md#contradicciones-de-figjam). Las etiquetas U/R/B del tablero no certifican vigencia.
- No existe evidencia para declarar aprobación visual, DESIGN_READY o DONE.

## Referencias locales disponibles

| Fuente | Evidencia | Alcance |
|---|---|---|
| [Contexto maestro](../Contexto_Maestro_App_OBDII_v2.md) §16 | Planifica prototipo Figma o Stitch | Requisito, sin URL |
| [Brief fase 1B](../ux_fase1.md) | Pantallas, componentes y recorridos propuestos para llevar a Figma | Propuesta local, no archivo Make/Design |
| [Reporte fase 1](../reporte_fase1.md) | Indica prototipo no construido y trabajo posterior del usuario | Estado declarado, sin inspección remota |
| [Checklist](../task.md) | Marca construcción/validación en un ítem; su cierre deja prototipo al usuario | Contradicción H-08, no aprobación verificable |

## Registro de diseños sincronizados

| Área | Nodo de FigJam | Estado de esta sincronización |
|---|---|---|
| Mapa general | 2:19 | Cuatro destinos principales; accesos secundarios a Conexión, Historial y Ajustes |
| F01 Primer vehículo | 2:74 | Guardar y persistir activo DD-27; fallo parcial reintenta solo selección |
| F02 Gestión de vehículos | 2:121 | Selector/destinos DD-28, cascada y kilometraje manual local |
| F03 Conexión y métricas | 2:220 | Apertura/cancelación DD-20/DD-26 y captura compartida DD-29 |
| F04 Diagnóstico y resultado pendiente | 2:323 | Guardado DD-25, fallo con reintento y nuevo escaneo sin paso obligado por Inicio |
| F05 Historial y recurrencia | 2:426 | Consulta por vehículo y retorno al origen, sin alterar alertas por consultar historia |
| F06 Mantenimiento | 2:505 | Edición del mismo ID DD-16 y validaciones DD-30; backend implementado, UI pendiente |
| [F07 Ajustes, unidades y Dashboard](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-554) | 25:554 | Tres interruptores DD-29, captura compartida y cambio de representación DD-31 |
| [F08 Formularios y contexto](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-555) | 25:555 | Descarte/guardado DD-30 y cierre de edición antes de Ajustes DD-31 |
| [F09 Continuidad ante rotación](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-556) | 25:556 | DD-32: UiState/StateFlow en ViewModels, sin duplicar operaciones por recreación |
| [F10 Odómetro de referencia futuro](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=25-557) | 25:557 | DD-21: referencia futura, no lectura disponible ni sobreescritura automática |
| [Decisiones abiertas](https://www.figma.com/board/1fsHY8ZUGVre2PrWrCi7z6?node-id=2-616) | 2:616 | Seis casos abiertos del saldo de screen-flows.md, separados de implementación |

Validación de esta sincronización: 332 elementos editables en 13 secciones internas (130 cuadros, 153 conectores y 36 textos); sin imágenes aplanadas. Comprobación estructural sin desbordes, solapamientos entre cuadros/textos ni conectores con destino inexistente; tipografía Inter consistente con el tablero. Composición revisada mediante captura general y una corrección visual posterior. Esto no acredita implementación Android, pruebas con hardware ni aprobación visual de las pantallas del producto.

## Inspección parcial de Design posterior

[Design — página inspeccionada](https://www.figma.com/design/yIZcqwztYdI5SZNs5VPwPt?node-id=3-2) sigue DRAFT. La inspección inicial identificó discrepancias en 3:21, 3:330 y 3:376 que posteriormente se abordaron según la actualización anterior. El retorno al origen fue decidido, pero falta validar la interacción; 3:303 y las variantes de los nuevos tipos requieren revisión. La validación visual completa sigue en el plan propio de diseño.

## Seguimiento

Cuando se cierren los casos abiertos del [saldo de revisión de flujos](screen-flows.md#saldo-de-la-revisión-final-de-flujos), actualizar primero las decisiones locales y después su representación en FigJam. Sigue pendiente diseñar/aprobar pantallas y componentes visuales contra [contratos](domain-contracts.md), [especificación](ui-ux-specification.md) y [sistema visual](design-system.md). La contradicción histórica del checklist/reporte se conserva como evidencia histórica; el acceso actual a FigJam no certifica aquel cierre ni pruebas de navegación.
