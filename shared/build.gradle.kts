import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "org.generosity.shared"
        compileSdk = 36
        minSdk = 26
        withHostTest {}
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    jvm()

    val xcframework = XCFramework("Shared")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcframework.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}

tasks.register<Sync>("prepareSharedBridge") {
    dependsOn("assembleSharedDebugXCFramework")
    from(layout.buildDirectory.dir("XCFrameworks/debug/Shared.xcframework"))
    into(rootProject.layout.projectDirectory.dir("iosApp/SharedBridge/build/Shared.xcframework"))
}
