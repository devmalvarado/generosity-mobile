(function () {
  var STORAGE_KEY = "generosity.locale";
  var mode = "system";
  var menuAnnounced = false;
  var searchNoted = false;
  var donateStatus = "";
  var publishNoted = false;

  var copy = {
    es: {
      "auth.signIn": "Iniciar sesión o vincular cuenta ↗",
      "auth.signOut": "Cerrar sesión",
      "auth.retry": "Comprobar conexión",
      "auth.loading": "Comprobando tu sesión…",
      "auth.guest": "Estás navegando como invitado.",
      "auth.failed": "No se pudo conectar con el servicio de cuentas. Puedes seguir navegando como invitado y reintentar.",
      "auth.signedIn": "Sesión iniciada: ",
      "auth.logoutFailed": "No se pudo cerrar la sesión. Conservamos tu sesión; vuelve a intentarlo.",

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
      "centers.title": "¿Administras un centro? Accede al portal de centros.",
      "centers.lead": "La misma cuenta Generosity puede tener permisos de un centro. Los concede personal autorizado; iniciar sesión no permite publicar por sí solo.",
      "centers.item1": "El perfil, los horarios y el contacto público no están en este portal de centros.",
      "centers.item2": "Una necesidad publicada tendrá cantidad, prioridad y cierre. El formulario de ejemplo no la guarda; el portal de centros sí, solo con un centro asignado.",
      "centers.item3": "Confirmar lo que otras personas traigan está en el portal de centros, no en este formulario, y solo para un centro asignado.",
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
      "access.flag": "Cuenta Generosity",
      "access.title": "Acceso de centros",
      "access.lead": "Usa tu cuenta Generosity en web, Android y iOS con Google, Apple o correo. El acceso se abre en otra pestaña para conservar tu borrador. Iniciar sesión no concede permisos de un centro ni publica necesidades.",
      "access.keep": "Si escribiste una necesidad en esta página, el texto sigue en el formulario. No se envió a ningún servidor.",
      "access.back": "Volver al borrador local",
      "access.dest.none": "No hay un destino previsto guardado.",
      "access.dest.publicar": "Destino previsto: publicar una necesidad. El inicio de sesión no concede permiso para publicar.",
      "access.dest.centros": "Destino previsto: el espacio de centros. Iniciar sesión no asigna un centro ni permite publicar.",
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
      "publish.notSent": "No se envió nada y no se publicó nada. Este formulario de ejemplo no permite publicar; usa el portal de centros con una cuenta autorizada.",
    },
    en: {
      "auth.signIn": "Sign in or link account ↗",
      "auth.signOut": "Sign out",
      "auth.retry": "Check connection",
      "auth.loading": "Checking your session…",
      "auth.guest": "You are browsing as a guest.",
      "auth.failed": "Could not reach the account service. You can keep browsing as a guest and retry.",
      "auth.signedIn": "Signed in: ",
      "auth.logoutFailed": "Could not sign out. Your session is still active; please retry.",

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
      "centers.title": "Do you run a center? Open the center workspace.",
      "centers.lead": "The same Generosity account can have center permissions. Authorized staff grant these permissions; signing in alone does not allow publishing.",
      "centers.item1": "Profile, hours, and public contact are not in this center workspace.",
      "centers.item2": "A published need has a quantity, a priority, and a closing time. The sample form does not store it; the center workspace does, only for an assigned center.",
      "centers.item3": "Confirming what other people bring is in the center workspace, not in this sample form, and only for an assigned center.",
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
      "access.flag": "Generosity account",
      "access.title": "Center access",
      "access.lead": "Use your Generosity account on web, Android and iOS with Google, Apple or email. Sign-in opens in another tab to keep your draft. Signing in does not grant center permissions or publish needs.",
      "access.keep": "If you wrote a need on this page, the text is still in the form. It was not sent to any server.",
      "access.back": "Back to the local draft",
      "access.dest.none": "No intended destination is saved.",
      "access.dest.publicar": "Intended destination: publish a need. Signing in does not grant permission to publish.",
      "access.dest.centros": "Intended destination: the center workspace. Signing in does not assign a center or allow publishing.",
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
      "publish.notSent": "Nothing was sent and nothing was published. This sample form does not allow publishing; use the center workspace with an authorized account.",
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
    renderAccount();
    if (menuAnnounced) menuStatus.textContent = drawer.hidden ? t("menu.closed") : t("menu.open");
  }

  var apiOrigin = "https://api.generosity.global";
  var accountUser = null;
  var accountStatus = "auth.loading";
  var accountCsrf = "";
  var accountBusy = false;
  var accountLink = document.getElementById("account-sign-in");
  var accountLogout = document.getElementById("account-sign-out");
  var accountRetry = document.getElementById("account-retry");

  function renderAccount() {
    document.getElementById("account-status").textContent = accountStatus === "auth.signedIn"
      ? t(accountStatus) + accountUser.name : t(accountStatus);
    accountLink.href = apiOrigin + "/auth/sign-in?client=web&locale=" + effective();
    accountLink.hidden = !accountCsrf;
    accountLogout.hidden = !accountUser;
    accountLogout.disabled = accountBusy || !accountCsrf;
    accountRetry.disabled = accountBusy;
    syncWorkspace();
  }

  async function checkAccount() {
    if (accountBusy) return;
    accountBusy = true;
    accountStatus = "auth.loading";
    renderAccount();
    try {
      var response = await fetch(apiOrigin + "/auth/session", { credentials: "include", cache: "no-store", signal: AbortSignal.timeout(10000), headers: { Accept: "application/json" } });
      if (!response.ok) throw new Error("Account unavailable");
      var data = await response.json();
      if (typeof data.csrf_token !== "string" || !data.csrf_token) throw new Error("Invalid session");
      accountCsrf = data.csrf_token;
      accountUser = data.user && typeof data.user.name === "string" ? data.user : null;
      accountStatus = accountUser ? "auth.signedIn" : "auth.guest";
    } catch (_) {
      accountStatus = "auth.failed";
      accountCsrf = "";
    } finally { accountBusy = false; renderAccount(); }
  }

  accountRetry.addEventListener("click", checkAccount);
  accountLogout.addEventListener("click", async function () {
    if (accountBusy || !accountCsrf) return;
    accountBusy = true;
    renderAccount();
    try {
      var response = await fetch(apiOrigin + "/auth/logout", { method: "POST", credentials: "include", cache: "no-store", signal: AbortSignal.timeout(10000), headers: { Accept: "application/json", "X-CSRF-TOKEN": accountCsrf } });
      if (!response.ok) throw new Error("Logout failed");
      accountUser = null;
      accountCsrf = "";
      accountStatus = "auth.guest";
    } catch (_) { accountStatus = "auth.logoutFailed"; }
    finally { accountBusy = false; renderAccount(); }
    if (!accountUser) checkAccount();
  });
  window.addEventListener("focus", checkAccount);

  // Staff state is separate from the labeled public examples above.
  var staff = { owner: null, center: '', centers: [], needs: [], contributions: [], needPage: 1, contributionPage: 1, needLast: 1, contributionLast: 1, editing: null, pending: null, busy: false, generation: 0, status: '', durations: {} };
  var staffCopy = {
    es: { title:'Portal de centros', scope:'Solo puedes gestionar los centros que tienes asignados.', center:'Centro autorizado', reload:'Actualizar centros', retry:'Reintentar cambio pendiente', needs:'Necesidades del centro', contributions:'Aportaciones', prev:'Anterior', next:'Siguiente', new:'Nueva necesidad', editor:'Editar necesidad', translations:'Escribe al menos un título y una descripción en español o inglés. No incluyas datos privados de beneficiarios.', goods:'Bienes', time:'Tiempo', urgent:'Urgente', quantity:'Cantidad requerida (opcional)', unit:'Unidad (p. ej., paquetes)', expires:'Último día (opcional, UTC)', status:'Estado', draft:'Borrador', published:'Publicada', closed:'Cerrada', save:'Guardar en el servidor', edit:'Editar', confirm:'Confirmar', fulfill:'Marcar como realizada', actual:'Minutos realizados (opcional)', offered:'Ofrecida', confirmed:'Confirmada', fulfilled:'Realizada', cancelled:'Cancelada', empty:'No hay registros.', none:'Tu cuenta no tiene centros asignados. Solicita acceso al responsable del centro.', loading:'Cargando…', saved:'El servidor confirmó el cambio.', failed:'No se pudo completar la operación. Conservamos el formulario.', pending:'Hay un envío sin confirmar. Reinténtalo antes de hacer otro cambio.', conflict:'Otro cambio modificó este registro. Conservamos tu formulario; revisa la versión actual antes de volver a editar.', denied:'Tu sesión o permiso ya no permite esta operación. Comprueba tu acceso.', invalid:'Escribe un título, una descripción y al menos una forma de ayudar.', unsafe:'No se pudo guardar el envío en este navegador. No se envió nada.' },
    en: { title:'Center workspace', scope:'You can only manage centers assigned to your account.', center:'Authorized center', reload:'Refresh centers', retry:'Retry pending change', needs:'Center needs', contributions:'Contributions', prev:'Previous', next:'Next', new:'New need', editor:'Edit need', translations:'Enter at least one title and description in Spanish or English. Do not include private beneficiary information.', goods:'Goods', time:'Time', urgent:'Urgent', quantity:'Required quantity (optional)', unit:'Unit (e.g., packs)', expires:'Last day (optional, UTC)', status:'Status', draft:'Draft', published:'Published', closed:'Closed', save:'Save to server', edit:'Edit', confirm:'Confirm', fulfill:'Mark completed', actual:'Minutes completed (optional)', offered:'Offered', confirmed:'Confirmed', fulfilled:'Completed', cancelled:'Cancelled', empty:'No records.', none:'Your account has no assigned centers. Ask the center administrator for access.', loading:'Loading…', saved:'The server confirmed the change.', failed:'Could not complete the operation. Your form is preserved.', pending:'A submission has an unknown outcome. Retry it before making another change.', conflict:'Another change updated this record. Your form is preserved; review the latest version before editing again.', denied:'Your session or permission no longer allows this operation. Check your access.', invalid:'Enter a title, a description and at least one way to help.', unsafe:'Could not save the submission in this browser. Nothing was sent.' }
  };
  ['es', 'en'].forEach(function (lang) { Object.keys(staffCopy[lang]).forEach(function (key) { copy[lang]['staff.' + key] = staffCopy[lang][key]; }); });
  function staffText(key) { return key ? t('staff.' + key) : ''; }
  function el(id) { return document.getElementById(id); }
  function localized(value) { return value && (value[effective()] || value.en || Object.values(value)[0]) || ''; }
  function staffMessage(key) { staff.status = key; el('staff-status').textContent = staffText(key); }
  function node(tag, text) { var item = document.createElement(tag); item.textContent = text; return item; }
  function staffButton(label, action) { var b = node('button', staffText(label)); b.type = 'button'; b.disabled = staff.busy || !!staff.pending; b.addEventListener('click', action); return b; }
  function storageKey() { return 'generosity.staff.pending.' + staff.owner; }
  function clearStaff() {
    if (staff.owner) { try { sessionStorage.removeItem(storageKey()); } catch (_) { /* Clear private UI even when browser storage is unavailable. */ } }
    staff.generation++; staff.owner = null; staff.center = ''; staff.centers = []; staff.needs = []; staff.contributions = []; staff.editing = null; staff.pending = null; staff.busy = false; staff.status = ''; staff.durations = {};
    el('staff-need-form').reset(); el('staff-needs').replaceChildren(); el('staff-contributions').replaceChildren(); el('staff-center').replaceChildren(); el('staff-content').hidden = true;
  }
  function syncWorkspace() {
    if (!staff) return;
    var owner = accountUser && String(accountUser.id);
    if (staff.owner && staff.owner !== owner) clearStaff();
    el('center-workspace').hidden = !owner || !accountCsrf;
    if (owner && accountCsrf && !staff.owner) {
      staff.owner = owner;
      try {
        var saved = JSON.parse(sessionStorage.getItem(storageKey()) || 'null');
        if (saved && saved.owner === owner && /^[0-9a-f-]{36}$/.test(saved.key) && /^(POST|PUT)$/.test(saved.method) && /^\/api\/v1\/(staff\/centers\/[^/]+\/needs(\/[^/]+)?|contributions\/[^/]+\/transition)$/.test(saved.path)) staff.pending = saved;
      } catch (_) { staffMessage('unsafe'); }
      loadStaff();
    }
    renderStaff();
  }
  async function staffRequest(path, method, body, key) {
    var owner = staff.owner;
    var headers = { Accept:'application/json' };
    if (method !== 'GET') { headers['Content-Type'] = 'application/json'; headers['X-CSRF-TOKEN'] = accountCsrf; headers['Idempotency-Key'] = key; }
    var response = await fetch(apiOrigin + path, { method:method, credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:headers, body:method === 'GET' ? undefined : JSON.stringify(body) });
    if (!accountUser || String(accountUser.id) !== owner) throw new Error('Account changed');
    if (!response.ok) { var error = new Error('Request rejected'); error.status = response.status; throw error; }
    return response.json();
  }
  async function loadStaff() {
    if (staff.busy || !staff.owner || !accountCsrf) return;
    staff.busy = true; var generation = ++staff.generation; staffMessage('loading'); renderStaff();
    try {
      var data = await staffRequest('/api/v1/staff/centers', 'GET');
      if (generation !== staff.generation) return;
      staff.centers = data.centers;
      if (!staff.centers.some(function (c) { return c.id === staff.center; })) { staff.center = staff.centers[0] ? staff.centers[0].id : ''; staff.needPage = staff.contributionPage = 1; editNeed(null); }
      el('staff-center').replaceChildren();
      staff.centers.forEach(function (c) { var option = node('option', localized(c.name)); option.value = c.id; el('staff-center').append(option); });
      el('staff-center').value = staff.center;
      staffMessage(staff.pending ? 'pending' : (staff.center ? '' : 'none'));
    } catch (e) { if (generation === staff.generation) staffMessage('failed'); }
    finally { if (generation === staff.generation) { staff.busy = false; renderStaff(); } }
    if (generation === staff.generation && staff.center) loadStaffData();
  }
  async function loadStaffData() {
    if (staff.busy || !staff.center) return;
    staff.busy = true; var generation = ++staff.generation; renderStaff();
    var path = '/api/v1/staff/centers/' + encodeURIComponent(staff.center);
    try {
      var results = await Promise.all([staffRequest(path + '/needs?page=' + staff.needPage, 'GET'), staffRequest(path + '/contributions?page=' + staff.contributionPage, 'GET')]);
      if (generation !== staff.generation) return;
      staff.needs = results[0].data; staff.needLast = results[0].last_page; staff.contributions = results[1].data; staff.contributionLast = results[1].last_page;
    } catch (e) {
      if (generation !== staff.generation) return;
      if ([401,403,419].includes(e.status)) { staff.needs = []; staff.contributions = []; staff.center = ''; staffMessage('denied'); }
      else staffMessage('failed');
    } finally { if (generation === staff.generation) { staff.busy = false; renderStaff(); } }
  }
  function renderStaff() {
    if (!staff || !staff.owner) return;
    el('staff-status').textContent = staffText(staff.status);
    el('staff-content').hidden = !staff.center;
    el('staff-fields').disabled = staff.busy || !!staff.pending || accountBusy;
    el('staff-center').disabled = staff.busy || !!staff.pending;
    el('staff-reload').disabled = staff.busy;
    el('staff-retry').hidden = !staff.pending; el('staff-retry').disabled = staff.busy;
    el('need-new').disabled = staff.busy || !!staff.pending;
    ['needs', 'contributions'].forEach(function (kind) {
      var page = kind === 'needs' ? staff.needPage : staff.contributionPage;
      var last = kind === 'needs' ? staff.needLast : staff.contributionLast;
      el(kind + '-prev').disabled = staff.busy || !!staff.pending || page <= 1;
      el(kind + '-next').disabled = staff.busy || !!staff.pending || page >= last;
    });
    el('staff-needs').replaceChildren(); el('staff-contributions').replaceChildren();
    if (!staff.needs.length) el('staff-needs').append(node('p', staffText('empty')));
    staff.needs.forEach(function (need) {
      var row = node('article', ''); row.append(node('h4', localized(need.title)), node('p', staffText(need.status)), staffButton('edit', function () { editNeed(need); el('need-title-' + effective()).focus(); })); el('staff-needs').append(row);
    });
    if (!staff.contributions.length) el('staff-contributions').append(node('p', staffText('empty')));
    staff.contributions.forEach(function (contribution) {
      var row = node('article', ''); row.append(node('h4', localized(contribution.title) || contribution.id), node('p', staffText(contribution.status)), node('p', contribution.activity || ''), node('p', contribution.availability || ''), node('p', contribution.notes || ''));
      if (contribution.quantity != null) row.append(node('p', contribution.quantity + ' ' + (contribution.quantity_unit || '')));
      if (String(contribution.user_id) !== staff.owner && ['offered','confirmed'].includes(contribution.status)) {
        var duration;
        if (contribution.status === 'confirmed' && contribution.method === 'time') {
          var label = node('label', staffText('actual')); duration = document.createElement('input'); duration.type = 'number'; duration.min = '1'; duration.max = '525600'; duration.step = '1'; duration.value = staff.durations[contribution.id] || ''; duration.disabled = staff.busy || !!staff.pending; duration.addEventListener('input', function () { staff.durations[contribution.id] = duration.value; }); label.append(duration); row.append(label);
        }
        row.append(staffButton(contribution.status === 'offered' ? 'confirm' : 'fulfill', function () {
          var body = { action:contribution.status === 'offered' ? 'confirm' : 'fulfill', version:contribution.version };
          if (duration && duration.value) { if (!duration.reportValidity()) return; body.actual_minutes = Number(duration.value); }
          mutateStaff('/api/v1/contributions/' + encodeURIComponent(contribution.id) + '/transition', 'POST', body);
        }));
      }
      el('staff-contributions').append(row);
    });
  }
  function editNeed(need) {
    staff.editing = need; el('staff-need-form').reset();
    ['es','en'].forEach(function (lang) { el('need-title-' + lang).value = need && need.title[lang] || ''; el('need-description-' + lang).value = need && need.description[lang] || ''; });
    el('need-goods').checked = !need || need.helpMethods.includes('goods'); el('need-time').checked = !!need && need.helpMethods.includes('time');
    el('need-urgent').checked = !!need && need.urgent; el('need-quantity').value = need && need.quantityRequired || ''; el('need-unit').value = need && need.unit || ''; el('need-expires').value = need && need.expiresOnIsoDate || ''; el('need-status').value = need ? need.status : 'draft';
  }
  async function mutateStaff(path, method, body) {
    if (staff.busy || staff.pending) return;
    var pending = { owner:staff.owner, path:path, method:method, body:body, key:crypto.randomUUID() };
    try { sessionStorage.setItem(storageKey(), JSON.stringify(pending)); } catch (_) { staffMessage('unsafe'); return; }
    staff.pending = pending; await retryStaff();
  }
  async function retryStaff() {
    if (staff.busy || !staff.pending) return;
    staff.busy = true; var generation = ++staff.generation; var pending = staff.pending; renderStaff();
    try {
      await staffRequest(pending.path, pending.method, pending.body, pending.key);
      if (generation !== staff.generation) return;
      sessionStorage.removeItem(storageKey()); staff.pending = null; staffMessage('saved');
      if (pending.path.includes('/needs')) editNeed(null);
      else delete staff.durations[pending.path.split('/')[4]];
    } catch (e) {
      if (generation !== staff.generation) return;
      if ([400,403,404,409,422].includes(e.status)) {
        try { sessionStorage.removeItem(storageKey()); staff.pending = null; } catch (_) { /* Retain the known operation key until storage recovers. */ }
      }
      staffMessage(e.status === 409 ? 'conflict' : [401,403,419].includes(e.status) ? 'denied' : staff.pending ? 'pending' : 'failed');
    } finally { if (generation === staff.generation) { staff.busy = false; renderStaff(); } }
    if (generation === staff.generation && !staff.pending) await loadStaffData();
  }
  el('staff-center').addEventListener('change', function () { staff.center = this.value; staff.needPage = staff.contributionPage = 1; editNeed(null); loadStaffData(); });
  el('staff-reload').addEventListener('click', loadStaff); el('staff-retry').addEventListener('click', retryStaff); el('need-new').addEventListener('click', function () { editNeed(null); });
  ['needs','contributions'].forEach(function (kind) { ['prev','next'].forEach(function (direction) { el(kind + '-' + direction).addEventListener('click', function () { var property = kind === 'needs' ? 'needPage' : 'contributionPage'; staff[property] += direction === 'next' ? 1 : -1; loadStaffData(); }); }); });
  el('staff-need-form').addEventListener('submit', function (event) {
    event.preventDefault(); if (staff.pending || staff.busy) return;
    var old = staff.editing; var title = Object.assign({}, old && old.title); var description = Object.assign({}, old && old.description);
    ['es','en'].forEach(function (lang) { var a = el('need-title-' + lang).value.trim(), b = el('need-description-' + lang).value.trim(); if (a) title[lang] = a; else delete title[lang]; if (b) description[lang] = b; else delete description[lang]; });
    var methods = ['goods','time'].filter(function (code) { return el('need-' + code).checked; });
    // Preserve an existing money declaration; this workspace cannot enable payments.
    if (old && old.helpMethods.includes('money')) methods.push('money');
    if (!Object.keys(title).length || !Object.keys(description).length || !methods.length) { staffMessage('invalid'); return; }
    var body = { title:title, description:description, helpMethods:methods, urgent:el('need-urgent').checked, quantityRequired:el('need-quantity').value ? Number(el('need-quantity').value) : null, unit:el('need-unit').value.trim() || null, expiresOnIsoDate:el('need-expires').value || null, status:el('need-status').value };
    if (old) body.version = old.version;
    mutateStaff('/api/v1/staff/centers/' + encodeURIComponent(staff.center) + '/needs' + (old ? '/' + encodeURIComponent(old.id) : ''), old ? 'PUT' : 'POST', body);
  });

  var stored = readStored();
  if (stored === "es" || stored === "en" || stored === "system") mode = stored;
  applyAll();
  checkAccount();
})();
