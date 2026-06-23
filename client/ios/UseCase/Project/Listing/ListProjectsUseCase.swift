import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ListProjectsUseCase {
    let projectListingGateway: ProjectListingGateway

    func callAsFunction(query: ListProjectsQuery) async throws -> [ProjectSummary] {
        try await projectListingGateway.listProjects(query: query)
    }
}
