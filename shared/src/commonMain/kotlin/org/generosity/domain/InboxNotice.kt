package org.generosity.domain

data class InboxNotice(val id: String, val eventId: String, val centerId: String, val needId: String,
    val title: LocalizedText, val createdAt: String, val read: Boolean)

data class PushDevice(val token: String, val platform: String, val authorized: Boolean)
