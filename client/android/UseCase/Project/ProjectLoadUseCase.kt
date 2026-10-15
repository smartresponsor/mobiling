package app.mobiling.client.usecase.project

import app.mobiling.client.contract.project.ProjectMobileItemPayload
import app.mobiling.client.data.project.ProjectGateway

class ProjectLoadUseCase(private val gateway: ProjectGateway) { suspend fun load(vendorId: String): List<ProjectMobileItemPayload> = gateway.loadProjects(vendorId) }
