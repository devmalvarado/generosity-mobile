package org.generosity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.generosity.domain.PublicOrigin

class PublicOriginTest {
    @Test
    fun confirmedOriginIsTheOnlyDefaultHost() {
        assertEquals("https://generosity.global", PublicOrigin.resolve())
        assertNull(PublicOrigin.resolve(""))
        assertNull(PublicOrigin.resolve("http://generosity.global"))
        assertNull(PublicOrigin.resolve("https://generosity.global/privacy"))
    }
}
