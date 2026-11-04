import java.util.Properties
import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services") version "4.5.0" apply false
}

if (file("google-services.json").exists() || file("src/preview/google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val mapsApiKey = providers.environmentVariable("MAPS_API_KEY")
    .orElse(provider { localProperties.getProperty("MAPS_API_KEY", "") })
    .get().trim()

val previewApiUrl = providers.gradleProperty("generosityPreviewApiUrl").orElse("https://api-preview.generosity.global").get()
require(previewApiUrl.matches(Regex("https://[A-Za-z0-9.-]+(:[0-9]+)?")) && URI(previewApiUrl).host.lowercase().trimEnd('.') !in listOf("api.generosity.global", "generosity.global", "www.generosity.global")) { "Preview requires a separate HTTPS API" }
val previewMapsKey = providers.environmentVariable("MAPS_PREVIEW_API_KEY").orElse(provider { localProperties.getProperty("MAPS_PREVIEW_API_KEY", "") }).get().trim()
val apiBaseUrl = providers.gradleProperty("generosityApiUrl").orElse("https://api.generosity.global").get()
require(apiBaseUrl.matches(Regex("https://[A-Za-z0-9.-]+(:[0-9]+)?"))) { "generosityApiUrl must be an HTTPS origin" }

android {
    namespace = "org.generosity.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.generosity.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("boolean", "MAPS_API_KEY_CONFIGURED", mapsApiKey.isNotEmpty().toString())
    }

    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
            isDefault = true
            buildConfigField("String", "CHANNEL", "\"production\"")
            buildConfigField("String", "AUTH_CLIENT", "\"android\"")
            buildConfigField("String", "AUTH_SCHEME", "\"org.generosity.app.android\"")
            manifestPlaceholders["AUTH_SCHEME"] = "org.generosity.app.android"
        }
        create("preview") {
            dimension = "environment"
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            buildConfigField("String", "CHANNEL", "\"preview\"")
            buildConfigField("String", "API_BASE_URL", "\"$previewApiUrl\"")
            buildConfigField("String", "AUTH_CLIENT", "\"android-preview\"")
            buildConfigField("String", "AUTH_SCHEME", "\"org.generosity.app.preview.android\"")
            buildConfigField("boolean", "MAPS_API_KEY_CONFIGURED", previewMapsKey.isNotEmpty().toString())
            manifestPlaceholders["MAPS_API_KEY"] = previewMapsKey
            manifestPlaceholders["AUTH_SCHEME"] = "org.generosity.app.preview.android"
        }
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
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-messaging")
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
tasks.named("preBuild") { dependsOn(rootProject.tasks.named("checkDesignSystem")) }

// A preview build never falls back to the root production Firebase configuration.
tasks.matching { it.name.startsWith("processProduction") && it.name.endsWith("GoogleServices") }.configureEach {
    enabled = file("google-services.json").isFile
}
tasks.matching { it.name.startsWith("processPreview") && it.name.endsWith("GoogleServices") }.configureEach {
    enabled = file("src/preview/google-services.json").isFile
    doFirst {
        val config = groovy.json.JsonSlurper().parse(file("src/preview/google-services.json")) as Map<*, *>
        val project = config["project_info"] as Map<*, *>
        require(project["project_id"] != "generosity-v01") { "Use a separate Firebase project for preview" }
    }
}
