# Procedencia

Adaptado el 2026-10-30 desde la landing estática de solo lectura:

`/Users/manuel.alvarado/Documents/Development/PoCs/generosity/portal`

Archivos de origen revisados y no modificados: `index.html`, `styles.css`, `portal.js`, `assets/logo.svg`.

- El 2026-10-31 el pin del PoC se sustituyó por la marca entregada en `/Users/manuel.alvarado/Downloads/gen2.png` (1051×588, RGBA, Display P3, fondo transparente, pin coral y wordmark en tinta). Se convirtió a sRGB y se recortó a `assets/logo.png`. No se trazó a SVG: el wordmark es raster y un calco lo deformaría. `Untitled.png`, de la misma entrega, trae el mismo dibujo sobre un gris opaco y no se usa, porque ese rectángulo no coincide con el papel `#FBF8F3`.
- `assets/favicon.png` es el pin recortado de ese archivo, en cuadrado de 192 px con fondo transparente. `assets/apple-touch-icon.png` es el mismo pin a 180 px sobre papel `#FBF8F3`, para que iOS no pinte de negro el canal alfa.
- La estructura, los nombres de ejemplo y la dirección visual (papel `#FBF8F3`, tinta `#1C1915`, coral `#E4572E`, Fraunces y Outfit) se reutilizan. El CSS se reescribió mobile-first; el JavaScript se reescribió para no simular envíos, pagos, revisión ni sesión.
- Fraunces y Outfit se piden a Google Fonts. Si no cargan, la página usa Iowan Old Style/Georgia y Avenir Next/Segoe UI.
- Comedor Los Olivos, Hogar Futuros Brillantes y Puente de Salud Abierta, sus cantidades y el dibujo del mapa son ejemplos de demostración del PoC. No son un catálogo, un censo ni la ubicación de quien visita.
- No se añadió un destino legal ni un enlace de descarga: el PoC no traía una política publicada, solo el ancla `#privacidad` y `mailto:hola@generosity.global`.


Actualización 2026-11-01: la cabecera y el pie usan `assets/wordmark.svg`, copiado
del kit aprobado `../assets/brand/svg/wordmark.svg`, para mostrar la marca horizontal
sin reducir el texto dentro del emblema apilado. Los favicon se conservan.
Las tarjetas presentan despensas, útiles escolares y acompañamiento sin nombres
de centros, avances de recaudación ni cupos ficticios. Privacidad y términos tienen
páginas propias ES/EN, generadas de los documentos canónicos del backend.
