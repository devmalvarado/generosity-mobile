const { test, expect } = require("@playwright/test");
const path = require("path");

const WIDTHS = [320, 360, 390, 768, 1280];
test.beforeEach(async ({ page }) => {
  await page.route("https://api.generosity.global/**", route => route.abort());
});

const BASE = "http://127.0.0.1:47321/";

async function overflow(page) {
  return page.evaluate(() => {
    const root = document.documentElement;
    return Math.max(root.scrollWidth, document.body.scrollWidth) - root.clientWidth;
  });
}

async function prepare(page, width) {
  await page.setViewportSize({ width: width, height: 800 });
  await page.goto("/");
  await page.locator("#idioma").selectOption("es");
}

test.describe("layout", () => {
  for (const width of WIDTHS) {
    test("no horizontal overflow at " + width, async ({ page }) => {
      await prepare(page, width);
      expect(await overflow(page)).toBeLessThanOrEqual(1);
      if (width < 1200) {
        await page.locator(".menu").click();
        await expect(page.locator("#movil")).toBeVisible();
        expect(await overflow(page)).toBeLessThanOrEqual(1);
        await page.keyboard.press("Escape");
      }
      await page.locator("#ciudad").fill("X".repeat(80));
      await page.locator("#buscador button").click();
      expect(await overflow(page)).toBeLessThanOrEqual(1);
    });
  }

  test("primary controls are at least 44px at 320", async ({ page }) => {
    await prepare(page, 320);
    for (const selector of [".menu", "#idioma", "[data-filter=todas]", "#buscador button"]) {
      const box = await page.locator(selector).boundingBox();
      expect(box, selector).not.toBeNull();
      expect(box.height, selector).toBeGreaterThanOrEqual(44);
      expect(box.width, selector).toBeGreaterThanOrEqual(44);
    }
  });
});

test.describe("menu", () => {
  test("closed menu is hidden and not focusable, and Escape restores focus", async ({ page }) => {
    await prepare(page, 390);
    const drawer = page.locator("#movil");
    await expect(drawer).toBeHidden();
    expect(await drawer.evaluate((el) => getComputedStyle(el).display)).toBe("none");

    await page.locator(".menu").focus();
    await page.keyboard.press("Tab");
    const inside = await page.evaluate(() => !!(document.activeElement && document.activeElement.closest("#movil")));
    expect(inside).toBe(false);

    await page.locator(".menu").click();
    await expect(page.locator(".menu")).toHaveAttribute("aria-expanded", "true");
    await expect(page.locator("#movil a").first()).toBeFocused();
    await expect(page.locator("#menu-estado")).toHaveText("Menú abierto");

    await page.keyboard.press("Escape");
    await expect(drawer).toBeHidden();
    await expect(page.locator(".menu")).toBeFocused();
    await expect(page.locator(".menu")).toHaveAttribute("aria-expanded", "false");
    await expect(page.locator("#menu-estado")).toHaveText("Menú cerrado");
    expect(await drawer.evaluate((el) => getComputedStyle(el).display)).toBe("none");
  });

  test("filtered cards stay out of view and out of the box", async ({ page }) => {
    await prepare(page, 390);
    await page.locator("[data-filter=salud]").click();
    await expect(page.locator("#comedor")).toBeHidden();
    await expect(page.locator("#puente")).toBeVisible();
    expect(await page.locator("#comedor").evaluate((el) => getComputedStyle(el).display)).toBe("none");
    await expect(page.locator(".empty")).toBeHidden();
  });
});

