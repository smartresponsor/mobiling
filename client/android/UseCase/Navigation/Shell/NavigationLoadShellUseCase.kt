package app.mobiling.client.usecase.navigation.shell

import app.mobiling.client.contract.navigation.shell.NavigationMobileShellPayload
import app.mobiling.client.data.navigation.shell.NavigationShellGateway

class NavigationLoadShellUseCase(
    private val gateway: NavigationShellGateway,
) {
    suspend operator fun invoke(): NavigationMobileShellPayload = gateway.loadMobileShell()
}
