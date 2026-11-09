import SwiftUI
import Shared

private final class NativePreviewModel: ObservableObject {
    let session: PreviewSession
    @Published var revision = 0
    init(groups: [String], language: String, connectedOnly: Bool) { session = PreviewSession(groups: groups, language: language, connectedOnly: connectedOnly) }
    func update(_ action: () -> Void) { action(); revision += 1 }
}

struct NativePreview: View {
    let groups: [String]
    let connectedOnly: Bool
    let onClose: (() -> Void)?
    let connectedContent: ((String, @escaping (String) -> Void) -> AnyView)?
    @StateObject private var model: NativePreviewModel
    @State private var sheet = ""
    @FocusState private var searchFocused: Bool
    @Environment(\.dynamicTypeSize) private var textSize
    let language: String
    init(groups: [String], language: String, connectedOnly: Bool = false, connectedContent: ((String, @escaping (String) -> Void) -> AnyView)? = nil, onClose: (() -> Void)? = nil) {
        self.groups = groups; self.onClose = onClose; self.connectedOnly = connectedOnly
        self.language = language; self.connectedContent = connectedContent
        _model = StateObject(wrappedValue: NativePreviewModel(groups: groups, language: language, connectedOnly: connectedOnly))
    }
    private var session: PreviewSession { model.session }
    var body: some View {
        Group {
            if session.navigationTabs().isEmpty {
                navigation
            } else {
                TabView(selection: Binding(get: { session.navigationTab() }, set: { route in model.update { session.navigate(id: route) } })) {
                    ForEach(session.navigationTabs(), id: \.self) { route in
                        navigation
                            .tabItem { Label(tabTitle(route), systemImage: ["map": "map", "needs": "heart", "help": "plus.circle", "impact": "chart.bar", "profile": "person"][route]!) }
                            .tag(route)
                    }
                }
            }
        }
        .tint(GenPalette.accent).font(GenTypography.font(.body))
        .sheet(isPresented: Binding(get: { !sheet.isEmpty }, set: { if !$0 { sheet = "" } })) {
            NavigationStack {
                ScrollView {
                    VStack(alignment: .leading, spacing: GenSpace.md) {
                        if sheet == "screens" {
                            ForEach(session.availableRoutes(), id: \.id) { route in
                                Button(route.title(language: session.language) + (connectedContent == nil || connectedOnly ? "" : PreviewSession.companion.connectedRoutes.contains(route.id) ? session.text(es: " · Conectado", en: " · Connected") : session.text(es: " · Diseño", en: " · Design"))) { model.update { session.navigate(id: route.id) }; sheet = "" }
                                    .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading).accessibilityIdentifier("preview-route-\(route.id)")
                            }
                            if let onClose {
                                Button(session.text(es: "Salir del diseño", en: "Exit design preview")) { sheet = ""; onClose() }
                                    .frame(minHeight: 44)
                            }
                        } else if let connectedContent {
                            connectedContent(sheet) { route in model.update { session.navigate(id: route) }; sheet = "" }
                        } else if sheet == "list" {
                            ForEach(session.visibleCenters(), id: \.id) { center in
                                Button(center.title(language: session.language)) { model.update { session.selectCenter(id: center.id) }; sheet = "" }.frame(minHeight: 44).accessibilityIdentifier("preview-list-\(center.id)")
                            }
                        } else {
                            ForEach(session.items().filter { ["category", "funding", "scenario", "locate", "reset"].contains($0.id) }, id: \.id) { control($0) }
                        }
                    }.padding(GenSpace.lg)
                }.toolbar { Button(session.text(es: "Cerrar", en: "Close")) { sheet = "" } }
            }.tint(GenPalette.accent)
        }
        .onChange(of: groups) { updated in model.update { session.updateAccess(groups: updated) }; if updated.isEmpty { onClose?() } }
        .onChange(of: language) { code in model.update { session.updateLanguage(code: code) } }
    }
    private func tabTitle(_ route: String) -> String {
        switch route {
        case "map": return session.text(es: "Mapa", en: "Map")
        case "needs": return session.text(es: "Necesidades", en: "Needs")
        case "help": return session.text(es: "Ayudar", en: "Help")
        case "impact": return session.text(es: "Impacto", en: "Impact")
        default: return session.text(es: "Perfil", en: "Profile")
        }
    }
    private var navigation: some View {
        NavigationStack {
            screen
                .navigationBarTitleDisplayMode(.inline)
                .toolbarBackground(session.route == "map" ? .hidden : .automatic, for: .navigationBar)
                .toolbar {
                    ToolbarItemGroup(placement: .navigationBarLeading) {
                        if session.route == "map" {
                            Button { searchFocused = false; sheet = "list" } label: {
                                Label(session.text(es: "Lista", en: "List"), systemImage: "list.bullet")
                            }.accessibilityIdentifier("preview-list")
                            Button { searchFocused = false; sheet = "filters" } label: {
                                Label(session.text(es: "Filtros", en: "Filters"), systemImage: "slider.horizontal.3")
                            }.accessibilityIdentifier("preview-filters")
                        } else {
                            Button { model.update { session.back() } } label: {
                                Label(session.text(es: "Atrás", en: "Back"), systemImage: "chevron.left")
                            }
                        }
                    }
                    ToolbarItem(placement: .principal) {
                        if session.route == "map" {
                            BrandWordmark(width: GenSize.wordmarkWidth * 2 / 3)
                        } else {
                            Text(session.title()).font(GenTypography.font(.headline)).accessibilityIdentifier("preview-title")
                        }
                    }
                    ToolbarItemGroup(placement: .navigationBarTrailing) {
                        if session.route == "map" && session.canOpen(id: "inbox") {
                            Button { searchFocused = false; model.update { session.navigate(id: "inbox") } } label: {
                                Label(session.text(es: "Notificaciones", en: "Notifications"), systemImage: "bell")
                            }.accessibilityIdentifier("preview-notifications")
                        }
                        Button { searchFocused = false; sheet = "screens" } label: {
                            Label(session.text(es: "Explorar pantallas", en: "Explore screens"), systemImage: "square.grid.2x2")
                        }.accessibilityIdentifier("preview-screens")
                    }
                }
        }
    }
    private var screen: some View {
        VStack(spacing: GenSpace.sm) {
            if let connectedContent, PreviewSession.companion.connectedRoutes.contains(session.route) {
                connectedContent(session.route) { route in model.update { session.navigate(id: route) } }
            } else if session.route == "map" {
                if textSize.isAccessibilitySize { ScrollView { map.frame(height: 850) } }
                else { map }
            }
            else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: GenSpace.md) {
                        ForEach(session.items(), id: \.id) { item in control(item) }
                    }.padding(GenSpace.lg)
                }.id(session.route).accessibilityIdentifier("preview-content")
            }
            if !session.message.isEmpty {
                Text(session.message).font(GenTypography.font(.footnote)).padding(.horizontal, GenSpace.md).accessibilityIdentifier("preview-feedback")
            }
        }.background(GenPalette.background)
    }
    @ViewBuilder private func control(_ item: PreviewItem) -> some View {
        Group {
            switch item.kind {
            case "field":
                VStack(alignment: .leading, spacing: GenSpace.xs) {
                    Text(item.title).font(GenTypography.font(.subheadline))
                    TextField("", text: Binding(get: { session.value(id: item.id) }, set: { value in model.update { session.setField(id: item.id, value: value) } }), axis: .vertical)
                        .textFieldStyle(.roundedBorder).lineLimit(1...4).accessibilityLabel(item.title)
                        .keyboardType(item.target == "number" ? .decimalPad : item.target == "phone" ? .phonePad : .default)
                }
            case "choice":
                VStack(alignment: .leading) {
                    Text(item.title).font(GenTypography.font(.subheadline))
                    Picker(item.title, selection: Binding(get: { item.value }, set: { value in model.update { session.setField(id: item.id, value: value) } })) {
                        ForEach(item.options, id: \.self) { option in
                            Text(option.components(separatedBy: "|").last!).tag(option.components(separatedBy: "|")[0])
                        }
                    }.pickerStyle(.menu).frame(minHeight: 44)
                }
            case "check":
                Toggle(item.title, isOn: Binding(get: { item.value == "true" }, set: { value in model.update { session.setField(id: item.id, value: value ? "true" : "false") } }))
            case "action", "link":
                Button { model.update { session.act(id: item.id) } } label: {
                    HStack { Text(item.title); Spacer(); if item.kind == "link" { Image(systemName: "chevron.right") } }
                        .padding(GenSpace.md).frame(maxWidth: .infinity, minHeight: 48)
                        .foregroundStyle(item.kind == "action" ? GenPalette.onAccent : GenPalette.accent)
                        .background(item.kind == "action" ? GenPalette.accent : GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                }.disabled(!item.enabled).opacity(item.enabled ? 1 : 0.5)
            case "center":
                Button { model.update { session.selectCenter(id: item.id); session.navigate(id: "center") } } label: {
                    HStack { IconTile(symbol: "house"); Text(item.title); if item.value == "verified" { Image(systemName: "checkmark.seal.fill") } }
                        .padding(GenSpace.md).frame(maxWidth: .infinity, alignment: .leading).background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                }.disabled(connectedContent != nil) // Fixture IDs have no real center destination.
            default:
                VStack(alignment: .leading, spacing: GenSpace.md) {
                    if item.kind == "brand" {
                        Image("GenerositySymbol").resizable().scaledToFit().frame(height: 120).accessibilityHidden(true)
                        BrandWordmark().padding(.vertical, GenSpace.md)
                    }
                    Text(item.title).font(GenTypography.font(["hero", "brand"].contains(item.kind) ? .title : item.kind == "note" ? .footnote : .headline))
                    if !item.detail.isEmpty { Text(item.detail).font(GenTypography.font(.subheadline)) }
                    if item.progress >= 0 { ProgressView(value: Double(item.progress)).accessibilityLabel(item.title + " " + item.detail) }
                }.padding(GenSpace.lg).frame(maxWidth: .infinity, alignment: .leading)
                    .background(["hero", "brand"].contains(item.kind) ? GenPalette.accent.opacity(0.12) : GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
            }
        }.accessibilityIdentifier("preview-\(item.id)")
    }
    private var searchContent: some View {
        HStack(spacing: GenSpace.sm) {
            Image(systemName: "magnifyingglass").foregroundStyle(GenPalette.secondary).accessibilityHidden(true)
            TextField(session.text(es: "Centro, colonia o dirección", en: "Center, area or address"),
                      text: Binding(get: { session.value(id: "query") }, set: { query in model.update { session.setField(id: "query", value: query) } }),
                      prompt: Text(session.text(es: "Centro, colonia o dirección", en: "Center, area or address")).foregroundColor(GenPalette.secondary))
                .font(GenTypography.font(.subheadline)).textFieldStyle(.plain)
                .foregroundStyle(GenPalette.text)
                .textInputAutocapitalization(.never).autocorrectionDisabled().submitLabel(.search)
                .focused($searchFocused).onSubmit { searchFocused = false }
                .accessibilityIdentifier("preview-query")
            Button { searchFocused = false; model.update { session.act(id: "locate") } } label: {
                Image(systemName: "location").frame(width: GenSize.iosTouch, height: GenSize.iosTouch)
            }.accessibilityLabel(session.text(es: "Usar ubicación simulada", en: "Use simulated location"))
                .accessibilityIdentifier("preview-locate")
        }.padding(.leading, GenSpace.md).padding(.trailing, GenSpace.xs).padding(.vertical, GenSpace.xs)
    }
    @ViewBuilder private var searchBar: some View {
        if #available(iOS 26.0, *) { searchContent.glassEffect(.regular, in: Capsule()) }
        else { searchContent.background(.regularMaterial, in: Capsule()) }
    }
    private var map: some View {
        GeometryReader { geometry in
            ZStack {
                Canvas { context, size in
                    context.fill(Path(CGRect(origin: .zero, size: size)), with: .color(GenPalette.categoryCommunity.opacity(0.13)))
                    var streets = Path()
                    for i in 0...10 {
                        streets.move(to: CGPoint(x: CGFloat(i) * size.width / 6 - 120, y: 0))
                        streets.addLine(to: CGPoint(x: CGFloat(i) * size.width / 6 + 120, y: size.height))
                    }
                    for i in 0...12 {
                        streets.move(to: CGPoint(x: 0, y: CGFloat(i) * size.height / 9))
                        streets.addLine(to: CGPoint(x: size.width, y: CGFloat(i) * size.height / 9 - 100))
                    }
                    context.stroke(streets, with: .color(GenPalette.surface), lineWidth: 10)
                    context.fill(Path(ellipseIn: CGRect(x: size.width * 0.65, y: 20, width: 150, height: 160)), with: .color(GenPalette.categoryHealth.opacity(0.15)))
                }.accessibilityHidden(true).ignoresSafeArea(edges: .top)
                ForEach(session.visibleCenters(), id: \.id) { center in
                    Button { model.update { session.selectCenter(id: center.id) } } label: {
                        Image(systemName: ["blood": "drop.fill", "health": "cross.case", "food": "fork.knife", "training": "graduationcap", "children": "house", "seniors": "person.2"][center.category]!)
                            .foregroundStyle(GenPalette.onAccent).frame(width: 48, height: 48)
                            .background(session.selected == center.id ? GenPalette.accent : GenPalette.categoryOther, in: Circle())
                    }.position(x: CGFloat(center.x) * geometry.size.width, y: CGFloat(0.35 + (center.y - 0.25) / 0.55 * 0.35) * geometry.size.height)
                        .accessibilityLabel(center.title(language: session.language)).accessibilityIdentifier("preview-pin-\(center.id)")
                }
                if session.value(id: "origin") == "true" { Image(systemName: "location.circle.fill").foregroundStyle(GenPalette.accent).accessibilityLabel(session.text(es: "Ubicación simulada", en: "Simulated location")).accessibilityIdentifier("preview-origin") }
                VStack(spacing: GenSpace.sm) {
                    searchBar
                    if let category = session.items().first(where: { $0.id == "category" }) {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack {
                                ForEach(category.options, id: \.self) { option in
                                    let parts = option.components(separatedBy: "|")
                                    Button(parts.last!) { model.update { session.setField(id: "category", value: parts[0]) } }
                                        .padding(.horizontal, GenSpace.md).frame(minHeight: 44)
                                        .background(category.value == parts[0] ? GenPalette.accentContainer : GenPalette.surface, in: Capsule())
                                }
                            }
                        }
                    }
                    if session.value(id: "scenario") == "offline" {
                        Text(session.text(es: "Sin conexión · Información ficticia guardada", en: "Offline · Saved fictional information"))
                            .font(GenTypography.font(.footnote)).padding(GenSpace.sm).background(GenPalette.surface)
                    }
                    Spacer(minLength: 20)
                    if let center = session.visibleCenters().first(where: { $0.id == session.selected }) {
                        VStack(alignment: .leading, spacing: GenSpace.sm) {
                            Text(center.title(language: session.language) + (center.verified ? " ✓" : "")).font(GenTypography.font(.headline))
                            Text("\(center.distance) km · \(center.area)").font(GenTypography.font(.footnote))
                            Button(session.text(es: "Ver centro y necesidades", en: "View center and needs")) { model.update { session.navigate(id: "center") } }.frame(minHeight: 44).accessibilityIdentifier("preview-details")
                        }.padding(GenSpace.md).frame(maxWidth: .infinity, alignment: .leading).background(GenPalette.surface, in: RoundedRectangle(cornerRadius: GenRadius.card))
                    } else { Text(session.text(es: "Sin resultados · Cambia los filtros", en: "No results · Change filters")).padding(GenSpace.md).background(GenPalette.surface) }
                }.padding(GenSpace.sm)
            }
        }
    }
}
