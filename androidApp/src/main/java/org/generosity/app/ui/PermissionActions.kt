package org.generosity.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.generosity.domain.SupportedLocale

@Composable
fun PlatformPermissionActions(locale: SupportedLocale) {
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { }
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(TextKey.OFFICIAL_VERIFICATION_BOUNDARY.label(locale))
        Button(onClick = { launcher.launch(permissions.toTypedArray()) }) {
            Text(TextKey.NEARBY_NOTIFICATIONS.label(locale))
        }
    }
}

