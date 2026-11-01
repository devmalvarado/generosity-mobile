import XCTest

final class GenerosityUiChecklistTests: XCTestCase {
    func testWelcomeOffersGuestAndAccountEntryInBothLanguages() {
        for (language, guest, signIn, create, list) in [
            ("en-US", "Continue as a guest", "Sign in", "Create account", "List"),
            ("es-MX", "Continuar como invitado", "Iniciar sesión", "Crear cuenta", "Lista")
        ] {
            let app = XCUIApplication()
            app.launchArguments = ["--demo-catalog", "--reset-entry", "-AppleLanguages", "(\(language))", "-generosity.languageMode", "system"]
            app.launch()
            for label in [guest, signIn, create] { XCTAssertTrue(app.buttons[label].waitForExistence(timeout: 5)) }
            capture(app, name: "Welcome - \(language)")
            app.buttons[guest].tap()
            XCTAssertTrue(app.tabBars.buttons[list].waitForExistence(timeout: 5))
            app.terminate()
            app.launchArguments = ["--demo-catalog", "-AppleLanguages", "(\(language))", "-generosity.languageMode", "system"]
            app.launch()
            XCTAssertTrue(app.tabBars.buttons[list].waitForExistence(timeout: 5))
            app.terminate()
        }
    }
    func testAnonymousDiscoverySupportsLanguageSwitchingAndCenterDetails() {
        let app = launch()

        XCTAssertTrue(app.images["brand-wordmark"].waitForExistence(timeout: 5))
        app.tabBars.buttons["Settings"].tap()
        app.buttons["Español"].tap()
        let listTab = app.tabBars.buttons["Lista"]
        XCTAssertTrue(listTab.waitForExistence(timeout: 10))
        listTab.tap()
        let center = app.buttons["center-kc-children-001"]
        for _ in 0..<5 where !center.isHittable {
            app.swipeUp()
        }
        center.tap()
        for title in ["Necesidades actuales", "Útiles escolares"] {
            let text = app.staticTexts[title]
            XCTAssertTrue(text.waitForExistence(timeout: 5))
            for _ in 0..<5 where !text.isHittable {
                app.swipeUp()
            }
            XCTAssertTrue(text.isHittable, "Expected visible detail: \(title)")
        }
    }

