package org.generosity.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import org.generosity.preview.*
import org.generosity.app.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NativePreview(groups: List<String>, language: String, owner: String,
    connectedContent: (@Composable (String, (String) -> Unit) -> Unit)? = null,
    onClose: () -> Unit) {
    val session = remember(owner) { PreviewSession(groups, language) }
    var revision by remember { mutableIntStateOf(0) }
    var inventory by remember { mutableStateOf(false) }
    var list by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    fun update(action: () -> Unit) { action(); revision++ }
    LaunchedEffect(groups) { update { session.updateAccess(groups) }; if(groups.isEmpty()) onClose() }
    LaunchedEffect(language) { update { session.updateLanguage(language) } }
    BackHandler { update { session.back() } }
    // Fixture state remains isolated; connected destinations observe the existing app state.
    val items = remember(revision, groups) { session.items() }
    val title = session.title()
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            CenterAlignedTopAppBar(
                title = {
                    if (session.route == "map") BrandWordmark(width = GenSize.wordmarkWidth * 2 / 3)
                    else Text(title, Modifier.testTag("preview-title"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    if (session.route == "map") Row {
                        IconButton(onClick = { focusManager.clearFocus(); list = true }, modifier = Modifier.testTag("preview-list").semantics { contentDescription = session.text("Lista", "List") }) { AppIcon(R.drawable.ic_list, tint = LocalContentColor.current) }
                        IconButton(onClick = { focusManager.clearFocus(); filters = true }, modifier = Modifier.testTag("preview-filters").semantics { contentDescription = session.text("Filtros", "Filters") }) { AppIcon(R.drawable.ic_settings, tint = LocalContentColor.current) }
                    } else TextButton(onClick = { update { session.back() } }) { Text(session.text("Atrás", "Back")) }
                },
                actions = {
                    if (session.route == "map" && session.canOpen("inbox")) {
                        IconButton(onClick = { focusManager.clearFocus(); update { session.navigate("inbox") } }, modifier = Modifier.testTag("preview-notifications").semantics { contentDescription = session.text("Notificaciones", "Notifications") }) {
                            AppIcon(R.drawable.ic_notifications, tint = LocalContentColor.current)
                        }
                    }
                    IconButton(onClick = { focusManager.clearFocus(); inventory = true }, modifier = Modifier.testTag("preview-screens").semantics { contentDescription = session.text("Explorar pantallas", "Explore screens") }) {
                        AppIcon(R.drawable.ic_grid, tint = LocalContentColor.current)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
            if (connectedContent != null && session.route in PreviewSession.connectedRoutes) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    connectedContent(session.route) { route -> update { session.navigate(route) } }
                }
            } else if (session.route == "map") {
                Box(Modifier.weight(1f).fillMaxWidth().testTag("preview-map")) {
                    PreviewMap(session.visibleCenters(), session.selected, session.language, session.value("origin") == "true", Modifier.fillMaxSize()) { id -> update { session.selectCenter(id) }; list = false }
                    Column(Modifier.align(Alignment.TopCenter).padding(GenSpace.sm), verticalArrangement = Arrangement.spacedBy(GenSpace.xs)) {
                        Surface(shape = RoundedCornerShape(GenRadius.pill), shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
                            TextField(
                                value = session.value("query"), onValueChange = { update { session.setField("query", it) } },
                                placeholder = { Text(session.text("Centro, colonia o dirección", "Center, area or address"), maxLines = 1) },
                                leadingIcon = { AppIcon(R.drawable.ic_search) },
                                trailingIcon = {
                                    IconButton(onClick = { focusManager.clearFocus(); update { session.act("locate") } }, modifier = Modifier.testTag("preview-locate").semantics { contentDescription = session.text("Usar ubicación simulada", "Use simulated location") }) {
                                        AppIcon(R.drawable.ic_location, tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("preview-query").semantics { contentDescription = session.text("Centro, colonia o dirección", "Center, area or address") },
                                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                            )
                        }
                        items.firstOrNull { it.id == "category" }?.let { item ->
                            Row(Modifier.horizontalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(GenRadius.card)),horizontalArrangement=Arrangement.spacedBy(GenSpace.xs)) {
                                item.options.forEach { option -> val code=option.substringBefore('|'); FilterChip(colors=FilterChipDefaults.filterChipColors(selectedContainerColor=MaterialTheme.colorScheme.primaryContainer),selected=item.value==code,onClick={update{session.setField(item.id,code)}},label={Text(option.substringAfter('|',option))}) }
                            }
                        }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(GenSpace.sm)) {
                        if(session.value("scenario")=="offline") Text(session.text("Sin conexión · Información ficticia guardada","Offline · Saved fictional information"),Modifier.background(MaterialTheme.colorScheme.surface).padding(GenSpace.sm),style=MaterialTheme.typography.bodySmall)
                        val center = session.visibleCenters().firstOrNull { it.id==session.selected }
                        if(center!=null) Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(GenRadius.card),shadowElevation=3.dp) {
                            Column(Modifier.padding(GenSpace.md)) {
                                Text(center.title(session.language)+(if(center.verified) " ✓" else ""),style=MaterialTheme.typography.titleMedium)
                                Text("${center.distance} km · ${center.area}",style=MaterialTheme.typography.bodySmall)
                                Button(onClick={update {session.navigate("center")}},modifier=Modifier.testTag("preview-details")) { Text(session.text("Ver centro y necesidades","View center and needs")) }
                            }
                        } else Surface { Text(session.text("Sin resultados · Cambia los filtros", "No results · Change filters"),Modifier.padding(GenSpace.md)) }
                    }
                }
            } else {
                key(session.route) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(GenSpace.lg).testTag("preview-content"),verticalArrangement=Arrangement.spacedBy(GenSpace.md)) {
                        items.forEach { item -> PreviewControl(item,session, connectedContent != null) { update(it) } }
                    }
                }
            }
            if(session.message.isNotBlank()) Text(session.message, Modifier.padding(GenSpace.sm).semantics { liveRegion=LiveRegionMode.Polite }.testTag("preview-feedback"),style=MaterialTheme.typography.bodySmall)
            if (session.navigationTabs().isNotEmpty()) NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
                session.navigationTabs().forEach { route ->
                    NavigationBarItem(
                        selected = session.navigationTab() == route,
                        onClick = { update { session.navigate(route) } },
                        modifier = Modifier.testTag("preview-tab-$route"),
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                        icon = { AppIcon(when (route) {
                            "map" -> R.drawable.ic_map
                            "needs" -> R.drawable.ic_list
                            "help" -> R.drawable.ic_gift
                            "impact" -> R.drawable.ic_heart
                            else -> R.drawable.ic_person
                        }, tint = LocalContentColor.current) },
                        label = { Text(when (route) {
                            "map" -> session.text("Mapa", "Map")
                            "needs" -> session.text("Necesidades", "Needs")
                            "help" -> session.text("Ayudar", "Help")
                            "impact" -> session.text("Impacto", "Impact")
                            else -> session.text("Perfil", "Profile")
                        }) }
                    )
                }
            }
        }
    }
    if(inventory || list || filters) AlertDialog(onDismissRequest={inventory=false;list=false;filters=false},
        title={Text(session.text(if(inventory) "Pantallas" else if(list) "Centros en esta zona" else "Filtros",if(inventory) "Screens" else if(list) "Centers in this area" else "Filters"))},
        text={ Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(GenSpace.sm)) {
            when {
                inventory -> session.availableRoutes().forEach { route -> TextButton(onClick={update{session.navigate(route.id)};inventory=false},modifier=Modifier.testTag("preview-route-${route.id}")) {
                    Text(route.title(session.language) + if (connectedContent == null) "" else if (route.id in PreviewSession.connectedRoutes) session.text(" · Conectado", " · Connected") else session.text(" · Diseño", " · Design"))
                } }
                connectedContent != null -> connectedContent(if (list) "list" else "filters") { route -> update { session.navigate(route) }; list=false; filters=false }
                list -> session.visibleCenters().forEach { c -> TextButton(onClick={update{session.selectCenter(c.id)};list=false},modifier=Modifier.testTag("preview-list-${c.id}")) { Text(c.title(session.language)) } }
                else -> items.filter { it.id in listOf("category","funding","scenario","locate","reset") }.forEach { PreviewControl(it,session) { update(it) } }
            }
        }},confirmButton={TextButton(onClick={inventory=false;list=false;filters=false}){Text(session.text("Cerrar","Close"))}},
        dismissButton={if(inventory) TextButton(onClick=onClose){Text(session.text("Salir del diseño","Exit design preview"))}})
}

