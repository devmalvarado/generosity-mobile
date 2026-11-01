package org.generosity.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.generosity.app.R
import org.generosity.domain.SupportedLocale

@Composable
fun WelcomeScreen(locale: SupportedLocale, busy: Boolean, failed: Boolean, onGuest: () -> Unit, onSignIn: () -> Unit, onCreate: () -> Unit) {
    GenerosityTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(GenSpace.welcome), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(GenSpace.xl)) {
                Spacer(Modifier.height(48.dp))
                Image(painterResource(R.drawable.generosity_mark), null, Modifier.size(GenSize.symbol))
                BrandWordmark()
                Text(localizedString(R.string.welcome_tagline, locale), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(32.dp))
                Button(onClick = onGuest, modifier = Modifier.fillMaxWidth().heightIn(min = GenSize.androidTouch)) { Text(localizedString(R.string.welcome_guest, locale)) }
                OutlinedButton(onClick = onSignIn, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = GenSize.androidTouch)) { Text(localizedString(R.string.auth_sign_in, locale)) }
                TextButton(onClick = onCreate, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = GenSize.androidTouch)) { Text(localizedString(R.string.welcome_create, locale)) }
                if (busy) CircularProgressIndicator()
                if (failed) Text(localizedString(R.string.auth_failed, locale), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
