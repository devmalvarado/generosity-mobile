# Portal público, primera etapa

Fecha: 2026-10-31. Alcance local de las tareas 16.1, 16.2, 16.3, 16.10 y las comprobaciones de navegador de 16.11. Esas tareas no se marcaron hechas en `tasks.md`: siguen pendientes la revisión en un teléfono real, el lector de pantalla y todo lo que depende de un backend. No se implementaron 16.4–16.9 ni 16.12. No se eligió proveedor de autenticación ni de pagos, no se publicó el sitio y no se modificó la landing original.

## Archivos

Creados en `portal/`. La landing de solo lectura no se tocó.

| Archivo | Papel |
| --- | --- |
| `portal/index.html` | Landing adaptada. Sin marco de interfaz. |
| `portal/styles.css` | Estilos mobile-first. `[hidden]` gana a `display: flex`. |
| `portal/portal.js` | Idioma, menú, filtros y formularios que no simulan un envío. |
| `portal/assets/logo.png` | Wordmark entregado el 2026-10-31, en sRGB y con alfa. Sustituye al pin del PoC. |
| `portal/assets/favicon.png` | Pin recortado, 192 px, fondo transparente. |
| `portal/assets/apple-touch-icon.png` | El mismo pin a 180 px, sobre papel `#FBF8F3`. |
| `portal/PROVENANCE.md` | Procedencia de marca, ejemplos y tipografías. |
| `portal/README.md` | Cómo verla y cómo comprobarla en local. |
| `portal/package.json`, `portal/package-lock.json` | Solo la herramienta de prueba, dentro de `portal/`. |
| `portal/playwright.config.js` | Chromium y WebKit contra `http://127.0.0.1:47321`. |
| `portal/tests/portal.spec.js` | Comprobaciones de esta etapa. |
| `portal/screenshots/es-390.png` | Página completa en español, 390 px CSS. |
| `portal/screenshots/en-390.png` | Página completa en inglés, 390 px CSS. |
| `portal/screenshots/es-1280.png` | Página completa en español, 1280 px CSS. |
| `portal/screenshots/en-1280.png` | Página completa en inglés, 1280 px CSS. |
| `portal/.gitignore` | Excluye `node_modules/` y reportes de Playwright. |

No se modificaron Android, iOS, shared, la configuración de la raíz, el README de la raíz ni las listas de tareas.

## Qué hace

- Conserva la dirección del PoC: papel `#FBF8F3`, tinta `#1C1915`, Fraunces y Outfit con reserva del sistema. El wordmark y el favicon salen de la marca entregada el 2026-10-31, no del pin SVG del PoC. El dibujo del mapa es ilustración, no un mapa ni la ubicación de quien visita.
- Las tarjetas (Comedor Los Olivos, Hogar Futuros Brillantes, Puente de Salud Abierta) y sus cifras están marcadas como demostración. No hay «128 centros activos» ni distancias presentadas como reales.
- El buscador no consulta un catálogo. Dice que no hay resultados reales y deja el texto escrito.
- Donar no registra una oferta, no cobra y no pide tarjeta ni datos bancarios. Si la cantidad no es válida, el error queda asociado al campo. El formulario no se vacía.
- Publicar no envía nada a revisión. Lleva a `#acceso?destino=publicar`, conserva el borrador y explica que no hay sesión ni permiso. No hay campo de contraseña.
- Idioma inicial según `navigator.languages`, en orden. `es*` es español, `en*` es inglés, cualquier otra cosa cae en inglés. El control guarda `system`, `es` o `en` en `localStorage` (`generosity.locale`). Volver a automático usa la preferencia actual del navegador. Cambiar de idioma no borra campos ni filtros. Si el almacenamiento lanza, la página sigue y la elección dura solo esa vista.
- El menú cerrado usa `hidden` y no entra en el tabulador. Escape lo cierra y devuelve el foco al botón. Al abrirlo, el estado se anuncia en una región `status`.
- El texto que escribe la persona se inserta con `textContent`, no como HTML.

## Cómo ejecutarlo

Desde `portal/`:

```bash
python3 -m http.server 47321 --bind 127.0.0.1
npm install
npx playwright install chromium webkit
npm test
```

`npm test` equivale a `playwright test`. El servidor de prueba reutiliza el puerto 47321 si ya está ocupado por esta misma carpeta. Vista local mientras ese proceso siga en pie: `http://127.0.0.1:47321/`.

