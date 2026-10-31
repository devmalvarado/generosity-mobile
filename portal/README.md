# Portal público de Generosity

Copia local de la primera etapa. Es un sitio estático: HTML, CSS y JavaScript. No hay framework de interfaz, ni backend, ni inicio de sesión.

La procedencia de la marca y de los ejemplos está en [PROVENANCE.md](PROVENANCE.md). La landing original no se modifica.

## Verla en el navegador

Desde esta carpeta:

```bash
python3 -m http.server 47321 --bind 127.0.0.1
```

Abrir `http://127.0.0.1:47321/`. Si ese puerto está ocupado, cambia el número y usa otro libre.

Para verla en `https://generosity.global` hay que subir solo `index.html`, `styles.css`, `portal.js` y `assets/` a un hosting de archivos y quitar el Website Builder. El constructor de GoDaddy no acepta esta carpeta. No subas `node_modules/`, pruebas ni capturas. No crees `/privacy` ni `/terms`: no hay documento legal.

Sin JavaScript la página sigue diciendo que no envía ni publica nada. Con JavaScript, los formularios se quedan en la página y conservan el texto.

## Comprobar

Las dependencias de prueba viven solo aquí.

```bash
npm install
npx playwright install chromium webkit
npm test
```

Eso abre Chromium y WebKit en 320, 360, 390, 768 y 1280 px CSS. No sustituye una revisión en Safari o Chrome de un teléfono, con teclado virtual, vertical y horizontal.

Las capturas quedan en `screenshots/`.
