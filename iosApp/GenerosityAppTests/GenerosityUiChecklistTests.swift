import XCTest

final class GenerosityUiChecklistTests: XCTestCase {
    func testAnonymousDiscoverySupportsLanguageSwitchingAndCenterDetails() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["Generosity"].waitForExistence(timeout: 5))
        app.buttons["Spanish"].tap()
        XCTAssertTrue(app.staticTexts["Centros de bondad cercanos"].waitForExistence(timeout: 5))
        app.buttons["Hogar Futuros Brillantes"].tap()
        XCTAssertTrue(app.staticTexts["Necesidades actuales"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Utiles escolares"].waitForExistence(timeout: 5))
    }

    func testProfileShowsAnonymousEmptyState() {
        let app = XCUIApplication()
        app.launch()

        app.tabBars.buttons["Profile"].tap()
        XCTAssertTrue(app.staticTexts["Anonymous browsing"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["No contributions recorded yet."].waitForExistence(timeout: 5))
    }
}

