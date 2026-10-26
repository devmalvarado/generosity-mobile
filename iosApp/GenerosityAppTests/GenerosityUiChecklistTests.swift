import XCTest

final class GenerosityUiChecklistTests: XCTestCase {
    func testAnonymousDiscoverySupportsLanguageSwitchingAndCenterDetails() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["Generosity"].waitForExistence(timeout: 5))
        app.buttons["Spanish"].tap()
        XCTAssertTrue(app.staticTexts["Centros de bondad cercanos"].waitForExistence(timeout: 5))
        let listTab = app.tabBars.buttons["Lista"]
        XCTAssertTrue(listTab.waitForExistence(timeout: 10))
        listTab.tap()
        let center = app.buttons["center-kc-children-001"]
        for _ in 0..<5 where !center.isHittable {
            app.swipeUp()
        }
        center.tap()
        for title in ["Necesidades actuales", "Utiles escolares"] {
            let text = app.staticTexts[title]
            XCTAssertTrue(text.waitForExistence(timeout: 5))
            for _ in 0..<5 where !text.isHittable {
                app.swipeUp()
            }
            XCTAssertTrue(text.isHittable, "Expected visible detail: \(title)")
        }
    }

    func testProfileShowsAnonymousEmptyState() {
        let app = XCUIApplication()
        app.launch()

        app.tabBars.buttons["Profile"].tap()
        XCTAssertTrue(app.staticTexts["Anonymous browsing"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["No contributions recorded yet."].waitForExistence(timeout: 5))
    }
}

