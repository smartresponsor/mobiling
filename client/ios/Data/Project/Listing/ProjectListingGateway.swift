import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol ProjectListingGateway {
    func listProjects(query: ListProjectsQuery) async throws -> [ProjectSummary]

    func loadProjectDetail(projectId: String) async throws -> ProjectDetailPayload
}