Herramientas de la última corrida: Node v22.17.0, npm 10.9.2, `@playwright/test` 1.64.1.

## Resultado

El 2026-10-31, `npm test` terminó con 31 pruebas pasadas y 1 omitida, en unos 9,7 s. Tras sustituir el pin del PoC por la marca entregada, la misma corrida se repitió ese día: 31 pasadas, 1 omitida, unos 9,3 s, cero fallos. Las cuatro capturas se regeneraron. La omitida es la captura en WebKit: las imágenes se toman solo en Chromium para no pisar el mismo archivo.

Cubierto en Chromium y WebKit:

- Sin desborde horizontal a 320, 360, 390, 768 y 1280 px CSS, en español, con el menú abierto en los anchos estrechos y con un texto largo en el buscador.
- Controles primarios de al menos 44×44 px CSS a 320.
- Menú cerrado fuera de vista y del tabulador; Escape devuelve el foco.
- Filtros y campos que sobreviven al cambio de idioma.
- Formularios desconectados sin recibo, sin revisión y sin cobro; el texto se conserva.
- `es-MX` y `es-AR` eligen español; `en-GB` elige inglés; `fr-FR` cae en inglés. La primera lengua reconocida de la lista gana. La elección persiste y automático vuelve a leer el navegador. Si `localStorage` rechaza la clave, no hay excepción y al recargar se usa el idioma del navegador.
- Texto al 200 % (`html { font-size: 200% }`) a 320 y 1280 sin desborde horizontal, con el envío a la vista.
- Entrada de teclado: en Chromium, Tab llega al salto de contenido. En este WebKit, Tab recorre campos y listas, como Safari con el acceso completo por teclado apagado; Enter activa el salto, el menú y el filtro cuando el control ya tiene foco.

Esas vistas simuladas no sustituyen Safari en iOS ni Chrome en Android, con teclado virtual, en vertical y horizontal. Tampoco se pasó VoiceOver ni TalkBack.

## Criterios

| Tarea | Hecho en esta copia | Sigue pendiente |
| --- | --- | --- |
| 16.1 | Fuente y logo reunidos en `portal/` sin borrar el PoC. Procedencia, vista local y pruebas documentadas. Sin framework de interfaz. | No hay decisión de despliegue ni de sesión. No se contrató un proveedor. |
| 16.2 | Sin métricas inventadas, sin recibo, sin revisión y sin pago. El formulario desconectado conserva el texto. Publicar lleva al acceso pendiente y guarda el destino en el hash, sin conceder permiso. | Un inicio de sesión real cuando exista proveedor. |
| 16.3 | Base de una columna desde 320 px; más columnas desde 768 y navegación de escritorio desde 1200 px. `hidden` no se muestra ni se enfoca. Objetivos primarios de 44 px. | Teclado virtual y orientación en un teléfono real. |
| 16.10 | Español e inglés, preferencia ordenada, persistencia, vuelta a automático, errores asociados y foco del menú. | Fechas localizadas: esta página no muestra una fecha de cierre. Lector de pantalla real. |
| 16.11 | Chromium y WebKit en los cinco anchos, capturas es/en, sin desborde, sin falso éxito, menú y formularios ejercitados, texto al 200 % en el automatismo. | La misma revisión con el teclado virtual de Safari y Chrome. |

16.4 a 16.9 y 16.12 no se empezaron. No hay espacio de personal, borradores en servidor, publicación, confirmación de aportes ni API.

## Cuando haya backend

Hace falta, y no se adelantó aquí:

- Un proveedor de autenticación y la regla de asignación a un centro. La pantalla `#acceso` debe sustituirse por ese inicio de sesión, conservando `destino=publicar`, sin crear sesión ni permiso antes de una respuesta real.
- Catálogo compartido con las apps. Hasta entonces las tarjetas siguen siendo demostración y el buscador no puede prometer cercanía.
- Publicación durable, con id de operación, versión y borrador privado. El formulario actual no debe vaciarse ni anunciar éxito si la red o la sesión fallan.
- Pagos solo con un proveedor aprobado. Esta página no debe pedir tarjeta ni datos bancarios.
- Destinos legales reales, si se aprueban. Hoy `#privacidad` son principios de la página, no una política, y el único contacto es `mailto:hola@generosity.global`.
- Pruebas de CSRF, cruce de centros, borradores fuera de la respuesta pública y sesión revocada. Eso es 16.12 y no puede resolverse solo en el navegador.
