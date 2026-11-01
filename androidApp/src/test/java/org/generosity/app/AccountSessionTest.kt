package org.generosity.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountSessionTest {
    @Test
    fun pkceMatchesRfc7636S256Vector() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            AccountSession.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
    }
}
