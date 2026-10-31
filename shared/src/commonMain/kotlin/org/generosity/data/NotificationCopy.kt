package org.generosity.data

import org.generosity.domain.LocalizedText
import org.generosity.domain.NotificationType

// Draft notification copy; delivery remains a separate feature.
object NotificationCopy {
    val titles = mapOf(
        NotificationType.NEARBY_CENTER to LocalizedText.of("Kindness center nearby", "Centro de bondad cercano"),
        NotificationType.URGENT_NEED to LocalizedText.of("Urgent need", "Necesidad urgente"),
        NotificationType.REMINDER to LocalizedText.of("Time to help", "Recordatorio para ayudar")
    )
    val bodies = mapOf(
        NotificationType.NEARBY_CENTER to LocalizedText.of("There is a kindness center near you.", "Hay un centro de bondad cerca de ti."),
        NotificationType.URGENT_NEED to LocalizedText.of(
            "A kindness center has an urgent need that matches your interests.",
            "Un centro de bondad tiene una necesidad urgente relacionada con tus intereses."
        ),
        NotificationType.REMINDER to LocalizedText.of("You scheduled time to help.", "Tienes una actividad de ayuda programada.")
    )
}
