import XCTest
import Combine
import Shared
import SharedBridge

final class BackendBridgeTests: XCTestCase {
    @MainActor
    func testEnteredPersonalHelpCrossesBridgeAndPrivateHistoryClearsOnSignOut() async throws {
        let connection = FixtureConnection()
        let suite = "bridge-test-" + UUID().uuidString
        let defaults = UserDefaults(suiteName: suite)!
        defer { defaults.removePersistentDomain(forName: suite) }
        let store = GenerosityStore(defaults: defaults, connection: connection)
        let signedIn = expectation(description: "Account restored")
        let account = store.$state.filter { $0.profile.id == "1" && !$0.syncing }.first().sink { _ in signedIn.fulfill() }
        store.signIn(userId: "1", displayName: "María")
        await fulfillment(of: [signedIn], timeout: 5)
        account.cancel()
        let saved = expectation(description: "Authoritative personal history received")
        let history = store.$state.filter { $0.contributions.count == 1 && !$0.writing }.first().sink { _ in saved.fulfill() }
        store.submitOffer(needId: nil, method: "goods", quantity: 3, minutes: 0, activity: "", availability: "", notes: "Niñas: útiles escolares", unit: "kits")
        await fulfillment(of: [saved], timeout: 5)
        history.cancel()
        let body = try JSONSerialization.jsonObject(with: Data(connection.lastBody.utf8)) as! [String: Any]
        XCTAssertEqual(body["quantity"] as? Int, 3)
        XCTAssertEqual(body["notes"] as? String, "Niñas: útiles escolares")
        XCTAssertEqual(body["quantity_unit"] as? String, "kits")
        XCTAssertFalse(connection.lastKey.isEmpty)
        XCTAssertEqual(store.state.contributions.first?.provenance, .selfReported)
        XCTAssertTrue(store.state.pendingOperations.isEmpty)
        let signedOut = expectation(description: "Private history cleared")
        let cleared = store.$state.filter { $0.profile.id == nil && $0.contributions.isEmpty }.first().sink { _ in signedOut.fulfill() }
        store.signOut()
        await fulfillment(of: [signedOut], timeout: 5)
        cleared.cancel()
    }
}

private final class FixtureConnection: NSObject, AppConnection {
    var disk: [String: String] = [:]
    var lastBody = ""
    var lastKey = ""
    func read(key: String) throws -> String { disk[key] ?? "" }
    func write(key: String, value: String?) throws { disk[key] = value }
    func doNewOperationId() -> String { UUID().uuidString }
    func send(request: ApiRequest, completion: @escaping (ApiResponse) -> Void) {
        let body: String
        if request.path.hasSuffix("/catalog") { body = #"{"catalog":{"schemaVersion":1,"centers":[]}}"# }
        else if request.method == "POST" {
            lastBody = request.body ?? ""; lastKey = request.operationId ?? ""
            body = "{}"
        } else {
            let rows = lastBody.isEmpty ? "[]" : #"[{"id":"personal-a","user_id":1,"method":"goods","quantity":3,"quantity_unit":"kits","title":{"es":"Ayuda personal"},"provenance":"self_reported","status":"offered","created_at":"2026-11-01T00:00:00Z","version":1}]"#
            body = #"{"user":{"id":1},"contributions":\#(rows)}"#
        }
        completion(ApiResponse(status: 200, body: body))
    }
}
