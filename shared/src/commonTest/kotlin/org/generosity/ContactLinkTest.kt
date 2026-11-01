package org.generosity

import kotlin.test.*
import org.generosity.domain.ContactInfo
import org.generosity.data.ApiJson

class ContactLinkTest {
    @Test fun publicContactsOnlyOpenExpectedSchemes() {
        assertEquals(listOf("tel:+525512345678", "mailto:help+mx@example.org", "https://example.org/contact"),
            ContactInfo("+52 (55) 1234-5678", "help+mx@example.org", "https://example.org/contact").links().map { it.url })
        assertTrue(ContactInfo("tel:1234567", "x@example.org?bcc=other@example.org", "javascript:alert(1)").links().isEmpty())
        assertTrue(ContactInfo(website = "https://user:password@example.org").links().isEmpty())
        assertTrue(ContactInfo(website = "https://example.org\n").links().isEmpty())
    }
    @Test fun deeplyNestedRemoteJsonIsRejectedWithoutOverflowingTheStack() {
        assertFails { ApiJson.parse("{\"a\":" + "[".repeat(100) + "0" + "]".repeat(100) + "}") }
    }
}
