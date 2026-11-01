import Foundation
import CryptoKit
import Shared

final class IOSConnection: NSObject, AppConnection, URLSessionTaskDelegate {
    var credential: (String) -> String? = { _ in nil }
    private let base = Bundle.main.object(forInfoDictionaryKey: "GenerosityAPIURL") as? String ?? "https://api.generosity.global"

    func send(request: ApiRequest, completion: @escaping (ApiResponse) -> Void) {
        let token = request.accountId.flatMap(credential)
        guard request.accountId == nil || token != nil else { completion(ApiResponse(status: 401, body: "{}")); return }
        guard let url = URL(string: base + request.path), url.scheme == "https" else { completion(ApiResponse(status: 0, body: "")); return }
        var outgoing = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 15)
        outgoing.httpMethod = request.method
        outgoing.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token { outgoing.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        if let key = request.operationId { outgoing.setValue(key, forHTTPHeaderField: "Idempotency-Key") }
        if let body = request.body {
            outgoing.httpBody = Data(body.utf8)
            outgoing.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        let session = URLSession(configuration: .ephemeral, delegate: self, delegateQueue: nil)
        session.dataTask(with: outgoing) { data, response, error in
            defer { session.finishTasksAndInvalidate() }
            let result: ApiResponse
            if error == nil, let http = response as? HTTPURLResponse, let data, data.count <= 16 * 1024 * 1024, let body = String(data: data, encoding: .utf8) {
                result = ApiResponse(status: Int32(http.statusCode), body: body)
            } else { result = ApiResponse(status: 0, body: "") }
            DispatchQueue.main.async { completion(result) }
        }.resume()
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse, newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) { completionHandler(nil) }

    private func file(_ key: String) throws -> URL {
        let root = try FileManager.default.url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
        var folder = root.appendingPathComponent("backend-" + digest(base), isDirectory: true)
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true, attributes: [.protectionKey: FileProtectionType.complete])
        var values = URLResourceValues(); values.isExcludedFromBackup = true
        try folder.setResourceValues(values)
        return folder.appendingPathComponent(digest(key))
    }

    func read(key: String) throws -> String {
        let url = try file(key)
        guard FileManager.default.fileExists(atPath: url.path) else { return "" }
        return try String(contentsOf: url, encoding: .utf8)
    }
    func write(key: String, value: String?) throws {
        let url = try file(key)
        if let value { try Data(value.utf8).write(to: url, options: [.atomic, .completeFileProtection]) }
        else if FileManager.default.fileExists(atPath: url.path) { try FileManager.default.removeItem(at: url) }
    }
    func doNewOperationId() -> String { UUID().uuidString.lowercased() }
    private func digest(_ text: String) -> String { SHA256.hash(data: Data(text.utf8)).map { String(format: "%02x", $0) }.joined() }
}
