import XCTest

final class GenerosityVisualTests: XCTestCase {
    func testEnglishVisualTour() throws { try tour(spanish: false) }
    func testSpanishVisualTour() throws { try tour(spanish: true) }

    private func tour(spanish: Bool) throws {
        let app = XCUIApplication()
        app.launchArguments = ["-AppleLanguages", spanish ? "(es-MX)" : "(en-US)", "-generosity.languageMode", "system"]
        app.launch()
        let list = spanish ? "Lista" : "List"
        let profile = spanish ? "Perfil" : "Profile"
        let settings = spanish ? "Ajustes" : "Settings"
        let prefix = spanish ? "es" : "en"
        XCTAssertTrue(app.tabBars.buttons[list].waitForExistence(timeout: 10))
        capture(app, prefix + "-map")
        app.tabBars.buttons[list].tap()
        capture(app, prefix + "-list")
        let center = app.buttons["center-kc-children-001"]
        reveal(center, app: app)
        center.tap()
        app.tabBars.buttons[spanish ? "Mapa" : "Map"].tap()
        reveal(app.buttons[spanish ? "Ver detalles" : "View details"], app: app)
        capture(app, prefix + "-selected-map")
        app.buttons[spanish ? "Ver detalles" : "View details"].tap()
        reveal(app.staticTexts["detail-kc-children-001"], app: app)
        capture(app, prefix + "-detail")
        reveal(app.staticTexts["remaining-need-school-supplies"], app: app)
        capture(app, prefix + "-need")
        app.tabBars.buttons[profile].tap()
        XCTAssertTrue(app.navigationBars[profile].waitForExistence(timeout: 5))
        capture(app, prefix + "-profile")
        let history = app.staticTexts[spanish ? "Aún no hay aportaciones registradas." : "No contributions recorded yet."]
        reveal(history, app: app)
        capture(app, prefix + "-history")
        app.tabBars.buttons[settings].tap()
        XCTAssertTrue(app.navigationBars[settings].waitForExistence(timeout: 5))
        capture(app, prefix + "-settings")
        // iOS 18.6 reports textClipped without identifying any element, and
        // reports contrast on section headers behind the tab bar at AX sizes.
        // Keep text-clipping/manual VoiceOver acceptance open in the matrix.
        try app.performAccessibilityAudit(for: [.contrast, .elementDetection, .sufficientElementDescription]) { issue in
            guard let element = issue.element else { return false }
            let visibleContent = app.frame.insetBy(dx: 0, dy: 1)
                .intersection(CGRect(x: 0, y: 0, width: app.frame.width, height: app.tabBars.firstMatch.frame.minY))
            let sectionHeaders = [spanish ? "IDIOMA" : "LANGUAGE", spanish ? "NOTIFICACIONES" : "NOTIFICATIONS"]
            if sectionHeaders.contains(element.label) && !visibleContent.contains(element.frame) {
                print("Offscreen audit finding retained for manual review: \(issue.detailedDescription); \(element.debugDescription)")
                return true
            }
            return false
        }
        app.tabBars.buttons[list].tap()
        let search = app.textFields["discovery-search"]
        for _ in 0..<24 where !search.isHittable { scroll(app, up: false) }
        XCTAssertTrue(search.isHittable)
        search.tap()
        search.typeText("zzzz")
        capture(app, prefix + "-keyboard")
        search.typeText("\n")
        XCTAssertTrue(app.staticTexts[spanish ? "No se encontraron centros de bondad." : "No kindness centers found."].waitForExistence(timeout: 5))
        capture(app, prefix + "-empty")
    }

    private func reveal(_ element: XCUIElement, app: XCUIApplication) {
        XCTAssertTrue(element.waitForExistence(timeout: 5))
        for _ in 0..<24 where !element.isHittable { scroll(app, up: true) }
        XCTAssertTrue(element.isHittable)
    }

    private func scroll(_ app: XCUIApplication, up: Bool) {
        // Swipe the scroll-view gutter, not the interactive map surface.
        let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.98, dy: up ? 0.8 : 0.25))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.98, dy: up ? 0.25 : 0.8))
        start.press(forDuration: 0.01, thenDragTo: end)
    }

    private func capture(_ app: XCUIApplication, _ name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
