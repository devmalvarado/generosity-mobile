package org.generosity.app.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale
import org.generosity.app.R
import org.generosity.domain.CenterCategory
import org.generosity.domain.HelpMethod
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.domain.VerificationStatus

fun Context.localizedResources(locale: SupportedLocale): Resources =
    createConfigurationContext(Configuration(resources.configuration).apply {
        setLocale(Locale.forLanguageTag(locale.code))
    }).resources

@Composable
private fun localizedResources(locale: SupportedLocale): Resources {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration, locale) { context.localizedResources(locale) }
}

@Composable
fun localizedString(@StringRes id: Int, locale: SupportedLocale, vararg args: Any): String =
    localizedResources(locale).getString(id, *args)

@Composable
fun localizedQuantity(@PluralsRes id: Int, quantity: Int, locale: SupportedLocale): String =
    localizedResources(locale).getQuantityString(id, quantity, quantity)

fun localizedNumber(value: Int, locale: SupportedLocale): String =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag(locale.code)).format(value)

enum class TextKey(@get:StringRes val resource: Int) {
    TAB_MAP(R.string.tab_map),
    TAB_LIST(R.string.tab_list),
    TAB_PROFILE(R.string.tab_profile),
    TAB_SETTINGS(R.string.tab_settings),
    NEARBY_CENTERS(R.string.nearby_centers),
    SEARCH_PLACEHOLDER(R.string.search_placeholder),
    EMPTY_CENTERS(R.string.empty_centers),
    MAP_LOADING(R.string.map_loading),
    MAP_LOAD_DELAYED(R.string.map_load_delayed),
    MAP_UNAVAILABLE(R.string.map_unavailable),
    RETRY(R.string.retry),
    CURRENT_NEEDS(R.string.current_needs),
    NO_CURRENT_NEEDS(R.string.no_current_needs),
    URGENT(R.string.urgent),
    MONEY_BOUNDARY(R.string.money_boundary),
    ANONYMOUS_PROFILE(R.string.anonymous_profile),
    REGISTERED_PROFILE(R.string.registered_profile),
    CONTRIBUTION_HISTORY_EMPTY(R.string.contribution_history_empty),
    GENEROSITY_POINTS(R.string.generosity_points),
    NEARBY_NOTIFICATIONS(R.string.nearby_notifications),
    URGENT_NOTIFICATIONS(R.string.urgent_notifications),
    REMINDER_NOTIFICATIONS(R.string.reminder_notifications),
    VISIT_SIGNAL(R.string.visit_signal),
    VISIT_SIGNAL_SUBMITTED(R.string.visit_signal_submitted),
    OFFICIAL_VERIFICATION_BOUNDARY(R.string.official_verification_boundary),
    OFFER_TIME(R.string.offer_time),
    SIGN_IN(R.string.sign_in),
    CONTINUE_ANONYMOUSLY(R.string.continue_anonymously),
    REGISTRATION_REQUIRED(R.string.registration_required),
    IMPACT_TITLE(R.string.impact_summary)
}

@Composable
fun TextKey.label(locale: SupportedLocale, vararg args: Any): String = localizedString(resource, locale, *args)

@Composable
fun CenterCategory.label(locale: SupportedLocale): String = localizedString(when (this) {
    CenterCategory.CHILDREN_SUPPORT -> R.string.category_children_support
    CenterCategory.OLDER_ADULTS -> R.string.category_older_adults
    CenterCategory.DISABILITY_SUPPORT -> R.string.category_disability_support
    CenterCategory.HEALTH -> R.string.category_health
    CenterCategory.ADDICTION_RECOVERY -> R.string.category_addiction_recovery
    CenterCategory.COMMUNITY_KITCHEN -> R.string.category_community_kitchen
    CenterCategory.EDUCATION -> R.string.category_education
    CenterCategory.VULNERABLE_FAMILIES -> R.string.category_vulnerable_families
    CenterCategory.ORGAN_TISSUE_DONATION -> R.string.category_organ_tissue_donation
}, locale)

@Composable
fun HelpMethod.label(locale: SupportedLocale): String = localizedString(when (this) {
    HelpMethod.TIME -> R.string.help_with_time
    HelpMethod.GOODS -> R.string.help_with_goods
    HelpMethod.MONEY -> R.string.help_with_money
}, locale)

@Composable
fun VerificationStatus.label(locale: SupportedLocale): String = localizedString(when (this) {
    VerificationStatus.IMPORTED -> R.string.verification_imported
    VerificationStatus.VISIT_SIGNALED -> R.string.verification_visit_signaled
    VerificationStatus.OFFICIALLY_VERIFIED -> R.string.verification_officially_verified
}, locale)

@Composable
fun NotificationType.label(locale: SupportedLocale): String = when (this) {
    NotificationType.NEARBY_CENTER -> TextKey.NEARBY_NOTIFICATIONS.label(locale)
    NotificationType.URGENT_NEED -> TextKey.URGENT_NOTIFICATIONS.label(locale)
    NotificationType.REMINDER -> TextKey.REMINDER_NOTIFICATIONS.label(locale)
}

