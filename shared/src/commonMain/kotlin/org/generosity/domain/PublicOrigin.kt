package org.generosity.domain

/**
 * The product owner confirmed this HTTPS origin on 2026-10-31.
 * It is not a legal-page URL, a hosting selection, or an app-link association.
 * An explicit blank override stays unavailable instead of inventing another host.
 */
object PublicOrigin {
    const val CONFIRMED = "https://generosity.global"

    fun resolve(override: String? = null): String? {
        val raw = (override ?: CONFIRMED).trim().trimEnd('/')
        if (!raw.startsWith("https://")) return null
        val host = raw.removePrefix("https://")
        if (host.isEmpty() || host.any { it.isWhitespace() || it == '/' }) return null
        return raw
    }
}
