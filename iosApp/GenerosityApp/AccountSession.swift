import AuthenticationServices
import CryptoKit
import Foundation
import Security
import SwiftUI
import SharedBridge

@MainActor
final class AccountSession: NSObject, ObservableObject, ASWebAuthenticationPresentationContextProviding {
    @Published private(set) var busy = false
    @Published private(set) var failed = false
    private var browser: ASWebAuthenticationSession?
    private let base = Bundle.main.object(forInfoDictionaryKey: "GenerosityAPIURL") as? String ?? "https://api.generosity.global"
    private let service = "org.generosity.app.session"
    private let callbackScheme = "org.generosity.app.ios"

    func signIn(store: GenerosityStore, register: Bool = false) {
        guard !busy else { return }
        failed = false
        do {
            let verifier = try Self.random()
            let state = try Self.random()
            guard var components = URLComponents(string: base + "/auth/sign-in"), components.scheme == "https" else { throw AuthError.invalidResponse }
            components.queryItems = [
                URLQueryItem(name: "client", value: "ios"), URLQueryItem(name: "locale", value: store.localeCode),
                URLQueryItem(name: "state", value: state),
                URLQueryItem(name: "code_challenge", value: Self.challenge(verifier))
            ]
            if register { components.queryItems?.append(URLQueryItem(name: "screen", value: "register")) }
            guard let url = components.url else { throw AuthError.invalidResponse }
            busy = true
            browser = ASWebAuthenticationSession(url: url, callbackURLScheme: callbackScheme) { [weak self, weak store] callback, error in
                Task { @MainActor in
                    guard let self, let store else { return }
                    self.browser = nil
                    defer { self.busy = false }
                    if let authError = error as? ASWebAuthenticationSessionError, authError.code == .canceledLogin { return }
                    do {
                        guard let callback else { throw AuthError.invalidResponse }
                        let code = try Self.handoff(callback, expectedState: state)
                        let result = try await self.request("/api/v1/auth/exchange", body: ["client": "ios", "code": code, "code_verifier": verifier])
                        guard let token = result["token"] as? String, let user = result["user"] as? [String: Any] else { throw AuthError.invalidResponse }
                        try self.saveToken(token, user: user)
                        try self.identify(user, store: store)
                    } catch { self.failed = true }
                }
            }
            browser?.presentationContextProvider = self
            if browser?.start() != true { busy = false; failed = true; browser = nil }
        } catch { failed = true }
    }

    func restore(store: GenerosityStore) async {
        guard !busy else { return }
        busy = true
        failed = false
        defer { busy = false }
        do {
            guard let token = try loadToken() else { return }
            if let stored = try loadStoredSession(), let data = stored.data(using: .utf8),
               let cached = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let id = cached["id"] as? Int, let name = cached["name"] as? String {
                store.signIn(userId: String(id), displayName: name)
            }
            let result = try await request("/api/v1/me", token: token)
            guard let user = result["user"] as? [String: Any] else { throw AuthError.invalidResponse }
            try identify(user, store: store)
        } catch {
            if (try? loadToken()) == nil { store.signOut() }
            failed = true
        }
    }