@Composable
private fun PreviewControl(item: PreviewItem, session: PreviewSession, connected: Boolean = false, update: (() -> Unit) -> Unit) {
    val modifier=Modifier.fillMaxWidth().testTag("preview-${item.id}")
    when(item.kind) {
        "field" -> OutlinedTextField(value=item.value,onValueChange={update{session.setField(item.id,it)}},label={Text(item.title)},modifier=modifier,
            keyboardOptions=KeyboardOptions(keyboardType=when(item.target){"number"->KeyboardType.Decimal;"phone"->KeyboardType.Phone;else->KeyboardType.Text}),shape=RoundedCornerShape(GenRadius.card),maxLines=4)
        "choice" -> Column(modifier) {
            Text(item.title,style=MaterialTheme.typography.labelLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(GenSpace.xs)) {
                item.options.forEach { option -> val code=option.substringBefore('|'); FilterChip(colors=FilterChipDefaults.filterChipColors(selectedContainerColor=MaterialTheme.colorScheme.primaryContainer),selected=item.value==code,onClick={update{session.setField(item.id,code)}},label={Text(option.substringAfter('|',option))}) }
            }
        }
        "check" -> Row(modifier.clickable(role=Role.Checkbox){update{session.setField(item.id,(item.value!="true").toString())}},verticalAlignment=Alignment.CenterVertically) {
            Checkbox(checked=item.value=="true",onCheckedChange=null); Text(item.title,Modifier.padding(GenSpace.sm))
        }
        "action", "link" -> if(item.kind=="action") Button(onClick={update{session.act(item.id)}},enabled=item.enabled,modifier=modifier,shape=RoundedCornerShape(GenRadius.card)){Text(item.title)}
            else OutlinedButton(onClick={update{session.act(item.id)}},modifier=modifier,shape=RoundedCornerShape(GenRadius.card)){ Text(item.title,Modifier.weight(1f)); Text("›") }
        "center" -> OutlinedButton(enabled = !connected, onClick={update{session.selectCenter(item.id);session.navigate("center")}},modifier=modifier){Text(item.title + if(item.value=="verified") " ✓" else "")}
        else -> {
            val hero=item.kind in listOf("hero","brand")
            val bg=if(hero) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            Surface(modifier,color=bg,shape=RoundedCornerShape(GenRadius.card)) {
                Column(Modifier.padding(GenSpace.lg),verticalArrangement=Arrangement.spacedBy(GenSpace.sm)) {
                    if(item.kind=="brand") { BrandWordmark(); Spacer(Modifier.height(GenSpace.lg)) }
                    Text(item.title,style=when(item.kind){"hero","brand"->MaterialTheme.typography.headlineMedium;"note"->MaterialTheme.typography.bodySmall;else->MaterialTheme.typography.titleMedium})
                    if(item.detail.isNotBlank()) Text(item.detail,style=MaterialTheme.typography.bodyMedium)
                    if(item.progress>=0) LinearProgressIndicator(progress={item.progress},modifier=Modifier.fillMaxWidth().height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviewMap(centers: List<PreviewCenter>, selection: String, language: String, showOrigin: Boolean, modifier: Modifier, onSelect: (String)->Unit) {
    val street=MaterialTheme.colorScheme.surface
    val block=MaterialTheme.colorScheme.surfaceVariant
    val park=MaterialTheme.colorScheme.primaryContainer
    BoxWithConstraints(modifier.background(block).clipToBounds()) {
        Canvas(Modifier.fillMaxSize()) {
            for(i in 0..10) drawLine(street,Offset(i*size.width/6-120,0f),Offset(i*size.width/6+120,size.height),12.dp.toPx())
            for(i in 0..12) drawLine(street,Offset(0f,i*size.height/9),Offset(size.width,i*size.height/9-100),9.dp.toPx())
            drawCircle(park,110.dp.toPx(),Offset(size.width*.85f,size.height*.22f))
            val road=Path().apply {moveTo(-20f,size.height*.2f);cubicTo(size.width*.6f,size.height*.3f,0f,size.height*.6f,size.width+20,size.height*.8f)}
            drawPath(road,street,style=androidx.compose.ui.graphics.drawscope.Stroke(24.dp.toPx()))
        }
        centers.forEach { c ->
            val selected=c.id==selection
            Button(onClick={onSelect(c.id)},modifier=Modifier.offset(x=(maxWidth*c.x-24.dp).coerceAtLeast(0.dp),y=(maxHeight*(.35f+(c.y-.25f)/.55f*.35f)-24.dp).coerceAtLeast(0.dp)).size(48.dp).testTag("preview-pin-${c.id}").semantics{contentDescription=c.title(language);this.selected=selected},contentPadding=PaddingValues(0.dp),shape=RoundedCornerShape(24.dp),colors=ButtonDefaults.buttonColors(containerColor=if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                AppIcon(when(c.category){"blood"->R.drawable.ic_heart;"health"->R.drawable.ic_health;"food"->R.drawable.ic_food;"training"->R.drawable.ic_education;else->R.drawable.ic_people}, tint=if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
            }
        }
        if(showOrigin) Text("●",modifier=Modifier.align(Alignment.Center).testTag("preview-origin").semantics{contentDescription=if(language=="es") "Ubicación simulada" else "Simulated location"},color=MaterialTheme.colorScheme.primary)
    }
}
