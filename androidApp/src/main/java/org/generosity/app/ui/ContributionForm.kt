package org.generosity.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.generosity.app.*
import org.generosity.app.R
import org.generosity.domain.*

@Composable
internal fun SyncStatus(state: GenerosityUiState, app: GenerosityAppState) {
    if (!state.backendConfigured) return
    Column(verticalArrangement = Arrangement.spacedBy(GenSpace.sm)) {
        TextButton(onClick = app::onResume, enabled = !state.syncing && !state.writing) {
            Text(localizedString(if (state.syncing) R.string.sync_busy else R.string.sync_refresh, state.locale))
        }
        if (state.syncError != null) Text(localizedString(if (state.syncError == "unauthenticated") R.string.sync_auth else R.string.sync_failed, state.locale), color = MaterialTheme.colorScheme.error)
        if (state.pendingOperations.isNotEmpty()) Text(localizedString(R.string.sync_pending, state.locale))
        state.pendingOperations.filter { it.errorCode != null }.forEach { pending ->
            Text(localizedString(R.string.sync_rejected, state.locale), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { app.discardRejectedOperation(pending.id) }, enabled = !state.writing) { Text(localizedString(R.string.sync_discard, state.locale)) }
        }
    }
}

@Composable
internal fun OfferDialog(state: GenerosityUiState, app: GenerosityAppState, need: CenterNeed?, method: HelpMethod, onDismiss: () -> Unit) {
    val locale = state.locale
    var quantity by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(need?.quantityUnit.orEmpty()) }
    var minutes by rememberSaveable { mutableStateOf("") }
    var activity by rememberSaveable { mutableStateOf("") }
    var availability by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val initialSave = remember { state.savedOffer }
    val owner = remember { state.profile.id }
    LaunchedEffect(state.savedOffer, state.profile.id) { if (initialSave != state.savedOffer || owner != state.profile.id) onDismiss() }
    val valid = if (method == HelpMethod.GOODS) (quantity.toIntOrNull() ?: 0) in 1..(need?.quantityRemaining ?: 1_000_000)
        else (minutes.toIntOrNull() ?: 0) in 1..525600 && activity.isNotBlank() && activity.length <= 500
    AlertDialog(onDismissRequest = { if (!state.writing) onDismiss() },
        title = { Text(need?.title?.resolve(locale) ?: localizedString(R.string.personal_add, locale)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (method == HelpMethod.GOODS) {
                    OutlinedTextField(quantity, { quantity = it }, label = { Text(localizedString(R.string.offer_quantity, locale)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(unit, { unit = it }, readOnly = need != null, label = { Text(localizedString(R.string.offer_unit, locale)) })
                } else {
                    OutlinedTextField(minutes, { minutes = it }, label = { Text(localizedString(R.string.offer_minutes, locale)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(activity, { activity = it }, label = { Text(localizedString(R.string.offer_activity, locale)) })
                    OutlinedTextField(availability, { availability = it }, label = { Text(localizedString(R.string.offer_availability, locale)) })
                }
                OutlinedTextField(notes, { notes = it }, label = { Text(localizedString(R.string.offer_notes, locale)) })
                if (need == null) Text(localizedString(R.string.personal_notice, locale))
                if (state.syncError != null) Text(localizedString(R.string.sync_failed, locale), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { Button(enabled = valid && unit.length <= 60 && availability.length <= 500 && notes.length <= 2000 && !state.writing && state.canRecordContributions, onClick = {
            val offer = ContributionOffer(method, quantity = quantity.toIntOrNull(), quantityUnit = unit.trim().takeIf { it.isNotEmpty() }, plannedDurationMinutes = minutes.toIntOrNull(), activity = activity.trim(), availability = availability.trim(), note = notes.trim())
            if (need == null) app.recordPersonalHelp(offer) else app.offerHelp(need.id, offer)
        }) { Text(localizedString(R.string.offer_send, locale)) } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !state.writing) { Text(localizedString(R.string.offer_cancel, locale)) } }
    )
}
