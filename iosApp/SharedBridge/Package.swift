// swift-tools-version: 5.9
import PackageDescription

// Run `./gradlew :shared:prepareSharedBridge` before resolving this package.
let package = Package(
    name: "SharedBridge",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "SharedBridge", targets: ["SharedBridge"])
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            path: "build/Shared.xcframework"
        ),
        .target(
            name: "SharedBridge",
            dependencies: ["Shared"],
            path: "Sources/SharedBridge"
        )
    ]
)
