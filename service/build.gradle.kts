plugins {
    kotlin("jvm")
    application
}

application {
    mainClass = "org.generosity.service.MainKt"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