    func signOut(store: GenerosityStore) async {
        guard !busy else { return }
        busy = true
        failed = false
        defer { busy = false }
        do {
            if let token = try loadToken() { _ = try await request("/api/v1/auth/logout", body: [:], token: token) }
            try deleteToken()
            store.signOut()
        } catch { failed = true }
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }.flatMap(\.windows).first(where: \.isKeyWindow) ?? ASPresentationAnchor()
    }

    static func challenge(_ verifier: String) -> String { url64(Data(SHA256.hash(data: Data(verifier.utf8)))) }

    static func handoff(_ url: URL, expectedState: String) throws -> String {
        guard url.scheme == "org.generosity.app.ios", url.host == nil, url.path == "/oauth/callback",
              let parts = URLComponents(url: url, resolvingAgainstBaseURL: false),
              parts.queryItems?.filter({ $0.name == "state" }).count == 1,
              parts.queryItems?.filter({ $0.name == "code" }).count == 1,
              parts.queryItems?.first(where: { $0.name == "state" })?.value == expectedState,
              let code = parts.queryItems?.first(where: { $0.name == "code" })?.value,
              code.range(of: "^[a-zA-Z0-9]{64}$", options: .regularExpression) != nil else { throw AuthError.invalidResponse }
        return code
    }

    private func identify(_ user: [String: Any], store: GenerosityStore) throws {
        guard let id = user["id"] as? Int, let name = user["name"] as? String else { throw AuthError.invalidResponse }
        if let token = try loadToken() { try saveToken(token, user: user) }
        store.signIn(userId: String(id), displayName: name)
    }

    func credentialFor(_ userId: String) -> String? {
        guard let stored = try? loadStoredSession(), let data = stored.data(using: .utf8),
              let record = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let owner = record["id"] as? Int, String(owner) == userId else { return nil }
        return record["token"] as? String
    }

    private func request(_ path: String, body: [String: String]? = nil, token: String? = nil) async throws -> [String: Any] {
        guard let url = URL(string: base + path), url.scheme == "https" else { throw AuthError.invalidResponse }
        var request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 15)
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token { request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        if let body {
            request.httpMethod = "POST"
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        // Mobile authentication uses bearer tokens only; no shared browser cookies or response cache.
        let session = URLSession(configuration: .ephemeral, delegate: NoAuthRedirects(), delegateQueue: nil)
        defer { session.invalidateAndCancel() }
        let (data, response) = try await session.data(for: request)
        guard let response = response as? HTTPURLResponse else { throw AuthError.invalidResponse }
        if response.statusCode == 401, token != nil {
            try deleteToken()
            if path.hasSuffix("/logout") { return [:] }
        }
        guard (200..<300).contains(response.statusCode) else { throw AuthError.invalidResponse }
        if response.statusCode == 204 { return [:] }
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] else { throw AuthError.invalidResponse }
        return json
    }

    private var keyQuery: [String: Any] { [kSecClass as String: kSecClassGenericPassword, kSecAttrService as String: service, kSecAttrAccount as String: "token"] }

    private func loadToken() throws -> String? {
        guard let stored = try loadStoredSession() else { return nil }
        if let data = stored.data(using: .utf8), let record = try? JSONSerialization.jsonObject(with: data) as? [String: Any] { return record["token"] as? String }
        return stored
    }

    private func loadStoredSession() throws -> String? {
        var query = keyQuery
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        var result: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        if status == errSecItemNotFound { return nil }
        guard status == errSecSuccess, let data = result as? Data, let token = String(data: data, encoding: .utf8) else { throw AuthError.storage }
        return token
    }

    private func saveToken(_ token: String, user: [String: Any]) throws {
        guard let id = user["id"] as? Int else { throw AuthError.invalidResponse }
        let data = try JSONSerialization.data(withJSONObject: ["token": token, "id": id, "name": user["name"] as? String ?? ""])
        let values: [String: Any] = [kSecValueData as String: data, kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly]
        var status = SecItemUpdate(keyQuery as CFDictionary, values as CFDictionary)
        if status == errSecItemNotFound {
            status = SecItemAdd(keyQuery.merging(values) { _, new in new } as CFDictionary, nil)
        }
        guard status == errSecSuccess else { throw AuthError.storage }
    }

    private func deleteToken() throws {
        let status = SecItemDelete(keyQuery as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else { throw AuthError.storage }
    }

    private static func random() throws -> String {
        var bytes = [UInt8](repeating: 0, count: 32)
        guard SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes) == errSecSuccess else { throw AuthError.storage }
        return url64(Data(bytes))
    }

    private static func url64(_ data: Data) -> String { data.base64EncodedString().replacingOccurrences(of: "+", with: "-").replacingOccurrences(of: "/", with: "_").replacingOccurrences(of: "=", with: "") }
    private enum AuthError: Error { case invalidResponse, storage }
}

private final class NoAuthRedirects: NSObject, URLSessionTaskDelegate {
    func urlSession(_ session: URLSession, task: URLSessionTask, willPerformHTTPRedirection response: HTTPURLResponse, newRequest request: URLRequest, completionHandler: @escaping (URLRequest?) -> Void) { completionHandler(nil) }
}
