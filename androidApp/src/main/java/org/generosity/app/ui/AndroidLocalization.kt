package org.generosity.app.ui

import org.generosity.domain.CenterCategory
import org.generosity.domain.HelpMethod
import org.generosity.domain.NotificationType
import org.generosity.domain.SupportedLocale
import org.generosity.domain.VerificationStatus

enum class TextKey {
    TAB_MAP,
    TAB_LIST,
    TAB_PROFILE,
    TAB_SETTINGS,
    LANGUAGE_ENGLISH,
    LANGUAGE_SPANISH,
    NEARBY_CENTERS,
    SEARCH_PLACEHOLDER,
    EMPTY_CENTERS,
    CURRENT_NEEDS,
    NO_CURRENT_NEEDS,
    URGENT,
    MONEY_BOUNDARY,
    ANONYMOUS_PROFILE,
    REGISTERED_PROFILE,
    CONTRIBUTION_HISTORY_EMPTY,
    GENEROSITY_POINTS,
    NEARBY_NOTIFICATIONS,
    URGENT_NOTIFICATIONS,
    REMINDER_NOTIFICATIONS,
    VISIT_SIGNAL,
    VISIT_SIGNAL_SUBMITTED,
    OFFICIAL_VERIFICATION_BOUNDARY
}

fun TextKey.label(locale: SupportedLocale): String = when (locale) {
    SupportedLocale.ENGLISH -> when (this) {
        TextKey.TAB_MAP -> "Map"
        TextKey.TAB_LIST -> "List"
        TextKey.TAB_PROFILE -> "Profile"
        TextKey.TAB_SETTINGS -> "Settings"
        TextKey.LANGUAGE_ENGLISH -> "English"
        TextKey.LANGUAGE_SPANISH -> "Spanish"
        TextKey.NEARBY_CENTERS -> "Nearby Kindness Centers"
        TextKey.SEARCH_PLACEHOLDER -> "Search centers"
        TextKey.EMPTY_CENTERS -> "No Kindness Centers found."
        TextKey.CURRENT_NEEDS -> "Current needs"
        TextKey.NO_CURRENT_NEEDS -> "No current needs are listed."
        TextKey.URGENT -> "Urgent"
        TextKey.MONEY_BOUNDARY -> "Money donations require a verified payment handoff. This app does not collect bank or payment details."
        TextKey.ANONYMOUS_PROFILE -> "Anonymous browsing"
        TextKey.REGISTERED_PROFILE -> "Registered profile"
        TextKey.CONTRIBUTION_HISTORY_EMPTY -> "No contributions recorded yet."
        TextKey.GENEROSITY_POINTS -> "Generosity points are informational and not redeemable."
        TextKey.NEARBY_NOTIFICATIONS -> "Nearby center alerts"
        TextKey.URGENT_NOTIFICATIONS -> "Urgent need alerts"
        TextKey.REMINDER_NOTIFICATIONS -> "Reminders"
        TextKey.VISIT_SIGNAL -> "I visited this center"
        TextKey.VISIT_SIGNAL_SUBMITTED -> "Visit signal saved. Official verification happens through a future center workflow."
        TextKey.OFFICIAL_VERIFICATION_BOUNDARY -> "Official verification requires a future backend and center backoffice workflow."
    }
    SupportedLocale.SPANISH -> when (this) {
        TextKey.TAB_MAP -> "Mapa"
        TextKey.TAB_LIST -> "Lista"
        TextKey.TAB_PROFILE -> "Perfil"
        TextKey.TAB_SETTINGS -> "Ajustes"
        TextKey.LANGUAGE_ENGLISH -> "Ingles"
        TextKey.LANGUAGE_SPANISH -> "Espanol"
        TextKey.NEARBY_CENTERS -> "Centros de bondad cercanos"
        TextKey.SEARCH_PLACEHOLDER -> "Buscar centros"
        TextKey.EMPTY_CENTERS -> "No se encontraron centros de bondad."
        TextKey.CURRENT_NEEDS -> "Necesidades actuales"
        TextKey.NO_CURRENT_NEEDS -> "No hay necesidades actuales publicadas."
        TextKey.URGENT -> "Urgente"
        TextKey.MONEY_BOUNDARY -> "Las donaciones de dinero requieren una transferencia de pago verificada. Esta app no recopila datos bancarios ni de pago."
        TextKey.ANONYMOUS_PROFILE -> "Navegacion anonima"
        TextKey.REGISTERED_PROFILE -> "Perfil registrado"
        TextKey.CONTRIBUTION_HISTORY_EMPTY -> "Aun no hay contribuciones registradas."
        TextKey.GENEROSITY_POINTS -> "Los puntos de generosidad son informativos y no son canjeables."
        TextKey.NEARBY_NOTIFICATIONS -> "Alertas de centros cercanos"
        TextKey.URGENT_NOTIFICATIONS -> "Alertas de necesidades urgentes"
        TextKey.REMINDER_NOTIFICATIONS -> "Recordatorios"
        TextKey.VISIT_SIGNAL -> "Visite este centro"
        TextKey.VISIT_SIGNAL_SUBMITTED -> "Senal de visita guardada. La verificacion oficial ocurre en un futuro flujo del centro."
        TextKey.OFFICIAL_VERIFICATION_BOUNDARY -> "La verificacion oficial requiere un futuro backend y portal para centros."
    }
}

