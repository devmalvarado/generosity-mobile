import XCTest
@testable import Generosity

final class PushSignalTests: XCTestCase {
    func testOnlyAccountAndNoticeIdentifiersAreAccepted() {
        let id = "d3dd6643-a544-480a-8ea7-6ad9b77a77e1"
        XCTAssertEqual(PushSignal(["account_id": "42", "notice_id": id], open: true)?.noticeId, id)
        XCTAssertNil(PushSignal(["account_id": "-42", "notice_id": id], open: true))
        XCTAssertNil(PushSignal(["account_id": "42", "notice_id": "https://example.com"], open: true))
        XCTAssertNil(PushSignal(["notice_id": id], open: false))
    }
}
