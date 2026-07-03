import Foundation

public struct HttpAttachmentGateway: AttachmentReader, AttachmentWriter {
    private let baseUrl: String
    private let session: URLSession

    public init(baseUrl: String, session: URLSession = .shared) {
        self.baseUrl = baseUrl
        self.session = session
    }

    public func listAttachment(ownerType: String, ownerId: String, context: String? = nil, slot: String? = nil) async throws -> AttachmentListPayload {
        guard var components = URLComponents(string: normalizedBaseUrl() + "/attachment") else {
            throw HttpAttachmentGatewayError.invalidUrl
        }

        var queryItems = [
            URLQueryItem(name: "ownerType", value: ownerType),
            URLQueryItem(name: "ownerId", value: ownerId)
        ]

        if let context, !context.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            queryItems.append(URLQueryItem(name: "context", value: context))
        }

        if let slot, !slot.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            queryItems.append(URLQueryItem(name: "slot", value: slot))
        }

        components.queryItems = queryItems

        guard let url = components.url else {
            throw HttpAttachmentGatewayError.invalidUrl
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpAttachmentGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpAttachmentGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw HttpAttachmentGatewayError.invalidPayload
        }

        let rawItems = object["items"] as? [[String: Any]] ?? []
        let items = rawItems.map { itemObject in
            AttachmentItemPayload(
                attachmentId: string(itemObject["attachmentId"]) ?? "attachment-unavailable",
                type: string(itemObject["type"]),
                mimeType: string(itemObject["mimeType"]),
                downloadUrl: string(itemObject["downloadUrl"]),
                payloadText: string(itemObject["payloadText"])
            )
        }

        return AttachmentListPayload(
            ownerType: string(object["ownerType"]) ?? ownerType,
            ownerId: string(object["ownerId"]) ?? ownerId,
            count: int(object["count"]) ?? items.count,
            items: items,
            payloadText: string(object["payloadText"])
        )
    }

    public func attachAttachment(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload {
        guard let url = URL(string: normalizedBaseUrl() + "/attachment/link") else {
            throw HttpAttachmentGatewayError.invalidUrl
        }

        var body: [String: Any] = [
            "attachmentId": request.attachmentId,
            "ownerType": request.ownerType,
            "ownerId": request.ownerId
        ]

        if let context = request.context {
            body["context"] = context
        }

        if let slot = request.slot {
            body["slot"] = slot
        }

        if let position = request.position {
            body["position"] = position
        }

        if let isPrimary = request.isPrimary {
            body["isPrimary"] = isPrimary
        }

        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        urlRequest.setValue("application/json", forHTTPHeaderField: "Accept")
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        urlRequest.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, response) = try await session.data(for: urlRequest)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpAttachmentGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpAttachmentGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw HttpAttachmentGatewayError.invalidPayload
        }

        return AttachmentLinkPayload(
            linkId: string(object["linkId"]),
            attachmentId: string(object["attachmentId"]) ?? String(request.attachmentId),
            ownerType: string(object["ownerType"]) ?? request.ownerType,
            ownerId: string(object["ownerId"]) ?? request.ownerId,
            context: string(object["context"]),
            slot: string(object["slot"]),
            position: int(object["position"]),
            isPrimary: object["isPrimary"] as? Bool,
            payloadText: string(object["payloadText"])
        )
    }

    public func fileHandoff(attachmentId: String) async throws -> AttachmentFileHandoffPayload {
        guard let url = URL(string: normalizedBaseUrl() + "/attachment/file/" + (attachmentId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? "")) else {
            throw HttpAttachmentGatewayError.invalidUrl
        }

        let object = try await jsonObject(url: url, method: "GET", body: nil)

        return AttachmentFileHandoffPayload(
            attachmentId: string(object["attachmentId"]) ?? attachmentId,
            downloadUrl: string(object["downloadUrl"]) ?? "",
            mimeType: string(object["mimeType"]),
            fileName: string(object["fileName"]),
            handoffMode: string(object["handoffMode"]) ?? "external_url",
            payloadText: string(object["payloadText"])
        )
    }

    public func uploadHandoff(request: AttachmentUploadHandoffRequest) async throws -> AttachmentUploadHandoffPayload {
        guard let url = URL(string: normalizedBaseUrl() + "/attachment/upload-handoff") else {
            throw HttpAttachmentGatewayError.invalidUrl
        }

        var body: [String: Any] = [
            "ownerType": request.ownerType,
            "ownerId": request.ownerId,
            "isPrimary": request.isPrimary
        ]

        if let context = request.context { body["context"] = context }
        if let slot = request.slot { body["slot"] = slot }
        if let title = request.title { body["title"] = title }
        if let description = request.description { body["description"] = description }
        if let altText = request.altText { body["altText"] = altText }

        let object = try await jsonObject(url: url, method: "POST", body: body)

        return AttachmentUploadHandoffPayload(
            uploadUrl: string(object["uploadUrl"]) ?? "",
            method: string(object["method"]) ?? "POST",
            fieldName: string(object["fieldName"]) ?? "file",
            handoffMode: string(object["handoffMode"]) ?? "multipart_direct",
            payloadText: string(object["payloadText"])
        )
    }

    private func jsonObject(url: URL, method: String, body: [String: Any]?) async throws -> [String: Any] {
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = method
        urlRequest.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = try JSONSerialization.data(withJSONObject: body)
        }

        let (data, response) = try await session.data(for: urlRequest)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw HttpAttachmentGatewayError.invalidResponse
        }

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpAttachmentGatewayError.requestFailed(errorMessage(data: data, statusCode: httpResponse.statusCode))
        }

        guard let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw HttpAttachmentGatewayError.invalidPayload
        }

        return object
    }

    private func string(_ value: Any?) -> String? {
        if let value = value as? String {
            let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? nil : trimmed
        }

        if let value = value as? NSNumber {
            return value.stringValue
        }

        return nil
    }

    private func int(_ value: Any?) -> Int? {
        if let value = value as? Int { return value }
        if let value = value as? NSNumber { return value.intValue }
        if let value = value as? String { return Int(value.trimmingCharacters(in: .whitespacesAndNewlines)) }
        return nil
    }

    private func int64(_ value: Any?) -> Int64? {
        if let value = value as? Int64 { return value }
        if let value = value as? Int { return Int64(value) }
        if let value = value as? NSNumber { return value.int64Value }
        if let value = value as? String { return Int64(value.trimmingCharacters(in: .whitespacesAndNewlines)) }
        return nil
    }

    private func errorMessage(data: Data, statusCode: Int) -> String {
        guard !data.isEmpty,
              let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return "Mobile attachment request failed with HTTP \(statusCode)."
        }

        let code = string(object["code"]) ?? "mobile_attachment_error"
        let message = string(object["message"]) ?? "Mobile attachment request failed."

        return "\(code): \(message)"
    }

    private func normalizedBaseUrl() -> String {
        String(baseUrl.drop(while: { $0 == " " }).reversed().drop(while: { $0 == "/" }).reversed())
    }
}

public enum HttpAttachmentGatewayError: Error {
    case invalidUrl
    case invalidResponse
    case invalidPayload
    case requestFailed(String)
}