fun CenterCategory.label(locale: SupportedLocale): String = when (locale) {
    SupportedLocale.ENGLISH -> when (this) {
        CenterCategory.CHILDREN_SUPPORT -> "Children support"
        CenterCategory.OLDER_ADULTS -> "Older adults"
        CenterCategory.DISABILITY_SUPPORT -> "Disability support"
        CenterCategory.HEALTH -> "Health"
        CenterCategory.ADDICTION_RECOVERY -> "Addiction recovery"
        CenterCategory.COMMUNITY_KITCHEN -> "Community kitchen"
        CenterCategory.EDUCATION -> "Education"
        CenterCategory.VULNERABLE_FAMILIES -> "Vulnerable families"
        CenterCategory.ORGAN_TISSUE_DONATION -> "Organ or tissue donation"
    }
    SupportedLocale.SPANISH -> when (this) {
        CenterCategory.CHILDREN_SUPPORT -> "Apoyo a ninos"
        CenterCategory.OLDER_ADULTS -> "Adultos mayores"
        CenterCategory.DISABILITY_SUPPORT -> "Apoyo a discapacidad"
        CenterCategory.HEALTH -> "Salud"
        CenterCategory.ADDICTION_RECOVERY -> "Recuperacion de adicciones"
        CenterCategory.COMMUNITY_KITCHEN -> "Comedor comunitario"
        CenterCategory.EDUCATION -> "Educacion"
        CenterCategory.VULNERABLE_FAMILIES -> "Familias vulnerables"
        CenterCategory.ORGAN_TISSUE_DONATION -> "Donacion de organos o tejidos"
    }
}

fun HelpMethod.label(locale: SupportedLocale): String = when (locale) {
    SupportedLocale.ENGLISH -> when (this) {
        HelpMethod.TIME -> "Donate time"
        HelpMethod.GOODS -> "Donate goods"
        HelpMethod.MONEY -> "Donate money"
    }
    SupportedLocale.SPANISH -> when (this) {
        HelpMethod.TIME -> "Donar tiempo"
        HelpMethod.GOODS -> "Donar bienes"
        HelpMethod.MONEY -> "Donar dinero"
    }
}

fun VerificationStatus.label(locale: SupportedLocale): String = when (locale) {
    SupportedLocale.ENGLISH -> when (this) {
        VerificationStatus.IMPORTED -> "Imported"
        VerificationStatus.VISIT_SIGNALED -> "Visited by user"
        VerificationStatus.OFFICIALLY_VERIFIED -> "Officially verified"
    }
    SupportedLocale.SPANISH -> when (this) {
        VerificationStatus.IMPORTED -> "Importado"
        VerificationStatus.VISIT_SIGNALED -> "Visitado por usuario"
        VerificationStatus.OFFICIALLY_VERIFIED -> "Verificado oficialmente"
    }
}

fun NotificationType.label(locale: SupportedLocale): String = when (this) {
    NotificationType.NEARBY_CENTER -> TextKey.NEARBY_NOTIFICATIONS.label(locale)
    NotificationType.URGENT_NEED -> TextKey.URGENT_NOTIFICATIONS.label(locale)
    NotificationType.REMINDER -> TextKey.REMINDER_NOTIFICATIONS.label(locale)
}