test.describe("forms and language", () => {
  test("disconnected forms keep values and do not claim success", async ({ page }) => {
    const dialogs = [];
    page.on("dialog", (dialog) => dialogs.push(dialog.message()));
    await prepare(page, 390);

    await page.locator("#cantidad").fill("5");
    await page.locator("#centro").selectOption("olivos");
    await page.locator("#formulario-donacion button[type=submit]").click();
    await expect(page.locator("#donacion-estado")).toHaveText(/No se envió nada/);
    await expect(page.locator("#donacion-estado")).not.toHaveText(/recibo|registramos|pago seguro|quedó registrado/i);
    await expect(page.locator("#cantidad")).toHaveValue("5");
    await expect(page.locator("#centro")).toHaveValue("olivos");

    await page.locator("input[value=dinero]").check();
    await page.locator("#formulario-donacion button[type=submit]").click();
    await expect(page.locator("#donacion-estado")).toContainText("no se hizo ningún cobro");
    await expect(page.locator("input[value=dinero]")).toBeChecked();

    await page.locator("input[value=bienes]").check();
    await page.locator("#cantidad").fill("0");
    await page.locator("#formulario-donacion button[type=submit]").click();
    await expect(page.locator("#cantidad")).toHaveAttribute("aria-invalid", "true");
    await expect(page.locator("#cantidad-error")).toContainText("1 o más");
    await expect(page.locator("#cantidad")).toHaveValue("0");
    await expect(page.locator("#cantidad")).toBeFocused();

    const payload = "<img src=x onerror=alert(1)>";
    await page.locator("#nombre-centro").fill("Centro Norte");
    await page.locator("#titulo").fill(payload);
    await page.locator("#detalle").fill("Para el invierno");
    await page.locator("#unidades").fill("7");
    await page.locator("#prioridad").selectOption("alta");
    await page.locator("#publicar button[type=submit]").click();
    await expect(page.locator("#publicar-estado")).toContainText("No se envió nada");
    await expect(page.locator("#publicar-estado")).toContainText("no permite publicar");
    await expect(page).toHaveURL(/#acceso\?destino=publicar$/);
    await expect(page.locator("#destino-previsto")).toContainText("publicar una necesidad");
    await expect(page.locator("#destino-previsto")).toContainText("no concede permiso");
    await expect(page.locator("#acceso")).toContainText("no concede permisos");
    await expect(page.locator("input[type=password]")).toHaveCount(0);
    await expect(page.locator("#titulo")).toHaveValue(payload);
    await expect(page.locator("#nombre-centro")).toHaveValue("Centro Norte");
    await expect(page.locator("#detalle")).toHaveValue("Para el invierno");
    await expect(page.locator("#unidades")).toHaveValue("7");
    await expect(page.locator("#prioridad")).toHaveValue("alta");
    expect(await page.locator("#publicar-estado").innerHTML()).not.toContain("<img");

    await page.locator("#ciudad").fill(payload);
    await page.locator("#buscador button").click();
    const searchHtml = await page.locator("#busqueda-estado").innerHTML();
    expect(searchHtml).not.toContain("<img");
    expect(searchHtml).toContain("&lt;img");
    await expect(page.locator("#ciudad")).toHaveValue(payload);
    expect(dialogs).toEqual([]);
  });

  test("language change keeps fields, filters and can return to automatic", async ({ page }) => {
    await prepare(page, 390);
    await page.locator("#ciudad").fill("Puebla");
    await page.locator("#cantidad").fill("3");
    await page.locator("#titulo").fill("Mantas");
    await page.locator("[data-filter=salud]").click();

    await page.locator("#idioma").selectOption("en");
    await expect(page.locator("html")).toHaveAttribute("lang", "en");
    await expect(page.locator("h1")).toHaveText("Help arrives when you know exactly who needs it.");
    await expect(page.locator("#ciudad")).toHaveValue("Puebla");
    await expect(page.locator("#cantidad")).toHaveValue("3");
    await expect(page.locator("#titulo")).toHaveValue("Mantas");
    await expect(page.locator("#puente")).toBeVisible();
    await expect(page.locator("#comedor")).toBeHidden();
    await expect(page.locator("[data-filter=salud]")).toHaveAttribute("aria-pressed", "true");

    await page.locator("#idioma").selectOption("es");
    await expect(page.locator("html")).toHaveAttribute("lang", "es");
    await expect(page.locator("#ciudad")).toHaveValue("Puebla");
    await expect(page.locator("#titulo")).toHaveValue("Mantas");
    await expect(page.locator("[data-filter=salud]")).toHaveAttribute("aria-pressed", "true");
  });
});

test.describe("locale resolution", () => {
  test("regional Spanish and English match, other languages fall back to English", async ({ browser }) => {
    const spanish = await browser.newContext({ locale: "es-MX" });
    const spanishPage = await spanish.newPage();
    await spanishPage.goto(BASE);
    await expect(spanishPage.locator("html")).toHaveAttribute("lang", "es");
    await expect(spanishPage.locator("#idioma")).toHaveValue("system");
    await spanish.close();

    const english = await browser.newContext({ locale: "en-GB" });
    const englishPage = await english.newPage();
    await englishPage.goto(BASE);
    await expect(englishPage.locator("html")).toHaveAttribute("lang", "en");
    await english.close();

    const french = await browser.newContext({ locale: "fr-FR" });
    const frenchPage = await french.newPage();
    await frenchPage.goto(BASE);
    await expect(frenchPage.locator("html")).toHaveAttribute("lang", "en");
    await french.close();
  });

  test("uses the first matching language and the current preference when returning to automatic", async ({ browser }) => {
    const context = await browser.newContext();
    await context.addInitScript(() => {
      const langs = ["fr-CA", "es-AR", "en-US"];
      Object.defineProperty(navigator, "languages", { configurable: true, get: () => langs });
      Object.defineProperty(navigator, "language", { configurable: true, get: () => langs[0] });
    });
    await context.route("https://api.generosity.global/**", route => route.fulfill({ status: 503, body: "unavailable" }));
    const page = await context.newPage();
    await page.goto(BASE);
    await expect(page.locator("html")).toHaveAttribute("lang", "es");
    await expect(page.locator("#idioma")).toHaveValue("system");

    await page.locator("#ciudad").fill("Puebla");
    await page.locator("#idioma").selectOption("en");
    await page.reload();
    await expect(page.locator("html")).toHaveAttribute("lang", "en");
    await expect(page.locator("#idioma")).toHaveValue("en");
    await page.locator("#ciudad").fill("Puebla");

    await page.evaluate(() => {
      Object.defineProperty(navigator, "languages", { configurable: true, get: () => ["en-GB", "fr"] });
      Object.defineProperty(navigator, "language", { configurable: true, get: () => "en-GB" });
    });
    await page.locator("#idioma").selectOption("system");
    await expect(page.locator("html")).toHaveAttribute("lang", "en");
    await expect(page.locator("#ciudad")).toHaveValue("Puebla");
    await context.close();
  });

  test("still works when storage rejects the language choice", async ({ browser }) => {
    const context = await browser.newContext({ locale: "es-MX" });
    await context.addInitScript(() => {
      const getItem = Storage.prototype.getItem;
      const setItem = Storage.prototype.setItem;
      Storage.prototype.getItem = function (key) {
        if (key === "generosity.locale") throw new Error("blocked");
        return getItem.call(this, key);
      };
      Storage.prototype.setItem = function (key, value) {
        if (key === "generosity.locale") throw new Error("blocked");
        return setItem.call(this, key, value);
      };
    });
    await context.route("https://api.generosity.global/**", route => route.fulfill({ status: 503, body: "unavailable" }));
    const page = await context.newPage();
    const errors = [];
    page.on("pageerror", (error) => errors.push(String(error)));
    await page.goto(BASE);
    await expect(page.locator("html")).toHaveAttribute("lang", "es");
    await page.locator("#titulo").fill("Mantas");
    await page.locator("#idioma").selectOption("en");
    await expect(page.locator("html")).toHaveAttribute("lang", "en");
    await expect(page.locator("#titulo")).toHaveValue("Mantas");
    await page.reload();
    await expect(page.locator("html")).toHaveAttribute("lang", "es");
    await expect(page.locator("#idioma")).toHaveValue("system");
    expect(errors).toEqual([]);
    await context.close();
  });
});

test.describe("keyboard and text scaling", () => {
  test("skip link, menu and filters work from the keyboard", async ({ page, browserName }) => {
    await prepare(page, 390);
    if (browserName === "webkit") {
      // This WebKit build matches Safari with full keyboard access off: Tab visits
      // text fields and selects, not links or buttons. Activation still uses the keyboard.
      await page.locator("#idioma").focus();
      await page.keyboard.press("Tab");
      await expect(page.locator("#ciudad")).toBeFocused();
      await page.locator(".skip").focus();
    } else {
      await page.locator("body").press("Tab");
    }
    await expect(page.locator(".skip")).toBeFocused();
    await page.keyboard.press("Enter");
    await expect(page.locator("#contenido")).toBeFocused();

    await page.locator(".menu").focus();
    await page.keyboard.press("Enter");
    await expect(page.locator("#movil")).toBeVisible();
    await expect(page.locator(".menu")).toHaveAttribute("aria-expanded", "true");
    await page.keyboard.press("Escape");
    await expect(page.locator(".menu")).toBeFocused();
    await expect(page.locator("#movil")).toBeHidden();

    await page.locator("[data-filter=infancia]").focus();
    await page.keyboard.press("Enter");
    await expect(page.locator("[data-filter=infancia]")).toHaveAttribute("aria-pressed", "true");
    await expect(page.locator("#hogar")).toBeVisible();
    await expect(page.locator("#comedor")).toBeHidden();
  });

  test("200 percent text does not overflow or hide the submit action", async ({ page }) => {
    await prepare(page, 320);
    await page.addStyleTag({ content: "html { font-size: 200% !important; }" });
    expect(await overflow(page)).toBeLessThanOrEqual(1);
    const submit = page.locator("#formulario-donacion button[type=submit]");
    await submit.scrollIntoViewIfNeeded();
    await expect(submit).toBeVisible();
    const box = await page.locator(".menu").boundingBox();
    expect(box.width).toBeGreaterThanOrEqual(44);
    expect(box.height).toBeGreaterThanOrEqual(44);

    await page.setViewportSize({ width: 1280, height: 800 });
    expect(await overflow(page)).toBeLessThanOrEqual(1);
  });
});

test("representative screenshots in Spanish and English", async ({ page, browserName }) => {
  test.skip(browserName !== "chromium", "one set of screenshots is enough");
  const dir = path.join(__dirname, "..", "screenshots");
  for (const width of [390, 1280]) {
    await page.setViewportSize({ width: width, height: 800 });
    await page.goto("/");
    await page.locator("#idioma").selectOption("es");
    await page.screenshot({ path: path.join(dir, "es-" + width + ".png"), fullPage: true });
    await page.locator("#idioma").selectOption("en");
    await page.screenshot({ path: path.join(dir, "en-" + width + ".png"), fullPage: true });
    expect(await overflow(page)).toBeLessThanOrEqual(1);
  }
});
