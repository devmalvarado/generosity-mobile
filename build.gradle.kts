plugins {
    kotlin("multiplatform") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("com.android.application") version "9.2.0" apply false
    id("com.android.kotlin.multiplatform.library") version "9.2.0" apply false
}

tasks.register<Exec>("checkDesignSystem") {
    group = "verification"
    description = "Reject stale generated design tokens and brand assets."
    workingDir = rootDir
    commandLine("python3", "scripts/generate-design-system.py", "--check")
}
