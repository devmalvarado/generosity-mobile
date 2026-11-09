package org.generosity.preview

import org.generosity.usecase.searchKey

/** In-memory design fixtures. Deliberately has no connection, repository, account or persistence dependency. */
data class PreviewRoute(val id: String, val group: String, val spanish: String, val english: String) {
    fun title(language: String) = if (language == "es") spanish else english
}
data class PreviewItem(
    val kind: String, val id: String, val title: String, val detail: String = "",
    val value: String = "", val options: List<String> = emptyList(), val target: String = "",
    val progress: Float = -1f, val enabled: Boolean = true
)
data class PreviewCenter(val id: String, val spanish: String, val english: String, val category: String,
    val funding: String, val area: String, val distance: String, val x: Float, val y: Float, val verified: Boolean) {
    fun title(language: String) = if (language == "es") spanish else english
}

class PreviewSession(groups: List<String>, language: String, private val connectedOnly: Boolean = false) {
    companion object {
        val routes = listOf(
            PreviewRoute("map", "discovery", "Mapa", "Map"),
            PreviewRoute("needs", "discovery", "Necesidades", "Needs"),
            PreviewRoute("center", "discovery", "Ficha del centro", "Center profile"),
            PreviewRoute("offer", "contributions", "Quiero ayudar", "Offer help"),
            PreviewRoute("history", "contributions", "Mis aportaciones", "My contributions"),
            PreviewRoute("impact", "impact", "Mi impacto", "My impact"),
            PreviewRoute("achievements", "impact", "Logros y certificados", "Achievements & certificates"),
            PreviewRoute("regions", "impact", "Ayuda por región", "Regional helping"),
            PreviewRoute("saved", "discovery", "Centros guardados", "Saved centers"),
            PreviewRoute("reviews", "discovery", "Reseñas", "Reviews"),
            PreviewRoute("inbox", "communication", "Notificaciones", "Notifications"),
            PreviewRoute("messages", "communication", "Conversaciones", "Conversations"),
            PreviewRoute("events", "community", "Eventos", "Events"),
            PreviewRoute("event", "community", "Detalle del evento", "Event details"),
            PreviewRoute("campaign", "community", "Campañas y equipos", "Campaigns & teams"),
            PreviewRoute("meals", "community", "Comidas coordinadas", "Meal train"),
            PreviewRoute("food", "community", "Rescate de alimentos", "Food rescue"),
            PreviewRoute("referral", "assistance", "Canalizar una necesidad", "Refer someone for help"),
            PreviewRoute("case", "assistance", "Seguimiento privado", "Private follow-up"),
            PreviewRoute("assessment", "assistance", "Levantamiento de necesidades", "Needs assessment"),
            PreviewRoute("coverage", "assistance", "Cobertura territorial", "Service coverage"),
            PreviewRoute("organization", "organizations", "Mi organización", "My organization"),
            PreviewRoute("businesses", "organizations", "Negocios que ayudan", "Businesses that help"),
            PreviewRoute("funding", "organizations", "Sostener Generosity", "Support Generosity"),
            PreviewRoute("donate", "payments", "Donación económica", "Donate money"),
            PreviewRoute("tax", "payments", "Comprobantes fiscales", "Tax documents"),
            PreviewRoute("staff", "operations", "Espacio del centro", "Center workspace"),
            PreviewRoute("onboarding", "operations", "Registrar una sede", "Register a center"),
            PreviewRoute("operations", "operations", "Revisión y conectores", "Review & connectors"),
            PreviewRoute("share", "communication", "Participar desde un enlace", "Join from a link"),
            PreviewRoute("assisted", "communication", "Participación asistida", "Assisted participation"),
            PreviewRoute("profile", "account", "Perfil", "Profile"),
            PreviewRoute("settings", "account", "Configuración", "Settings"),
            PreviewRoute("welcome", "discovery", "Bienvenida", "Welcome")
        )
        val groupNames = routes.map { it.group }.distinct()
        val flagNames = groupNames.map { "preview.$it" }
        // Native hosts bind these destinations to the existing app state, never to fixture fields.
        val connectedRoutes = listOf("map", "needs", "center", "help", "offer", "personal", "history", "impact", "inbox", "profile", "settings", "welcome")
        private val extraRoutes = mapOf("help" to "contributions", "personal" to "contributions", "review" to "discovery",
            "phone" to "account", "delete" to "account", "certificate" to "impact", "organizer" to "community", "editor" to "operations")
        val centers = listOf(
            PreviewCenter("c1", "Casa Comunitaria Jacaranda", "Jacaranda Community House", "food", "mixed", "Barrio del Sol", "0.4", .38f, .46f, true),
            PreviewCenter("c2", "Centro de Salud Horizonte", "Horizonte Health Center", "health", "public", "Barrio del Sol", "0.9", .72f, .35f, true),
            PreviewCenter("c3", "Hogar de Mayores Los Olivos", "Los Olivos Senior Home", "seniors", "private", "Las Flores", "1.2", .68f, .64f, false),
            PreviewCenter("c4", "Banco de Sangre Encuentro", "Encuentro Blood Bank", "blood", "public", "Las Flores", "1.6", .25f, .72f, false),
            PreviewCenter("c5", "Casa Hogar Semillas", "Semillas Children's Home", "children", "private", "Barrio Jardín", "2.1", .22f, .33f, true),
            PreviewCenter("c6", "Taller Aprender Juntos", "Learn Together Workshop", "training", "mixed", "Barrio Jardín", "2.4", .79f, .78f, false)
        )
    }
    var language = if (language.startsWith("es")) "es" else "en"
        private set
    private var allowed = groups.filter { it in groupNames }.toSet()
    var route = availableRoutes().firstOrNull()?.id.orEmpty()
        private set
    var selected = "c1"
        private set
    private val stack = mutableListOf<String>()
    private val fields = mutableMapOf<String, String>()
    private val saved = mutableSetOf("c1")
    private val records = mutableMapOf<String, String>()
    var message = ""
        private set
    fun text(es: String, en: String) = if (language == "es") es else en
    fun updateLanguage(code: String) { language = if (code.startsWith("es")) "es" else "en" }
    fun availableRoutes() = routes.filter { canOpen(it.id) }
    fun canOpen(id: String) = (!connectedOnly || id in connectedRoutes) &&
        (routes.firstOrNull { it.id == id }?.group ?: extraRoutes[id]) in allowed
    fun navigationTabs() = listOf("map", "needs", "help", "impact", "profile").filter { canOpen(it) }
    fun navigationTab(): String {
        val tabs = navigationTabs()
        if (route in tabs) return route
        val group = routes.firstOrNull { it.id == route }?.group ?: extraRoutes[route]
        val root = when (group) {
            "contributions" -> "help"
            "impact" -> "impact"
            "account" -> "profile"
            else -> "map"
        }
        return root.takeIf { it in tabs } ?: tabs.firstOrNull().orEmpty()
    }
    fun title() = routes.firstOrNull { it.id == route }?.title(language) ?: when(route) {
        "help" -> text("¿Cómo quieres ayudar?", "How would you like to help?")
        "personal" -> text("Ayuda personal", "Personal help")
        "review" -> text("Tu experiencia", "Your experience")
        "phone" -> text("Verificar celular", "Verify mobile number")
        "delete" -> text("Eliminar cuenta", "Delete account")
        "certificate" -> text("Constancia de participación", "Participation certificate")
        "organizer" -> text("Organiza una campaña", "Organize a campaign")
        "editor" -> text("Nueva necesidad", "New need")
        else -> text("Funciones no disponibles", "Features unavailable")
    }
    fun updateAccess(groups: List<String>) {
        val next = groups.filter { it in groupNames }.toSet()
        if (next == allowed) return
        allowed = next
        fields.clear(); saved.clear(); records.clear(); stack.clear(); selected = "c1"; message = ""
        route = availableRoutes().firstOrNull()?.id.orEmpty()
    }
    fun navigate(id: String) {
        if (!canOpen(id)) return
        if (route != id) stack += route
        route = id; message = ""
    }
    fun back() { route = stack.removeLastOrNull()?.takeIf(::canOpen) ?: availableRoutes().firstOrNull()?.id.orEmpty(); message = "" }
    fun value(id: String) = fields[id].orEmpty()
    fun visibleCenters(): List<PreviewCenter> {
        if (!canOpen("map") || value("scenario") == "empty") return emptyList()
        return centers.filter { c ->
            (value("category").isBlank() || value("category") == "all" || value("category") == c.category) &&
            (value("funding").isBlank() || value("funding") == "all" || value("funding") == c.funding) &&
            (value("query").isBlank() || "${c.spanish} ${c.english} ${c.area} Coyoacán".searchKey().contains(value("query").searchKey()))
        }
    }
    fun fundingTitle(code: String) = when(code) {
        "public" -> text("Público", "Public")
        "private" -> text("Privado", "Private")
        else -> text("Mixto", "Mixed")
    }
    private fun statusTitle() = when(records["offer-status"]) {
        "cancelled" -> text("Cancelado", "Cancelled")
        "confirmed" -> text("Confirmado", "Confirmed")
        "completed" -> text("Completado por el centro", "Completed by the center")
        else -> text("Pendiente de confirmación", "Awaiting confirmation")
    }
    private fun validDate(value: String): Boolean {
        if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(value)) return false
        val (year, month, day) = value.split('-').map { it.toInt() }
        if (year !in 1900..2200 || month !in 1..12) return false
        val leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        return day in 1..listOf(31, if(leap) 29 else 28,31,30,31,30,31,31,30,31,30,31)[month-1]
    }
    fun selectedCenter() = centers.first { it.id == selected }
    fun selectCenter(id: String) { if (canOpen("map") && centers.any { it.id == id }) selected = id }
    fun setField(id: String, value: String) {
        val field = items().firstOrNull { it.id == id && it.kind in listOf("field", "choice", "check") } ?: return
        if (!field.enabled || value.length > 500) return
        if (field.kind == "choice" && value !in field.options.map { it.substringBefore('|') }) return
        if (field.kind == "check" && value !in listOf("true", "false")) return
        fields[id] = value
        if (id == "language") language = if (value == "es") "es" else "en"
        if (id in listOf("query", "category", "funding")) visibleCenters().let { list -> if (list.none { it.id == selected }) list.firstOrNull()?.let { selected = it.id } }
        message = ""
    }
    fun act(id: String) {
        val action = items().firstOrNull { it.id == id && it.kind in listOf("action", "link") && it.enabled } ?: return
        if (action.target.isNotBlank()) {
            if (id.startsWith("offer-")) selected = id.removePrefix("offer-")
            navigate(action.target); return
        }
        when(id) {
            "save" -> if (!saved.add(selected)) saved.remove(selected)
            "reset" -> listOf("category", "funding", "query", "scenario").forEach { fields.remove(it) }
            "locate" -> fields["origin"] = "true"
            "join", "rank", "context", "block", "connector" -> records[id] = (records[id] != "true").toString()
            "read" -> records["read"] = "true"
            "cancel-offer" -> records["offer-status"] = "cancelled"
            "delete-confirm" -> { fields.clear(); records.clear(); saved.clear(); navigate("welcome") }
            "waitlist" -> records["waitlist"] = "true"
            "confirm-offer" -> records["offer-status"] = "confirmed"
            "receive-offer" -> records["offer-status"] = "completed"
            "submit" -> {
                val required = items().filter { it.kind in listOf("field", "check") && it.detail != "optional" }
                if (required.any { value(it.id).isBlank() || (it.kind == "check" && value(it.id) != "true") ||
                        (it.target == "date" && !validDate(value(it.id))) ||
                        (it.kind == "field" && it.target == "number" && (value(it.id).toDoubleOrNull()?.let { n -> n.isFinite() && n > 0 } != true)) }) {
                    message = text("Completa los campos y consentimientos indicados. Las cantidades deben ser mayores que cero.", "Complete the required fields and consents. Quantities must be greater than zero."); return
                }
                if (route == "onboarding") {
                    if (value("lat").toDoubleOrNull()?.let { it.isFinite() && it in -90.0..90.0 } != true || value("lon").toDoubleOrNull()?.let { it.isFinite() && it in -180.0..180.0 } != true || !value("maps").startsWith("https://")) {
                        message = text("Revisa latitud, longitud y el enlace HTTPS.", "Check latitude, longitude and the HTTPS link."); return
                    }
                }
                records[route] = items().filter { it.kind in listOf("field", "choice") }.joinToString(" · ") { value(it.id).ifBlank { it.value }.substringBefore('|') }
                if (route == "offer") { records["offer-status"] = "pending"; navigate("history") }
                else if (route == "referral") navigate("case")
                else if (route == "personal") navigate("history")
            }
        }
        message = text("Simulación guardada en esta sesión. No se enviaron datos.", "Simulation saved in this session. No data was sent.")
    }

    fun items(): List<PreviewItem> {
        if (!canOpen(route)) return emptyList()
        val rows = mutableListOf<PreviewItem>()
        fun card(es: String, en: String, detailEs: String = "", detailEn: String = "", kind: String = "card", progress: Float = -1f) {
            rows += PreviewItem(kind, "card-${rows.size}", text(es,en), text(detailEs,detailEn), progress = progress)
        }
        fun note(es: String, en: String) = card(es,en,kind="note")
        fun action(id: String, es: String, en: String, enabled: Boolean = true) { rows += PreviewItem("action",id,text(es,en),enabled=enabled) }
        fun link(target: String, es: String = "", en: String = "") {
            if (canOpen(target)) rows += PreviewItem("link", "go-$target", if (es.isNotBlank()) text(es,en) else routes.firstOrNull { it.id == target }?.title(language).orEmpty(), target=target)
        }
        fun field(id: String, es: String, en: String, type: String = "text", optional: Boolean = false) { rows += PreviewItem("field",id,text(es,en),if(optional) "optional" else "required",value(id),target=type) }
        fun check(id: String, es: String, en: String) { rows += PreviewItem("check",id,text(es,en),value=value(id)) }
        fun choice(id: String, es: String, en: String, options: List<String>) { rows += PreviewItem("choice",id,text(es,en),value=value(id).ifBlank { options.first().substringBefore('|') },options=options) }
        fun submit() = action("submit", "Guardar simulación", "Save simulation")
        fun centerRow(c: PreviewCenter) { rows += PreviewItem("center", c.id, c.title(language), "${c.area} · ${c.distance} km · ${fundingTitle(c.funding)}", value=if(c.verified) "verified" else "") }
        val c = selectedCenter()
        val context = records["context"] == "true"
        when (route) {
            "map" -> {
                field("query","Centro, colonia o dirección","Center, area or address", optional=true)
                choice("category","Categoría","Category",listOf("all|${text("Todas","All")}","food|${text("Comedores","Kitchens")}","health|${text("Salud","Health")}","blood|${text("Sangre","Blood")}","seniors|${text("Mayores","Older adults")}","children|${text("Infancia","Children")}","training|${text("Capacitación","Training")}"))
                choice("funding","Origen de los recursos","Funding source",listOf("all|${text("Cualquiera","Any")}","public|${text("Público","Public")}","private|${text("Privado","Private")}","mixed|${text("Mixto","Mixed")}"))
                choice("scenario","Estado del mapa","Map state",listOf("normal|${text("Con resultados","With results")}","empty|${text("Sin resultados","No results")}","offline|${text("Sin conexión","Offline")}"))
                action("locate","Usar ubicación simulada","Use simulated location")
                if (value("scenario") == "offline") note("Sin conexión · Información ficticia guardada","Offline · Saved fictional information")
                if (visibleCenters().isEmpty()) { note("No encontramos centros. Prueba otra categoría.","No centers found. Try another category."); action("reset","Restablecer filtros","Reset filters") }
                link("inbox"); link("center", "Ver centro y necesidades", "View center and needs")
            }
            "needs" -> {
                choice("need-mode","Mostrar","Show",listOf("all|${text("Todas","All")}","urgent|${text("Urgentes","Urgent")}","nearby|${text("Cerca de mí","Nearby")}"))
                if(value("need-mode") == "nearby") note("A menos de 1 km de un origen simulado","Within 1 km of a simulated origin")
                fun need(centerId: String, es: String, en: String, detailEs: String, detailEn: String, progress: Float) {
                    card(es,en,detailEs,detailEn,progress=progress)
                    if(canOpen("offer")) rows += PreviewItem("link","offer-$centerId",text("Quiero ayudar","Offer help"),target="offer")
                }
                need("c1","Alimentos no perecederos","Non-perishable food","URGENTE · Jacaranda · 24 de 40 kg","URGENT · Jacaranda · 24 of 40 kg",.6f)
                if(value("need-mode") !in listOf("urgent","nearby")) need("c3","Una tarde de compañía","An afternoon together","Lectura y conversación · 3 de 6 personas","Reading and conversation · 3 of 6 people",.5f)
                if(value("need-mode") != "nearby") need("c4","Jornada de donación de sangre","Blood donation day","URGENTE · Consulta requisitos · 8 de 20","URGENT · Check requirements · 8 of 20",.4f)
                link("events"); link("food"); link("campaign")
            }
            "center" -> {
                card(c.title(language),c.title(language),"${c.area} · ${c.distance} km · ${if(c.verified) "✓ Verificación ficticia" else "Sin verificar"}","${c.area} · ${c.distance} km · ${if(c.verified) "✓ Fictional verification" else "Unverified"}","hero")
                action("save",if(selected in saved) "♥ Guardado" else "♡ Guardar",if(selected in saved) "♥ Saved" else "♡ Save"); link("share")
                card("Atención a la comunidad","Community care","Lun–vie 09:00–17:00 · Consulta cuotas y disponibilidad con el centro.","Mon–Fri 9 am–5 pm · Confirm fees and availability with the center.")
                card("Origen de recursos","Funding source",fundingTitle(c.funding),fundingTitle(c.funding))
                if(c.id in listOf("c1","c3","c4")) { link("needs", "Necesidades actuales", "Current needs"); link("offer") }
                else note("Este centro no tiene necesidades publicadas en la muestra.","This center has no published needs in the sample.")
                link("donate"); link("referral"); link("reviews")
                action("contact","Contactar (simulado)","Contact (simulated)"); action("maps","Abrir Maps (simulado)","Open Maps (simulated)")
                note("Dirección y contactos ficticios. No se abren destinos reales.","Fictional address and contacts. No real destinations are opened.")
            }
            "saved" -> { centers.filter { it.id in saved }.forEach(::centerRow); if(saved.isEmpty()) note("Guarda un centro desde su ficha.","Save a center from its profile."); link("map") }
            "help" -> { link("needs"); link("personal","Registrar ayuda personal","Record personal help"); note("La ayuda autodeclarada no genera puntos validados.","Self-reported help does not earn validated points.") }
            "offer", "personal" -> {
                if(route=="offer" && selected !in listOf("c1","c3","c4")) {
                    note("Este centro no tiene necesidades de muestra.","This center has no sample needs.")
                    link("needs"); return rows
                }
                card(if(route=="offer") c.title(language) else "Ayuda personal",if(route=="offer") c.title(language) else "Personal help",if(context) "Como Manos Unidas" else "Como Lucía Torres",if(context) "As Helping Hands" else "As Lucía Torres")
                if(route=="offer" && canOpen("organization")) action("context","Cambiar persona / organización","Switch person / organization")
                if(route=="personal") field("activity","¿Qué hiciste?","What did you do?")
                field("quantity","Cantidad","Quantity","number"); choice("unit","Unidad","Unit",if(route=="personal") listOf("hour|${text("Horas","Hours")}") else if(selected=="c1") listOf("kg|kg", "box|${text("Caja de 10 piezas","Box of 10 items")}") else listOf("hour|${text("Horas","Hours")}"))
                field("date","Fecha propuesta (AAAA-MM-DD)","Proposed date (YYYY-MM-DD)","date"); field("notes","Notas (opcional)","Notes (optional)",optional=true)
                note("Ofrecer no equivale a completar. El centro confirma la recepción; las ayudas personales se muestran aparte.","An offer is not completion. The center confirms receipt; personal help is shown separately."); submit()
            }
            "history" -> {
                records["offer"]?.let { card("Ofrecimiento","Offer", "$it · ${statusTitle()}","$it · ${statusTitle()}"); if(records["offer-status"] in listOf("pending","confirmed")) action("cancel-offer","Cancelar ofrecimiento","Cancel offer") }
                card("Completada por el centro","Completed by the center","Acompañamiento · 2 horas · Hogar Los Olivos","Companionship · 2 hours · Los Olivos Home")
                card("Autodeclarada","Self-reported",records["personal"] ?: "Ayuda a un vecino · Sin puntos",records["personal"] ?: "Helping a neighbor · No points")
                link("reviews"); link("case"); link("tax")
            }
            "impact" -> {
                card("1,250", "1,250", "Puntos de generosidad · Nivel 3 · 250 para el siguiente nivel", "Generosity points · Level 3 · 250 to next level", "hero",.83f)
                card("18 h  ·  12 entregas  ·  4 centros","18 h  ·  12 deliveries  ·  4 centers",kind="metrics")
                card("3 días de generosidad","3 helping days","Racha de ayuda validada de muestra","Sample validated helping streak")
                note("Valores ficticios. Solo ayudas completadas y validadas cuentan para el impacto.","Fictional values. Only completed and validated help counts toward impact.")
                link("achievements"); link("regions"); link("campaign")
            }
            "achievements" -> {
                card("♥ Primer paso","♥ First step","Primera ayuda validada · Conseguido","First validated contribution · Earned")
                card("Tiempo que transforma","Time that makes a difference","18 de 24 horas","18 of 24 hours",progress=.75f)
                link("certificate","Ver constancia y badge","View certificate and badge")
                note("Criterios de muestra. Los certificados reales requieren evidencia y emisor autorizado.","Sample criteria. Real certificates require evidence and an authorized issuer.")
            }
            "certificate" -> { card("GENEROSITY", "GENEROSITY","MUESTRA SIN VALIDEZ","INVALID SAMPLE","hero"); card("Lucía Torres","Lucía Torres","6 horas · Jardines Vivos · Noviembre 2026","6 hours · Living Gardens · November 2026"); note("Sin PDF válido ni comprobante fiscal. Vista de reconocimiento únicamente.","No valid PDF or tax receipt. Recognition preview only.") }
            "regions" -> {
                choice("country","País","Country",listOf("MX|México","US|United States","ES|España")); choice("division","División","Area",listOf("neighborhood|${text("Colonia","Neighborhood")}","postal|${text("Código postal","Postal code")}")); choice("period","Periodo","Period",listOf("month|${text("Este mes","This month")}","year|${text("Este año","This year")}"))
                if(value("country") in listOf("US","ES")) note("Datos insuficientes para publicar una comparación.","Insufficient data to publish a comparison.") else {
                    card("1 · Barrio Jardín","1 · Garden District","128 h","128 h",progress=1f); card("2 · Barrio del Sol","2 · Sun District","96 h","96 h",progress=.75f); card("3 · Las Flores","3 · Flower District","72 h","72 h",progress=.56f)
                }
                note("Comparación ficticia. Sin datos no significa sin ayuda; se protegen grupos pequeños.","Fictional comparison. Missing data does not mean no help; small groups are protected."); link("coverage")
            }
            "reviews", "review" -> {
                if(route=="reviews") { card("4.8 ★","4.8 ★","12 reseñas de muestra","12 sample reviews","hero"); card("María R.","María R.","Ayuda completada · Reseña moderada. El equipo nos orientó para entregar lo necesario.","Completed help · Moderated review. The team helped us deliver what was needed."); action("report","Reportar reseña","Report review"); link("review","Escribir una reseña","Write a review") }
                else { note("Solo tras ayuda validada. Publicación sujeta a moderación.","Only after validated help. Publication requires moderation."); choice("rating","Valoración","Rating",listOf("5|5 ★","4|4 ★","3|3 ★","2|2 ★","1|1 ★")); field("review-text","Reseña","Review"); submit() }
            }
            "inbox" -> { action("read","Marcar como leídas","Mark as read"); note(if(records["read"]=="true") "Todas leídas" else "2 sin leer",if(records["read"]=="true") "All read" else "2 unread"); link("history","El centro confirmó tu ofrecimiento","The center confirmed your offer"); link("needs","Una necesidad cerca de ti","A need near you"); link("messages"); link("settings"); note("Esta bandeja no envía push ni cambia permisos del teléfono.","This inbox sends no push notifications and changes no phone permissions.") }
            "messages" -> {
                card("Jardines Vivos","Living Gardens","Coordinación · 09:15 · Reunión en la entrada. Trae agua y sombrero.","Coordinator · 9:15 am · Meet at the entrance. Bring water and a hat.")
                records["messages"]?.let { card("Tú (simulación)","You (simulation)",it,it) }
                if(records["block"]!="true") { field("message","Mensaje al equipo","Message to the team"); submit() }
                action("block",if(records["block"]=="true") "Desbloquear" else "Reportar / bloquear",if(records["block"]=="true") "Unblock" else "Report / block")
                note("Solo participantes autorizados. Usa información ficticia; no incluyas datos de salud.","Authorized participants only. Use fictional information; do not enter health details.")
            }
            "events" -> { card("Jardines Vivos","Living Gardens","REFORESTACIÓN · Sábado 09:00 · 4 lugares","TREE PLANTING · Saturday 9 am · 4 places"); card("Barrio Limpio","Clean Neighborhood","LIMPIEZA · Domingo 10:00 · 8 lugares","CLEANUP · Sunday 10 am · 8 places"); link("event"); link("meals"); link("campaign"); link("organizer","Quiero organizar","I want to organize") }
            "event" -> {
                card("Jardines Vivos","Living Gardens","Sábado 09:00–12:00 · Parque del Sol","Saturday 9 am–noon · Sun Park","hero")
                card("12 participantes · 4 lugares","12 participants · 4 places","Material incluido · Trae agua y sombrero","Materials provided · Bring water and a hat")
                action("join",if(records["join"]=="true") "Cancelar participación" else "Quiero participar",if(records["join"]=="true") "Cancel participation" else "I want to join")
                action("waitlist",if(records["waitlist"]=="true") "En espera (simulado)" else "Probar lista de espera",if(records["waitlist"]=="true") "On waitlist (simulated)" else "Preview waitlist",records["waitlist"]!="true")
                note("Inscripción ficticia. El centro valida la asistencia. Eventos de pago deshabilitados.","Fictional enrollment. The center validates attendance. Paid events are disabled."); link("messages"); link("campaign")
            }
            "campaign" -> {
                card("Un mes de buenas acciones","A month of good deeds","1–30 noviembre · Manos Unidas","November 1–30 · Helping Hands","hero")
                card("42 / 60 h","42 / 60 h","Horas validadas de muestra","Sample validated hours",progress=.7f)
                card("1 · Semillas · 24 h","1 · Seeds · 24 h"); card("2 · Horizonte · 18 h","2 · Horizon · 18 h")
                action("rank",if(records["rank"]=="true") "Salir del ranking individual" else "Participar en ranking individual",if(records["rank"]=="true") "Leave individual ranking" else "Opt into individual ranking")
                if(records["rank"]=="true") card("Lucía T. · Posición 5","Lucía T. · Rank 5","6 horas validadas de muestra","6 sample validated hours")
                note("Participación voluntaria. Ranking visible solo dentro de esta campaña.","Participation is optional. Ranking is visible only inside this campaign."); link("events"); link("certificate","Ver reconocimiento","View recognition")
            }
            "meals" -> {
                card("Una mesa compartida","A shared table","Turnos de entrega al centro · 13:00–14:00","Center delivery slots · 1–2 pm","hero")
                choice("meal","Elegir turno · 2 porciones","Choose slot · 2 portions",listOf("mon|${text("Lun 9","Mon 9")}","wed|${text("Mié 11","Wed 11")}","thu|${text("Jue 12","Thu 12")}","fri|${text("Vie 13","Fri 13")}"))
                note("Martes cubierto. Consulta restricciones alimentarias. Instrucciones privadas solo para entregas asignadas.","Tuesday covered. Confirm dietary requirements. Private instructions are for assigned deliveries only."); submit(); link("messages")
            }
            "food" -> {
                card("20 porciones preparadas","20 prepared portions","Escuela de Gastronomía Encuentro · Recoger antes de las 14:00","Encuentro Culinary School · Pick up before 2 pm","hero")
                action("coordinate","Solicitar coordinación con un centro","Request center coordination")
                field("food","Alimento y alérgenos","Food and allergens"); field("portions","Porciones","Portions","number"); field("deadline","Fecha y hora límite","Pickup deadline"); field("storage","Condiciones de conservación","Storage conditions"); submit()
                note("Oferta ficticia. No publica alimentos ni certifica su seguridad.","Fictional offer. Does not publish food or certify its safety.")
            }
            "referral" -> {
                card(c.title(language),c.title(language)); note("Primero avisa al centro. Una visita posible aún no es una necesidad validada.","Notify the center first. A possible visit is not a validated need.")
                choice("support","Tipo de apoyo","Support type",listOf("food|${text("Alimentos","Food")}","medicine|${text("Medicamento","Medicine")}","guidance|${text("Orientación","Guidance")}")); choice("contact","Cómo contactará al centro","How they will contact the center",listOf("visit|${text("Visita","Visit")}","call|${text("Llamada","Call")}")); check("consent","La persona conoce y acepta este aviso mínimo.","The person understands and agrees to this minimal notice.")
                note("No registres diagnósticos, recetas ni domicilios privados.","Do not enter diagnoses, prescriptions or private addresses."); submit()
            }
            "case" -> {
                card("AYUDA-2048","HELP-2048","Caso privado de muestra","Sample private case","hero")
                card("1 · Aviso enviado","1 · Heads-up sent","Posible visita al centro","Possible visit to the center"); card("2 · Contacto recibido","2 · Contact received","El centro recibió la solicitud","The center received the request"); card("3 · En evaluación","3 · Under assessment","Compras y donaciones bloqueadas","Purchases and donations blocked"); card("4 · Apoyo y entrega","4 · Support and handover","Se confirmarán recepción y entrega por separado","Receipt and handover will be confirmed separately")
                note("Medicamentos: evaluación cualificada. Sin datos privados ni ubicaciones de hogares.","Medicines require qualified assessment. No private data or household locations."); link("messages")
            }
            "assessment" -> {
                card("Escuchar antes de actuar","Listen before acting","Acceso de campo autorizado · Muestra","Authorized field access · Sample","hero")
                check("assessment-consent","La persona recibió la explicación y consiente.","The person received the explanation and consents.")
                choice("assessment-need","Necesidad principal","Main need",listOf("food|${text("Alimentación","Food")}","health|${text("Salud","Health")}","training|${text("Capacitación","Training")}")); choice("followup","Seguimiento privado","Private follow-up",listOf("no|${text("No solicitado","Not requested")}","yes|${text("Con consentimiento específico","With specific consent")}")); if(value("followup")=="yes") check("followup-consent","Autoriza el seguimiento privado.","Authorizes private follow-up.")
                submit(); link("coverage"); note("Respuestas ficticias privadas. El mapa solo muestra agregados protegidos.","Private fictional responses. The map shows protected aggregates only.")
            }
            "coverage" -> {
                choice("service","Servicio","Service",listOf("food|${text("Alimentación","Food")}","health|${text("Salud","Health")}","training|${text("Capacitación","Training")}")); choice("coverage-scenario","Comparar escenario","Compare scenario",listOf("current|${text("Cobertura actual","Current coverage")}","new|${text("Nuevo punto hipotético","Hypothetical new point")}"))
                card("Barrio Jardín","Garden District","2 centros · Área ilustrativa","2 centers · Illustrative area",progress=.8f); card("Barrio del Sol","Sun District",if(value("coverage-scenario")=="new") "Hipótesis: nuevo punto de atención" else "Cobertura por estudiar",if(value("coverage-scenario")=="new") "Hypothesis: new service point" else "Coverage to study"); card("Las Flores","Flower District","Datos insuficientes · Suprimidos","Insufficient data · Suppressed")
                note("No expone hogares ni determina automáticamente que un centro sobra.","Does not expose households or automatically classify a center as unnecessary."); link("regions"); link("assessment")
            }
            "organization" -> { card("Manos Unidas","Helping Hands","Organización de muestra · Miembro","Sample organization · Member","hero"); action("context",if(context) "Actuar como persona" else "Actuar como organización",if(context) "Act as individual" else "Act as organization"); card("42 h · 8 entregas · 12 miembros","42 h · 8 deliveries · 12 members",kind="metrics"); link("campaign"); link("offer"); link("achievements"); action("invite","Vista de invitación a miembros","Preview member invitation"); note("Cada persona acepta participar. La organización no ve su historial privado.","Each person opts in. The organization cannot view private member history.") }
            "businesses" -> { card("Panadería La Esquina","Corner Bakery","PATROCINADO · MUESTRA · Apoyo alimentario documentado","SPONSORED · SAMPLE · Documented food support","hero"); action("collaboration","Ver colaboración","View collaboration"); note("Directorio comercial separado. Pagar no cambia el orden ni la verificación de los centros.","Separate business directory. Payment does not affect center ranking or verification."); link("funding") }
            "funding" -> { card("Ayudar también necesita sostenerse","Helping also needs support",kind="hero"); card("Infraestructura y operación","Infrastructure and operations","Periodo, moneda y gastos efectivamente pagados","Period, currency and actual paid expenses"); card("Patrocinios y directorio","Sponsorships and directory","Ingresos netos después de comisiones y ajustes","Net revenue after fees and adjustments"); note("Todavía no hay reporte financiero publicado. No se inventan ingresos.","No financial report is published yet. No income is invented."); link("businesses") }
            "donate" -> { card(c.title(language),c.title(language),"Donación económica · Flujo futuro","Money donation · Future flow","hero"); field("amount","Importe","Amount","number",optional=true); choice("currency","Moneda","Currency",listOf("MXN","USD","EUR")); note("Falta proveedor y aprobación del modelo del instituto. No se realizarán cargos.","Provider and institute model approval are pending. No charges will be made."); action("checkout","Pago externo no habilitado","External checkout disabled",false); link("tax") }
            "tax" -> { choice("tax-country","País de emisión","Issuing country",listOf("none|${text("Seleccionar","Select")}","MX|México","US|United States","ES|España")); note("Falta país, emisor elegible y proveedor. Una constancia no es un recibo fiscal.","Country, eligible issuer and provider are required. A certificate is not a tax receipt."); action("issue","Emisión no habilitada","Issuance disabled",false); link("achievements") }
            "staff" -> {
                choice("staff-center","Centro autorizado de muestra","Sample authorized center",listOf("c1|Casa Comunitaria Jacaranda","c3|Hogar Los Olivos")); link("editor","Publicar una necesidad","Publish a need")
                card("Ofrecimientos","Offers",records["offer"] ?: "2 ofrecimientos de muestra",records["offer"] ?: "2 sample offers")
                if(records["offer-status"]=="pending") action("confirm-offer","Confirmar ofrecimiento de muestra","Confirm sample offer")
                if(records["offer-status"]=="confirmed") action("receive-offer","Confirmar recepción de muestra","Confirm sample receipt")
                records["editor"]?.let { card("Necesidad simulada","Simulated need",it,it) }; link("case"); link("events"); link("onboarding"); link("operations")
                note("Vista de diseño. No concede roles ni acceso a centros reales.","Design view. Grants no roles or real center access.")
            }
            "editor" -> { field("need-title","Título","Title"); choice("need-type","Tipo de ayuda","Help type",listOf("goods|${text("En especie","Goods")}","time|${text("Tiempo","Time")}")); field("need-quantity","Cantidad","Quantity","number"); field("need-unit","Unidad","Unit"); field("need-date","Fecha límite","Deadline"); field("need-description","Descripción","Description"); submit(); link("staff") }
            "onboarding" -> {
                field("center-name","Nombre del centro","Center name"); choice("new-category","Categoría","Category",listOf("food|${text("Comedor","Kitchen")}","children|${text("Casa hogar","Children's home")}","blood|${text("Banco de sangre","Blood bank")}")); field("public-contact","Contacto público","Public contact"); field("address","Dirección pública","Public address"); field("maps","Enlace HTTPS de Google Maps","Google Maps HTTPS link"); field("lat","Latitud","Latitude"); field("lon","Longitud","Longitude"); choice("new-funding","Origen de recursos","Funding source",listOf("unknown|${text("No informado","Not provided")}","public|${text("Público","Public")}","private|${text("Privado","Private")}","mixed|${text("Mixto","Mixed")}")); submit(); note("Solicitar no equivale a verificación. Sedes protegidas necesitan una ruta sin dirección pública.","Applying is not verification. Protected locations need a route without a public address.")
            }
            "operations" -> {
                card("Cola de revisión","Review queue","Solicitud de alta · Identidad y contacto pendientes","Center application · Identity and contact pending"); card("Moderación de reseñas","Review moderation","Publicación solo tras aprobación","Publish only after approval")
                card("Conector institucional","Institutional connector",if(records["connector"]=="true") "Pausado" else "Última ejecución: revisión pendiente",if(records["connector"]=="true") "Paused" else "Last run: review pending"); action("connector","Pausar / reanudar conector simulado","Pause / resume simulated connector"); action("conflicts","Ver conflictos de muestra","View sample conflicts")
                note("Solo vista nativa del diseño administrativo. Los roles reales se controlan en el servidor.","Native preview of administrative design only. Real roles are controlled by the server.")
            }
            "share" -> { card("Una oportunidad para ayudar","An opportunity to help","Ficha pública de muestra · Sin instalar la app","Sample public profile · No app installation needed","hero"); link("needs"); link("assisted"); action("share-preview","Vista previa para compartir","Sharing preview"); note("No se crea ni envía un enlace público. La futura web permitirá participar desde el navegador.","No public link is created or sent. The future web flow will allow browser participation.") }
            "assisted" -> { note("Un coordinador autorizado registra solo lo necesario con consentimiento.","An authorized coordinator records only what is necessary with consent."); field("alias","Referencia o alias","Reference or alias"); choice("assisted-activity","Actividad","Activity",listOf("food|${text("Entrega de alimentos","Food delivery")}","time|${text("Voluntariado","Volunteering")}")); check("assisted-consent","La persona acepta este registro.","The person agrees to this record."); submit() }
            "profile" -> { card("Lucía Torres","Lucía Torres","Cuenta de diseño · Nivel 3","Design account · Level 3","hero"); link("history"); link("saved"); link("events"); link("organization"); link("organizer","Organizar una campaña","Organize a campaign"); link("staff"); link("onboarding"); link("settings") }
            "settings" -> {
                choice("language","Idioma del prototipo","Prototype language",listOf("$language|${if(language=="es") "Español" else "English"}","${if(language=="es") "en|English" else "es|Español"}"))
                check("urgent","Necesidades urgentes","Urgent needs"); check("nearby","Cerca de mí","Nearby"); check("reminders","Recordatorios de actividades","Activity reminders")
                link("phone","Agregar o verificar celular","Add or verify mobile number"); action("providers","Conexiones Google, Apple y Facebook","Google, Apple and Facebook connections"); link("delete","Eliminar mi cuenta de muestra","Delete my sample account")
                note("No cambia permisos ni preferencias de la cuenta real. La apariencia sigue al dispositivo.","Does not change permissions or real account preferences. Appearance follows the device.")
            }
            "phone" -> { note("Necesario para crear grupos o eventos. No se envía SMS; falta configurar proveedor.","Required to create groups or events. No SMS is sent; provider setup is pending."); field("phone","Número con código de país","Number with country code","phone"); submit(); if(records["phone"]!=null) { field("code","Código (no enviado)","Code (not sent)",optional=true); note("No se verificó ningún número real.","No real number was verified.") } }
            "delete" -> { note("Eliminar una cuenta real impide volver a acceder. Aquí solo se descartan datos de muestra.","Deleting a real account prevents further access. Here only sample data is discarded."); link("settings","Cancelar","Cancel"); action("delete-confirm","Confirmar eliminación de muestra","Confirm sample deletion") }
            "organizer" -> { note("La creación requiere ayuda validada, nivel, puntos y celular verificado. Los umbrales se definirán antes de habilitarla.","Creation requires validated help, level, points and a verified phone. Thresholds will be set before enabling it."); link("history"); link("phone","Verificar celular","Verify phone"); link("campaign","Ver campaña de muestra","View sample campaign") }
            "welcome" -> { card("Pequeñas acciones, gran impacto.","Small actions, big impact.","La generosidad está más cerca de lo que crees.","Generosity is closer than you think.","brand"); link("map","Continuar como invitado","Continue as guest"); link("profile","Iniciar sesión","Sign in"); if(canOpen("profile")) rows += PreviewItem("link","create-account",text("Crear cuenta","Create account"),target="profile"); note("Vista de diseño: no crea cuentas ni acepta documentos legales.","Design view: does not create accounts or accept legal documents.") }
        }
        if(records[route]!=null && route !in listOf("history","messages")) note("Guardado en esta sesión: ${records[route]}","Saved in this session: ${records[route]}")
        return rows
    }
}
