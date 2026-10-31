package org.generosity.service

import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Starts the local runtime and stops. It does not enable a demo identity,
 * a payment provider or a notification sender.
 */
fun main() {
    val root = Path.of(System.getenv("GENEROSITY_DATA") ?: "build/local-service")
    val service = LocalGenerosityService(root)
    service.start()
    val health = service.health()
    println("ready data=$root identity=${health["identity"]} payments=${health["payments"]} delivery=${health["delivery"]}")
    if (health["identity"] != "unconfigured" || health["payments"] != "unavailable") {
        exitProcess(2)
    }
}
