(function () {
  var STORAGE_KEY = "generosity.locale";
  var mode = "system";
  var menuAnnounced = false;

  var copy = {
  "es": {
    "auth.signIn": "Iniciar sesión ↗",
    "auth.signOut": "Cerrar sesión",
    "auth.retry": "Comprobar conexión",
    "auth.loading": "Comprobando tu sesión…",
    "auth.guest": "Estás navegando como invitado.",
    "auth.failed": "No se pudo conectar con el servicio de cuentas. Puedes seguir navegando como invitado y reintentar.",
    "auth.signedIn": "Sesión iniciada: ",
    "auth.logoutFailed": "No se pudo cerrar la sesión. Conservamos tu sesión; vuelve a intentarlo.",
    "meta.title": "Generosity — Encuentra dónde ayudar, cerca de ti",
    "meta.description": "Encuentra dónde ayudar cerca de ti. Conecta con necesidades de alimentación, educación y salud, o registra tu organización en Generosity.",
    "skip": "Saltar al contenido",
    "nav.sections": "Secciones",
    "nav.how": "Cómo funciona",
    "nav.needs": "Necesidades",
    "nav.centers": "Para centros",
    "nav.donate": "Cómo ayudar",
    "lang.label": "Idioma",
    "lang.system": "Automático",
    "menu.label": "Menú",
    "menu.close": "Cerrar menú",
    "menu.open": "Menú abierto",
    "menu.closed": "Menú cerrado",
    "hero.where": "Encuentra. Conecta. Ayuda.",
    "hero.title": "La ayuda llega cuando sabes exactamente quién la necesita.",
    "hero.lead": "Conecta con las necesidades de tu comunidad y encuentra una forma de ayudar. Tu tiempo, tus conocimientos y lo que compartes pueden hacer la diferencia.",
    "map.title": "Ilustración de una comunidad conectada por la ayuda.",
    "map.pinFood": "Conocer la causa de alimentación",
    "map.pinKids": "Conocer la causa de infancia",
    "map.pinHealth": "Conocer la causa de salud",
    "map.you": "Comunidad",
    "map.food": "Comedor",
    "map.kids": "Infancia",
    "map.health": "Salud",
    "how.title": "La ayuda empieza con una conexión.",
    "how.discover.title": "Descubres",
    "how.discover.body": "Acércate a una causa que te importe: alimentación, infancia, salud o educación.",
    "how.understand.title": "Entiendes",
    "how.understand.body": "Conoce las necesidades de cada organización y sus condiciones para recibir apoyo.",
    "how.help.title": "Ayudas",
    "how.help.body": "Acuerda con el centro qué aportar, cómo hacerlo y cuándo entregar tu ayuda.",
    "how.return.title": "Vuelves",
    "how.return.body": "Da seguimiento a tu aportación y mantén el vínculo con quienes trabajan por tu comunidad.",
    "needs.title": "Una necesidad concreta. Muchas formas de ayudar.",
    "needs.filters": "Filtrar necesidades por causa",
    "needs.empty": "Elige otra causa para conocer más formas de ayudar.",
    "filter.all": "Todas",
    "filter.food": "Comedores",
    "filter.kids": "Infancia",
    "filter.health": "Salud",
    "card.food.cause": "Comedor comunitario",
    "card.food.title": "Despensas para compartir",
    "card.food.body": "Arroz, frijol, aceite y lentejas para apoyar la alimentación de las familias.",
    "card.food.action": "Quiero aportar alimentos",
    "card.kids.cause": "Infancia y educación",
    "card.kids.title": "Útiles escolares para el nuevo ciclo",
    "card.kids.body": "Cuadernos, lápices y mochilas para acompañar el regreso a clases.",
    "card.kids.action": "Quiero aportar útiles",
    "card.health.cause": "Salud",
    "card.health.title": "Acompañamiento a citas médicas",
    "card.health.body": "Comparte tu tiempo para acompañar a una persona a su consulta y ayudarle durante el traslado.",
    "card.health.action": "Quiero aportar tiempo",
    "donate.title": "Tu manera de ayudar también cuenta.",
    "donate.lead": "Ayudar puede empezar con unas horas, materiales útiles o algo que sabes hacer. Coordina cada aportación con la organización que la recibe.",
    "donate.time.title": "Tiempo",
    "donate.time.body": "Comparte tiempo para acompañar, organizar actividades o apoyar el trabajo cotidiano de un centro.",
    "donate.goods.title": "Bienes",
    "donate.goods.body": "Aporta alimentos, materiales e insumos de acuerdo con las necesidades y condiciones de recepción del centro.",
    "centers.title": "Un espacio para quienes ayudan todos los días.",
    "centers.lead": "El portal reúne las necesidades y aportaciones de los centros que tienes asignados.",
    "centers.item1": "Selecciona el centro que representas.",
    "centers.item2": "Publica y actualiza sus necesidades, cantidades y prioridades.",
    "centers.item3": "Consulta las ofertas de ayuda y confirma su recepción.",
    "centers.item4": "Mantén la coordinación con quienes participan.",
    "centers.access": "Ir al acceso de centros",
    "publish.title": "Gestiona las necesidades de tu centro.",
    "publish.intro": "Entra con tu cuenta Generosity y selecciona un centro autorizado para publicar necesidades y dar seguimiento a sus aportaciones.",
    "publish.submit": "Entrar al portal de centros",
    "access.flag": "Cuenta Generosity",
    "access.title": "Acceso de centros",
    "access.lead": "Inicia sesión con tu cuenta Generosity para gestionar tus centros y solicitudes de sedes.",
    "access.dest.none": "",
    "access.dest.publicar": "Selecciona un centro autorizado para publicar una necesidad.",
    "access.dest.centros": "Selecciona el centro que representas para entrar a su espacio.",
    "footer.tag": "Una iniciativa del Instituto de Deberes y Derechos Humanos.",
    "footer.nav": "Pie",
    "footer.privacy": "Aviso de privacidad",
    "footer.terms": "Términos y condiciones",
    "footer.contact": "Contacto",
    "hero.explore": "Explora las necesidades",
    "hero.register": "Registra tu organización",
    "donate.knowledge.title": "Conocimientos",
    "donate.knowledge.body": "Comparte tus habilidades mediante tutorías, talleres o apoyo profesional.",
    "participate.title": "Haz que tu organización sea parte.",
    "participate.body": "Cuéntanos dónde trabajan y a quiénes ayudan. Registra una sede para iniciar el vínculo con Generosity.",
    "participate.access": "Ya represento a un centro",
    "card.food.item1": "Alimentos no perecederos",
    "card.food.item2": "Despensas",
    "card.kids.item1": "Material escolar",
    "card.kids.item2": "Mochilas",
    "card.health.item1": "Acompañamiento",
    "card.health.item2": "Traslados"
  },
  "en": {
    "auth.signIn": "Sign in ↗",
    "auth.signOut": "Sign out",
    "auth.retry": "Check connection",
    "auth.loading": "Checking your session…",
    "auth.guest": "You are browsing as a guest.",
    "auth.failed": "Could not reach the account service. You can keep browsing as a guest and retry.",
    "auth.signedIn": "Signed in: ",
    "auth.logoutFailed": "Could not sign out. Your session is still active; please retry.",
    "meta.title": "Generosity — Find where to help, near you",
    "meta.description": "Find where to help near you. Connect with food, education and health needs, or register your organization with Generosity.",
    "skip": "Skip to content",
    "nav.sections": "Sections",
    "nav.how": "How it works",
    "nav.needs": "Needs",
    "nav.centers": "For centers",
    "nav.donate": "Ways to help",
    "lang.label": "Language",
    "lang.system": "Automatic",
    "menu.label": "Menu",
    "menu.close": "Close menu",
    "menu.open": "Menu open",
    "menu.closed": "Menu closed",
    "hero.where": "Find. Connect. Help.",
    "hero.title": "Help arrives when you know exactly who needs it.",
    "hero.lead": "Connect with needs in your community and find a way to help. Your time, skills and contributions can make a difference.",
    "map.title": "Illustration of a community connected through helping.",
    "map.pinFood": "Explore food support",
    "map.pinKids": "Explore support for children",
    "map.pinHealth": "Explore health support",
    "map.you": "Community",
    "map.food": "Meals",
    "map.kids": "Childhood",
    "map.health": "Health",
    "how.title": "Helping starts with a connection.",
    "how.discover.title": "You find",
    "how.discover.body": "Find a cause you care about: food, children, health or education.",
    "how.understand.title": "You understand",
    "how.understand.body": "Learn what each organization needs and how it receives support.",
    "how.help.title": "You help",
    "how.help.body": "Agree with the center on what you can contribute, how to help and when to deliver it.",
    "how.return.title": "You return",
    "how.return.body": "Follow up on your contribution and stay connected with the people supporting your community.",
    "needs.title": "A specific need. Many ways to help.",
    "needs.filters": "Filter needs by cause",
    "needs.empty": "Choose another cause to discover more ways to help.",
    "filter.all": "All",
    "filter.food": "Meal centers",
    "filter.kids": "Childhood",
    "filter.health": "Health",
    "card.food.cause": "Community meal center",
    "card.food.title": "Food parcels to share",
    "card.food.body": "Rice, beans, oil and lentils to help families put food on the table.",
    "card.food.action": "Contribute food",
    "card.kids.cause": "Childhood and education",
    "card.kids.title": "School supplies for a new term",
    "card.kids.body": "Notebooks, pencils and backpacks to support children returning to school.",
    "card.kids.action": "Contribute school supplies",
    "card.health.cause": "Health",
    "card.health.title": "Companionship for medical appointments",
    "card.health.body": "Offer your time to accompany someone to an appointment and help them get there.",
    "card.health.action": "Contribute time",
    "donate.title": "Your way of helping matters.",
    "donate.lead": "Helping can start with a few hours, useful supplies or a skill you can share. Coordinate each contribution with the organization receiving it.",
    "donate.time.title": "Time",
    "donate.time.body": "Share your time by offering companionship, organizing activities or supporting a center’s daily work.",
    "donate.goods.title": "Goods",
    "donate.goods.body": "Contribute food, materials and supplies based on the center’s needs and delivery requirements.",
    "centers.title": "A space for people who help every day.",
    "centers.lead": "The workspace brings together needs and contributions for the centers assigned to you.",
    "centers.item1": "Select the center you represent.",
    "centers.item2": "Publish and update its needs, quantities and priorities.",
    "centers.item3": "Review offers of help and confirm receipt.",
    "centers.item4": "Stay coordinated with the people taking part.",
    "centers.access": "Go to center access",
    "publish.title": "Manage your center’s needs.",
    "publish.intro": "Sign in with your Generosity account and select an authorized center to publish needs and follow up on contributions.",
    "publish.submit": "Open the center workspace",
    "access.flag": "Generosity account",
    "access.title": "Center access",
    "access.lead": "Sign in with your Generosity account to manage your centers and location applications.",
    "access.dest.none": "",
    "access.dest.publicar": "Select an authorized center to publish a need.",
    "access.dest.centros": "Select the center you represent to open its workspace.",
    "footer.tag": "An initiative of Instituto de Deberes y Derechos Humanos.",
    "footer.nav": "Footer",
    "footer.privacy": "Privacy notice",
    "footer.terms": "Terms and conditions",
    "footer.contact": "Contact",
    "hero.explore": "Explore needs",
    "hero.register": "Register your organization",
    "donate.knowledge.title": "Skills",
    "donate.knowledge.body": "Share what you know through tutoring, workshops or professional support.",
    "participate.title": "Bring your organization into the community.",
    "participate.body": "Tell us where you work and who you support. Register a location to connect with Generosity.",
    "participate.access": "I already represent a center",
    "card.food.item1": "Non-perishable food",
    "card.food.item2": "Food parcels",
    "card.kids.item1": "School supplies",
    "card.kids.item2": "Backpacks",
    "card.health.item1": "Companionship",
    "card.health.item2": "Getting there"
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
    each("[data-legal]", function (el) {
      var pages = effective() === "es" ? {privacy: "aviso-de-privacidad.html", terms: "terminos-y-condiciones.html"} : {privacy: "privacy-notice.html", terms: "terms-and-conditions.html"};
      el.href = pages[el.getAttribute("data-legal")];
    });
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
    syncApplications();
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

  // Keep private staff state scoped to the signed-in account.
  var staff = { owner: null, center: '', centers: [], needs: [], contributions: [], needPage: 1, contributionPage: 1, needLast: 1, contributionLast: 1, editing: null, pending: null, busy: false, generation: 0, status: '', durations: {} };
  var staffCopy = {
    es: { title:'Portal de centros', scope:'Solo puedes gestionar los centros que tienes asignados.', center:'Centro autorizado', reload:'Actualizar centros', retry:'Reintentar cambio pendiente', needs:'Necesidades del centro', contributions:'Aportaciones', prev:'Anterior', next:'Siguiente', new:'Nueva necesidad', editor:'Editar necesidad', translations:'Escribe al menos un título y una descripción en español o inglés. No incluyas datos privados de beneficiarios.', goods:'Bienes', time:'Tiempo', urgent:'Urgente', quantity:'Cantidad requerida (opcional)', unit:'Unidad (p. ej., paquetes)', expires:'Último día (opcional, UTC)', status:'Estado', draft:'Borrador', published:'Publicada', closed:'Cerrada', save:'Guardar cambios', edit:'Editar', confirm:'Confirmar', fulfill:'Marcar como realizada', actual:'Minutos realizados (opcional)', offered:'Ofrecida', confirmed:'Confirmada', fulfilled:'Realizada', cancelled:'Cancelada', empty:'No hay registros.', none:'Tu cuenta no tiene centros asignados. Solicita acceso al responsable del centro.', loading:'Cargando…', saved:'El servidor confirmó el cambio.', failed:'No se pudo completar la operación. Conservamos el formulario.', pending:'Hay un envío sin confirmar. Reinténtalo antes de hacer otro cambio.', conflict:'Otro cambio modificó este registro. Conservamos tu formulario; revisa la versión actual antes de volver a editar.', denied:'Tu sesión o permiso ya no permite esta operación. Comprueba tu acceso.', invalid:'Escribe un título, una descripción y al menos una forma de ayudar.', unsafe:'No se pudo guardar el envío en este navegador. No se envió nada.' },
    en: { title:'Center workspace', scope:'You can only manage centers assigned to your account.', center:'Authorized center', reload:'Refresh centers', retry:'Retry pending change', needs:'Center needs', contributions:'Contributions', prev:'Previous', next:'Next', new:'New need', editor:'Edit need', translations:'Enter at least one title and description in Spanish or English. Do not include private beneficiary information.', goods:'Goods', time:'Time', urgent:'Urgent', quantity:'Required quantity (optional)', unit:'Unit (e.g., packs)', expires:'Last day (optional, UTC)', status:'Status', draft:'Draft', published:'Published', closed:'Closed', save:'Save changes', edit:'Edit', confirm:'Confirm', fulfill:'Mark completed', actual:'Minutes completed (optional)', offered:'Offered', confirmed:'Confirmed', fulfilled:'Completed', cancelled:'Cancelled', empty:'No records.', none:'Your account has no assigned centers. Ask the center administrator for access.', loading:'Loading…', saved:'The server confirmed the change.', failed:'Could not complete the operation. Your form is preserved.', pending:'A submission has an unknown outcome. Retry it before making another change.', conflict:'Another change updated this record. Your form is preserved; review the latest version before editing again.', denied:'Your session or permission no longer allows this operation. Check your access.', invalid:'Enter a title, a description and at least one way to help.', unsafe:'Could not save the submission in this browser. Nothing was sent.' }
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

  var applicationCopy = {
    es: {
      nav:'Registrar una sede', eyebrow:'Para organizaciones', title:'Tu organización ayuda. Hagamos que más personas la encuentren.', lead:'Solicita el registro de una sede. Si tienen varias, envía una solicitud por cada ubicación.',
      step1:'Cuéntanos quiénes son y qué apoyo ofrecen.', step2:'Envía la solicitud desde tu cuenta Generosity con correo verificado.', step3:'Recibe un folio y consulta tu solicitud pendiente de revisión.',
      private:'Los datos de esta solicitud son privados. No publiques datos de personas beneficiarias ni domicilios protegidos. El envío no publica la sede, no la verifica ni concede permisos para administrarla.', existing:'Ya tengo un centro asignado', formTitle:'Registrar una sede', required:'Los campos marcados con * son obligatorios. Puedes preparar el formulario antes de iniciar sesión; conserva esta pestaña abierta.',
      organization:'1. Tu organización', organization_name:'Nombre de la organización *', activities:'Tipo de centro y actividades *', activitiesHint:'Por ejemplo: comedor comunitario, albergue, atención de salud o capacitación. Describe a quiénes apoyan.', location:'2. La sede', site_name:'Nombre de la sede *', country:'País *', city:'Ciudad o localidad *', contact:'3. Contacto de la organización', contact_name:'Tu nombre *', contact_role:'Cargo o relación con la organización *', emailHint:'Usaremos el correo verificado de tu cuenta para contactarte sobre esta solicitud.',
      google_maps_url:'Enlace de Google Maps', latitude:'Latitud', longitude:'Longitud', locationHint:'Comparte la ficha del lugar en Google Maps. Para las coordenadas, selecciona el punto exacto de entrada y copia primero la latitud y después la longitud; no uses las del centro de la pantalla. Si no las conoces o la ubicación es protegida, deja estos datos vacíos. Si indicas coordenadas, escribe ambas.',
      optional:'Agregar datos opcionales', legal_name:'Nombre legal', region:'Estado, provincia o región', postal_code:'Código postal', address:'Dirección de atención al público (omite ubicaciones protegidas)', phone:'Teléfono con código de país', website:'Sitio web oficial', representation:'Declaro que represento a esta organización. *', consent:'Autorizo que Generosity guarde estos datos para revisar la solicitud y contactarme sobre ella. *',
      signIn:'Iniciar sesión ↗', create:'Crear cuenta ↗', submit:'Enviar solicitud', retry:'Reintentar el mismo envío', history:'Mis solicitudes de sedes', refresh:'Actualizar solicitudes', guest:'Inicia sesión o crea una cuenta con correo verificado para enviar. El formulario permanecerá en esta pestaña.', verify:'Verifica el correo de tu cuenta antes de enviar la solicitud.', signed:'La solicitud se enviará desde tu cuenta: ', unavailable:'No se pudo comprobar tu sesión. Conservamos el formulario; comprueba la conexión en el acceso de cuentas.',
      invalid:'Revisa los campos marcados y confirma ambas declaraciones.', sending:'Enviando solicitud…', pending:'No pudimos confirmar el resultado. Conservamos el mismo envío para que puedas reintentarlo sin duplicarlo.', saved:'Solicitud recibida. Pendiente de revisión; la sede aún no está publicada. Folio: ', denied:'Tu sesión necesita actualizarse. Inicia sesión y reintenta el mismo envío.', rejected:'El servidor rechazó el envío. Revisa los datos antes de volver a enviarlo.', unsafe:'Este navegador no pudo guardar el envío pendiente. No se envió nada.', loading:'Consultando tus solicitudes…', empty:'Todavía no has enviado solicitudes de sedes.', listFailed:'No se pudieron consultar tus solicitudes. Puedes actualizar para volver a intentarlo.', status_pending:'Pendiente de revisión', status_other:'Consulta al equipo de Generosity', reference:'Folio: '
    },
    en: {
      nav:'Register a location', eyebrow:'For organizations', title:'Your organization helps. Let’s help more people find it.', lead:'Apply to register a location. If you have several, submit one application for each location.',
      step1:'Tell us who you are and the support you provide.', step2:'Submit through your Generosity account with a verified email.', step3:'Receive a reference number and track your application while it awaits review.',
      private:'Application details are private. Do not include beneficiary information or protected addresses. Submitting does not publish or verify the location, or grant access to manage it.', existing:'I already have an assigned center', formTitle:'Register a location', required:'Fields marked * are required. You can fill out the form before signing in; keep this tab open.',
      organization:'1. Your organization', organization_name:'Organization name *', activities:'Type of center and activities *', activitiesHint:'For example: community kitchen, shelter, health care or training. Describe the people you support.', location:'2. The location', site_name:'Location name *', country:'Country *', city:'City or town *', contact:'3. Organization contact', contact_name:'Your name *', contact_role:'Role or relationship to the organization *', emailHint:'We will use your account’s verified email to contact you about this application.',
      google_maps_url:'Google Maps link', latitude:'Latitude', longitude:'Longitude', locationHint:'Share the place listing in Google Maps. For coordinates, select the exact entrance pin and copy latitude first, then longitude; do not use the map viewport coordinates. Leave these fields blank if unknown or the location is protected. If providing coordinates, enter both.',
      optional:'Add optional details', legal_name:'Legal name', region:'State, province or region', postal_code:'Postal code', address:'Public service address (omit protected locations)', phone:'Phone number with country code', website:'Official website', representation:'I confirm that I represent this organization. *', consent:'I authorize Generosity to store these details to review the application and contact me about it. *',
      signIn:'Sign in ↗', create:'Create account ↗', submit:'Submit application', retry:'Retry the same submission', history:'My location applications', refresh:'Refresh applications', guest:'Sign in or create an account with a verified email to submit. Your form will remain in this tab.', verify:'Verify your account’s email before submitting.', signed:'Submitting from your account: ', unavailable:'We could not check your session. Your form is preserved; check the connection in account access.',
      invalid:'Review the marked fields and confirm both declarations.', sending:'Submitting application…', pending:'We could not confirm the outcome. The same submission is saved so you can retry without creating a duplicate.', saved:'Application received and pending review. The location is not published yet. Reference: ', denied:'Your session needs to be refreshed. Sign in and retry the same submission.', rejected:'The server rejected the submission. Review the details before submitting again.', unsafe:'This browser could not save the pending submission. Nothing was sent.', loading:'Loading your applications…', empty:'You have not submitted any location applications yet.', listFailed:'Could not load your applications. Refresh to try again.', status_pending:'Pending review', status_other:'Contact the Generosity team', reference:'Reference: '
    }
  };
  ['es','en'].forEach(function (lang) { Object.keys(applicationCopy[lang]).forEach(function (key) { copy[lang]['application.' + key] = applicationCopy[lang][key]; }); });
  var application = { owner:null, pending:null, busy:false, loading:false, page:1, last:1, items:[], status:'', reference:'', listStatus:'', generation:0 };
  var registrationForm = el('site-registration');
  function applicationText(key) { return key ? t('application.' + key) : ''; }
  function applicationStorage(owner) { return 'generosity.application.pending.' + owner; }
  function applicationReady() { return accountStatus === 'auth.signedIn' && accountCsrf && accountUser && accountUser.email_verified_at; }
  function syncApplications() {
    if (!application) return;
    var owner = accountUser && String(accountUser.id);
    if (application.owner !== owner) {
      if (application.owner) {
        try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* Still clear another account's private view. */ }
        registrationForm.reset();
      }
      application = { owner:owner, pending:null, busy:false, loading:false, page:1, last:1, items:[], status:'', reference:'', listStatus:'', generation:application.generation + 1 };
      clearApplicationErrors();
      if (owner) {
        try {
          var saved = JSON.parse(sessionStorage.getItem(applicationStorage(owner)) || 'null');
          if (saved && saved.owner === owner && /^[0-9a-f-]{36}$/.test(saved.key) && saved.body && typeof saved.body === 'object') {
            application.pending = saved; application.status = 'pending';
            Array.from(registrationForm.elements).forEach(function (field) {
              if (!field.name || !Object.prototype.hasOwnProperty.call(saved.body, field.name)) return;
              if (field.type === 'checkbox') field.checked = saved.body[field.name] === true;
              else field.value = saved.body[field.name] || '';
            });
          }
        } catch (_) { application.status = 'unsafe'; }
        if (applicationReady()) loadApplications();
      }
    }
    if (applicationReady() && !application.listStatus && !application.items.length && !application.loading) loadApplications();
    renderApplication();
  }
  function renderApplication() {
    var ready = applicationReady();
    el('registration-fields').disabled = application.busy || !!application.pending;
    el('registration-submit').disabled = !ready || accountBusy || application.busy;
    el('registration-submit').textContent = applicationText(application.pending ? 'retry' : 'submit');
    el('registration-access').hidden = !!ready;
    el('registration-sign-in').href = apiOrigin + '/auth/sign-in?client=web&locale=' + effective();
    el('registration-create').href = el('registration-sign-in').href + '&screen=register';
    el('registration-session').textContent = ready ? applicationText('signed') + accountUser.email
      : applicationText(accountStatus === 'auth.failed' ? 'unavailable' : accountUser ? 'verify' : 'guest');
    el('registration-result').textContent = applicationText(application.status) + (application.status === 'saved' ? application.reference : '');
    el('registration-history').hidden = !ready;
    el('registration-history-status').textContent = applicationText(application.listStatus);
    el('registration-refresh').disabled = application.loading || !ready;
    el('registration-prev').disabled = application.loading || application.page <= 1;
    el('registration-next').disabled = application.loading || application.page >= application.last;
    el('registration-list').replaceChildren();
    application.items.forEach(function (item) {
      var row = node('li', item.organization_name + ' · ' + item.site_name);
      row.append(node('p', applicationText(item.status === 'pending' ? 'status_pending' : 'status_other')));
      row.append(node('small', applicationText('reference') + item.id));
      el('registration-list').append(row);
    });
  }
  async function loadApplications() {
    if (!applicationReady() || application.loading) return;
    var generation = application.generation;
    application.loading = true; application.listStatus = 'loading'; renderApplication();
    try {
      var response = await fetch(apiOrigin + '/api/v1/me/center-applications?page=' + application.page, { credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:{Accept:'application/json'} });
      if (!response.ok) throw new Error('Applications unavailable');
      var data = await response.json();
      if (!Array.isArray(data.data) || !Number.isInteger(data.last_page)) throw new Error('Invalid applications response');
      if (generation !== application.generation) return;
      application.items = data.data; application.last = data.last_page;
      application.listStatus = data.data.length ? '' : 'empty';
    } catch (_) { if (generation === application.generation) application.listStatus = 'listFailed'; }
    finally { if (generation === application.generation) { application.loading = false; renderApplication(); } }
  }
  function clearApplicationErrors() {
    el('registration-errors').textContent = '';
    Array.from(registrationForm.elements).forEach(function (field) { field.removeAttribute('aria-invalid'); field.removeAttribute('aria-errormessage'); });
  }
  function applicationErrors(names) {
    el('registration-errors').textContent = applicationText('invalid');
    var first;
    names.forEach(function (name) {
      var field = registrationForm.elements.namedItem(name);
      if (field && field.focus) { field.setAttribute('aria-invalid', 'true'); field.setAttribute('aria-errormessage', 'registration-errors'); if (!first) first = field; }
    });
    if (first) { var details = first.closest('details'); if (details) details.open = true; first.focus(); }
    else el('registration-errors').focus();
  }
  registrationForm.addEventListener('submit', async function (event) {
    event.preventDefault();
    if (!applicationReady() || application.busy || accountBusy) return;
    clearApplicationErrors();
    if (!application.pending) {
      var invalid = Array.from(registrationForm.elements).filter(function (field) { return field.willValidate && !field.checkValidity(); });
      var latitude = registrationForm.elements.namedItem('latitude'), longitude = registrationForm.elements.namedItem('longitude');
      if (!!latitude.value !== !!longitude.value) invalid.push(latitude.value ? longitude : latitude);
      if (invalid.length) { applicationErrors(invalid.map(function (field) { return field.name; })); return; }
      var body = {};
      Array.from(registrationForm.elements).forEach(function (field) {
        if (field.name) body[field.name] = field.type === 'checkbox' ? field.checked : field.value.trim() || null;
      });
      try {
        var pending = { owner:application.owner, key:crypto.randomUUID(), body:body };
        sessionStorage.setItem(applicationStorage(application.owner), JSON.stringify(pending));
        application.pending = pending;
      } catch (_) { application.status = 'unsafe'; renderApplication(); return; }
    }
    var submission = application.pending, generation = application.generation;
    application.busy = true; application.status = 'sending'; renderApplication();
    try {
      var response = await fetch(apiOrigin + '/api/v1/center-applications', { method:'POST', credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:{Accept:'application/json', 'Content-Type':'application/json', 'X-CSRF-TOKEN':accountCsrf, 'Idempotency-Key':submission.key}, body:JSON.stringify(submission.body) });
      var data = await response.json();
      if (generation !== application.generation) return;
      if (!response.ok) {
        if ([400,404,409,422].includes(response.status)) {
          try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* A rejected replay remains safe if storage is unavailable. */ }
          application.pending = null;
          application.status = 'rejected';
          application.busy = false;
          renderApplication();
          if (response.status === 422) applicationErrors(Object.keys(data.error && data.error.details || {}));
        } else application.status = [401,403,419].includes(response.status) ? 'denied' : 'pending';
        return;
      }
      if (!data.application || typeof data.application.id !== 'string' || data.application.status !== 'pending') throw new Error('Invalid receipt');
      try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* Replaying an acknowledged key cannot create another application. */ }
      application.pending = null; application.status = 'saved'; application.reference = data.application.id;
      registrationForm.reset(); application.page = 1;
      el('registration-result').focus();
      await loadApplications();
    } catch (_) { if (generation === application.generation) application.status = 'pending'; }
    finally { if (generation === application.generation) { application.busy = false; renderApplication(); } }
  });
  el('registration-refresh').addEventListener('click', loadApplications);
  ['prev','next'].forEach(function (direction) { el('registration-' + direction).addEventListener('click', function () { application.page += direction === 'next' ? 1 : -1; loadApplications(); }); });

  var stored = readStored();
  if (stored === "es" || stored === "en" || stored === "system") mode = stored;
  applyAll();
  checkAccount();
})();
