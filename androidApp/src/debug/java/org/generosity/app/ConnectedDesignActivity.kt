package org.generosity.app

/** Opens the new layout with the real connection; business feature flags remain enforced. */
class ConnectedDesignActivity : MainActivity() {
    override val usesConnectedDesign = true
}
