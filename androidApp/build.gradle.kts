import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey = providers.environmentVariable("MAPS_API_KEY")
    .orElse(provider { localProperties.getProperty("MAPS_API_KEY", "") })
    .get().trim()

val apiBaseUrl = providers.gradleProperty("generosityApiUrl").orElse("https://api.generosity.global").get()
require(apiBaseUrl.matches(Regex("https://[A-Za-z0-9.-]+(:[0-9]+)?"))) { "generosityApiUrl must be an HTTPS origin" }

android {
    namespace = "org.generosity.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.generosity.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "MAPS_API_KEY_CONFIGURED", mapsApiKey.isNotEmpty().toString())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        // Keep every supported language available for the offline in-app selector.
        language { enableSplit = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.compose.foundation:foundation:1.11.0")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui:1.11.0")
    // Keep SDK 36 compatibility; revisit alongside the app's SDK/Compose upgrade.
    implementation("com.google.maps.android:maps-compose:8.3.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.11.0")
}

val verifyMapProviderBoundary by tasks.registering {
    description = "Keep map SDK types out of shared logic and provider-neutral Android UI."
    group = "verification"
    val sources = files(
        rootProject.fileTree("shared/src") { include("**/*.kt") },
        fileTree("src") {
            include("**/*.kt")
            exclude("**/ui/map/google/**")
        }
    )
    inputs.files(sources)
    doLast {
        val providerType = Regex("com\\.google\\.(android\\.gms\\.maps|maps\\.android)|org\\.maplibre")
        val leaks = sources.files.filter { providerType.containsMatchIn(it.readText()) }
        check(leaks.isEmpty()) { "Map SDK references outside the adapter: ${leaks.joinToString()}" }
    }
}
tasks.named("check") { dependsOn(verifyMapProviderBoundary) }
