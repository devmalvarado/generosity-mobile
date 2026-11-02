package org.generosity.app.ui

import android.app.Activity
import android.graphics.Typeface
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontFamily
import org.generosity.app.R
import org.generosity.domain.CenterCategory

private val LightColors = lightColorScheme(
    primary = GenColors.accentLight,
    onPrimary = GenColors.onAccentLight,
    primaryContainer = GenColors.accentContainerLight,
    onPrimaryContainer = GenColors.onAccentContainerLight,
    background = GenColors.backgroundLight,
    onBackground = GenColors.textLight,
    surface = GenColors.surfaceLight,
    onSurface = GenColors.textLight,
    surfaceVariant = GenColors.surfaceVariantLight,
    onSurfaceVariant = GenColors.secondaryLight,
    outline = GenColors.outlineLight,
    outlineVariant = GenColors.lineLight,
    error = GenColors.errorLight,
    onError = GenColors.onErrorLight
)
private val DarkColors = darkColorScheme(
    primary = GenColors.accentDark,
    onPrimary = GenColors.onAccentDark,
    primaryContainer = GenColors.accentContainerDark,
    onPrimaryContainer = GenColors.onAccentContainerDark,
    background = GenColors.backgroundDark,
    onBackground = GenColors.textDark,
    surface = GenColors.surfaceDark,
    onSurface = GenColors.textDark,
    surfaceVariant = GenColors.surfaceVariantDark,
    onSurfaceVariant = GenColors.secondaryDark,
    outline = GenColors.outlineDark,
    outlineVariant = GenColors.lineDark,
    error = GenColors.errorDark,
    onError = GenColors.onErrorDark
)

private val AppTypography = Typography().let { base ->
    val body = FontFamily(Typeface.create(GenType.body, Typeface.NORMAL))
    val heading = FontFamily(Typeface.create(GenType.heading, Typeface.NORMAL))
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = heading, fontSize = base.displayLarge.fontSize * GenType.scale, lineHeight = base.displayLarge.lineHeight * GenType.scale),
        displayMedium = base.displayMedium.copy(fontFamily = heading, fontSize = base.displayMedium.fontSize * GenType.scale, lineHeight = base.displayMedium.lineHeight * GenType.scale),
        displaySmall = base.displaySmall.copy(fontFamily = heading, fontSize = base.displaySmall.fontSize * GenType.scale, lineHeight = base.displaySmall.lineHeight * GenType.scale),
        headlineLarge = base.headlineLarge.copy(fontFamily = heading, fontSize = base.headlineLarge.fontSize * GenType.scale, lineHeight = base.headlineLarge.lineHeight * GenType.scale),
        headlineMedium = base.headlineMedium.copy(fontFamily = heading, fontSize = base.headlineMedium.fontSize * GenType.scale, lineHeight = base.headlineMedium.lineHeight * GenType.scale),
        headlineSmall = base.headlineSmall.copy(fontFamily = heading, fontSize = base.headlineSmall.fontSize * GenType.scale, lineHeight = base.headlineSmall.lineHeight * GenType.scale),
        titleLarge = base.titleLarge.copy(fontFamily = heading, fontSize = base.titleLarge.fontSize * GenType.scale, lineHeight = base.titleLarge.lineHeight * GenType.scale),
        titleMedium = base.titleMedium.copy(fontFamily = heading, fontSize = base.titleMedium.fontSize * GenType.scale, lineHeight = base.titleMedium.lineHeight * GenType.scale),
        titleSmall = base.titleSmall.copy(fontFamily = heading, fontSize = base.titleSmall.fontSize * GenType.scale, lineHeight = base.titleSmall.lineHeight * GenType.scale),
        bodyLarge = base.bodyLarge.copy(fontFamily = body, fontSize = base.bodyLarge.fontSize * GenType.scale, lineHeight = base.bodyLarge.lineHeight * GenType.scale),
        bodyMedium = base.bodyMedium.copy(fontFamily = body, fontSize = base.bodyMedium.fontSize * GenType.scale, lineHeight = base.bodyMedium.lineHeight * GenType.scale),
        bodySmall = base.bodySmall.copy(fontFamily = body, fontSize = base.bodySmall.fontSize * GenType.scale, lineHeight = base.bodySmall.lineHeight * GenType.scale),
        labelLarge = base.labelLarge.copy(fontFamily = body, fontSize = base.labelLarge.fontSize * GenType.scale, lineHeight = base.labelLarge.lineHeight * GenType.scale),
        labelMedium = base.labelMedium.copy(fontFamily = body, fontSize = base.labelMedium.fontSize * GenType.scale, lineHeight = base.labelMedium.lineHeight * GenType.scale),
        labelSmall = base.labelSmall.copy(fontFamily = body, fontSize = base.labelSmall.fontSize * GenType.scale, lineHeight = base.labelSmall.lineHeight * GenType.scale)
    )
}

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
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(small = RoundedCornerShape(GenRadius.control), medium = RoundedCornerShape(GenRadius.card), large = RoundedCornerShape(GenRadius.panel)),
        content = content
    )
}

@Composable
fun BrandWordmark() {
    Image(painterResource(R.drawable.generosity_wordmark), "Generosity",
        Modifier.size(GenSize.wordmarkWidth, GenSize.wordmarkHeight).testTag("brand-wordmark"))
}

@Composable
fun AppIcon(@DrawableRes icon: Int, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(painterResource(icon), contentDescription = null, modifier = modifier.size(GenSize.icon), tint = tint)
}

@Composable
fun CategoryBadge(category: CenterCategory?, size: Dp = GenSize.badge) {
    val dark = isSystemInDarkTheme()
    val color = when (category) {
        CenterCategory.BLOOD_DONATION, CenterCategory.HEALTH, CenterCategory.DISABILITY_SUPPORT -> if (dark) GenColors.categoryHealthDark else GenColors.categoryHealthLight
        CenterCategory.COMMUNITY_KITCHEN, CenterCategory.OLDER_ADULTS -> if (dark) GenColors.categoryCommunityDark else GenColors.categoryCommunityLight
        else -> if (dark) GenColors.categoryOtherDark else GenColors.categoryOtherLight
    }
    Box(Modifier.size(size).background(color.copy(alpha = .13f), RoundedCornerShape(GenRadius.card)), contentAlignment = Alignment.Center) {
        AppIcon(category.icon(), tint = color)
    }
}

fun CenterCategory?.icon(): Int = when (this) {
    CenterCategory.BLOOD_DONATION, CenterCategory.HEALTH, CenterCategory.ORGAN_TISSUE_DONATION -> R.drawable.ic_health
    CenterCategory.EDUCATION -> R.drawable.ic_education
    CenterCategory.COMMUNITY_KITCHEN -> R.drawable.ic_food
    CenterCategory.ADDICTION_RECOVERY -> R.drawable.ic_heart
    else -> R.drawable.ic_people
}
