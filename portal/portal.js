(function () {
  var STORAGE_KEY = "generosity.locale";
  var mode = "system";
  var menuAnnounced = false;
  var searchNoted = false;
  var donateStatus = "";
  var publishNoted = false;

  var copy = {
    es: {
      "meta.title": "Generosity — Encuentra dónde ayudar, cerca de ti",
      "meta.description": "Generosity quiere conectar a las personas con centros de bondad. Esta página es una demostración: no envía donaciones ni publica necesidades.",
      "skip": "Saltar al contenido",
      "nav.sections": "Secciones",
      "nav.how": "Cómo funciona",
      "nav.needs": "Necesidades",
      "nav.centers": "Para centros",
      "nav.donate": "Donar",
      "nav.publish": "Publicar una necesidad",
      "lang.label": "Idioma",
      "lang.system": "Automático",
      "menu.label": "Menú",
      "menu.close": "Cerrar menú",
      "menu.open": "Menú abierto",
      "menu.closed": "Menú cerrado",
      "hero.where": "Demostración · sin catálogo conectado",
      "hero.title": "La ayuda llega cuando sabes exactamente quién la necesita.",
      "hero.lead": "Generosity quiere mostrar centros de bondad, lo que les falta y una forma concreta de cubrirlo: unas horas, unos paquetes, una aportación. Esta página todavía no consulta un catálogo en vivo.",
      "finder.label": "Busca por ciudad",
      "finder.placeholder": "Ciudad de México",
      "finder.submit": "Ver ejemplos",
      "map.title": "Ilustración decorativa de calles y pines. No es un mapa en vivo ni muestra tu ubicación.",
      "map.pinFood": "Ir al ejemplo del comedor",
      "map.pinKids": "Ir al ejemplo de infancia",
      "map.pinHealth": "Ir al ejemplo de salud",
      "map.you": "Referencia",
      "map.food": "Comedor",
      "map.kids": "Infancia",
      "map.health": "Salud",
      "how.title": "Cuatro pasos, y el último te trae de vuelta.",
      "how.discover.title": "Descubres",
      "how.discover.body": "Un mapa con centros de la zona, filtrados por causa: infancia, adultos mayores, salud, comedores, educación. Hoy el dibujo de arriba es solo una ilustración.",
      "how.understand.title": "Entiendes",
      "how.understand.body": "Cada centro, cuando el catálogo exista, mostrará quién es, cuándo abre, cómo contactarlo y qué está pidiendo.",
      "how.help.title": "Ayudas",
      "how.help.body": "La idea es comprometerse con algo concreto. Esta página no registra ese compromiso ni actualiza cantidades.",
      "how.return.title": "Vuelves",
      "how.return.body": "El historial sería tuyo. Sin puntos que canjear y sin ranking que comparar con nadie. Aquí todavía no hay historial.",
      "needs.banner": "Contenido de demostración. No es un catálogo en vivo, no son centros reales y no hay métricas de impacto.",
      "needs.title": "Ejemplos de lo que un centro podría pedir.",
      "needs.filters": "Filtrar ejemplos por causa",
      "needs.empty": "No hay ejemplos de demostración para ese filtro.",
      "filter.all": "Todas",
      "filter.food": "Comedores",
      "filter.kids": "Infancia",
      "filter.health": "Salud",
      "demo.card": "Ejemplo de demostración",
      "card.urgent": "Urgente",
      "card.food.cause": "Comedor comunitario",
      "card.food.title": "20 despensas para esta semana",
      "card.food.body": "Texto de ejemplo: arroz, frijol, aceite y lentejas. No describe un comedor que esté operando.",
      "card.food.where": "Comedor Los Olivos · nombre de ejemplo",
      "card.food.count": "Cifra de ejemplo: 5 de 20. No es un avance real.",
      "card.food.action": "Quiero cubrir una parte",
      "card.kids.cause": "Infancia y educación",
      "card.kids.title": "Útiles escolares para el nuevo ciclo",
      "card.kids.body": "Texto de ejemplo: cuadernos, lápices y mochilas. No corresponde a un grupo real de niños.",
      "card.kids.where": "Hogar Futuros Brillantes · nombre de ejemplo",
      "card.kids.count": "Cifra de ejemplo: 24 de 40. No es un avance real.",
      "card.kids.action": "Ver cómo ayudar con bienes",
      "card.health.cause": "Salud",
      "card.health.title": "Turnos de dos horas, entre semana",
      "card.health.body": "Texto de ejemplo: acompañar a una cita. No hay turnos abiertos en esta página.",
      "card.health.where": "Puente de Salud Abierta · nombre de ejemplo",
      "card.health.count": "Cifra de ejemplo: 6 turnos. No es una agenda real.",
      "card.health.action": "Ver cómo ayudar con tiempo",
      "donate.title": "Ayuda con lo que tienes, no con lo que te sobre.",
      "donate.lead": "Tres formas previstas de ayudar. Esta página no registra una oferta, no cobra y no pide tarjeta ni datos bancarios.",
      "donate.time.title": "Tiempo",
      "donate.time.body": "Elegir una actividad, un día y unas horas. Aquí no hay confirmación del centro ni historial.",
      "donate.goods.title": "Bienes",
      "donate.goods.body": "Comprometerse con una parte de lo pedido. Esta página no actualiza lo que falta.",
      "donate.money.title": "Dinero",
      "donate.money.body": "No disponible. No hay proveedor de pagos conectado y no se aceptan aportaciones desde este sitio.",
      "donate.legend": "Cómo quieres ayudar",
      "donate.time.choice": "Donar tiempo",
      "donate.goods.choice": "Donar bienes",
      "donate.money.choice": "Donar dinero",
      "donate.center": "Centro de demostración",
      "donate.amount": "Cantidad",
      "donate.submit": "Enviar intención",
      "center.olivos": "Comedor Los Olivos",
      "center.hogar": "Hogar Futuros Brillantes",
      "center.puente": "Puente de Salud Abierta",
      "centers.title": "¿Administras un centro? Publicar todavía no está conectado.",
      "centers.lead": "Una cuenta de centro será distinta a la de una persona. Hoy no hay proveedor de autenticación, así que nadie inicia sesión ni recibe permiso para publicar.",
      "centers.item1": "El perfil, los horarios y el contacto quedan para cuando exista un espacio de personal.",
      "centers.item2": "Una necesidad publicada tendrá cantidad, prioridad y cierre. Esta página no la guarda.",
      "centers.item3": "Confirmar lo que otras personas traigan también depende de ese espacio, que no está en esta etapa.",
      "centers.item4": "La verificación oficial y los pagos no forman parte de esta página.",
      "centers.access": "Ir al acceso de centros",
      "publish.title": "Borrador local, no una publicación",
      "publish.intro": "Puedes escribir aquí para no perder la idea. Enviar no publica, no entra a una revisión y no demuestra que administras el centro.",
      "publish.center": "Nombre del centro",
      "publish.centerPlaceholder": "Comedor Los Olivos",
      "publish.need": "Qué necesitas",
      "publish.needPlaceholder": "Despensas para esta semana",
      "publish.qty": "Cantidad",
      "publish.priority": "Prioridad",
      "publish.detail": "Detalle",
      "publish.detailPlaceholder": "Para quién es y cuándo lo necesitas",
      "publish.submit": "Continuar al acceso de centros",
      "priority.normal": "Normal",
      "priority.medium": "Media",
      "priority.high": "Alta",
      "priority.urgent": "Urgente",
      "access.flag": "Conexión pendiente",
      "access.title": "Acceso de centros",
      "access.lead": "La autenticación todavía no está conectada. No hay un proveedor elegido. Esta pantalla no inicia sesión, no asigna permisos y no publica nada.",
      "access.keep": "Si escribiste una necesidad en esta página, el texto sigue en el formulario. No se envió a ningún servidor.",
      "access.back": "Volver al borrador local",
      "access.dest.none": "No hay un destino previsto guardado.",
      "access.dest.publicar": "Destino previsto: publicar una necesidad. Quedará como referencia cuando exista un inicio de sesión real. Ahora no concede permiso para publicar.",
      "access.dest.centros": "Destino previsto: el espacio de centros. Ahora no concede acceso.",
      "privacy.title": "Lo que esta página no hace.",
      "privacy.rank.title": "No rankea a las personas.",
      "privacy.rank.body": "No hay tabla de los más generosos ni puntos que se canjeen. Tampoco hay un historial en este sitio.",
      "privacy.card.title": "No pide ni guarda una tarjeta.",
      "privacy.card.body": "No hay pago en esta página. No escribas datos de tarjeta ni bancarios: no hay dónde guardarlos y no se deben enviar.",
      "privacy.location.title": "No pide tu ubicación.",
      "privacy.location.body": "El dibujo no es un mapa y esta página no solicita permiso de ubicación ni alertas de proximidad.",
      "privacy.note": "Esto no es una política de privacidad. No hay un documento legal publicado para enlazar.",
      "footer.tag": "generosity conecta, cuando el servicio exista, a las personas con centros de bondad. Esta página no publica ni cobra.",
      "footer.nav": "Pie",
      "footer.privacy": "Privacidad",
      "footer.contact": "Contacto",
      "search.empty": "No hay un catálogo conectado. Las tarjetas de abajo son ejemplos de demostración, no resultados de una búsqueda.",
      "search.named": "No hay un catálogo conectado. Las tarjetas de abajo son ejemplos de demostración, no resultados para «",
      "search.namedEnd": "».",
      "hint.time": "Puedes anotar horas aquí. Esta página no reserva un turno ni avisa a un centro.",
      "hint.goods": "Cantidad anotada: ",
      "hint.goodsEnd": ". Es un ejemplo de formulario; no queda un compromiso ni cambia lo que falta.",
      "hint.money": "El dinero no está disponible en este sitio. No se cobra nada y no hace falta una tarjeta.",
      "error.amount": "Escribe un número entero de 1 o más.",
      "donate.invalid": "No se envió nada. Revisa la cantidad: hace falta un número de 1 o más.",
      "donate.notSent": "No se envió nada y no se publicó nada. No hay un servicio conectado. El formulario conserva lo que escribiste.",
      "donate.money": "No se envió nada y no se hizo ningún cobro. Las aportaciones de dinero no están disponibles en esta página.",
      "publish.notSent": "No se envió nada y no se publicó nada. La autenticación no está conectada, así que no hay sesión ni revisión."
    },
    en: {
      "meta.title": "Generosity — Find where to help, near you",
      "meta.description": "Generosity aims to connect people with kindness centers. This page is a demonstration: it does not send donations or publish needs.",
      "skip": "Skip to content",
      "nav.sections": "Sections",
      "nav.how": "How it works",
      "nav.needs": "Needs",
      "nav.centers": "For centers",
      "nav.donate": "Donate",
      "nav.publish": "Publish a need",
      "lang.label": "Language",
      "lang.system": "Automatic",
      "menu.label": "Menu",
      "menu.close": "Close menu",
      "menu.open": "Menu open",
      "menu.closed": "Menu closed",
      "hero.where": "Demonstration · no catalog connected",
      "hero.title": "Help arrives when you know exactly who needs it.",
      "hero.lead": "Generosity wants to show kindness centers, what they are short of, and a concrete way to cover it: a few hours, a few packages, a contribution. This page does not query a live catalog yet.",
      "finder.label": "Search by city",
      "finder.placeholder": "Mexico City",
      "finder.submit": "See examples",
      "map.title": "Decorative illustration of streets and pins. Not a live map, and it does not show your location.",
      "map.pinFood": "Go to the meal-center example",
      "map.pinKids": "Go to the childhood example",
      "map.pinHealth": "Go to the health example",
      "map.you": "Reference",
      "map.food": "Meals",
      "map.kids": "Childhood",
      "map.health": "Health",
      "how.title": "Four steps, and the last one brings you back.",
      "how.discover.title": "You find",
      "how.discover.body": "A map of nearby centers, filtered by cause: childhood, older adults, health, meal centers, education. The drawing above is only an illustration today.",
      "how.understand.title": "You understand",
      "how.understand.body": "When a catalog exists, each center will show who they are, when they are open, how to reach them, and what they are asking for.",
      "how.help.title": "You help",
      "how.help.body": "The idea is to commit to something concrete. This page does not record that commitment or update quantities.",
      "how.return.title": "You return",
      "how.return.body": "The history would be yours. No points to redeem and no ranking to compare with anyone. There is no history here yet.",
      "needs.banner": "Demonstration content. Not a live catalog, not real centers, and not impact metrics.",
      "needs.title": "Examples of what a center might ask for.",
      "needs.filters": "Filter examples by cause",
      "needs.empty": "There are no demonstration examples for that filter.",
      "filter.all": "All",
      "filter.food": "Meal centers",
      "filter.kids": "Childhood",
      "filter.health": "Health",
      "demo.card": "Demonstration example",
      "card.urgent": "Urgent",
      "card.food.cause": "Community meal center",
      "card.food.title": "20 grocery parcels for this week",
      "card.food.body": "Sample text: rice, beans, oil, and lentils. It does not describe a meal center that is operating.",
      "card.food.where": "Comedor Los Olivos · sample name",
      "card.food.count": "Sample figure: 5 of 20. Not real progress.",
      "card.food.action": "I want to cover part of it",
      "card.kids.cause": "Childhood and education",
      "card.kids.title": "School supplies for the new term",
      "card.kids.body": "Sample text: notebooks, pencils, and backpacks. It does not refer to a real group of children.",
      "card.kids.where": "Hogar Futuros Brillantes · sample name",
      "card.kids.count": "Sample figure: 24 of 40. Not real progress.",
      "card.kids.action": "See how to help with goods",
      "card.health.cause": "Health",
      "card.health.title": "Two-hour shifts, on weekdays",
      "card.health.body": "Sample text: going with someone to an appointment. There are no open shifts on this page.",
      "card.health.where": "Puente de Salud Abierta · sample name",
      "card.health.count": "Sample figure: 6 shifts. Not a real schedule.",
      "card.health.action": "See how to help with time",
      "donate.title": "Help with what you have, not with what you can spare.",
      "donate.lead": "Three planned ways to help. This page does not record an offer, does not charge anyone, and does not ask for a card or bank details.",
      "donate.time.title": "Time",
      "donate.time.body": "Choose an activity, a day, and some hours. There is no center confirmation and no history here.",
      "donate.goods.title": "Goods",
      "donate.goods.body": "Commit to part of what was asked. This page does not update what is still missing.",
      "donate.money.title": "Money",
      "donate.money.body": "Unavailable. No payment provider is connected, and this site does not accept contributions.",
      "donate.legend": "How you want to help",
      "donate.time.choice": "Give time",
      "donate.goods.choice": "Give goods",
      "donate.money.choice": "Give money",
      "donate.center": "Demonstration center",
      "donate.amount": "Quantity",
      "donate.submit": "Send intention",
      "center.olivos": "Comedor Los Olivos",
      "center.hogar": "Hogar Futuros Brillantes",
      "center.puente": "Puente de Salud Abierta",
      "centers.title": "Do you run a center? Publishing is not connected yet.",
      "centers.lead": "A center account will be different from a personal one. There is no authentication provider today, so nobody signs in and nobody receives permission to publish.",
      "centers.item1": "Profile, hours, and contact wait until a staff space exists.",
      "centers.item2": "A published need will have a quantity, a priority, and a closing time. This page does not store it.",
      "centers.item3": "Confirming what other people bring also depends on that space, which is not part of this stage.",
      "centers.item4": "Official verification and payments are not part of this page.",
      "centers.access": "Go to center access",
      "publish.title": "A local draft, not a publication",
      "publish.intro": "You can write here so the idea is not lost. Sending does not publish, does not enter a review, and does not prove you run the center.",
      "publish.center": "Center name",
      "publish.centerPlaceholder": "Comedor Los Olivos",
      "publish.need": "What you need",
      "publish.needPlaceholder": "Grocery parcels for this week",
      "publish.qty": "Quantity",
      "publish.priority": "Priority",
      "publish.detail": "Detail",
      "publish.detailPlaceholder": "Who it is for and when you need it",
      "publish.submit": "Continue to center access",
      "priority.normal": "Normal",
      "priority.medium": "Medium",
      "priority.high": "High",
      "priority.urgent": "Urgent",
      "access.flag": "Connection pending",
      "access.title": "Center access",
      "access.lead": "Authentication is not connected yet. No provider has been chosen. This screen does not sign anyone in, does not assign permissions, and does not publish anything.",
      "access.keep": "If you wrote a need on this page, the text is still in the form. It was not sent to any server.",
      "access.back": "Back to the local draft",
      "access.dest.none": "No intended destination is saved.",
      "access.dest.publicar": "Intended destination: publish a need. It will be kept as a reference when a real sign-in exists. It does not grant permission to publish now.",
      "access.dest.centros": "Intended destination: the center space. It does not grant access now.",
      "privacy.title": "What this page does not do.",
      "privacy.rank.title": "It does not rank people.",
      "privacy.rank.body": "There is no table of the most generous and no points to redeem. There is also no history on this site.",
      "privacy.card.title": "It does not ask for or store a card.",
      "privacy.card.body": "There is no payment on this page. Do not type card or bank details: there is nowhere to store them, and they should not be sent.",
      "privacy.location.title": "It does not ask for your location.",
      "privacy.location.body": "The drawing is not a map, and this page does not request location permission or proximity alerts.",
      "privacy.note": "This is not a privacy policy. There is no published legal document to link to.",
      "footer.tag": "generosity will connect people with kindness centers when the service exists. This page does not publish or charge.",
      "footer.nav": "Footer",
      "footer.privacy": "Privacy",
      "footer.contact": "Contact",
      "search.empty": "No catalog is connected. The cards below are demonstration examples, not search results.",
      "search.named": "No catalog is connected. The cards below are demonstration examples, not results for “",
      "search.namedEnd": "”.",
      "hint.time": "You can note hours here. This page does not book a shift or notify a center.",
      "hint.goods": "Noted quantity: ",
      "hint.goodsEnd": ". This is a sample form; no commitment is recorded and nothing still needed changes.",
      "hint.money": "Money is not available on this site. Nothing is charged and no card is required.",
      "error.amount": "Enter a whole number of 1 or more.",
      "donate.invalid": "Nothing was sent. Check the quantity: a number of 1 or more is required.",
      "donate.notSent": "Nothing was sent and nothing was published. No service is connected. The form keeps what you entered.",
      "donate.money": "Nothing was sent and nothing was charged. Money contributions are not available on this page.",
      "publish.notSent": "Nothing was sent and nothing was published. Authentication is not connected, so there is no session and no review."
    }
  };

  function readStored() {
    try {
      return window.localStorage.getItem(STORAGE_KEY);
    } catch (error) {
      return null;
    }
  }

  function writeStored(value) {
    try {
      window.localStorage.setItem(STORAGE_KEY, value);
    } catch (error) {
      /* Storage can be blocked. The choice still applies for this view. */
    }
  }

  function browserLanguages() {
    var list = navigator.languages;
    if (list && list.length) return list;
    if (navigator.language) return [navigator.language];
    return ["en"];
  }

  function matchLanguage(list) {
    var i, tag, primary;
    for (i = 0; i < list.length; i += 1) {
      tag = String(list[i] || "").toLowerCase().replace(/_/g, "-");
      primary = tag.split("-")[0];
      if (primary === "es") return "es";
      if (primary === "en") return "en";
    }
    return "en";
  }

  function effective() {
    return mode === "es" || mode === "en" ? mode : matchLanguage(browserLanguages());
  }

  function t(key) {
    var lang = effective();
    var table = copy[lang] || copy.en;
    if (Object.prototype.hasOwnProperty.call(table, key)) return table[key];
    return copy.en[key] || "";
  }

  function each(selector, fn) {
    Array.prototype.forEach.call(document.querySelectorAll(selector), fn);
  }

  function applyStatic() {
    var meta = document.querySelector('meta[name="description"]');
    document.documentElement.lang = effective();
    document.title = t("meta.title");
    if (meta) meta.setAttribute("content", t("meta.description"));
    each("[data-i18n]", function (el) {
      el.textContent = t(el.getAttribute("data-i18n"));
    });
    each("[data-i18n-placeholder]", function (el) {
      el.setAttribute("placeholder", t(el.getAttribute("data-i18n-placeholder")));
    });
    each("[data-i18n-aria]", function (el) {
      el.setAttribute("aria-label", t(el.getAttribute("data-i18n-aria")));
    });
    var select = document.getElementById("idioma");
    if (select && select.value !== mode) select.value = mode;
  }

  var menu = document.querySelector(".menu");
  var drawer = document.getElementById("movil");
  var menuStatus = document.getElementById("menu-estado");
  var wide = window.matchMedia("(min-width: 75rem)");

  function focusable(root) {
    return Array.prototype.filter.call(
      root.querySelectorAll("a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled])"),
      function (el) {
        return !el.closest("[hidden]");
      }
    );
  }

  function setMenu(open, restoreFocus) {
    menuAnnounced = true;
    menu.setAttribute("aria-expanded", open ? "true" : "false");
    drawer.hidden = !open;
    menuStatus.textContent = open ? t("menu.open") : t("menu.closed");
    if (open) {
      var items = focusable(drawer);
      if (items[0]) items[0].focus();
    } else if (restoreFocus) {
      menu.focus();
    }
  }

  menu.addEventListener("click", function () {
    setMenu(menu.getAttribute("aria-expanded") !== "true", true);
  });

  document.querySelector(".close-menu").addEventListener("click", function () {
    setMenu(false, true);
  });

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && !drawer.hidden) {
      event.preventDefault();
      setMenu(false, true);
    }
  });

  drawer.addEventListener("keydown", function (event) {
    if (event.key !== "Tab" || drawer.hidden) return;
    var items = focusable(drawer);
    if (!items.length) return;
    var first = items[0];
    var last = items[items.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  });

  drawer.addEventListener("click", function (event) {
    var link = event.target.closest("a");
    if (!link) return;
    setMenu(false, false);
    var id = (link.getAttribute("href") || "").split("?")[0].replace("#", "");
    var target = id && document.getElementById(id);
    if (target) target.focus();
  });

  function closeIfWide() {
    if (wide.matches && !drawer.hidden) setMenu(false, false);
  }
  if (wide.addEventListener) wide.addEventListener("change", closeIfWide);
  else if (wide.addListener) wide.addListener(closeIfWide);

  var board = document.querySelector(".board");
  var empty = document.querySelector(".empty");
  document.querySelector(".filters").addEventListener("click", function (event) {
    var button = event.target.closest("button");
    if (!button) return;
    each(".filters button", function (item) {
      var on = item === button;
      item.classList.toggle("on", on);
      item.setAttribute("aria-pressed", on ? "true" : "false");
    });
    var cause = button.getAttribute("data-filter");
    var visible = 0;
    each(".need", function (card) {
      var show = cause === "todas" || card.getAttribute("data-cause") === cause;
      card.hidden = !show;
      if (show) visible += 1;
    });
    empty.hidden = visible !== 0;
  });

  var donateForm = document.getElementById("formulario-donacion");
  var amount = document.getElementById("cantidad");
  var hint = document.getElementById("aviso");
  var amountError = document.getElementById("cantidad-error");
  var donateResult = document.getElementById("donacion-estado");

  function selectedWay() {
    var checked = donateForm.querySelector("input[name=forma]:checked");
    return checked ? checked.value : "bienes";
  }

  function amountValue() {
    var number = Number(amount.value);
    return amount.value !== "" && isFinite(number) ? number : null;
  }

  function renderHint() {
    var way = selectedWay();
    if (way === "dinero") {
      hint.textContent = t("hint.money");
      return;
    }
    if (way === "tiempo") {
      hint.textContent = t("hint.time");
      return;
    }
    hint.textContent = t("hint.goods") + String(amount.value) + t("hint.goodsEnd");
  }

  function renderDonate() {
    renderHint();
    if (donateStatus === "invalid") {
      amountError.textContent = t("error.amount");
      amount.setAttribute("aria-invalid", "true");
      amount.setAttribute("aria-describedby", "aviso cantidad-error");
      donateResult.textContent = t("donate.invalid");
      return;
    }
    amountError.textContent = "";
    amount.removeAttribute("aria-invalid");
    amount.setAttribute("aria-describedby", "aviso");
    if (donateStatus === "money") donateResult.textContent = t("donate.money");
    else if (donateStatus === "not-sent") donateResult.textContent = t("donate.notSent");
    else donateResult.textContent = "";
  }

  donateForm.addEventListener("change", renderHint);
  amount.addEventListener("input", renderHint);
  donateForm.addEventListener("submit", function (event) {
    event.preventDefault();
    var way = selectedWay();
    var value = amountValue();
    if (way !== "dinero" && (value === null || value < 1)) {
      donateStatus = "invalid";
      renderDonate();
      amount.focus();
      return;
    }
    donateStatus = way === "dinero" ? "money" : "not-sent";
    renderDonate();
  });

  var searchForm = document.getElementById("buscador");
  var city = document.getElementById("ciudad");
  var searchStatus = document.getElementById("busqueda-estado");

  function renderSearch() {
    if (!searchNoted) {
      searchStatus.textContent = "";
      return;
    }
    var query = city.value;
    searchStatus.textContent = query
      ? t("search.named") + query + t("search.namedEnd")
      : t("search.empty");
  }

  searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    searchNoted = true;
    renderSearch();
  });

  var publishForm = document.getElementById("publicar");
  var publishResult = document.getElementById("publicar-estado");
  var destination = document.getElementById("destino-previsto");

  function readDestination() {
    var hash = location.hash || "";
    if (hash.indexOf("#acceso") !== 0) return "";
    var query = hash.split("?")[1] || "";
    var dest = "";
    if (typeof URLSearchParams === "function") {
      dest = new URLSearchParams(query).get("destino") || "";
    }
    return dest === "publicar" || dest === "centros" ? dest : "";
  }

  function renderDestination() {
    var dest = readDestination();
    var key = dest ? "access.dest." + dest : "access.dest.none";
    destination.textContent = t(key);
  }

  function renderPublish() {
    publishResult.textContent = publishNoted ? t("publish.notSent") : "";
  }

  function openAccess() {
    var next = "#acceso?destino=publicar";
    if (location.hash === next) {
      renderDestination();
      document.getElementById("acceso").focus();
    } else {
      location.hash = next;
    }
  }

  publishForm.addEventListener("submit", function (event) {
    event.preventDefault();
    publishNoted = true;
    renderPublish();
    openAccess();
  });

  window.addEventListener("hashchange", function () {
    renderDestination();
    if ((location.hash || "").indexOf("#acceso") === 0) {
      document.getElementById("acceso").focus();
    }
  });

  var language = document.getElementById("idioma");
  language.addEventListener("change", function () {
    var next = language.value;
    mode = next === "es" || next === "en" ? next : "system";
    writeStored(mode);
    applyAll();
  });

  function applyAll() {
    applyStatic();
    renderHint();
    renderDonate();
    renderSearch();
    renderPublish();
    renderDestination();
    if (menuAnnounced) menuStatus.textContent = drawer.hidden ? t("menu.closed") : t("menu.open");
  }

  var stored = readStored();
  if (stored === "es" || stored === "en" || stored === "system") mode = stored;
  applyAll();
})();
