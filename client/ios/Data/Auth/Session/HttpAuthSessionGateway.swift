import Foundation

public struct HttpAuthSessionGateway: AuthRecoverySessionGateway {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func startAuth(request: StartAuthRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/signin",
            body: [
                "email": request.login,
                "password": request.password,
            ]
        )
    }

    public func registerAuth(request: RegisterAuthRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/register",
            body: [
                "displayName": request.displayName,
                "email": request.email,
                "password": request.password,
            ]
        )
    }

    public func restoreAuth() async throws -> AuthSessionPayload {
        try await sendSessionRequest(method: "GET", path: "/access/session", body: nil)
    }

    public func logoutAuth() async throws {
        _ = try await sendSessionRequest(method: "POST", path: "/access/logout", body: nil)
    }

    public func resendVerification() async throws -> AuthSessionPayload {
        try await sendSessionRequest(method: "POST", path: "/access/verification/resend", body: nil)
    }

    public func confirmVerification(request: ConfirmVerificationRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/verification/confirm",
            body: ["code": request.code]
        )
    }

    public func challengeSecondFactor() async throws -> AuthSessionPayload {
        try await sendSessionRequest(method: "POST", path: "/access/second-factor/challenge", body: nil)
    }

    public func verifySecondFactor(request: VerifySecondFactorRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/second-factor/verify",
            body: ["code": request.code]
        )
    }

    public func requestRecovery(request: RequestRecoveryRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/recovery/request",
            body: ["email": request.email]
        )
    }

    public func resetRecovery(request: ResetRecoveryRequest) async throws -> AuthSessionPayload {
        try await sendSessionRequest(
            method: "POST",
            path: "/access/recovery/reset",
            body: [
                "email": request.email,
                "code": request.code,
                "password": request.password,
            ]
        )
    }

    private func sendSessionRequest(method: String, path: String, body: [String: Any]?) async throws -> AuthSessionPayload {
        guard let url = URL(string: normalizedBaseUrl() + path) else {
            throw HttpAuthSessionGatewayError.invalidUrl
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        } else if method == "POST" {
            request.httpBody = Data()
        }

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpAuthSessionGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpAuthSessionGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        return try payload(from: data)
    }

    private func payload(from data: Data) throws -> AuthSessionPayload {
        if data.isEmpty {
            return AuthSessionPayload(
                status: "unauthenticated",
                sessionId: nil,
                authenticated: false,
                requiresVerification: false,
                requiresSecondFactor: false
            )
        }

        let decoded = try JSONSerialization.jsonObject(with: data)
        guard let object = decoded as? [String: Any] else {
            throw HttpAuthSessionGatewayError.invalidPayload
        }

        let identity = object["identity"] as? [String: Any]
        let authenticated = identity != nil

        return AuthSessionPayload(
            status: object["status"] as? String ?? (authenticated ? "authenticated" : "unauthenticated"),
            sessionId: nil,
            authenticated: authenticated,
            requiresVerification: object["requiresVerification"] as? Bool ?? false,
            requiresSecondFactor: object["requiresSecondFactor"] as? Bool ?? false
        )
    }

    private func errorMessage(data: Data, statusCode: Int) -> String {
        guard !data.isEmpty,
              let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return "Mobile access request failed with HTTP \(statusCode)."
        }

        let code = object["code"] as? String ?? "mobile_access_error"
        let message = object["message"] as? String ?? "Mobile access request failed."

        return "\(code): \(message)"
    }

    private func normalizedBaseUrl() -> String {
        String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed())
    }
}

public enum HttpAuthSessionGatewayError: Error {
    case invalidUrl
    case invalidResponse
    case invalidPayload
    case requestFailed(String)
}