    func testProfileShowsAnonymousEmptyState() {
        let app = launch()

        app.tabBars.buttons["Profile"].tap()
        XCTAssertTrue(app.staticTexts["Browsing as a guest"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.buttons["Sign in"].isHittable)
        XCTAssertTrue(app.buttons["Create account"].isHittable)
        XCTAssertTrue(app.staticTexts["No contributions recorded yet."].waitForExistence(timeout: 5))
    }

    func testOrderedRegionalDeviceLanguagesResolveOnFirstScreen() {
        let app = launch(languages: "(fr-FR, es-MX, en-US)")
        XCTAssertTrue(app.staticTexts["Centros de bondad cercanos"].waitForExistence(timeout: 10))
        app.tabBars.buttons["Ajustes"].tap()
        XCTAssertTrue(app.buttons["Usar idioma del dispositivo"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Usar idioma del dispositivo"].isSelected)
        capture(app, name: "Language settings - Spanish device mode")
    }

    func testUnsupportedDeviceLanguageFallsBackToEnglish() {
        let app = launch(languages: "(fr-FR, de-DE)")
        XCTAssertTrue(app.staticTexts["Nearby kindness centers"].waitForExistence(timeout: 10))
    }

    func testOverrideSurvivesRestartAndDeviceChangeThenReturnsToDevice() {
        let app = launch()
        app.tabBars.buttons["Settings"].tap()
        capture(app, name: "Language settings - English")
        app.buttons["Español"].tap()
        XCTAssertTrue(app.tabBars.buttons["Ajustes"].waitForExistence(timeout: 5))
        app.terminate()
        app.launchArguments = ["--demo-catalog", "-generosity.entered", "YES", "-AppleLanguages", "(fr-FR, en-GB)"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Centros de bondad cercanos"].waitForExistence(timeout: 10))
        app.tabBars.buttons["Ajustes"].tap()
        XCTAssertTrue(app.buttons["Español"].isSelected)
        app.buttons["Usar idioma del dispositivo"].tap()
        XCTAssertTrue(app.tabBars.buttons["Settings"].waitForExistence(timeout: 5))
        app.terminate()
        app.launchArguments = ["--demo-catalog", "-generosity.entered", "YES", "-AppleLanguages", "(es-MX, en-US)"]
        app.launch()
        XCTAssertTrue(app.staticTexts["Centros de bondad cercanos"].waitForExistence(timeout: 10))
    }

    func testLanguageChangeKeepsSearchAndRecomputesLocalizedResults() {
        let app = launch()
        app.tabBars.buttons["List"].tap()
        let search = app.textFields["Search centers"]
        search.tap()
        search.typeText("Hogar\n")
        app.tabBars.buttons["Settings"].tap()
        app.buttons["Español"].tap()
        app.tabBars.buttons["Lista"].tap()
        XCTAssertEqual(app.textFields["Buscar centros"].value as? String, "Hogar")
        XCTAssertTrue(app.buttons["center-kc-children-001"].waitForExistence(timeout: 5))
    }

    func testSharedCategorySelectionSurvivesRepeatedNavigation() {
        let app = launch()
        app.tabBars.buttons["List"].tap()
        app.buttons["category-children_support"].tap()
        let center = app.buttons["center-kc-children-001"]
        XCTAssertTrue(center.waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["center-kc-seniors-001"].exists)
        center.tap()
        XCTAssertTrue(app.staticTexts["detail-kc-children-001"].exists)
        XCTAssertEqual(app.staticTexts["remaining-need-school-supplies"].label, "15 items still needed")
        for _ in 0..<3 {
            app.tabBars.buttons["Map"].tap()
            XCTAssertTrue(app.buttons["category-children_support"].isSelected)
            XCTAssertTrue(app.buttons["marker-kc-children-001"].waitForExistence(timeout: 5))
            XCTAssertFalse(app.buttons["marker-kc-seniors-001"].exists)
            app.tabBars.buttons["Settings"].tap()
            app.tabBars.buttons["List"].tap()
            XCTAssertTrue(app.staticTexts["detail-kc-children-001"].exists)
            XCTAssertEqual(app.staticTexts["remaining-need-school-supplies"].label, "15 items still needed")
        }
        capture(app, name: "Shared discovery - category and selected center")
    }

    func testMapSelectionOpensTheSameSharedDetailInList() {
        let app = launch()
        let marker = app.buttons["marker-kc-children-001"]
        XCTAssertTrue(marker.waitForExistence(timeout: 10))
        marker.tap()
        app.tabBars.buttons["List"].tap()
        XCTAssertTrue(app.staticTexts["detail-kc-children-001"].waitForExistence(timeout: 5))
        XCTAssertEqual(app.staticTexts["remaining-need-school-supplies"].label, "15 items still needed")
        XCTAssertTrue(app.staticTexts["Urgent"].exists)
        XCTAssertTrue(app.staticTexts["Online donations are not available yet. The app does not collect bank or payment details."].exists)
    }

    func testSearchHidesStaleDetailAndKeepsQueryAcrossTabs() {
        let app = launch()
        app.tabBars.buttons["List"].tap()
        let center = app.buttons["center-kc-children-001"]
        XCTAssertTrue(center.waitForExistence(timeout: 5))
        center.tap()
        let search = app.textFields["discovery-search"]
        search.tap()
        search.typeText("zzzz-no-center\n")
        XCTAssertTrue(app.staticTexts["No kindness centers found."].waitForExistence(timeout: 5))
        XCTAssertFalse(app.staticTexts["detail-kc-children-001"].exists)
        app.tabBars.buttons["Map"].tap()
        XCTAssertEqual(app.textFields["discovery-search"].value as? String, "zzzz-no-center")
        XCTAssertFalse(app.buttons["marker-kc-children-001"].exists)
        XCTAssertFalse(app.staticTexts["detail-kc-children-001"].exists)
    }

    private func launch(languages: String = "(en-US)") -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = ["--demo-catalog", "-generosity.entered", "YES", "-AppleLanguages", languages, "-generosity.languageMode", "system"]
        app.launch()
        return app
    }

    private func capture(_ app: XCUIApplication, name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}

