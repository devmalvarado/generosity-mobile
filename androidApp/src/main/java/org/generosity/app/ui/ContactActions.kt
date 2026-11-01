package org.generosity.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.generosity.app.R
import org.generosity.domain.ContactInfo
import org.generosity.domain.SupportedLocale

@Composable
internal fun ContactActions(contact: ContactInfo, locale: SupportedLocale) {
    val context = LocalContext.current
    var failed by remember(contact) { mutableStateOf(false) }
    Column {
        contact.links().forEach { link ->
            TextButton(onClick = {
                failed = runCatching { context.startActivity(Intent(if (link.kind == "phone") Intent.ACTION_DIAL else Intent.ACTION_VIEW, Uri.parse(link.url))) }.isFailure
            }) {
                Text(localizedString(when (link.kind) { "phone" -> R.string.contact_phone; "email" -> R.string.contact_email; else -> R.string.contact_website }, locale))
            }
        }
        if (failed) Text(localizedString(R.string.contact_failed, locale), color = MaterialTheme.colorScheme.error)
    }
}
