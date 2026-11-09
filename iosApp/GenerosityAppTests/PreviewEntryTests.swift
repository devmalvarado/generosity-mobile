import XCTest

#if PREVIEW_APP_TESTS
final class PreviewEntryTests: XCTestCase {
    // Read-only acceptance against the preview API with discovery enabled.
    func testPreviewStartsInConnectedDesignAndKeepsItAfterRelaunch() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["-generosity.entered", "YES", "-AppleLanguages", "(en-US)", "-generosity.languageMode", "system"]
        for _ in 0..<2 {
            app.launch()
            XCTAssertTrue(app.textFields["connected-query"].waitForExistence(timeout: 30))
            XCTAssertFalse(app.buttons["open-native-preview"].exists)
            XCTAssertFalse(app.buttons["preview-pin-c1"].exists)
            app.buttons["preview-screens"].tap()
            XCTAssertTrue(app.buttons["preview-route-map"].waitForExistence(timeout: 5))
            XCTAssertFalse(app.buttons["Exit design preview"].exists)
            app.buttons["Close"].tap()
            XCTAssertTrue(app.tabBars.buttons["Map"].exists)
            app.terminate()
        }
    }

    func testMissingGroupsCannotRestoreOldLayoutOrGrantSections() {
        continueAfterFailure = false
        let app = XCUIApplication()
        // The bundled catalog supplies no preview grants and makes no requests.
        app.launchArguments = ["--demo-catalog", "-generosity.entered", "YES", "-AppleLanguages", "(en-US)", "-generosity.languageMode", "system"]
        app.launch()
        XCTAssertTrue(app.staticTexts["preview-unavailable"].waitForExistence(timeout: 15))
        XCTAssertTrue(app.buttons["Try again"].exists)
        XCTAssertTrue(app.buttons["Sign in"].exists)
        XCTAssertFalse(app.buttons["open-native-preview"].exists)
        XCTAssertFalse(app.tabBars.firstMatch.exists)
        XCTAssertFalse(app.textFields["connected-query"].exists)
        XCTAssertFalse(app.buttons["preview-screens"].exists)
    }
}
#endif
