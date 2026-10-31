import XCTest
@testable import Generosity

final class LocalizationTests: XCTestCase {
    func testSpanishAndEnglishPluralsUseTheSelectedLanguage() {
        XCTAssertEqual(L10n.remaining(0, locale: .spanish), "Faltan 0 unidades")
        XCTAssertEqual(L10n.remaining(1, locale: .spanish), "Falta 1 unidad")
        XCTAssertEqual(L10n.remaining(15, locale: .spanish), "Faltan 15 unidades")
        XCTAssertEqual(L10n.remaining(1, locale: .english), "1 item still needed")
        XCTAssertEqual(L10n.remaining(15, locale: .english), "15 items still needed")
        XCTAssertEqual(L10n.plural("offer_goods", count: 1, locale: .spanish), "Puedo llevar 1 artículo")
        XCTAssertEqual(L10n.plural("offer_goods", count: 5, locale: .spanish), "Puedo llevar 5 artículos")
    }

    func testRegionalLanguageAndAccentedResources() {
        let locale = AppLocale(rawValue: "es_MX")!
        XCTAssertEqual(locale.rawValue, "es")
        XCTAssertEqual(locale.nativeName, "Español")
        XCTAssertEqual(L10n.resource("visit_signal", locale: locale), "Visité este centro")
        XCTAssertEqual(L10n.resource("category_organ_tissue_donation", locale: locale), "Donación de órganos y tejidos")
        XCTAssertEqual(locale.shared, AppLocale.spanish.shared)
        XCTAssertNil(AppLocale(rawValue: "not-a-language"))
    }
}
