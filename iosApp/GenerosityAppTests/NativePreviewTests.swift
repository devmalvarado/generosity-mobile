import XCTest

final class NativePreviewTests: XCTestCase {
    func testConnectedDesignUsesSharedCatalogAndAccountScreens() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["--connected-design", "--demo-catalog", "-AppleLanguages", "(es-MX)", "-generosity.languageMode", "system"]
        app.launch()
        let loaded = app.textFields["connected-query"].waitForExistence(timeout: 15)
        if !loaded {
            print(app.debugDescription)
            let shot = XCTAttachment(screenshot: app.screenshot()); shot.lifetime = .keepAlways; add(shot)
        }
        XCTAssertTrue(loaded)
        XCTAssertFalse(app.buttons["preview-pin-c1"].exists)
        app.buttons["preview-list"].tap()
        let center = app.buttons["connected-center-kc-children-001"]
        XCTAssertTrue(center.waitForExistence(timeout: 5))
        center.tap()
        XCTAssertTrue(app.staticTexts["detail-kc-children-001"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.staticTexts.containing(NSPredicate(format: "label CONTAINS %@", "Casa Comunitaria Jacaranda")).firstMatch.exists)
        for route in ["needs", "history", "impact", "inbox", "profile", "settings", "welcome", "map"] {
            app.buttons["preview-screens"].tap()
            let destination = app.buttons["preview-route-" + route]
            for _ in 0..<12 where !destination.isHittable { app.scrollViews.firstMatch.swipeUp() }
            XCTAssertTrue(destination.isHittable, route)
            XCTAssertTrue(destination.label.contains("Conectado"), route)
            destination.tap()
            XCTAssertTrue(app.buttons["preview-screens"].waitForExistence(timeout: 5), route)
            if route == "map" { XCTAssertTrue(app.textFields["connected-query"].exists) }
        }
    }

    func testNativeTabBarWithoutSessionBanner() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["--native-preview", "-AppleLanguages", "(es-MX)", "-generosity.languageMode", "system"]
        app.launch()
        let bar = app.tabBars.firstMatch
        XCTAssertTrue(bar.waitForExistence(timeout: 15))
        XCTAssertFalse(app.staticTexts["Diseño de prueba · Datos ficticios · Solo esta sesión"].exists)
        for (label, title) in [("Necesidades", "Necesidades"), ("Ayudar", "¿Cómo quieres ayudar?"), ("Impacto", "Mi impacto"), ("Perfil", "Perfil"), ("Mapa", "Mapa")] {
            bar.buttons[label].tap()
            XCTAssertTrue(bar.buttons[label].isSelected)
            if label == "Mapa" { XCTAssertTrue(app.images["brand-wordmark"].exists) }
            else { XCTAssertEqual(app.staticTexts["preview-title"].label, title) }
        }
        app.buttons["preview-pin-c2"].tap()
        app.buttons["preview-details"].tap()
        XCTAssertTrue(app.staticTexts.containing(NSPredicate(format: "label BEGINSWITH %@", "Centro de Salud Horizonte")).firstMatch.waitForExistence(timeout: 5))
        app.buttons["Atrás"].tap()
        XCTAssertTrue(app.images["brand-wordmark"].exists)
        let shot = XCTAttachment(screenshot: app.screenshot())
        shot.name = "native-system-bars"
        shot.lifetime = .keepAlways
        add(shot)
    }

    func testNativeMapDetailAndAllScreens() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["--native-preview", "-AppleLanguages", "(es-MX)", "-generosity.languageMode", "system"]
        app.launch()
        XCTAssertTrue(app.buttons["preview-pin-c2"].waitForExistence(timeout: 15))
        app.buttons["preview-pin-c2"].tap()
        app.buttons["preview-details"].tap()
        XCTAssertTrue(app.staticTexts.containing(NSPredicate(format: "label BEGINSWITH %@", "Centro de Salud Horizonte")).firstMatch.waitForExistence(timeout: 5))
        for route in ["welcome", "needs", "center", "offer", "history", "impact", "achievements", "regions", "saved", "reviews", "inbox", "messages", "events", "event", "campaign", "meals", "food", "referral", "case", "assessment", "coverage", "organization", "businesses", "funding", "donate", "tax", "staff", "onboarding", "operations", "share", "assisted", "profile", "settings", "map"] {
            app.buttons["preview-screens"].tap()
            let destination=app.buttons["preview-route-"+route]
            for _ in 0..<12 where !destination.isHittable { app.scrollViews.firstMatch.swipeUp() }
            XCTAssertTrue(destination.isHittable, route)
            let expectedTitle = destination.label
            destination.tap()
            let dismissed = XCTNSPredicateExpectation(predicate: NSPredicate(format: "exists == false"), object: app.buttons["Cerrar"])
            XCTAssertEqual(XCTWaiter.wait(for: [dismissed], timeout: 5), .completed)
            if route == "map" { XCTAssertTrue(app.images["brand-wordmark"].exists) }
            else { XCTAssertEqual(app.staticTexts["preview-title"].label, expectedTitle, route) }
            if ["welcome","map","impact","campaign","donate"].contains(route) {
                let shot=XCTAttachment(screenshot:app.screenshot()); shot.name="native-preview-"+route; shot.lifetime = .keepAlways; add(shot)
            }
        }
    }

    func testFloatingMapSearchAndToolbarActions() {
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["--native-preview", "-AppleLanguages", "(es-MX)", "-generosity.languageMode", "system"]
        app.launch()
        XCTAssertTrue(app.images["brand-wordmark"].waitForExistence(timeout: 15))
        XCTAssertFalse(app.staticTexts["preview-title"].exists)
        XCTAssertFalse(app.buttons["Salir"].exists)
        app.buttons["preview-list"].tap()
        app.buttons["preview-list-c2"].tap()
        XCTAssertTrue(app.staticTexts.containing(NSPredicate(format: "label BEGINSWITH %@", "Centro de Salud Horizonte")).firstMatch.waitForExistence(timeout: 5))
        app.buttons["preview-filters"].tap()
        XCTAssertTrue(app.staticTexts["Origen de los recursos"].waitForExistence(timeout: 5))
        app.buttons["Cerrar"].tap()
        let query = app.textFields["preview-query"]
        query.tap()
        query.typeText("Encuentro\n")
        XCTAssertTrue(app.buttons["preview-pin-c4"].exists)
        XCTAssertFalse(app.buttons["preview-pin-c2"].exists)
        app.buttons["preview-locate"].tap()
        XCTAssertTrue(app.images["preview-origin"].exists)
        app.buttons["preview-notifications"].tap()
        XCTAssertEqual(app.staticTexts["preview-title"].label, "Notificaciones")
        app.buttons["Atrás"].tap()
        app.buttons["preview-screens"].tap()
        XCTAssertTrue(app.buttons["preview-route-map"].waitForExistence(timeout: 5))
        app.buttons["Cerrar"].tap()
    }
}
