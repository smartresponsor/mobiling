import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadProjectDetailUseCase {
    let projectListingGateway: ProjectListingGateway

    func callAsFunction(projectId: String) async throws -> ProjectDetailPayload {
        try await projectListingGateway.loadProjectDetail(projectId: projectId)
    }
}
