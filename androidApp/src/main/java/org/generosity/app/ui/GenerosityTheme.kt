package org.generosity.app.ui

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import org.generosity.app.R
import org.generosity.domain.CenterCategory

private val LightColors = lightColorScheme(
    primary = Color(0xFFC6401D), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE5), onPrimaryContainer = Color(0xFF8F2C14),
    background = Color(0xFFFAF8F5), onBackground = Color(0xFF191C22),
    surface = Color.White, onSurface = Color(0xFF191C22),
    surfaceVariant = Color(0xFFF2F0ED), onSurfaceVariant = Color(0xFF59616C),
    outline = Color(0xFF757B83), outlineVariant = Color(0xFFE4E2DF)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB197), onPrimary = Color(0xFF4C1500),
    primaryContainer = Color(0xFF592717), onPrimaryContainer = Color(0xFFFFD9CC),
    background = Color(0xFF17191C), onBackground = Color(0xFFF7F5F2),
    surface = Color(0xFF222529), onSurface = Color(0xFFF7F5F2),
    surfaceVariant = Color(0xFF30343A), onSurfaceVariant = Color(0xFFB8BCC3),
    outline = Color(0xFF9299A3), outlineVariant = Color(0xFF444950)
)

@Composable
fun GenerosityTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}

@Composable
fun BrandWordmark() {
    Image(painterResource(R.drawable.generosity_wordmark), "Generosity",
        Modifier.size(180.dp, 36.dp).testTag("brand-wordmark"))
}

@Composable
fun AppIcon(@DrawableRes icon: Int, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(painterResource(icon), contentDescription = null, modifier = modifier.size(24.dp), tint = tint)
}

@Composable
fun CategoryBadge(category: CenterCategory?, size: Int = 44) {
    val color = when (category) {
        CenterCategory.BLOOD_DONATION, CenterCategory.HEALTH, CenterCategory.DISABILITY_SUPPORT -> Color(0xFF357D64)
        CenterCategory.COMMUNITY_KITCHEN, CenterCategory.OLDER_ADULTS -> Color(0xFF9A581D)
        else -> Color(0xFF7060A5)
    }
    Box(Modifier.size(size.dp).background(color.copy(alpha = .13f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        AppIcon(category.icon(), tint = if (isSystemInDarkTheme()) Color(0xFFD6C9FF) else color)
    }
}

fun CenterCategory?.icon(): Int = when (this) {
    CenterCategory.BLOOD_DONATION, CenterCategory.HEALTH, CenterCategory.ORGAN_TISSUE_DONATION -> R.drawable.ic_health
    CenterCategory.EDUCATION -> R.drawable.ic_education
    CenterCategory.COMMUNITY_KITCHEN -> R.drawable.ic_food
    CenterCategory.ADDICTION_RECOVERY -> R.drawable.ic_heart
    else -> R.drawable.ic_people
}
