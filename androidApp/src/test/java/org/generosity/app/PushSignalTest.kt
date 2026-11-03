package org.generosity.app

import org.junit.Assert.*
import org.junit.Test

class PushSignalTest {
    @Test fun acceptsOnlyOwnedNoticeIdentifiersNotExternalDestinations() {
        val id = "d3dd6643-a544-480a-8ea7-6ad9b77a77e1"
        assertEquals(PushSignal("42", id, true), PushSignal.parse("42", id, true))
        assertNull(PushSignal.parse("-42", id, true))
        assertNull(PushSignal.parse("42", "https://example.com", true))
        assertNull(PushSignal.parse(null, id, false))
    }
}
