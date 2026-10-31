package org.generosity.domain

enum class ContactChannel { PHONE, EMAIL, WEBSITE }

sealed class ContactAction {
    data class Available(val channel: ContactChannel, val destination: String) : ContactAction()
    data class Unavailable(val channel: ContactChannel) : ContactAction()
}

/**
 * Shows only destinations the catalog already contains. Missing fields create
 * no action. Unsupported schemes are reported unavailable and never opened.
 */
object ContactDestinations {
    fun actions(contact: ContactInfo, availableHandlers: Set<ContactChannel>): List<ContactAction> =
        listOfNotNull(
            action(ContactChannel.PHONE, contact.phone, availableHandlers, ::validPhone, ::phoneDestination),
            action(ContactChannel.EMAIL, contact.email, availableHandlers, ::validEmail, ::emailDestination),
            action(ContactChannel.WEBSITE, contact.website, availableHandlers, ::validHttps, { it })
        )

    private fun action(
        channel: ContactChannel,
        raw: String?,
        handlers: Set<ContactChannel>,
        valid: (String) -> Boolean,
        destination: (String) -> String
    ): ContactAction? {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (!valid(value)) return ContactAction.Unavailable(channel)
        if (channel !in handlers) return ContactAction.Unavailable(channel)
        return ContactAction.Available(channel, destination(value))
    }

    private fun validPhone(value: String): Boolean {
        if (value.any { it == ':' || it.isLetter() }) return false
        val digits = value.count { it.isDigit() }
        return digits in 7..15 && value.all { it.isDigit() || it in "+-() " }
    }

    private fun phoneDestination(value: String): String = "tel:" + value.filter { it.isDigit() || it == '+' }

    private fun validEmail(value: String): Boolean =
        EMAIL.matches(value) && !value.contains(':') && !value.contains('/')

    private fun emailDestination(value: String): String = "mailto:$value"

    private fun validHttps(value: String): Boolean =
        value.startsWith("https://") &&
            !value.startsWith("https:///") &&
            value.substringAfter("https://").substringBefore('/').contains('.') &&
            value.none { it.isWhitespace() } &&
            !value.contains('@')

    private val EMAIL = Regex("""[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}""")
}
