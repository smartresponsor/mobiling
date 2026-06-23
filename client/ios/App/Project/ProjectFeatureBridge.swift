import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// App-level bridge for project-domain controlled rewire.
@available(*, deprecated, message: "Prefer VendorBusinessBridge.ownedProject() or VendorNavigationBridge.project for vendor-owned navigation.")
struct ProjectFeatureBridge {
    let listingGateway: ProjectListingGateway

    func list(query: ListProjectsQuery) async -> [ProjectSummary] {
        await ListProjectsUseCase(gateway: listingGateway).invoke(query: query)
    }

    func detail(projectId: String) async -> ProjectDetailPayload {
        await LoadProjectDetailUseCase(gateway: listingGateway).invoke(projectId: projectId)
    }
}
