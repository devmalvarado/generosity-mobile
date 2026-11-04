package org.generosity.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.generosity.app.R
import org.generosity.domain.DiscoveryArea
import org.generosity.domain.SupportedLocale

@Composable
internal fun AreaSearchDialog(
    locale: SupportedLocale, search: suspend (String, SupportedLocale) -> List<DiscoveryArea>,
    onSelect: (DiscoveryArea) -> Unit, onDismiss: () -> Unit, initialQuery: String = ""
) {
    var query by remember { mutableStateOf(initialQuery.take(200)) }
    var results by remember { mutableStateOf<List<DiscoveryArea>>(emptyList()) }
    var message by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    fun submit() {
        if (query.isBlank() || busy) return
        val token = ++generation
        busy = true; message = null; results = emptyList()
        job = scope.launch {
            try {
                val found = withTimeout(15_000) { search(query.trim(), locale) }
                if (token != generation) return@launch
                results = found
                if (results.isEmpty()) message = R.string.area_empty
            } catch (_: TimeoutCancellationException) { if (token == generation) message = R.string.area_failed }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { if (token == generation) message = R.string.area_failed }
            finally { if (token == generation) busy = false }
        }
    }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(localizedString(R.string.area_search, locale)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
                Text(localizedString(R.string.area_hint, locale))
                OutlinedTextField(query, onValueChange = {
                    generation++; job?.cancel(); busy = false; results = emptyList(); message = null; query = it.take(200)
                }, label = { Text(localizedString(R.string.area_query, locale)) }, singleLine = true,
                    modifier = Modifier.testTag("area-query"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }))
                if (busy) Text(localizedString(R.string.area_loading, locale))
                message?.let { Text(localizedString(it, locale)) }
                results.forEachIndexed { index, place ->
                    TextButton(onClick = { onSelect(place) }, modifier = Modifier.fillMaxWidth().testTag("area-result-$index")) { Text(place.label) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { submit() }, enabled = !busy && query.isNotBlank(), modifier = Modifier.testTag("area-submit")) { Text(localizedString(R.string.area_submit, locale)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(localizedString(R.string.offer_cancel, locale)) } }
    )
}
