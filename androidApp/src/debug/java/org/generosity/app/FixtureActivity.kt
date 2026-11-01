package org.generosity.app

/** Test catalog only. This activity and its manifest entry are absent from release builds. */
class FixtureActivity : MainActivity() {
    override val usesDemoCatalog = true
}
