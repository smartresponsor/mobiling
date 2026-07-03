import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public enum MobileLinkParser {
    private static let customPrefix = "smartresponsor://mobile/"
    private static let universalPrefix = "https://app.smartresponsor.com/mobile/"

    public static func parse(_ raw: String?) -> MobileLink? {
        let value = (raw ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        guard !value.isEmpty else {
            return nil
        }

        let normalized: String
        if value.hasPrefix(customPrefix) {
            normalized = String(value.dropFirst(customPrefix.count))
        } else if value.hasPrefix(universalPrefix) {
            normalized = String(value.dropFirst(universalPrefix.count))
        } else if value.hasPrefix("/") {
            normalized = String(value.drop { $0 == "/" })
        } else {
            normalized = value
        }

        let parts = normalized.split(separator: "?", maxSplits: 1, omittingEmptySubsequences: false)
        let route = normalizeRoute(String(parts.first ?? ""))
        guard !route.isEmpty else {
            return nil
        }

        let query = parts.count > 1 ? parseQuery(String(parts[1])) : [:]
        return MobileLink(raw: value, route: route, query: query)
    }

    private static func normalizeRoute(_ value: String) -> String {
        value
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .split(separator: "/", omittingEmptySubsequences: true)
            .joined(separator: "/")
    }

    private static func parseQuery(_ value: String) -> [String: String] {
        var result: [String: String] = [:]
        for part in value.split(separator: "&", omittingEmptySubsequences: true) {
            let pair = part.split(separator: "=", maxSplits: 1, omittingEmptySubsequences: false)
            let key = String(pair.first ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
            guard !key.isEmpty else {
                continue
            }
            result[key] = pair.count > 1 ? String(pair[1]).trimmingCharacters(in: .whitespacesAndNewlines) : ""
        }
        return result
    }
}
