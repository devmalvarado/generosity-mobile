import XCTest
@testable import Generosity

final class AccountSessionTests: XCTestCase {
    func testProtectedCacheSurvivesAdapterRestartAndSeparatesKeys() throws {
        let key = "test-" + UUID().uuidString
        let first = IOSConnection()
        defer { try? first.write(key: key, value: nil) }
        try first.write(key: key, value: "Niñas: apoyo médico")
        let restarted = IOSConnection()
        XCTAssertEqual(try restarted.read(key: key), "Niñas: apoyo médico")
        XCTAssertEqual(try restarted.read(key: key + "-other"), "")
        try restarted.write(key: key, value: nil)
        XCTAssertEqual(try first.read(key: key), "")
    }
    @MainActor
    func testPKCEUsesRFC7636S256Vector() async {
        XCTAssertEqual(AccountSession.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"), "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
    }

    @MainActor
    func testCallbackRequiresExactClientPathStateAndSingleCode() async throws {
        let code = String(repeating: "a", count: 64)
        let valid = "org.generosity.app.ios:/oauth/callback?state=expected&code=" + code
        XCTAssertEqual(try AccountSession.handoff(URL(string: valid)!, expectedState: "expected"), code)
        for invalid in [valid.replacingOccurrences(of: "state=expected", with: "state=attacker"), valid.replacingOccurrences(of: ".ios:", with: ".android:"), valid.replacingOccurrences(of: "/oauth/callback", with: "/other"), valid + "&state=expected", valid + "&code=" + code] {
            XCTAssertThrowsError(try AccountSession.handoff(URL(string: invalid)!, expectedState: "expected"))
        }
    }
}
