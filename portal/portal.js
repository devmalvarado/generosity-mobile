(function () {
  function redirectLegacyRoute() {
    if (document.body.dataset.page !== 'landing') return false;
    var hash = location.hash;
    var path = /^#(descubrir|centro\/|necesidad\/)/.test(hash) ? '/ayudar/'
      : /^#registrar-sede(?:$|\?)/.test(hash) ? '/registrar-sede/'
      : /^#(acceso(?:$|\?)|center-workspace$)/.test(hash) ? '/kindness-center/' : '';
    if (!path) return false;
    location.replace(path + location.search + hash);
    return true;
  }
  if (redirectLegacyRoute()) return;

  var STORAGE_KEY = "generosity.locale";
  var mode = "system";
  var menuAnnounced = false;

  var copy = {
  "es": {
    "auth.signIn": "Iniciar sesión ↗",
    "auth.signOut": "Cerrar sesión",
    "auth.retry": "Comprobar conexión",
    "auth.loading": "Comprobando tu sesión…",
    "auth.preview": "Explora el catálogo aquí. El acceso a tu cuenta se abre en el sitio publicado.",
    "auth.guest": "Estás navegando como invitado.",
    "auth.failed": "No se pudo conectar con el servicio de cuentas. Puedes seguir navegando como invitado y reintentar.",
    "auth.signedIn": "Sesión iniciada: ",
    "auth.logoutFailed": "No se pudo cerrar la sesión. Conservamos tu sesión; vuelve a intentarlo.",
    "meta.title": "Generosity — Encuentra dónde ayudar, cerca de ti",
    "meta.description": "Generosity, una iniciativa del Instituto de Deberes y Derechos Humanos para conectar personas, organizaciones y causas. Registra tu sede y coordina la ayuda de tu comunidad.",
    "skip": "Saltar al contenido",
    "nav.sections": "Secciones",
    "nav.how": "Cómo funciona",
    "nav.needs": "Causas",
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
    "hero.lead": "Una iniciativa del Instituto de Deberes y Derechos Humanos para acercar a personas y organizaciones a las causas de su comunidad. Tu tiempo, tus conocimientos y lo que compartes pueden hacer la diferencia.",
    "map.title": "Ilustración de una comunidad conectada por la ayuda.",
    "map.pinFood": "Conocer la causa de alimentación",
    "map.pinKids": "Conocer la causa de infancia",
    "map.pinHealth": "Conocer la causa de salud",
    "map.you": "Comunidad",
    "map.food": "Comedor",
    "map.kids": "Infancia",
    "map.health": "Salud",
    "how.eyebrow": "La ayuda, paso a paso",
    "how.title": "De una necesidad a una ayuda concreta.",
    "how.lead": "Un centro comparte lo que necesita. Alguien cerca recibe el aviso. Entre todos, la ayuda llega a su destino.",
    "how.publish.title": "El centro publica",
    "how.publish.body": "Desde su portal, el centro indica qué necesita, la cantidad y cómo recibir la ayuda. La necesidad aparece en Generosity.",
    "how.notify.title": "La comunidad se entera",
    "how.notify.body": "Con las notificaciones activadas, puedes recibir un aviso en tu teléfono y conocer una necesidad cerca de ti.",
    "how.help.title": "Tú ayudas. El centro confirma.",
    "how.help.body": "Ofreces lo que puedes aportar, aunque sea una parte. El centro coordina la entrega y confirma lo recibido para actualizar el avance.",
    "how.action": "Encuentra dónde ayudar",
    "journey.needs": "Necesidades",
    "journey.published": "Publicada por el centro",
    "journey.center": "Comedor comunitario",
    "journey.need": "Alimentos para compartir",
    "journey.description": "Buscamos 10 cajas de alimentos para preparar las comidas de esta semana.",
    "journey.goalLabel": "Meta del centro",
    "journey.goal": "10 cajas",
    "journey.unit": "Cada caja contiene 10 piezas.",
    "journey.offer": "Quiero ayudar",
    "journey.lockDate": "Miércoles, 4 de noviembre",
    "journey.now": "ahora",
    "journey.notificationTitle": "Hay una forma de ayudar cerca de ti",
    "journey.notificationBody": "Un comedor de tu comunidad necesita alimentos. Tu aportación puede hacer la diferencia.",
    "journey.notificationHint": "Una conexión que se convierte en ayuda.",
    "journey.history": "Mi aportación",
    "journey.thanks": "Tu ayuda ya llegó",
    "journey.received": "El centro confirmó la recepción.",
    "journey.contribution": "Aportaste 1 caja",
    "journey.progressLabel": "Avance de la necesidad",
    "journey.progress": "1 de 10 cajas recibidas",
    "journey.remaining": "Faltan 9 cajas. Cada aportación suma.",
    "journey.sent": "Oferta enviada",
    "journey.accepted": "Coordinada con el centro",
    "journey.delivered": "Entrega confirmada",
    "journey.historyAction": "Ver mi historial",
    "needs.title": "Hay muchas formas de hacer el bien.",
    "needs.filters": "Explorar causas",
    "needs.empty": "Elige otra causa para conocer más formas de ayudar.",
    "needs.illustration": "Estas tarjetas ilustran causas. No son necesidades publicadas.",
    "card.illustration": "Ilustración, no es el catálogo",
    "filter.all": "Todas",
    "filter.food": "Comedores",
    "filter.kids": "Infancia",
    "filter.health": "Salud",
    "card.food.cause": "Comedor comunitario",
    "card.food.title": "Alimentación que une",
    "card.food.body": "Alimentos, preparación de comidas y apoyo a comedores comunitarios. Compartir una mesa también construye comunidad.",
    "card.food.action": "Ver el catálogo publicado",
    "card.kids.cause": "Infancia y educación",
    "card.kids.title": "Oportunidades para crecer",
    "card.kids.body": "Materiales escolares, tutorías y acompañamiento educativo para fortalecer el aprendizaje y el desarrollo.",
    "card.kids.action": "Ver el catálogo publicado",
    "card.health.cause": "Salud",
    "card.health.title": "Cuidado y acompañamiento",
    "card.health.body": "Apoyo a centros de atención, insumos solicitados por sus responsables y tiempo para acompañar a quienes lo necesitan.",
    "card.health.action": "Ver el catálogo publicado",
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
    "footer.tag": "Pequeñas acciones, gran impacto. Una iniciativa del Instituto de Deberes y Derechos Humanos.",
    "footer.nav": "Pie",
    "footer.privacy": "Aviso de privacidad",
    "footer.terms": "Términos y condiciones",
    "footer.contact": "Instituto y contacto",
    "hero.explore": "Conoce las causas",
    "hero.register": "Registra tu organización",
    "donate.knowledge.title": "Conocimientos",
    "donate.knowledge.body": "Comparte tus habilidades mediante tutorías, talleres o apoyo profesional.",
    "participate.title": "Haz que tu organización sea parte.",
    "participate.body": "Cuéntanos dónde trabajan y a quiénes ayudan. Registra una sede para iniciar el vínculo con Generosity.",
    "participate.access": "Ya represento a un centro",
    "privacy.title": "Cuidamos la información que compartes.",
    "privacy.purpose.title": "Una finalidad concreta.",
    "privacy.purpose.body": "Los datos de tu solicitud de sede se utilizan para revisarla y contactarte sobre ella.",
    "privacy.access.title": "Acceso según responsabilidades.",
    "privacy.access.body": "Tu cuenta permite consultar tus solicitudes. La gestión de necesidades corresponde al personal autorizado de cada centro.",
    "privacy.care.title": "Ayudar con dignidad.",
    "privacy.care.body": "Comparte información de la organización y omite datos de personas beneficiarias, expedientes médicos y domicilios protegidos.",
    "contact.title": "Una iniciativa del Instituto.",
    "contact.lead": "Generosity es una herramienta del Instituto de Deberes y Derechos Humanos, constituido como asociación civil.",
    "contact.fiscal": "RFC y domicilio fiscal",
    "contact.team": "Personal del Instituto atiende soporte, asuntos legales y privacidad en este correo.",
    "contact.support": "Soporte",
    "contact.legal": "Asuntos legales",
    "contact.privacy": "Privacidad y datos personales",
    "discovery.eyebrow": "Cerca de ti",
    "discovery.title": "Encuentra dónde ayudar",
    "discovery.mapPrivacy": "Privacidad del mapa · OpenStreetMap ↗",
    "discovery.toList": "Ir a la lista",
    "discovery.account": "Tu cuenta",
    "discovery.locate": "Usar mi ubicación",
    "discovery.clearLocation": "Ver todos",
    "discovery.map": "Mapa de centros",
    "discovery.show": "Mostrar",
    "discovery.refresh": "Actualizar",
    "discovery.locating": "Buscando tu ubicación…",
    "discovery.located": "Centros ordenados por cercanía a tu ubicación.",
    "discovery.locationDenied": "No se permitió el acceso a tu ubicación. Puedes buscar por colonia o ciudad.",
    "discovery.locationFailed": "No pudimos obtener tu ubicación. Inténtalo de nuevo o busca una ciudad.",
    "discovery.mapFailed": "No pudimos cargar el mapa. Puedes seguir usando la lista.",
    "discovery.onMap": "Ver en el mapa",
    "discovery.details": "Ver centro",
    "discovery.needDetails": "Quiero ayudar",
    "discovery.result": "resultado",
    "discovery.results": "resultados",
    "discovery.verified": "Centro verificado",
    "discovery.yourLocation": "Tu ubicación",
    "discovery.needsAtCenter": "Necesidades del centro",
    "discovery.lead": "Conoce los centros y descubre qué necesitan.",
    "discovery.searchLabel": "Buscar centros y necesidades",
    "discovery.searchPlaceholder": "Centro, colonia o ciudad",
    "discovery.searchHint": "Busca en el directorio o usa tu ubicación para ordenar por cercanía.",
    "discovery.prevCenters": "Centros, página anterior",
    "discovery.nextCenters": "Centros, página siguiente",
    "discovery.prevNeeds": "Necesidades, página anterior",
    "discovery.nextNeeds": "Necesidades, página siguiente",
    "discovery.prev": "Anterior",
    "discovery.next": "Siguiente",
    "discovery.offerTitle": "Quiero ayudar",
    "discovery.offerNote": "Indica cuánto puedes aportar. El centro confirmará tu ofrecimiento.",
    "discovery.method": "Forma de ayudar",
    "discovery.goods": "Bienes",
    "discovery.time": "Tiempo",
    "discovery.quantity": "Cantidad",
    "discovery.minutes": "Minutos",
    "discovery.activity": "Actividad",
    "discovery.availability": "Disponibilidad (opcional)",
    "discovery.notes": "Nota (opcional)",
    "discovery.submit": "Enviar oferta",
    "discovery.retry": "Reintentar la misma oferta",
    "discovery.signIn": "Iniciar sesión para enviar",
    "discovery.historyTitle": "Tus ofertas",
    "discovery.historyNote": "Consulta el estado de tus aportaciones y coordina tu ayuda.",
    "discovery.cancelRetry": "Reintentar la misma cancelación",
    "discovery.linkLabel": "Enlace de este registro",
    "discovery.downloadNote": "El archivo incluye el enlace y la fecha de descarga. No indica lugares disponibles. No es una imagen para escanear.",
    "discovery.copy": "Copiar enlace",
    "discovery.share": "Compartir",
    "discovery.download": "Descargar invitación",
    "discovery.loading": "Cargando el catálogo publicado…",
    "discovery.unavailable": "No pudimos cargar el directorio. Conservamos tu búsqueda; pulsa Actualizar para intentarlo de nuevo.",
    "discovery.missing": "Este enlace no corresponde a un registro publicado. No se abrió otro.",
    "discovery.emptyCenters": "Aún no hay centros publicados.",
    "discovery.emptyNeeds": "Por ahora no hay necesidades publicadas.",
    "discovery.back": "Volver al mapa",
    "discovery.noMatch": "No encontramos coincidencias. Prueba con otra colonia o ciudad.",
    "discovery.centers": "Centros",
    "discovery.needs": "Necesidades",
    "discovery.open": "Ver",
    "discovery.updated": "Actualizado: ",
    "discovery.address": "Lugar publicado: ",
    "discovery.urgent": "Marcada como urgente por el centro.",
    "discovery.expires": "Publicada hasta: ",
    "discovery.expired": "La fecha publicada ya pasó. No se puede enviar una oferta desde aquí.",
    "discovery.committed": "Comprometido: ",
    "discovery.available": "Disponible para ofrecer: ",
    "discovery.received": "Recibido por el centro: ",
    "discovery.money": "El dinero no se puede aportar desde esta página.",
    "discovery.noMethods": "Esta necesidad no acepta bienes ni tiempo desde esta página.",
    "discovery.needSignIn": "Para enviar hace falta una sesión con correo verificado. La oferta no se envió.",
    "discovery.sending": "Enviando la oferta…",
    "discovery.pending": "No se pudo confirmar el resultado. Reintenta la misma oferta para no duplicarla.",
    "discovery.saved": "El servidor registró la oferta como ofrecida. Esto no confirma la recepción. Referencia: ",
    "discovery.unconfirmed": "El servidor respondió, pero no confirmó una oferta ofrecida. No se cuenta como registrada.",
    "discovery.capacity": "No hay cupo para esa cantidad. No se registró la oferta.",
    "discovery.capacityKnown": "No hay cupo para esa cantidad. Disponible según el servidor: ",
    "discovery.needUnavailable": "La necesidad no está disponible. No se registró la oferta.",
    "discovery.denied": "Tu sesión no permite enviar. La oferta no se registró.",
    "discovery.sessionDenied": "Tu sesión no permite esta acción.",
    "discovery.rejected": "El servidor rechazó la oferta. No se registró.",
    "discovery.conflict": "La misma clave no coincide con otro envío. No se duplicó.",
    "discovery.unsafe": "Este navegador no pudo guardar el envío. No se envió nada.",
    "discovery.invalid": "Revisa la cantidad, los minutos o la actividad antes de enviar.",
    "discovery.historyEmpty": "No hay ofertas en esta página.",
    "discovery.historyFailed": "No se pudieron cargar tus ofertas.",
    "discovery.historyLoading": "Cargando tus ofertas…",
    "discovery.cancel": "Cancelar oferta",
    "discovery.cancelling": "Enviando la cancelación…",
    "discovery.cancelPending": "No se pudo confirmar la cancelación. Reintenta la misma acción.",
    "discovery.cancelled": "El servidor marcó la oferta como cancelada.",
    "discovery.cancelUnconfirmed": "El servidor respondió, pero no confirmó la cancelación.",
    "discovery.cancelConflict": "Otro cambio actualizó esta oferta. Actualiza la lista antes de volver a intentar.",
    "discovery.cancelUnavailable": "Esta cancelación no está disponible. La oferta no cambió desde esta página.",
    "discovery.copied": "Enlace copiado. No se registró un envío.",
    "discovery.copyFailed": "No se pudo copiar. El enlace sigue visible. No se registró un envío.",
    "discovery.shareCancelled": "Se cerró la hoja de compartir. No se registró un envío ni una oferta.",
    "discovery.shareFailed": "No se pudo abrir la hoja de compartir. No se registró un envío.",
    "discovery.status_offered": "Ofrecida",
    "discovery.status_confirmed": "Confirmada por el centro",
    "discovery.status_fulfilled": "Marcada como realizada por el centro",
    "discovery.status_cancelled": "Cancelada",
    "discovery.status_other": "Estado no reconocido. Actualiza la lista.",
    "discovery.contact": "Contacto publicado"
  },
  "en": {
    "auth.signIn": "Sign in ↗",
    "auth.signOut": "Sign out",
    "auth.retry": "Check connection",
    "auth.loading": "Checking your session…",
    "auth.preview": "Explore the directory here. Account access opens on the published site.",
    "auth.guest": "You are browsing as a guest.",
    "auth.failed": "Could not reach the account service. You can keep browsing as a guest and retry.",
    "auth.signedIn": "Signed in: ",
    "auth.logoutFailed": "Could not sign out. Your session is still active; please retry.",
    "meta.title": "Generosity — Find where to help, near you",
    "meta.description": "Generosity is an initiative of Instituto de Deberes y Derechos Humanos connecting people, organizations and causes. Register your location and coordinate support for your community.",
    "skip": "Skip to content",
    "nav.sections": "Sections",
    "nav.how": "How it works",
    "nav.needs": "Causes",
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
    "hero.lead": "An initiative of Instituto de Deberes y Derechos Humanos that brings people and organizations closer to causes in their community. Your time, skills and contributions can make a difference.",
    "map.title": "Illustration of a community connected through helping.",
    "map.pinFood": "Explore food support",
    "map.pinKids": "Explore support for children",
    "map.pinHealth": "Explore health support",
    "map.you": "Community",
    "map.food": "Meals",
    "map.kids": "Childhood",
    "map.health": "Health",
    "how.eyebrow": "How helping happens",
    "how.title": "From a need to a helping hand.",
    "how.lead": "A center shares what it needs. Someone nearby gets the news. Together, people turn that need into real support.",
    "how.publish.title": "A center shares a need",
    "how.publish.body": "The center uses its portal to say what it needs, how much and how to help. The need appears in Generosity.",
    "how.notify.title": "The community hears about it",
    "how.notify.body": "With notifications enabled, you can receive an alert on your phone and learn about a need near you.",
    "how.help.title": "You help. The center confirms.",
    "how.help.body": "Offer what you can, even part of the total. The center coordinates delivery and confirms what it receives to update progress.",
    "how.action": "Find a place to help",
    "journey.needs": "Needs",
    "journey.published": "Published by the center",
    "journey.center": "Community kitchen",
    "journey.need": "Food to share",
    "journey.description": "We need 10 boxes of food to prepare this week’s meals.",
    "journey.goalLabel": "The center’s goal",
    "journey.goal": "10 boxes",
    "journey.unit": "Each box contains 10 items.",
    "journey.offer": "I want to help",
    "journey.lockDate": "Wednesday, November 4",
    "journey.now": "now",
    "journey.notificationTitle": "Someone nearby could use your help",
    "journey.notificationBody": "A community kitchen near you needs food. Your contribution can make a difference.",
    "journey.notificationHint": "A connection that turns into care.",
    "journey.history": "My contribution",
    "journey.thanks": "Your help has arrived",
    "journey.received": "The center confirmed your delivery.",
    "journey.contribution": "You donated 1 box",
    "journey.progressLabel": "Progress toward the goal",
    "journey.progress": "1 of 10 boxes received",
    "journey.remaining": "9 boxes to go. Every contribution counts.",
    "journey.sent": "Offer sent",
    "journey.accepted": "Arranged with the center",
    "journey.delivered": "Delivery confirmed",
    "journey.historyAction": "View my history",
    "needs.title": "There are many ways to make a difference.",
    "needs.filters": "Explore causes",
    "needs.empty": "Choose another cause to discover more ways to help.",
    "needs.illustration": "These cards illustrate causes. They are not published needs.",
    "card.illustration": "Illustration, not the published catalog",
    "filter.all": "All",
    "filter.food": "Meal centers",
    "filter.kids": "Childhood",
    "filter.health": "Health",
    "card.food.cause": "Community meal center",
    "card.food.title": "Food that brings us together",
    "card.food.body": "Food, meal preparation and support for community kitchens. Sharing a meal helps build a community.",
    "card.food.action": "View the published catalog",
    "card.kids.cause": "Childhood and education",
    "card.kids.title": "Opportunities to grow",
    "card.kids.body": "School supplies, tutoring and educational support that help children learn and develop.",
    "card.kids.action": "View the published catalog",
    "card.health.cause": "Health",
    "card.health.title": "Care and companionship",
    "card.health.body": "Support for care centers, supplies requested by their staff and time to accompany people who need it.",
    "card.health.action": "View the published catalog",
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
    "footer.tag": "Small actions, lasting impact. An initiative of Instituto de Deberes y Derechos Humanos.",
    "footer.nav": "Footer",
    "footer.privacy": "Privacy notice",
    "footer.terms": "Terms and conditions",
    "footer.contact": "Institute and contact",
    "hero.explore": "Explore the causes",
    "hero.register": "Register your organization",
    "donate.knowledge.title": "Skills",
    "donate.knowledge.body": "Share what you know through tutoring, workshops or professional support.",
    "participate.title": "Bring your organization into the community.",
    "participate.body": "Tell us where you work and who you support. Register a location to connect with Generosity.",
    "participate.access": "I already represent a center",
    "privacy.title": "We take care of the information you share.",
    "privacy.purpose.title": "A clear purpose.",
    "privacy.purpose.body": "Your location application details are used to review your request and contact you about it.",
    "privacy.access.title": "Access based on responsibilities.",
    "privacy.access.body": "Your account lets you view your applications. Only authorized center staff can manage their needs.",
    "privacy.care.title": "Help with dignity.",
    "privacy.care.body": "Share information about the organization and leave out beneficiary details, medical records and protected addresses.",
    "contact.title": "An initiative of the Institute.",
    "contact.lead": "Generosity is a tool operated by Instituto de Deberes y Derechos Humanos, a civil association.",
    "contact.fiscal": "Tax ID and registered tax address",
    "contact.team": "Institute staff handle support, legal and privacy requests at this email address.",
    "contact.support": "Support",
    "contact.legal": "Legal matters",
    "contact.privacy": "Privacy and personal data",
    "discovery.eyebrow": "Near you",
    "discovery.title": "Find a place to help",
    "discovery.mapPrivacy": "Map privacy · OpenStreetMap ↗",
    "discovery.toList": "Go to list",
    "discovery.account": "Your account",
    "discovery.locate": "Use my location",
    "discovery.clearLocation": "Show all",
    "discovery.map": "Map of centers",
    "discovery.show": "Show",
    "discovery.refresh": "Refresh",
    "discovery.locating": "Finding your location…",
    "discovery.located": "Centers sorted by distance from your location.",
    "discovery.locationDenied": "Location access was denied. You can search by neighborhood or city.",
    "discovery.locationFailed": "We couldn’t find your location. Try again or search for a city.",
    "discovery.mapFailed": "We couldn’t load the map. You can still use the list.",
    "discovery.onMap": "Show on map",
    "discovery.details": "View center",
    "discovery.needDetails": "I want to help",
    "discovery.result": "result",
    "discovery.results": "results",
    "discovery.verified": "Verified center",
    "discovery.yourLocation": "Your location",
    "discovery.needsAtCenter": "This center’s needs",
    "discovery.lead": "Explore centers and discover what they need.",
    "discovery.searchLabel": "Search centers and needs",
    "discovery.searchPlaceholder": "Center, neighborhood or city",
    "discovery.searchHint": "Search the directory or use your location to see the nearest centers first.",
    "discovery.prevCenters": "Centers, previous page",
    "discovery.nextCenters": "Centers, next page",
    "discovery.prevNeeds": "Needs, previous page",
    "discovery.nextNeeds": "Needs, next page",
    "discovery.prev": "Previous",
    "discovery.next": "Next",
    "discovery.offerTitle": "I want to help",
    "discovery.offerNote": "Tell the center how much you can contribute. They will confirm your offer.",
    "discovery.method": "Way to help",
    "discovery.goods": "Goods",
    "discovery.time": "Time",
    "discovery.quantity": "Quantity",
    "discovery.minutes": "Minutes",
    "discovery.activity": "Activity",
    "discovery.availability": "Availability (optional)",
    "discovery.notes": "Note (optional)",
    "discovery.submit": "Send offer",
    "discovery.retry": "Retry the same offer",
    "discovery.signIn": "Sign in to send",
    "discovery.historyTitle": "Your offers",
    "discovery.historyNote": "Check the status of your contributions and coordinate your help.",
    "discovery.cancelRetry": "Retry the same cancellation",
    "discovery.linkLabel": "Link for this record",
    "discovery.downloadNote": "The file includes the link and the download date. It does not show remaining places. It is not a scannable image.",
    "discovery.copy": "Copy link",
    "discovery.share": "Share",
    "discovery.download": "Download invitation",
    "discovery.loading": "Loading the published catalog…",
    "discovery.unavailable": "We couldn’t load the directory. Your search is saved; select Refresh to try again.",
    "discovery.missing": "This link is not a published record. Another record was not opened.",
    "discovery.emptyCenters": "No centers have been published yet.",
    "discovery.emptyNeeds": "There are no published needs right now.",
    "discovery.back": "Back to the map",
    "discovery.noMatch": "No matches found. Try another neighborhood or city.",
    "discovery.centers": "Centers",
    "discovery.needs": "Needs",
    "discovery.open": "View",
    "discovery.updated": "Updated: ",
    "discovery.address": "Published place: ",
    "discovery.urgent": "Marked urgent by the center.",
    "discovery.expires": "Published through: ",
    "discovery.expired": "The published date has passed. An offer cannot be sent from here.",
    "discovery.committed": "Committed: ",
    "discovery.available": "Available to offer: ",
    "discovery.received": "Received by the center: ",
    "discovery.money": "Money cannot be contributed from this page.",
    "discovery.noMethods": "This need does not accept goods or time from this page.",
    "discovery.needSignIn": "Sending requires a session with a verified email. The offer was not sent.",
    "discovery.sending": "Sending the offer…",
    "discovery.pending": "The outcome could not be confirmed. Retry the same offer so it is not duplicated.",
    "discovery.saved": "The server recorded the offer as offered. This does not confirm receipt. Reference: ",
    "discovery.unconfirmed": "The server responded, but did not confirm an offered state. It is not counted as recorded.",
    "discovery.capacity": "There is no room for that quantity. The offer was not recorded.",
    "discovery.capacityKnown": "There is no room for that quantity. Available according to the server: ",
    "discovery.needUnavailable": "The need is not available. The offer was not recorded.",
    "discovery.denied": "Your session cannot send this. The offer was not recorded.",
    "discovery.sessionDenied": "Your session cannot perform this action.",
    "discovery.rejected": "The server rejected the offer. It was not recorded.",
    "discovery.conflict": "The same key does not match another submission. It was not duplicated.",
    "discovery.unsafe": "This browser could not save the submission. Nothing was sent.",
    "discovery.invalid": "Review the quantity, minutes or activity before sending.",
    "discovery.historyEmpty": "There are no offers on this page.",
    "discovery.historyFailed": "Your offers could not be loaded.",
    "discovery.historyLoading": "Loading your offers…",
    "discovery.cancel": "Cancel offer",
    "discovery.cancelling": "Sending the cancellation…",
    "discovery.cancelPending": "The cancellation could not be confirmed. Retry the same action.",
    "discovery.cancelled": "The server marked the offer as cancelled.",
    "discovery.cancelUnconfirmed": "The server responded, but did not confirm the cancellation.",
    "discovery.cancelConflict": "Another change updated this offer. Refresh the list before trying again.",
    "discovery.cancelUnavailable": "This cancellation is not available. The offer was not changed from this page.",
    "discovery.copied": "Link copied. No send was recorded.",
    "discovery.copyFailed": "The link could not be copied. It remains visible. No send was recorded.",
    "discovery.shareCancelled": "The share sheet was closed. No send or offer was recorded.",
    "discovery.shareFailed": "The share sheet could not be opened. No send was recorded.",
    "discovery.status_offered": "Offered",
    "discovery.status_confirmed": "Confirmed by the center",
    "discovery.status_fulfilled": "Marked completed by the center",
    "discovery.status_cancelled": "Cancelled",
    "discovery.status_other": "Unrecognized state. Refresh the list.",
    "discovery.contact": "Published contact"
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
      el.href = "https://api.generosity.global/legal/" + el.getAttribute("data-legal") + "?locale=" + effective();
    });
    var meta = document.querySelector('meta[name="description"]');
    document.documentElement.lang = effective();
    document.title = document.body.dataset.page === "landing" ? t("meta.title") : t("page." + document.body.dataset.page + ".title") + " · Generosity";
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
  document.querySelector(".filters")?.addEventListener("click", function (event) {
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
    if (!destination) return;
    var dest = readDestination();
    var key = dest ? "access.dest." + dest : "access.dest.none";
    destination.textContent = t(key);
  }

  window.addEventListener("hashchange", function () {
    if (redirectLegacyRoute()) return;
    renderDestination();
    if ((location.hash || "").indexOf("#acceso") === 0) {
      document.getElementById("acceso").focus();
    }
    routeDiscovery(true);
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
    renderDiscovery();
    if (menuAnnounced) menuStatus.textContent = drawer.hidden ? t("menu.closed") : t("menu.open");
  }

  var apiOrigin = document.querySelector('meta[name="generosity-api-origin"]').content;
  var localCatalogPreview = document.querySelector('meta[name="generosity-public-api-origin"]')?.content === '/__public';
  var appChannel = document.querySelector('meta[name="generosity-channel"]').content;
  var featureAccess = {};
  function featureEnabled(name) { return featureAccess[name] === true; }
  function featureUnavailable() { return effective() === 'es' ? 'Esta función no está disponible en este momento.' : 'This feature is currently unavailable.'; }
  async function refreshFeatureAccess() {
    featureAccess = {};
    try {
      var response = await fetch(apiOrigin + (accountUser ? '/api/v1/me/features' : '/api/v1/features'), { credentials:'include', cache:'no-store', signal:AbortSignal.timeout(10000), headers:{Accept:'application/json', 'X-Generosity-Channel':appChannel} });
      if (!response.ok) return;
      var data = await response.json();
      if (data.channel === appChannel && data.features && Object.values(data.features).every(function (v) { return typeof v === 'boolean'; })) featureAccess = data.features;
    } catch (_) { /* Unavailable configuration never enables a new action. */ }
  }
  var accountUser = null;
  var accountStatus = "auth.loading";
  var accountCsrf = "";
  var accountBusy = false;
  var accountLink = document.getElementById("account-sign-in");
  var accountLogout = document.getElementById("account-sign-out");
  var accountRetry = document.getElementById("account-retry");

  function renderAccount() {
    if (!accountLink) return;
    document.getElementById("account-status").textContent = accountStatus === "auth.signedIn"
      ? t(accountStatus) + accountUser.name : t(accountStatus);
    accountLink.href = apiOrigin + "/auth/sign-in?client=web&locale=" + effective();
    accountLink.hidden = !!(accountUser && accountCsrf);
    accountRetry.hidden = localCatalogPreview;
    accountLogout.hidden = !accountUser;
    accountLogout.disabled = accountBusy || !accountCsrf;
    accountRetry.disabled = accountBusy;
    syncWorkspace();
    syncApplications();
    syncDiscovery();
  }

  async function checkAccount() {
    if (!accountLink || accountBusy) return;
    if (localCatalogPreview) { accountStatus = "auth.preview"; renderAccount(); return; }
    accountBusy = true;
    featureAccess = {};
    accountStatus = "auth.loading";
    renderAccount();
    try {
      var response = await fetch(apiOrigin + "/auth/session", { credentials: "include", cache: "no-store", signal: AbortSignal.timeout(10000), headers: { 'X-Generosity-Channel': appChannel, Accept: "application/json" } });
      if (!response.ok) throw new Error("Account unavailable");
      var data = await response.json();
      if (typeof data.csrf_token !== "string" || !data.csrf_token) throw new Error("Invalid session");
      accountCsrf = data.csrf_token;
      accountUser = data.user && typeof data.user.name === "string" ? data.user : null;
      accountStatus = accountUser ? "auth.signedIn" : "auth.guest";
    } catch (_) {
      accountStatus = "auth.failed";
      accountCsrf = "";
    } finally { await refreshFeatureAccess(); accountBusy = false; renderAccount(); }
  }

  accountRetry?.addEventListener("click", checkAccount);
  accountLogout?.addEventListener("click", async function () {
    if (accountBusy || !accountCsrf) return;
    accountBusy = true;
    renderAccount();
    try {
      var response = await fetch(apiOrigin + "/auth/logout", { method: "POST", credentials: "include", cache: "no-store", signal: AbortSignal.timeout(10000), headers: { 'X-Generosity-Channel': appChannel, Accept: "application/json", "X-CSRF-TOKEN": accountCsrf } });
      if (!response.ok) throw new Error("Logout failed");
      accountUser = null;
      featureAccess = {};
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
    es: { title:'Portal de centros', scope:'Solo puedes gestionar los centros que tienes asignados.', center:'Centro autorizado', reload:'Actualizar centros', retry:'Reintentar cambio pendiente', needs:'Necesidades del centro', contributions:'Aportaciones', prev:'Anterior', next:'Siguiente', new:'Nueva necesidad', editor:'Editar necesidad', translations:'Escribe al menos un título y una descripción en español o inglés. No incluyas datos privados de beneficiarios.', goods:'Bienes', time:'Tiempo', urgent:'Urgente', quantity:'Cantidad requerida (opcional)', unit:'Unidad (p. ej., cajas de 10 piezas)', committed:'Comprometido', received:'Recibido por el centro', remaining:'Disponible para aportar', units:'unidades', receiptUnknown:'Aún no hay datos de recepción.', partialHint:'Cada persona puede aportar una parte. Para 10 cajas de 10 piezas, escribe cantidad 10 y unidad «cajas de 10 piezas». Si aceptas piezas sueltas, usa cantidad 100 y unidad «piezas».', expires:'Último día (opcional, UTC)', status:'Estado', draft:'Borrador', published:'Publicada', closed:'Cerrada', save:'Guardar cambios', edit:'Editar', confirm:'Confirmar', fulfill:'Marcar como realizada', actual:'Minutos realizados (opcional)', offered:'Ofrecida', confirmed:'Confirmada', fulfilled:'Realizada', cancelled:'Cancelada', empty:'No hay registros.', none:'Tu cuenta no tiene centros asignados. Solicita acceso al responsable del centro.', loading:'Cargando…', saved:'El servidor confirmó el cambio.', failed:'No se pudo completar la operación. Conservamos el formulario.', pending:'Hay un envío sin confirmar. Reinténtalo antes de hacer otro cambio.', conflict:'Otro cambio modificó este registro. Conservamos tu formulario; revisa la versión actual antes de volver a editar.', denied:'Tu sesión o permiso ya no permite esta operación. Comprueba tu acceso.', invalid:'Escribe un título, una descripción y al menos una forma de ayudar.', unsafe:'No se pudo guardar el envío en este navegador. No se envió nada.' },
    en: { title:'Center workspace', scope:'You can only manage centers assigned to your account.', center:'Authorized center', reload:'Refresh centers', retry:'Retry pending change', needs:'Center needs', contributions:'Contributions', prev:'Previous', next:'Next', new:'New need', editor:'Edit need', translations:'Enter at least one title and description in Spanish or English. Do not include private beneficiary information.', goods:'Goods', time:'Time', urgent:'Urgent', quantity:'Required quantity (optional)', unit:'Unit (e.g., boxes of 10 items)', committed:'Committed', received:'Received by the center', remaining:'Available to contribute', units:'units', receiptUnknown:'Receipt data is not available yet.', partialHint:'People can contribute part of the total. For 10 boxes of 10 items, enter quantity 10 and unit “boxes of 10 items”. If you accept individual items, enter quantity 100 and unit “items”.', expires:'Last day (optional, UTC)', status:'Status', draft:'Draft', published:'Published', closed:'Closed', save:'Save changes', edit:'Edit', confirm:'Confirm', fulfill:'Mark completed', actual:'Minutes completed (optional)', offered:'Offered', confirmed:'Confirmed', fulfilled:'Completed', cancelled:'Cancelled', empty:'No records.', none:'Your account has no assigned centers. Ask the center administrator for access.', loading:'Loading…', saved:'The server confirmed the change.', failed:'Could not complete the operation. Your form is preserved.', pending:'A submission has an unknown outcome. Retry it before making another change.', conflict:'Another change updated this record. Your form is preserved; review the latest version before editing again.', denied:'Your session or permission no longer allows this operation. Check your access.', invalid:'Enter a title, a description and at least one way to help.', unsafe:'Could not save the submission in this browser. Nothing was sent.' }
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
    if (!staff || !el("center-workspace")) return;
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
    var headers = { 'X-Generosity-Channel':appChannel, Accept:'application/json' };
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
    el('need-status').querySelector('[value=published]').disabled = !featureEnabled('needs.publish') && (!staff.editing || staff.editing.status !== 'published');
    ['needs', 'contributions'].forEach(function (kind) {
      var page = kind === 'needs' ? staff.needPage : staff.contributionPage;
      var last = kind === 'needs' ? staff.needLast : staff.contributionLast;
      el(kind + '-prev').disabled = staff.busy || !!staff.pending || page <= 1;
      el(kind + '-next').disabled = staff.busy || !!staff.pending || page >= last;
    });
    el('staff-needs').replaceChildren(); el('staff-contributions').replaceChildren();
    if (!staff.needs.length) el('staff-needs').append(node('p', staffText('empty')));
    staff.needs.forEach(function (need) {
      var row = node('article', ''); row.append(node('h4', localized(need.title)), node('p', staffText(need.status)));
      if (featureEnabled('needs.progress') && need.helpMethods.includes('goods') && Number.isInteger(need.quantityRequired) && need.quantityRequired > 0 && Number.isInteger(need.quantityCommitted) && need.quantityCommitted >= 0 && need.quantityCommitted <= need.quantityRequired) {
        var format = new Intl.NumberFormat(effective());
        var unit = need.unit || staffText('units');
        var label = staffText('committed') + ': ' + format.format(need.quantityCommitted) + ' / ' + format.format(need.quantityRequired) + ' ' + unit + ' (' + format.format(Math.floor(need.quantityCommitted * 100 / need.quantityRequired)) + '%)';
        var progress = document.createElement('progress'); progress.className = 'need-progress'; progress.max = need.quantityRequired; progress.value = need.quantityCommitted; progress.setAttribute('aria-label', localized(need.title) + ' — ' + label);
        row.append(node('p', label), progress);
        row.append(node('p', Number.isInteger(need.quantityReceived) && need.quantityReceived >= 0 && need.quantityReceived <= need.quantityCommitted
          ? staffText('received') + ': ' + format.format(need.quantityReceived) + ' / ' + format.format(need.quantityRequired) + ' ' + unit : staffText('receiptUnknown')));
        row.append(node('p', staffText('remaining') + ': ' + format.format(need.quantityRequired - need.quantityCommitted) + ' (' + unit + ')'));
      }
      row.append(staffButton('edit', function () { editNeed(need); el('need-title-' + effective()).focus(); })); el('staff-needs').append(row);
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
    el('need-status').querySelector('[value=published]').disabled = !featureEnabled('needs.publish') && (!need || need.status !== 'published');
    ['es','en'].forEach(function (lang) { el('need-title-' + lang).value = need && need.title[lang] || ''; el('need-description-' + lang).value = need && need.description[lang] || ''; });
    el('need-goods').checked = !need || need.helpMethods.includes('goods'); el('need-time').checked = !!need && need.helpMethods.includes('time');
    el('need-urgent').checked = !!need && need.urgent; el('need-quantity').value = need && need.quantityRequired || ''; el('need-unit').value = need && need.unit || ''; el('need-expires').value = need && need.expiresOnIsoDate || ''; el('need-status').value = need ? need.status : 'draft';
  }
  async function mutateStaff(path, method, body) {
    if (staff.busy || staff.pending) return;
    if (path.includes('/needs') && body.status === 'published' && (!staff.editing || staff.editing.status !== 'published') && !featureEnabled('needs.publish')) { el('staff-status').textContent = featureUnavailable(); return; }
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
  if (el('center-workspace')) {
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

  }

  var applicationCopy = {
    es: {
      nav:'Registrar una sede', eyebrow:'Para organizaciones', title:'Tu organización ayuda. Hagamos que más personas la encuentren.', lead:'Solicita el registro de una sede. Si tienen varias, envía una solicitud por cada ubicación.',
      step1:'Cuéntanos quiénes son y qué apoyo ofrecen.', step2:'Envía la solicitud desde tu cuenta Generosity con correo verificado.', step3:'Recibe un folio y consulta tu solicitud pendiente de revisión.',
      private:'Los datos de esta solicitud son privados. No publiques datos de personas beneficiarias ni domicilios protegidos. El envío no publica la sede, no la verifica ni concede permisos para administrarla.', existing:'Ya tengo un centro asignado', formTitle:'Registrar una sede', required:'Los campos marcados con * son obligatorios. Puedes preparar el formulario antes de iniciar sesión; conserva esta pestaña abierta.',
      organization:'1. Tu organización', organization_name:'Nombre de la organización *', activities:'Tipo de centro y actividades *', activitiesHint:'Por ejemplo: comedor comunitario, albergue, atención de salud o capacitación. Describe a quiénes apoyan.', location:'2. La sede', site_name:'Nombre de la sede *', country:'País *', city:'Ciudad o localidad *', contact:'3. Contacto de la organización', contact_name:'Tu nombre *', contact_role:'Cargo o relación con la organización *', emailHint:'Usaremos el correo verificado de tu cuenta para contactarte sobre esta solicitud.',
      google_maps_url:'Enlace de Google Maps', latitude:'Latitud', longitude:'Longitud', locationHint:'Comparte la ficha del lugar en Google Maps. Para las coordenadas, selecciona el punto exacto de entrada y copia primero la latitud y después la longitud; no uses las del centro de la pantalla. Si no las conoces o la ubicación es protegida, deja estos datos vacíos. Si indicas coordenadas, escribe ambas.', claimed_center_id:'Identificador de un centro ya publicado (opcional)', claimHint:'Solo si pides representar un centro que ya está en el catálogo. Escribe su identificador, por ejemplo comedor-centro. Enviar la solicitud no modifica esa ficha, no la publica y no concede acceso para administrarla.',
      optional:'Agregar datos opcionales', legal_name:'Nombre legal', region:'Estado, provincia o región', postal_code:'Código postal', address:'Dirección de atención al público (omite ubicaciones protegidas)', phone:'Teléfono con código de país', website:'Sitio web oficial', representation:'Declaro que represento a esta organización. *', consent:'Autorizo que Generosity guarde estos datos para revisar la solicitud y contactarme sobre ella. *',
      signIn:'Iniciar sesión ↗', create:'Crear cuenta ↗', submit:'Enviar solicitud', retry:'Reintentar el mismo envío', history:'Mis solicitudes de sedes', refresh:'Actualizar solicitudes', guest:'Inicia sesión o crea una cuenta con correo verificado para enviar. El formulario permanecerá en esta pestaña.', verify:'Verifica el correo de tu cuenta antes de enviar la solicitud.', signed:'La solicitud se enviará desde tu cuenta: ', unavailable:'No se pudo comprobar tu sesión. Conservamos el formulario; comprueba la conexión en el acceso de cuentas.',
      invalid:'Revisa los campos marcados y confirma ambas declaraciones.', sending:'Enviando solicitud…', pending:'No pudimos confirmar el resultado. Conservamos el mismo envío para que puedas reintentarlo sin duplicarlo.', saved:'Solicitud recibida. Pendiente de revisión; la sede aún no está publicada. Folio: ', saved_claim:'Solicitud recibida. Pendiente de revisión. La referencia al centro existente quedó registrada; esa ficha no cambió y no se concedió acceso para administrarla. Folio: ', claim_unconfirmed:'Solicitud recibida como pendiente, pero el servidor no confirmó la referencia al centro existente. Esa referencia no quedó registrada. Folio: ', claim_unavailable:'No se encontró un centro publicado con ese identificador. No se envió la solicitud y no se modificó el catálogo.', denied:'Tu sesión necesita actualizarse. Inicia sesión y reintenta el mismo envío.', rejected:'El servidor rechazó el envío. Revisa los datos antes de volver a enviarlo.', unsafe:'Este navegador no pudo guardar el envío pendiente. No se envió nada.', loading:'Consultando tus solicitudes…', empty:'Todavía no has enviado solicitudes de sedes.', listFailed:'No se pudieron consultar tus solicitudes. Puedes actualizar para volver a intentarlo.', historyNote:'Esta lista muestra el estado que devuelve el servidor. No hay un plazo de respuesta publicado y esta página no envía un aviso. Retirar una solicitud conserva el folio y las notas ya registradas; no hay un plazo de conservación publicado.', status_pending:'Pendiente de revisión', status_changes_requested:'Se solicitaron cambios', status_approved:'Aprobada', status_approved_waiting:'Aprobada. El acceso de personal todavía no está activo.', status_accepted:'Acceso de personal aceptado. Esto no verifica la organización.', status_rejected:'No aceptada', status_withdrawn:'Retirada', status_other:'Estado no reconocido. Actualiza la lista.', claimed:'Centro referido: ', named_center:'Centro nombrado: ', rationale_approve:'Nota de la aprobación:', rationale_request_changes:'Cambios solicitados:', rationale_reject:'Motivo:', rationale_withdraw:'Nota al retirar:', rationale_other:'Nota registrada:', reference:'Folio: ', withdraw:'Retirar solicitud', withdraw_note:'Nota opcional', withdraw_hint:'Retirar no borra el folio ni las notas ya registradas y no revoca un acceso ya aceptado.', withdraw_confirm:'Confirmar retiro', accept:'Aceptar acceso de personal', accept_hint:'Aceptar crea solo el acceso de personal nombrado. No verifica la organización, no cambia la ficha pública y no habilita pagos.', accept_other:'La aprobación nombra a otra cuenta. Solo esa cuenta puede aceptar el acceso de personal.', accept_unavailable:'Esta aprobación no se puede aceptar desde esta página.', retry_action:'Reintentar la misma acción', acting:'Enviando…', action_pending:'No pudimos confirmar el resultado. Reintenta la misma acción para no duplicarla.', action_conflict:'Otro cambio actualizó esta solicitud. Actualiza la lista antes de volver a intentarlo.', action_denied:'Tu sesión necesita actualizarse. Inicia sesión y reintenta la misma acción.', action_unavailable:'Esta acción no está disponible en el servidor. No se cambió la solicitud.', action_center_unavailable:'El centro nombrado no está disponible. No se concedió acceso.', action_rejected:'El servidor rechazó la acción. Actualiza la lista antes de volver a intentarlo.', action_accepted:'Aceptaste el acceso de personal. Esto no verifica la organización ni habilita pagos.', action_withdrawn:'Solicitud retirada. El folio y las notas registradas se conservan.', action_unconfirmed:'El servidor respondió, pero no confirmó el cambio. Actualiza la lista.'
    },
    en: {
      nav:'Register a location', eyebrow:'For organizations', title:'Your organization helps. Let’s help more people find it.', lead:'Apply to register a location. If you have several, submit one application for each location.',
      step1:'Tell us who you are and the support you provide.', step2:'Submit through your Generosity account with a verified email.', step3:'Receive a reference number and track your application while it awaits review.',
      private:'Application details are private. Do not include beneficiary information or protected addresses. Submitting does not publish or verify the location, or grant access to manage it.', existing:'I already have an assigned center', formTitle:'Register a location', required:'Fields marked * are required. You can fill out the form before signing in; keep this tab open.',
      organization:'1. Your organization', organization_name:'Organization name *', activities:'Type of center and activities *', activitiesHint:'For example: community kitchen, shelter, health care or training. Describe the people you support.', location:'2. The location', site_name:'Location name *', country:'Country *', city:'City or town *', contact:'3. Organization contact', contact_name:'Your name *', contact_role:'Role or relationship to the organization *', emailHint:'We will use your account’s verified email to contact you about this application.',
      google_maps_url:'Google Maps link', latitude:'Latitude', longitude:'Longitude', locationHint:'Share the place listing in Google Maps. For coordinates, select the exact entrance pin and copy latitude first, then longitude; do not use the map viewport coordinates. Leave these fields blank if unknown or the location is protected. If providing coordinates, enter both.', claimed_center_id:'Identifier of an already published center (optional)', claimHint:'Only if you are asking to represent a center already in the catalog. Enter its identifier, for example community-kitchen. Submitting does not change that listing, publish it, or grant access to manage it.',
      optional:'Add optional details', legal_name:'Legal name', region:'State, province or region', postal_code:'Postal code', address:'Public service address (omit protected locations)', phone:'Phone number with country code', website:'Official website', representation:'I confirm that I represent this organization. *', consent:'I authorize Generosity to store these details to review the application and contact me about it. *',
      signIn:'Sign in ↗', create:'Create account ↗', submit:'Submit application', retry:'Retry the same submission', history:'My location applications', refresh:'Refresh applications', guest:'Sign in or create an account with a verified email to submit. Your form will remain in this tab.', verify:'Verify your account’s email before submitting.', signed:'Submitting from your account: ', unavailable:'We could not check your session. Your form is preserved; check the connection in account access.',
      invalid:'Review the marked fields and confirm both declarations.', sending:'Submitting application…', pending:'We could not confirm the outcome. The same submission is saved so you can retry without creating a duplicate.', saved:'Application received and pending review. The location is not published yet. Reference: ', saved_claim:'Application received and pending review. The existing-center reference was recorded; that listing was not changed and no access to manage it was granted. Reference: ', claim_unconfirmed:'Application received as pending, but the server did not confirm the existing-center reference. That reference was not recorded. Reference: ', claim_unavailable:'No published center was found with that identifier. The application was not submitted and the catalog was not changed.', denied:'Your session needs to be refreshed. Sign in and retry the same submission.', rejected:'The server rejected the submission. Review the details before submitting again.', unsafe:'This browser could not save the pending submission. Nothing was sent.', loading:'Loading your applications…', empty:'You have not submitted any location applications yet.', listFailed:'Could not load your applications. Refresh to try again.', historyNote:'This list shows the state returned by the server. No response time is published, and this page does not send a notice. Withdrawing an application keeps the reference and notes already recorded; no retention period is published.', status_pending:'Pending review', status_changes_requested:'Changes requested', status_approved:'Approved', status_approved_waiting:'Approved. Staff access is not active yet.', status_accepted:'Staff access accepted. This does not verify the organization.', status_rejected:'Not accepted', status_withdrawn:'Withdrawn', status_other:'Unrecognized state. Refresh the list.', claimed:'Referred center: ', named_center:'Named center: ', rationale_approve:'Approval note:', rationale_request_changes:'Changes requested:', rationale_reject:'Reason:', rationale_withdraw:'Withdrawal note:', rationale_other:'Recorded note:', reference:'Reference: ', withdraw:'Withdraw application', withdraw_note:'Optional note', withdraw_hint:'Withdrawing does not delete the reference or notes already recorded, and does not revoke access that was already accepted.', withdraw_confirm:'Confirm withdrawal', accept:'Accept staff access', accept_hint:'Accepting creates only the named staff access. It does not verify the organization, change the public listing, or enable payments.', accept_other:'The approval names another account. Only that account can accept staff access.', accept_unavailable:'This approval cannot be accepted from this page.', retry_action:'Retry the same action', acting:'Sending…', action_pending:'We could not confirm the outcome. Retry the same action so it is not duplicated.', action_conflict:'Another change updated this application. Refresh the list before trying again.', action_denied:'Your session needs to be refreshed. Sign in and retry the same action.', action_unavailable:'This action is not available on the server. The application was not changed.', action_center_unavailable:'The named center is not available. No access was granted.', action_rejected:'The server rejected the action. Refresh the list before trying again.', action_accepted:'You accepted staff access. This does not verify the organization or enable payments.', action_withdrawn:'Application withdrawn. The reference and recorded notes are kept.', action_unconfirmed:'The server responded, but did not confirm the change. Refresh the list.'
    }
  };
  ['es','en'].forEach(function (lang) { Object.keys(applicationCopy[lang]).forEach(function (key) { copy[lang]['application.' + key] = applicationCopy[lang][key]; }); });
  var application = { owner:null, pending:null, busy:false, loading:false, page:1, last:1, items:[], status:'', reference:'', listStatus:'', action:null, actionBusy:false, actionStatus:'', notes:{}, generation:0 };
  var registrationForm = el('site-registration');
  function applicationText(key) { return key ? t('application.' + key) : ''; }
  function applicationStorage(owner) { return 'generosity.application.pending.' + owner; }
  function applicationActionStorage(owner) { return 'generosity.application.action.' + owner; }
  function applicationId(value) { return typeof value === 'string' && /^[0-9a-f-]{36}$/.test(value); }
  function applicationVersion(item) { return !!item && applicationId(item.id) && Number.isInteger(item.version) && item.version >= 1; }
  function sameAccount(item) { return !!(accountUser && item && /^[0-9]+$/.test(String(item.account_id)) && String(item.account_id) === String(accountUser.id)); }
  function staffGrant(item) { return !!(item && Array.isArray(item.roles) && item.roles.length === 1 && item.roles[0] === 'staff'); }
  function canWithdraw(item) {
    if (!applicationVersion(item) || item.accepted_at) return false;
    if (item.status === 'pending' || item.status === 'changes_requested') return true;
    return item.status === 'approved' && item.acceptance_required === true;
  }
  function canAccept(item) {
    return applicationVersion(item) && item.status === 'approved' && item.acceptance_required === true && !item.accepted_at && staffGrant(item) && sameAccount(item);
  }
  function statusLabel(item) {
    if (item.status === 'approved' && item.accepted_at) return 'status_accepted';
    if (item.status === 'approved' && item.acceptance_required === true) return 'status_approved_waiting';
    return ['pending', 'changes_requested', 'approved', 'rejected', 'withdrawn'].indexOf(item.status) >= 0 ? 'status_' + item.status : 'status_other';
  }
  function applicationReady() { return accountStatus === 'auth.signedIn' && accountCsrf && accountUser && accountUser.email_verified_at; }
  function syncApplications() {
    if (!application || !registrationForm) return;
    var owner = accountUser && String(accountUser.id);
    if (application.owner !== owner) {
      if (application.owner) {
        try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* Still clear another account's private view. */ }
        try { sessionStorage.removeItem(applicationActionStorage(application.owner)); } catch (_) { /* A late action must not follow the next account. */ }
        registrationForm.reset();
      }
      application = { owner:owner, pending:null, busy:false, loading:false, page:1, last:1, items:[], status:'', reference:'', listStatus:'', action:null, actionBusy:false, actionStatus:'', notes:{}, generation:application.generation + 1 };
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
        try {
          var savedAction = JSON.parse(sessionStorage.getItem(applicationActionStorage(owner)) || 'null');
          if (savedAction && savedAction.owner === owner && /^[0-9a-f-]{36}$/.test(savedAction.key) && applicationId(savedAction.id) && (savedAction.kind === 'withdraw' || savedAction.kind === 'accept') && Number.isInteger(savedAction.version) && savedAction.version >= 1 && (savedAction.rationale === undefined || typeof savedAction.rationale === 'string')) {
            application.action = savedAction; application.actionStatus = 'action_pending';
          }
        } catch (_) { application.actionStatus = 'unsafe'; }
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
    var showsReference = application.status === 'saved' || application.status === 'saved_claim' || application.status === 'claim_unconfirmed';
    el('registration-result').textContent = applicationText(application.status) + (showsReference ? application.reference : '');
    el('registration-history').hidden = !ready;
    el('registration-history-status').textContent = applicationText(application.listStatus);
    el('registration-action-status').textContent = applicationText(application.actionStatus);
    el('registration-action-retry').hidden = !application.action;
    el('registration-action-retry').disabled = !application.action || application.actionBusy || !ready;
    el('registration-refresh').disabled = application.loading || !ready;
    el('registration-prev').disabled = application.loading || application.page <= 1;
    el('registration-next').disabled = application.loading || application.page >= application.last;
    el('registration-list').replaceChildren();
    application.items.forEach(function (item) {
      var organization = typeof item.organization_name === 'string' ? item.organization_name : '';
      var site = typeof item.site_name === 'string' ? item.site_name : '';
      var row = node('li', organization + ' · ' + site);
      row.append(node('p', applicationText(statusLabel(item))));
      if (typeof item.claimed_center_id === 'string' && item.claimed_center_id) row.append(node('p', applicationText('claimed') + item.claimed_center_id));
      if (item.status === 'approved' && typeof item.center_id === 'string' && item.center_id) row.append(node('p', applicationText('named_center') + item.center_id));
      var rationale = item.decision && typeof item.decision.rationale === 'string' ? item.decision.rationale.trim() : '';
      if (rationale) {
        var actionName = item.decision && item.decision.action;
        var rationaleKey = ['approve', 'request_changes', 'reject', 'withdraw'].indexOf(actionName) >= 0 ? 'rationale_' + actionName : 'rationale_other';
        row.append(node('p', applicationText(rationaleKey) + ' ' + rationale));
      }
      if (typeof item.id === 'string') row.append(node('small', applicationText('reference') + item.id));
      if (!application.action && item.status === 'approved' && item.acceptance_required === true && !item.accepted_at) {
        if (canAccept(item)) row.append(acceptControl(item));
        else row.append(node('p', applicationText(/^[0-9]+$/.test(String(item.account_id || '')) && !sameAccount(item) ? 'accept_other' : 'accept_unavailable')));
      }
      if (!application.action && canWithdraw(item)) row.append(withdrawControl(item));
      el('registration-list').append(row);
    });
  }
  function acceptControl(item) {
    var box = node('div', ''); box.className = 'registration-action';
    box.append(node('p', applicationText('accept_hint')));
    var button = node('button', applicationText('accept'));
    button.type = 'button'; button.className = 'solid';
    button.disabled = application.actionBusy || application.loading || !applicationReady();
    button.addEventListener('click', function () { beginApplicationAction('accept', item); });
    box.append(button);
    return box;
  }
  function withdrawControl(item) {
    var box = node('div', ''); box.className = 'registration-action';
    var details = document.createElement('details');
    var summary = document.createElement('summary'); summary.textContent = applicationText('withdraw'); details.append(summary);
    var label = node('label', applicationText('withdraw_note'));
    var note = document.createElement('textarea'); note.maxLength = 2000; note.rows = 2; note.value = application.notes[item.id] || '';
    note.addEventListener('input', function () { application.notes[item.id] = note.value; });
    label.append(note); details.append(label);
    details.append(node('p', applicationText('withdraw_hint')));
    var button = node('button', applicationText('withdraw_confirm'));
    button.type = 'button'; button.className = 'ghost';
    button.disabled = application.actionBusy || application.loading || !applicationReady();
    button.addEventListener('click', function () { beginApplicationAction('withdraw', item, note.value.trim()); });
    details.append(button); box.append(details);
    return box;
  }
  function clearApplicationAction() {
    if (application.owner) { try { sessionStorage.removeItem(applicationActionStorage(application.owner)); } catch (_) { /* A rejected action must not stay retryable. */ } }
    application.action = null;
  }
  function beginApplicationAction(kind, item, rationale) {
    if (application.action || application.actionBusy || !applicationReady() || !applicationVersion(item)) return;
    if (kind === 'accept' && !canAccept(item)) return;
    if (kind === 'withdraw' && !canWithdraw(item)) return;
    var action = { owner:application.owner, id:item.id, kind:kind, version:item.version };
    if (kind === 'withdraw' && rationale) action.rationale = rationale.slice(0, 2000);
    try {
      action.key = crypto.randomUUID();
      sessionStorage.setItem(applicationActionStorage(application.owner), JSON.stringify(action));
    } catch (_) { application.actionStatus = 'unsafe'; renderApplication(); return; }
    application.action = action;
    sendApplicationAction(action);
  }
  async function sendApplicationAction(action) {
    if (!action || action.owner !== application.owner || !applicationReady() || application.actionBusy) return;
    var generation = application.generation;
    application.actionBusy = true; application.actionStatus = 'acting'; renderApplication();
    try {
      var body = { version:action.version };
      if (action.kind === 'withdraw' && action.rationale) body.rationale = action.rationale;
      var response = await fetch(apiOrigin + '/api/v1/center-applications/' + encodeURIComponent(action.id) + (action.kind === 'accept' ? '/acceptance' : '/withdrawal'), { method:'POST', credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:{'X-Generosity-Channel':appChannel, Accept:'application/json', 'Content-Type':'application/json', 'X-CSRF-TOKEN':accountCsrf, 'Idempotency-Key':action.key}, body:JSON.stringify(body) });
      var data = {};
      try { data = await response.json(); } catch (_) { data = {}; }
      if (generation !== application.generation) return;
      if (!response.ok) {
        if ([400,404,409,422].includes(response.status)) {
          clearApplicationAction();
          var code = data.error && data.error.code;
          application.actionStatus = response.status === 404 ? 'action_unavailable' : code === 'center_unavailable' ? 'action_center_unavailable' : code === 'version_conflict' || code === 'invalid_transition' || response.status === 409 ? 'action_conflict' : 'action_rejected';
        } else application.actionStatus = [401,403,419].includes(response.status) ? 'action_denied' : 'action_pending';
        return;
      }
      if (!data.application || data.application.id !== action.id) throw new Error('Invalid receipt');
      clearApplicationAction();
      var confirmed = action.kind === 'withdraw' ? data.application.status === 'withdrawn' : !!(data.application.accepted_at || data.application.acceptance_required === false);
      application.actionStatus = confirmed ? (action.kind === 'withdraw' ? 'action_withdrawn' : 'action_accepted') : 'action_unconfirmed';
      var index = application.items.findIndex(function (item) { return item.id === data.application.id; });
      if (index >= 0) application.items[index] = data.application;
      await loadApplications();
    } catch (_) { if (generation === application.generation) application.actionStatus = 'action_pending'; }
    finally {
      if (generation === application.generation) {
        application.actionBusy = false; renderApplication();
        if (application.actionStatus && application.actionStatus !== 'acting') el('registration-action-status').focus();
      }
    }
  }
  async function loadApplications() {
    if (!applicationReady() || application.loading) return;
    var generation = application.generation;
    application.loading = true; application.listStatus = 'loading'; renderApplication();
    try {
      var response = await fetch(apiOrigin + '/api/v1/me/center-applications?page=' + application.page, { credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:{'X-Generosity-Channel':appChannel, Accept:'application/json'} });
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
  if (registrationForm) {
  registrationForm.addEventListener('submit', async function (event) {
    event.preventDefault();
    if (!applicationReady() || application.busy || accountBusy) return;
    clearApplicationErrors();
    if (!application.pending) {
      var invalid = Array.from(registrationForm.elements).filter(function (field) { return field.willValidate && !field.checkValidity(); });
      var latitude = registrationForm.elements.namedItem('latitude'), longitude = registrationForm.elements.namedItem('longitude');
      if (!!latitude.value !== !!longitude.value) invalid.push(latitude.value ? longitude : latitude);
      var claim = registrationForm.elements.namedItem('claimed_center_id');
      if (claim.value.trim() && !/^[a-z0-9][a-z0-9-]*$/.test(claim.value.trim())) invalid.push(claim);
      if (invalid.length) { applicationErrors(invalid.map(function (field) { return field.name; })); return; }
      var body = {};
      Array.from(registrationForm.elements).forEach(function (field) {
        if (!field.name || (field.name === 'claimed_center_id' && !field.value.trim())) return;
        body[field.name] = field.type === 'checkbox' ? field.checked : field.value.trim() || null;
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
      var response = await fetch(apiOrigin + '/api/v1/center-applications', { method:'POST', credentials:'include', cache:'no-store', signal:AbortSignal.timeout(15000), headers:{'X-Generosity-Channel':appChannel, Accept:'application/json', 'Content-Type':'application/json', 'X-CSRF-TOKEN':accountCsrf, 'Idempotency-Key':submission.key}, body:JSON.stringify(submission.body) });
      var data = await response.json();
      if (generation !== application.generation) return;
      if (!response.ok) {
        if ([400,404,409,422].includes(response.status)) {
          try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* A rejected replay remains safe if storage is unavailable. */ }
          application.pending = null;
          var code = data.error && data.error.code;
          application.status = code === 'center_unavailable' ? 'claim_unavailable' : 'rejected';
          application.busy = false;
          renderApplication();
          if (response.status === 422) applicationErrors(Object.keys(data.error && data.error.details || {}));
          else if (code === 'center_unavailable') applicationErrors(['claimed_center_id']);
        } else application.status = [401,403,419].includes(response.status) ? 'denied' : 'pending';
        return;
      }
      if (!data.application || typeof data.application.id !== 'string' || data.application.status !== 'pending') throw new Error('Invalid receipt');
      try { sessionStorage.removeItem(applicationStorage(application.owner)); } catch (_) { /* Replaying an acknowledged key cannot create another application. */ }
      var claimed = submission.body.claimed_center_id;
      application.pending = null; application.reference = data.application.id;
      application.status = claimed && data.application.claimed_center_id !== claimed ? 'claim_unconfirmed' : claimed ? 'saved_claim' : 'saved';
      registrationForm.reset(); application.page = 1;
      el('registration-result').focus();
      await loadApplications();
    } catch (_) { if (generation === application.generation) application.status = 'pending'; }
    finally { if (generation === application.generation) { application.busy = false; renderApplication(); } }
  });
  el('registration-refresh').addEventListener('click', loadApplications);
  el('registration-action-retry').addEventListener('click', function () { sendApplicationAction(application.action); });
  ['prev','next'].forEach(function (direction) { el('registration-' + direction).addEventListener('click', function () { application.page += direction === 'next' ? 1 : -1; loadApplications(); }); });

  }

  var discovery = {
    view: 'centers', origin: null, locationStatus: '', locating: false, selected: '',
    generation: 0, busy: false, status: '', centers: [], needs: [], centerPage: 1, needPage: 1, centerLast: 1, needLast: 1,
    record: null, relatedNeeds: null, owner: null, offerStatus: '', offerBusy: false, offerPending: null, offerGeneration: 0,
    history: [], historyPage: 1, historyLast: 1, historyStatus: '', historyBusy: false, historyGeneration: 0, cancelPending: null, cancelGeneration: 0, shareStatus: ''
  };

  function discoveryRoute() {
    if (!el('descubrir')) return null;
    var hash = location.hash || '#descubrir';
    if (['#descubrir', '#contenido', '#acceso'].includes(hash)) return { view: 'list' };
    var center = hash.match(/^#centro\/([a-z0-9][a-z0-9-]{0,99})$/);
    if (center) return { view: 'center', id: center[1] };
    var need = hash.match(/^#necesidad\/([A-Za-z0-9][A-Za-z0-9-]{0,99})$/);
    if (need) return { view: 'need', id: need[1] };
    if (hash.indexOf('#centro/') === 0 || hash.indexOf('#necesidad/') === 0) return { view: 'invalid' };
    return null;
  }

  function recordUrl(kind, id) {
    return location.origin + location.pathname + location.search + (kind === 'center' ? '#centro/' : '#necesidad/') + id;
  }

  function publicText(value) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return '';
    var text = localized(value);
    return typeof text === 'string' ? text : '';
  }

  function isoStamp(value) {
    return typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value) ? value.slice(0, 16) : '';
  }

  function isoDate(value) {
    return typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value) ? value : '';
  }

  function utcToday() { return new Date().toISOString().slice(0, 10); }

  function httpsUrl(value) {
    if (typeof value !== 'string' || value.length > 500 || value.indexOf('@') !== -1) return '';
    try {
      var url = new URL(value);
      if (url.protocol !== 'https:' || url.username || url.password || url.hostname.indexOf('.') === -1) return '';
      return url.href;
    } catch (_) { return ''; }
  }

  function publicEmail(value) {
    return typeof value === 'string' && value.length <= 200 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? value : '';
  }

  function publicPhone(value) {
    return typeof value === 'string' && /^\+?[0-9][0-9 ()-]{5,18}$/.test(value) ? value : '';
  }

  function slugId(value) { return typeof value === 'string' && /^[a-z0-9][a-z0-9-]{0,99}$/.test(value); }
  function publicId(value) { return typeof value === 'string' && /^[A-Za-z0-9][A-Za-z0-9-]{0,99}$/.test(value); }
  function uuid(value) { return typeof value === 'string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value); }

  function pageCount(data) {
    var last = data && data.last_page;
    return Number.isInteger(last) && last >= 1 ? last : 1;
  }

  function searchText() { return normalizedSearch(el('discovery-query').value); }

  function normalizedSearch(text) { return String(text || '').trim().toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, ''); }

  function matchesQuery(text) {
    var query = searchText();
    return !query || normalizedSearch(text).indexOf(query) !== -1;
  }

  function verifiedUser() {
    return !!(accountUser && accountCsrf && typeof accountUser.email_verified_at === 'string' && accountUser.email_verified_at);
  }

  function selectedNeed() {
    return discovery.record && discovery.record.kind === 'need' ? discovery.record.data : null;
  }

  function needMethods(need) {
    var methods = need && Array.isArray(need.helpMethods) ? need.helpMethods : [];
    return { goods: methods.indexOf('goods') !== -1, time: methods.indexOf('time') !== -1, money: methods.indexOf('money') !== -1 };
  }

  function needExpired(need) {
    var date = isoDate(need && need.expiresOnIsoDate);
    return !!(date && date < utcToday());
  }

  function selectedMethod() {
    var chosen = document.querySelector('input[name="discovery-method"]:checked');
    return chosen ? chosen.value : '';
  }

  function setField(id, show) {
    var field = el(id);
    var label = document.querySelector('label[for="' + id + '"]');
    if (!field) return;
    field.hidden = !show;
    field.disabled = !show || discovery.offerBusy || !!discovery.offerPending;
    if (label) label.hidden = !show;
  }

  function intentKey() { return 'generosity.discovery.intent'; }
  function offerKey() { return 'generosity.offer.pending.' + discovery.owner; }
  function cancelKey() { return 'generosity.offer.cancel.' + discovery.owner; }

  function readJson(key) {
    try { return JSON.parse(sessionStorage.getItem(key) || 'null'); } catch (_) { return null; }
  }

  function clearOfferPrivate() {
    discovery.offerGeneration += 1;
    discovery.cancelGeneration += 1;
    discovery.historyGeneration += 1;
    discovery.offerBusy = false;
    discovery.historyBusy = false;
    try {
      sessionStorage.removeItem(intentKey());
      if (discovery.owner) {
        sessionStorage.removeItem(offerKey());
        sessionStorage.removeItem(cancelKey());
      }
    } catch (_) { /* Private fields are still cleared in the page. */ }
    discovery.offerPending = null;
    discovery.cancelPending = null;
    discovery.offerStatus = '';
    discovery.history = [];
    discovery.historyStatus = '';
    ['discovery-quantity', 'discovery-minutes', 'discovery-activity', 'discovery-availability', 'discovery-notes'].forEach(function (id) {
      if (el(id)) el(id).value = '';
    });
  }

  function saveIntent() {
    var need = selectedNeed();
    if (!need || !publicId(need.id)) return;
    try {
      sessionStorage.setItem(intentKey(), JSON.stringify({
        needId: need.id, method: selectedMethod(),
        quantity: el('discovery-quantity').value, minutes: el('discovery-minutes').value,
        activity: el('discovery-activity').value, availability: el('discovery-availability').value,
        notes: el('discovery-notes').value
      }));
    } catch (_) { /* The form remains in this view if storage is blocked. */ }
  }

  function restoreIntent(id) {
    var saved = readJson(intentKey());
    if (!saved || saved.needId !== id) return;
    if (saved.method === 'goods' || saved.method === 'time') {
      var radio = document.querySelector('input[name="discovery-method"][value="' + saved.method + '"]');
      if (radio && !radio.disabled) radio.checked = true;
    }
    ['quantity', 'minutes', 'activity', 'availability', 'notes'].forEach(function (name) {
      if (typeof saved[name] === 'string') el('discovery-' + name).value = saved[name];
    });
  }

  function loadStoredPending() {
    if (!discovery.owner) return;
    var offer = readJson(offerKey());
    discovery.offerPending = offer && offer.owner === discovery.owner && uuid(offer.key) && offer.body ? offer : null;
    var cancel = readJson(cancelKey());
    discovery.cancelPending = cancel && cancel.owner === discovery.owner && uuid(cancel.key) && uuid(cancel.id) ? cancel : null;
  }

  function syncDiscovery() {
    if (!el('descubrir')) return;
    var owner = accountUser && accountUser.id != null ? String(accountUser.id) : null;
    if (discovery.owner && discovery.owner !== owner) clearOfferPrivate();
    var arrived = !discovery.owner && owner;
    discovery.owner = owner;
    if (owner && arrived) loadStoredPending();
    if (discoveryRoute() && verifiedUser()) loadHistory();
    renderDiscovery();
  }

  function routeDiscovery(focus) {
    var route = discoveryRoute();
    if (!route) return;
    if (focus && el('descubrir')) el('descubrir').focus();
    if (route.view === 'invalid') {
      discovery.record = null;
      discovery.status = 'missing';
      renderDiscovery();
      return;
    }
    if (route.view === 'list') loadCatalog();
    else loadRecord(route.view === 'center' ? 'center' : 'need', route.id);
    if (verifiedUser()) loadHistory();
  }

  function consumeIntentHash() {
    if (!el('descubrir') || location.hash) return;
    var saved = readJson(intentKey());
    if (saved && publicId(saved.needId)) location.hash = '#necesidad/' + saved.needId;
  }

  async function publicGet(path) {
    var publicOrigin = document.querySelector('meta[name="generosity-public-api-origin"]')?.content || apiOrigin;
    var response = await fetch(publicOrigin + path, { credentials: 'omit', cache: 'no-store', signal: AbortSignal.timeout(15000), headers: { Accept: 'application/json' } });
    var data = null;
    try { data = await response.json(); } catch (_) { data = null; }
    return { ok: response.ok, status: response.status, data: data };
  }

  function readCatalog(data) {
    var catalog = data && data.catalog;
    if (!catalog || catalog.schemaVersion !== 1 || !Array.isArray(catalog.centers)) throw new Error('Invalid catalog');
    // The shared API bounds this public snapshot to 2,000 centers / 10,000 needs.
    var centers = catalog.centers.filter(function (center) { return center && slugId(center.id) && publicText(center.name); });
    var needs = centers.flatMap(function (center) {
      return (Array.isArray(center.needs) ? center.needs : []).filter(function (need) {
        return need && publicId(need.id) && need.centerId === center.id && ['active', 'covered', 'expired'].includes(need.status) && publicText(need.title);
      }).map(function (need) { return Object.assign({}, need, { status: 'published' }); });
    });
    return { centers: centers, needs: needs };
  }

  async function loadCatalog() {
    var generation = ++discovery.generation;
    discovery.busy = true;
    discovery.status = 'loading';
    discovery.record = null;
    renderDiscovery();
    try {
      var result = await publicGet('/api/v1/sync/catalog?category_version=2&progress_version=1');
      if (generation !== discovery.generation) return;
      if (!result.ok) throw new Error('Catalog unavailable');
      var catalog = readCatalog(result.data);
      discovery.centers = catalog.centers; discovery.needs = catalog.needs;
      discovery.status = '';
    } catch (_) {
      if (generation !== discovery.generation) return;
      discovery.centers = [];
      discovery.needs = [];
      discovery.status = 'unavailable';
    } finally {
      if (generation === discovery.generation) { discovery.busy = false; renderDiscovery(); }
    }
  }

  function acceptableRecord(kind, data, id) {
    if (!data || data.id !== id) return false;
    if (kind === 'need') return data.status === 'published' && publicText(data.title);
    return publicText(data.name);
  }

  async function loadRecord(kind, id) {
    var generation = ++discovery.generation;
    discovery.busy = true;
    discovery.record = null;
    discovery.relatedNeeds = null;
    discovery.status = 'loading';
    renderDiscovery();
    var path = kind === 'center' ? '/api/v1/centers/' + encodeURIComponent(id) : '/api/v1/needs/' + encodeURIComponent(id);
    try {
      var result = await publicGet(path);
      if (generation !== discovery.generation) return;
      if (result.status === 404) discovery.status = 'missing';
      else if (!result.ok || !acceptableRecord(kind, result.data, id)) discovery.status = 'unavailable';
      else {
        if (kind === 'center') {
          var relatedNeeds = null;
          try {
            var snapshot = await publicGet('/api/v1/sync/catalog?category_version=2&progress_version=1');
            if (snapshot.ok) relatedNeeds = readCatalog(snapshot.data).needs.filter(function (need) { return need.centerId === id; });
          } catch (_) { /* Center details remain usable if the needs request fails. */ }
          if (generation !== discovery.generation) return;
          discovery.relatedNeeds = relatedNeeds;
        }
        discovery.record = { kind: kind, data: result.data };
        discovery.status = '';
        if (kind === 'need') restoreIntent(id);
      }
    } catch (_) {
      if (generation !== discovery.generation) return;
      discovery.status = 'unavailable';
    } finally {
      if (generation === discovery.generation) { discovery.busy = false; renderDiscovery(); }
    }
  }

  function appendText(parent, tag, text) {
    if (!text) return;
    parent.append(node(tag, text));
  }

  function contactLines(contact) {
    if (!contact || typeof contact !== 'object') return [];
    var lines = [];
    var phone = publicPhone(contact.phone);
    var email = publicEmail(contact.email);
    var site = httpsUrl(contact.website);
    if (phone) lines.push(phone);
    if (email) lines.push(email);
    if (site) lines.push(site);
    return lines;
  }

  function quantityLine(need) {
    if (!Number.isInteger(need.quantityRequired) || need.quantityRequired < 1 || !Number.isInteger(need.quantityCommitted) || need.quantityCommitted < 0 || need.quantityCommitted > need.quantityRequired) return '';
    var format = new Intl.NumberFormat(effective());
    var unit = typeof need.unit === 'string' && need.unit.length <= 60 ? need.unit : '';
    return t('discovery.committed') + format.format(need.quantityCommitted) + ' / ' + format.format(need.quantityRequired) + (unit ? ' ' + unit : '') + '. ' + t('discovery.available') + format.format(need.quantityRequired - need.quantityCommitted);
  }

  function renderRecord(parent, kind, data) {
    var title = kind === 'center' ? publicText(data.name) : publicText(data.title);
    var heading = node('h3', title);
    parent.append(heading);
    appendText(parent, 'p', kind === 'center' ? publicText(data.description) : publicText(data.description));
    if (kind === 'center') {
      appendText(parent, 'p', publicText(data.address) ? t('discovery.address') + publicText(data.address) : '');
      if (Array.isArray(data.categories)) {
        var categories = data.categories.filter(function (item) { return typeof item === 'string' && item.length <= 80; }).slice(0, 12);
        appendText(parent, 'p', categories.join(', '));
      }
      contactLines(data.contact).forEach(function (line) { appendText(parent, 'p', t('discovery.contact') + ': ' + line); });
      appendText(parent, 'h4', t('discovery.needsAtCenter'));
      if (discovery.relatedNeeds === null) appendText(parent, 'p', t('discovery.unavailable'));
      else if (!discovery.relatedNeeds.length) appendText(parent, 'p', t('discovery.emptyNeeds'));
      (discovery.relatedNeeds || []).forEach(function (need) {
        var link = node('a', publicText(need.title)); link.className = 'related-need'; link.href = '#necesidad/' + need.id; parent.append(link);
      });
    } else {
      var methods = needMethods(data);
      if (data.urgent === true) appendText(parent, 'p', t('discovery.urgent'));
      var expires = isoDate(data.expiresOnIsoDate);
      if (expires) appendText(parent, 'p', t('discovery.expires') + expires);
      if (needExpired(data)) appendText(parent, 'p', t('discovery.expired'));
      appendText(parent, 'p', quantityLine(data));
      if (Number.isInteger(data.quantityReceived) && data.quantityReceived >= 0) appendText(parent, 'p', t('discovery.received') + new Intl.NumberFormat(effective()).format(data.quantityReceived));
      if (methods.money) appendText(parent, 'p', t('discovery.money'));
      if (!methods.goods && !methods.time) appendText(parent, 'p', t('discovery.noMethods'));
      if (slugId(data.centerId)) {
        var centerLink = node('a', data.centerId);
        centerLink.href = '#centro/' + data.centerId;
        parent.append(centerLink);
      }
    }
    var updated = isoStamp(data.updatedAt);
    appendText(parent, 'p', updated ? t('discovery.updated') + updated : '');
  }

  var discoveryMap = null, mapMarkers = {}, mapSignature = '', originMarker = null;

  function centerCoordinates(center) {
    var point = center && center.coordinates;
    if (!point || !Number.isFinite(point.latitude) || !Number.isFinite(point.longitude) || Math.abs(point.latitude) > 90 || Math.abs(point.longitude) > 180) return null;
    return [point.latitude, point.longitude];
  }

  function centerDistance(center) {
    var point = centerCoordinates(center);
    if (!point || !discovery.origin) return Infinity;
    var rad = Math.PI / 180;
    var a = Math.sin((point[0] - discovery.origin[0]) * rad / 2) ** 2 + Math.cos(point[0] * rad) * Math.cos(discovery.origin[0] * rad) * Math.sin((point[1] - discovery.origin[1]) * rad / 2) ** 2;
    return 6371 * 2 * Math.asin(Math.sqrt(Math.min(1, a)));
  }

  function filteredCenters() {
    var centers = discovery.centers.filter(function (center) {
      return matchesQuery(publicText(center.name) + ' ' + publicText(center.address) + ' ' + publicText(center.description));
    });
    if (discovery.origin) centers.sort(function (a, b) { return centerDistance(a) - centerDistance(b); });
    return centers;
  }

  function selectMapCenter(center, fromCard) {
    if (!discoveryMap || !mapMarkers[center.id]) return;
    discovery.selected = center.id;
    if (discovery.view === 'centers') {
      var index = filteredCenters().findIndex(function (row) { return row.id === center.id; });
      if (index >= 0) discovery.centerPage = Math.floor(index / 12) + 1;
    }
    renderDiscovery();
    discoveryMap.setView(centerCoordinates(center), Math.max(discoveryMap.getZoom(), 14));
    mapMarkers[center.id].openPopup();
    if (fromCard) el('discovery-map').scrollIntoView({ block: 'nearest' });
  }

  function renderDiscoveryMap(centers) {
    if (!window.L) { el('discovery-map-status').textContent = t('discovery.mapFailed'); return; }
    if (!discoveryMap) {
      discoveryMap = L.map('discovery-map', { scrollWheelZoom: false }).setView([23, -102], 5);
      // Public OSM tiles: visible attribution, normal browser cache/referrer, no prefetch.
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).on('tileerror', function () { el('discovery-map-status').textContent = t('discovery.mapFailed'); })
        .on('tileload', function () { el('discovery-map-status').textContent = ''; }).addTo(discoveryMap);
      new ResizeObserver(function () { discoveryMap.invalidateSize(); }).observe(el('discovery-map'));
    }
    var points = centers.filter(function (center) { return centerCoordinates(center); });
    var signature = effective() + JSON.stringify(points.map(function (center) { return [center.id, publicText(center.name), centerCoordinates(center)]; })) + JSON.stringify(discovery.origin);
    if (signature === mapSignature) return;
    mapSignature = signature;
    Object.values(mapMarkers).forEach(function (marker) { marker.remove(); });
    mapMarkers = {};
    points.forEach(function (center) {
      var popup = node('div', ''); popup.className = 'map-center-popup';
      popup.append(node('strong', publicText(center.name)));
      var link = node('a', t('discovery.details')); link.href = '#centro/' + center.id; popup.append(link);
      var marker = L.marker(centerCoordinates(center), {
        title: publicText(center.name), alt: publicText(center.name),
        icon: L.divIcon({ className: 'center-pin', html: '<span aria-hidden="true">♥</span>', iconSize: [32, 32], iconAnchor: [16, 32] })
      }).bindPopup(popup).addTo(discoveryMap);
      marker.on('click', function () { selectMapCenter(center, false); });
      mapMarkers[center.id] = marker;
    });
    if (originMarker) { originMarker.remove(); originMarker = null; }
    if (discovery.origin) {
      originMarker = L.circleMarker(discovery.origin, { radius: 8, color: '#fff', weight: 3, fillColor: '#2563eb', fillOpacity: 1 }).bindTooltip(t('discovery.yourLocation')).addTo(discoveryMap);
      var nearest = points.slice().sort(function (a, b) { return centerDistance(a) - centerDistance(b); }).slice(0, 5);
      discoveryMap.fitBounds([discovery.origin].concat(nearest.map(centerCoordinates)), { padding: [40, 40], maxZoom: 14 });
    } else if (points.length) discoveryMap.fitBounds(points.map(centerCoordinates), { padding: [40, 40], maxZoom: 14 });
  }

  function locateDiscovery() {
    if (discovery.locating) return;
    if (!navigator.geolocation || !window.isSecureContext) { discovery.locationStatus = 'locationFailed'; renderDiscovery(); return; }
    discovery.locating = true; discovery.locationStatus = 'locating'; renderDiscovery();
    navigator.geolocation.getCurrentPosition(function (position) {
      var point = centerCoordinates({ coordinates: position.coords });
      discovery.locating = false;
      if (!point) { discovery.locationStatus = 'locationFailed'; renderDiscovery(); return; }
      // Visitor coordinates stay in memory; never store them or send them to the API.
      discovery.origin = point; discovery.locationStatus = 'located';
      discovery.centerPage = discovery.needPage = 1; renderDiscovery();
    }, function (error) {
      discovery.locating = false;
      discovery.locationStatus = error.code === 1 ? 'locationDenied' : 'locationFailed'; renderDiscovery();
    }, { enableHighAccuracy: false, timeout: 10000, maximumAge: 60000 });
  }

  function renderDiscovery() {
    if (!el('discovery-status')) return;
    el('discovery-status').textContent = discovery.status ? t('discovery.' + discovery.status) : '';
    var listRoute = discoveryRoute() && discoveryRoute().view === 'list';
    el('discovery-browse').hidden = !listRoute;
    // Keep missing-record feedback reachable when the list is hidden.
    if (!listRoute) el('discovery-detail').before(el('discovery-status'));
    else el('discovery-results').before(el('discovery-status'));
    var centers = filteredCenters();
    var needs = discovery.needs.filter(function (need) {
      var center = discovery.centers.find(function (row) { return row.id === need.centerId; });
      return matchesQuery(publicText(need.title) + ' ' + publicText(need.description) + ' ' + publicText(center?.name) + ' ' + publicText(center?.address));
    });
    if (discovery.origin) needs.sort(function (a, b) { return centerDistance(discovery.centers.find(function (c) { return c.id === a.centerId; })) - centerDistance(discovery.centers.find(function (c) { return c.id === b.centerId; })); });
    var rows = discovery.view === 'centers' ? centers : needs;
    ['centers', 'needs'].forEach(function (kind) {
      var pageKey = kind === 'centers' ? 'centerPage' : 'needPage';
      var lastKey = kind === 'centers' ? 'centerLast' : 'needLast';
      discovery[lastKey] = Math.max(1, Math.ceil((kind === 'centers' ? centers : needs).length / 12));
      discovery[pageKey] = Math.min(discovery[pageKey], discovery[lastKey]);
      el('discovery-' + kind + '-tab').setAttribute('aria-pressed', String(discovery.view === kind));
      ['prev', 'next'].forEach(function (direction) {
        var button = el('discovery-' + kind + '-' + direction);
        button.hidden = !listRoute || discovery.view !== kind || discovery[lastKey] === 1 || !!discovery.status;
        button.disabled = discovery.busy || (direction === 'prev' ? discovery[pageKey] <= 1 : discovery[pageKey] >= discovery[lastKey]);
      });
    });
    el('discovery-count').textContent = !discovery.status ? rows.length + ' ' + t('discovery.' + (rows.length === 1 ? 'result' : 'results')) : '';
    el('discovery-refresh').disabled = discovery.busy;
    el('discovery-locate').disabled = discovery.locating;
    el('discovery-location-clear').hidden = !discovery.origin;
    el('discovery-location-status').textContent = t('discovery.' + (discovery.locationStatus || 'searchHint'));
    var results = el('discovery-results');
    results.replaceChildren();
    if (listRoute && !discovery.status) {
      if (!rows.length) results.append(node('p', t('discovery.' + (searchText() ? 'noMatch' : discovery.view === 'centers' ? 'emptyCenters' : 'emptyNeeds'))));
      var page = discovery.view === 'centers' ? discovery.centerPage : discovery.needPage;
      rows.slice((page - 1) * 12, page * 12).forEach(function (row) {
        var card = node('article', '');
        card.className = 'catalog-card';
        card.dataset.id = row.id;
        card.setAttribute('aria-current', String(discovery.selected === row.id));
        card.append(node('h3', publicText(discovery.view === 'centers' ? row.name : row.title)));
        if (discovery.view === 'centers') {
          if (row.verificationStatus === 'officially_verified') {
            var verified = node('span', '✓ ' + t('discovery.verified')); verified.className = 'verified-center'; card.append(verified);
          }
          appendText(card, 'p', publicText(row.address));
          if (Number.isFinite(centerDistance(row))) appendText(card, 'p', new Intl.NumberFormat(effective(), { maximumFractionDigits: 1 }).format(centerDistance(row)) + ' km');
        } else {
          var description = node('p', publicText(row.description)); description.className = 'catalog-description'; card.append(description);
          appendText(card, 'p', quantityLine(row));
        }
        var actions = node('div', ''); actions.className = 'catalog-actions';
        var open = node('a', t('discovery.' + (discovery.view === 'centers' ? 'details' : 'needDetails')));
        open.className = 'text-button';
        open.href = (discovery.view === 'centers' ? '#centro/' : '#necesidad/') + row.id;
        actions.append(open);
        if (discovery.view === 'centers' && centerCoordinates(row)) {
          var show = node('button', t('discovery.onMap')); show.type = 'button'; show.className = 'text-button';
          show.addEventListener('click', function () { selectMapCenter(row, true); }); actions.append(show);
        }
        card.append(actions); results.append(card);
      });
    }
    var mapCenters = discovery.view === 'centers' ? centers : discovery.centers.filter(function (center) { return needs.some(function (need) { return need.centerId === center.id; }); });
    if (listRoute) renderDiscoveryMap(discovery.status ? [] : mapCenters);
    var detail = el('discovery-detail');
    detail.replaceChildren();
    detail.hidden = !discovery.record;
    if (discovery.record) {
      var back = node('a', t('discovery.back'));
      back.className = 'ghost';
      back.href = '#descubrir';
      detail.append(back);
      renderRecord(detail, discovery.record.kind, discovery.record.data);
    }
    renderOffer();
    renderShare();
    renderHistory();
  }

  function renderOffer() {
    var need = selectedNeed();
    el('discovery-offer').hidden = !need;
    var methods = needMethods(need);
    var expired = needExpired(need);
    el('discovery-offer-limit').textContent = !featureEnabled('contributions.create') && accountUser ? featureUnavailable() : !need ? '' : expired ? t('discovery.expired') : methods.money ? t('discovery.money') : (!methods.goods && !methods.time ? t('discovery.noMethods') : '');
    var goods = el('discovery-methods').querySelector('input[value="goods"]');
    var time = el('discovery-methods').querySelector('input[value="time"]');
    goods.disabled = !methods.goods || expired || discovery.offerBusy || !!discovery.offerPending;
    time.disabled = !methods.time || expired || discovery.offerBusy || !!discovery.offerPending;
    if (goods.checked && goods.disabled) goods.checked = false;
    if (time.checked && time.disabled) time.checked = false;
    if (!selectedMethod()) {
      if (methods.goods && !goods.disabled) goods.checked = true;
      else if (methods.time && !time.disabled) time.checked = true;
    }
    var method = selectedMethod();
    el('discovery-methods').hidden = !need || (!methods.goods && !methods.time);
    setField('discovery-quantity', method === 'goods');
    setField('discovery-minutes', method === 'time');
    setField('discovery-activity', method === 'time');
    setField('discovery-availability', !!method);
    setField('discovery-notes', !!method);
    el('discovery-submit').disabled = !featureEnabled('contributions.create') || discovery.offerBusy || !!discovery.offerPending || !method || expired;
    el('discovery-retry').hidden = !discovery.offerPending;
    el('discovery-retry').disabled = discovery.offerBusy;
    el('discovery-sign-in').hidden = verifiedUser();
    el('discovery-sign-in').href = apiOrigin + '/auth/sign-in?client=web&locale=' + effective();
    var status = discovery.offerStatus;
    var text = '';
    if (status.indexOf('capacity:') === 0) text = t('discovery.capacityKnown') + status.slice('capacity:'.length);
    else if (status.indexOf('saved:') === 0) text = t('discovery.saved') + status.slice('saved:'.length);
    else if (status) text = t('discovery.' + status);
    el('discovery-offer-status').textContent = text;
  }

  function renderShare() {
    var share = el('discovery-share');
    var record = discovery.record;
    var id = record && (record.kind === 'center' ? (slugId(record.data.id) ? record.data.id : '') : (publicId(record.data.id) ? record.data.id : ''));
    share.hidden = !id;
    if (!id) return;
    el('discovery-link').value = recordUrl(record.kind, id);
    var canShare = typeof navigator.share === 'function';
    el('discovery-share-button').hidden = !canShare;
    el('discovery-share-status').textContent = discovery.shareStatus ? t('discovery.' + discovery.shareStatus) : '';
  }

  function offerStatusLabel(status) {
    if (status === 'offered' || status === 'confirmed' || status === 'fulfilled' || status === 'cancelled') return t('discovery.status_' + status);
    return t('discovery.status_other');
  }

  function renderHistory() {
    var box = el('discovery-history');
    box.hidden = !verifiedUser() || !discoveryRoute();
    el('discovery-history-status').textContent = discovery.historyStatus ? t('discovery.' + discovery.historyStatus) : '';
    el('discovery-history-prev').disabled = discovery.historyBusy || !!discovery.cancelPending || discovery.historyPage <= 1;
    el('discovery-history-next').disabled = discovery.historyBusy || !!discovery.cancelPending || discovery.historyPage >= discovery.historyLast;
    el('discovery-history-retry').hidden = !discovery.cancelPending;
    el('discovery-history-retry').disabled = discovery.historyBusy;
    var list = el('discovery-history-list');
    list.replaceChildren();
    if (!box.hidden && !discovery.history.length && !discovery.historyStatus) list.append(node('p', t('discovery.historyEmpty')));
    discovery.history.forEach(function (item) {
      if (!uuid(item.id) || String(item.user_id) !== discovery.owner) return;
      var card = document.createElement('article');
      card.className = 'catalog-card';
      card.append(node('h4', publicText(item.title) || item.id));
      card.append(node('p', offerStatusLabel(item.status)));
      if (item.method === 'goods' && Number.isInteger(item.quantity)) card.append(node('p', String(item.quantity)));
      if (item.method === 'time' && Number.isInteger(item.minutes)) card.append(node('p', String(item.minutes)));
      appendText(card, 'p', typeof item.activity === 'string' ? item.activity : '');
      appendText(card, 'p', typeof item.notes === 'string' ? item.notes : '');
      if (['offered', 'confirmed'].indexOf(item.status) !== -1 && Number.isInteger(item.version) && item.version >= 1) {
        var button = node('button', t('discovery.cancel'));
        button.type = 'button';
        button.className = 'ghost';
        button.disabled = discovery.historyBusy || !!discovery.cancelPending;
        button.addEventListener('click', function () { sendCancel(item); });
        card.append(button);
      }
      list.append(card);
    });
  }

  function offerBody() {
    var need = selectedNeed();
    var method = selectedMethod();
    if (!need || !publicId(need.id) || needExpired(need)) return null;
    var methods = needMethods(need);
    if (method === 'goods' && methods.goods) {
      var quantity = Number(el('discovery-quantity').value);
      if (!Number.isInteger(quantity) || quantity < 1 || quantity > 1000000) return null;
      return optionalText({ need_id: need.id, method: 'goods', quantity: quantity });
    }
    if (method === 'time' && methods.time) {
      var minutes = Number(el('discovery-minutes').value);
      var activity = el('discovery-activity').value.trim();
      if (!Number.isInteger(minutes) || minutes < 1 || minutes > 525600 || !activity || activity.length > 500) return null;
      return optionalText({ need_id: need.id, method: 'time', minutes: minutes, activity: activity });
    }
    return null;
  }

  function optionalText(body) {
    var availability = el('discovery-availability').value.trim();
    var notes = el('discovery-notes').value.trim();
    if (availability) body.availability = availability.slice(0, 500);
    if (notes) body.notes = notes.slice(0, 2000);
    return body;
  }

  async function sendOffer(pending) {
    if (discovery.offerBusy) return;
    if (!verifiedUser()) {
      discovery.offerStatus = 'needSignIn';
      renderDiscovery();
      el('discovery-sign-in').focus();
      return;
    }
    if (!pending) {
      if (!featureEnabled('contributions.create')) { el('discovery-offer-status').textContent = featureUnavailable(); return; }
      if (discovery.offerPending) { discovery.offerStatus = 'pending'; renderDiscovery(); return; }
      var body = offerBody();
      if (!body) { discovery.offerStatus = 'invalid'; renderDiscovery(); return; }
      try {
        pending = { owner: discovery.owner, key: crypto.randomUUID(), body: body };
        sessionStorage.setItem(offerKey(), JSON.stringify(pending));
        discovery.offerPending = pending;
      } catch (_) { discovery.offerStatus = 'unsafe'; renderDiscovery(); return; }
    }
    var generation = ++discovery.offerGeneration;
    var owner = discovery.owner;
    discovery.offerBusy = true;
    discovery.offerStatus = 'sending';
    renderDiscovery();
    try {
      var response = await fetch(apiOrigin + '/api/v1/contributions', {
        method: 'POST', credentials: 'include', cache: 'no-store', signal: AbortSignal.timeout(15000),
        headers: { 'X-Generosity-Channel': appChannel, Accept: 'application/json', 'Content-Type': 'application/json', 'X-CSRF-TOKEN': accountCsrf, 'Idempotency-Key': pending.key },
        body: JSON.stringify(pending.body)
      });
      var data = null;
      try { data = await response.json(); } catch (_) { data = null; }
      if (generation !== discovery.offerGeneration || discovery.owner !== owner) return;
      if (!response.ok) {
        var code = data && data.error && data.error.code;
        if ([401, 403, 419].indexOf(response.status) !== -1) discovery.offerStatus = 'denied';
        else if (response.status >= 500) discovery.offerStatus = 'pending';
        else {
          try { sessionStorage.removeItem(offerKey()); } catch (_) { /* A rejected key is not retried. */ }
          discovery.offerPending = null;
          if (code === 'capacity_exceeded') {
            var available = data.error.details && data.error.details.available;
            discovery.offerStatus = Number.isInteger(available) && available >= 0 ? 'capacity:' + available : 'capacity';
          } else if (code === 'need_unavailable') discovery.offerStatus = 'needUnavailable';
          else if (code === 'operation_reused') discovery.offerStatus = 'conflict';
          else discovery.offerStatus = 'rejected';
        }
        return;
      }
      if (!data || !uuid(data.id) || data.status !== 'offered' || data.method !== pending.body.method || data.need_id !== pending.body.need_id) {
        try { sessionStorage.removeItem(offerKey()); } catch (_) { /* Do not replay an unconfirmed receipt. */ }
        discovery.offerPending = null;
        discovery.offerStatus = 'unconfirmed';
        return;
      }
      try { sessionStorage.removeItem(offerKey()); sessionStorage.removeItem(intentKey()); } catch (_) { /* The receipt is already stored by the server. */ }
      discovery.offerPending = null;
      discovery.offerStatus = 'saved:' + data.id;
      el('discovery-offer-status').focus();
      if (verifiedUser()) loadHistory();
    } catch (_) {
      if (generation === discovery.offerGeneration) discovery.offerStatus = 'pending';
    } finally {
      if (generation === discovery.offerGeneration) { discovery.offerBusy = false; renderDiscovery(); }
    }
  }

  async function loadHistory() {
    if (discovery.historyBusy || !verifiedUser()) return;
    var generation = ++discovery.historyGeneration;
    var owner = discovery.owner;
    discovery.historyBusy = true;
    discovery.historyStatus = 'historyLoading';
    renderHistory();
    try {
      var result = await publicGet('/api/v1/me/contributions?page=' + discovery.historyPage);
      if (generation !== discovery.historyGeneration || discovery.owner !== owner) return;
      if (!result.ok || !result.data || !Array.isArray(result.data.data)) discovery.historyStatus = 'historyFailed';
      else {
        discovery.history = result.data.data;
        discovery.historyLast = pageCount(result.data);
        discovery.historyStatus = '';
      }
    } catch (_) {
      if (generation === discovery.historyGeneration) discovery.historyStatus = 'historyFailed';
    } finally {
      if (generation === discovery.historyGeneration) { discovery.historyBusy = false; renderHistory(); }
    }
  }

  async function sendCancel(item) {
    if (discovery.historyBusy || !verifiedUser()) return;
    var pending = discovery.cancelPending;
    if (!pending) {
      if (!item || !uuid(item.id) || !Number.isInteger(item.version) || String(item.user_id) !== discovery.owner) return;
      try {
        pending = { owner: discovery.owner, key: crypto.randomUUID(), id: item.id, version: item.version };
        sessionStorage.setItem(cancelKey(), JSON.stringify(pending));
        discovery.cancelPending = pending;
      } catch (_) { discovery.historyStatus = 'unsafe'; renderHistory(); return; }
    }
    var generation = ++discovery.cancelGeneration;
    var owner = discovery.owner;
    discovery.historyBusy = true;
    discovery.historyStatus = 'cancelling';
    renderHistory();
    try {
      var response = await fetch(apiOrigin + '/api/v1/contributions/' + encodeURIComponent(pending.id) + '/transition', {
        method: 'POST', credentials: 'include', cache: 'no-store', signal: AbortSignal.timeout(15000),
        headers: { 'X-Generosity-Channel': appChannel, Accept: 'application/json', 'Content-Type': 'application/json', 'X-CSRF-TOKEN': accountCsrf, 'Idempotency-Key': pending.key },
        body: JSON.stringify({ action: 'cancel', version: pending.version })
      });
      var data = null;
      try { data = await response.json(); } catch (_) { data = null; }
      if (generation !== discovery.cancelGeneration || discovery.owner !== owner) return;
      if (!response.ok) {
        var code = data && data.error && data.error.code;
        if ([401, 403, 419].indexOf(response.status) !== -1) discovery.historyStatus = 'sessionDenied';
        else if (response.status >= 500) discovery.historyStatus = 'cancelPending';
        else {
          try { sessionStorage.removeItem(cancelKey()); } catch (_) { /* The rejected cancellation is not retried. */ }
          discovery.cancelPending = null;
          discovery.historyStatus = code === 'version_conflict' ? 'cancelConflict' : 'cancelUnavailable';
        }
        return;
      }
      try { sessionStorage.removeItem(cancelKey()); } catch (_) { /* The server already stored the cancellation. */ }
      discovery.cancelPending = null;
      discovery.historyStatus = data && data.status === 'cancelled' ? 'cancelled' : 'cancelUnconfirmed';
    } catch (_) {
      if (generation === discovery.cancelGeneration) discovery.historyStatus = 'cancelPending';
    } finally {
      if (generation === discovery.cancelGeneration) {
        discovery.historyBusy = false;
        renderHistory();
        if (discovery.historyStatus === 'cancelled' || discovery.historyStatus === 'cancelConflict') loadHistory();
      }
    }
  }

  if (el('descubrir')) {
  el('discovery-search').addEventListener('submit', function (event) { event.preventDefault(); renderDiscovery(); });
  el('discovery-offer').addEventListener('submit', function (event) { event.preventDefault(); });
  el('discovery-query').addEventListener('input', function () { discovery.centerPage = discovery.needPage = 1; renderDiscovery(); });
  el('discovery-refresh').addEventListener('click', loadCatalog);
  el('discovery-list-focus').addEventListener('click', function () { el('discovery-results').focus(); });
  el('discovery-locate').addEventListener('click', locateDiscovery);
  el('discovery-location-clear').addEventListener('click', function () { discovery.origin = null; discovery.locationStatus = ''; mapSignature = ''; renderDiscovery(); });
  ['centers', 'needs'].forEach(function (kind) { el('discovery-' + kind + '-tab').addEventListener('click', function () { discovery.view = kind; renderDiscovery(); }); });
  el('discovery-sign-in').addEventListener('click', saveIntent);
  el('discovery-centers-prev').addEventListener('click', function () { if (discovery.busy || discovery.centerPage <= 1) return; discovery.centerPage -= 1; renderDiscovery(); });
  el('discovery-centers-next').addEventListener('click', function () { if (discovery.busy || discovery.centerPage >= discovery.centerLast) return; discovery.centerPage += 1; renderDiscovery(); });
  el('discovery-needs-prev').addEventListener('click', function () { if (discovery.busy || discovery.needPage <= 1) return; discovery.needPage -= 1; renderDiscovery(); });
  el('discovery-needs-next').addEventListener('click', function () { if (discovery.busy || discovery.needPage >= discovery.needLast) return; discovery.needPage += 1; renderDiscovery(); });
  el('discovery-methods').addEventListener('change', function () { saveIntent(); renderDiscovery(); });
  ['discovery-quantity', 'discovery-minutes', 'discovery-activity', 'discovery-availability', 'discovery-notes'].forEach(function (id) {
    el(id).addEventListener('input', saveIntent);
  });
  el('discovery-submit').addEventListener('click', function () { sendOffer(null); });
  el('discovery-retry').addEventListener('click', function () { sendOffer(discovery.offerPending); });
  el('discovery-history-prev').addEventListener('click', function () { if (discovery.historyBusy || discovery.historyPage <= 1) return; discovery.historyPage -= 1; loadHistory(); });
  el('discovery-history-next').addEventListener('click', function () { if (discovery.historyBusy || discovery.historyPage >= discovery.historyLast) return; discovery.historyPage += 1; loadHistory(); });
  el('discovery-history-retry').addEventListener('click', function () { sendCancel(null); });
  el('discovery-copy').addEventListener('click', async function () {
    var url = el('discovery-link').value;
    try {
      await navigator.clipboard.writeText(url);
      discovery.shareStatus = 'copied';
    } catch (_) { discovery.shareStatus = 'copyFailed'; }
    renderShare();
  });
  el('discovery-share-button').addEventListener('click', async function () {
    try {
      await navigator.share({ url: el('discovery-link').value });
      discovery.shareStatus = '';
    } catch (error) { discovery.shareStatus = error && error.name === 'AbortError' ? 'shareCancelled' : 'shareFailed'; }
    renderShare();
  });
  el('discovery-download').addEventListener('click', function () {
    var record = discovery.record;
    if (!record) return;
    var title = record.kind === 'center' ? publicText(record.data.name) : publicText(record.data.title);
    var text = 'Generosity\n' + title + '\n' + el('discovery-link').value + '\n' + new Date().toISOString().slice(0, 10) + '\n' + t('discovery.downloadNote') + '\n';
    var blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
    var url = URL.createObjectURL(blob);
    var link = document.createElement('a');
    link.href = url;
    link.download = 'generosity-invitation.txt';
    document.body.append(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  });
  el('discovery-share').addEventListener('submit', function (event) { event.preventDefault(); });

  }

  Object.assign(copy.es, {"nav.app": "Explora la app", "nav.home": "Inicio", "account.title": "Tu cuenta Generosity", "account.lead": "Inicia sesión para continuar. Después vuelve a esta página; tu información se actualizará automáticamente.", "page.centers.title": "Portal de centros", "page.centers.lead": "Selecciona tu centro, publica sus necesidades y da seguimiento a las aportaciones.", "page.registration.title": "Registra tu sede", "page.registration.lead": "Comparte los datos de tu organización y consulta el avance de tu solicitud.", "page.help.title": "Encuentra dónde ayudar", "page.help.lead": "Descubre centros, consulta sus necesidades y da seguimiento a tus aportaciones."});
  Object.assign(copy.en, {"nav.app": "Explore the app", "nav.home": "Home", "account.title": "Your Generosity account", "account.lead": "Sign in to continue, then return to this page. Your information will refresh automatically.", "page.centers.title": "Center portal", "page.centers.lead": "Select your center, publish its needs and follow up on contributions.", "page.registration.title": "Register your location", "page.registration.lead": "Share your organization’s details and track your application.", "page.help.title": "Find ways to help", "page.help.lead": "Discover centers, explore their needs and track your contributions."});

  var stored = readStored();
  if (stored === "es" || stored === "en" || stored === "system") mode = stored;
  consumeIntentHash();
  applyAll();
  routeDiscovery(!!discoveryRoute());
  checkAccount();
})();
