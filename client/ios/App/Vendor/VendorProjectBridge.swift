import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-owned project bridge.
struct VendorProjectBridge {
    let feature: ProjectFeatureBridge

    func list(query: ListProjectsQuery) async -> [ProjectSummary] {
        await feature.list(query: query)
    }

    func detail(projectId: String) async -> ProjectDetailPayload {
        await feature.detail(projectId: projectId)
    }
}
