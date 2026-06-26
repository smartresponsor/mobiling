package app.mobiling.client.usecase.navigation.shell

import app.mobiling.client.contract.navigation.shell.MobileNavigationShellPayload
import app.mobiling.client.data.navigation.shell.NavigationShellGateway

class LoadNavigationShellUseCase(
    private val gateway: NavigationShellGateway,
) {
    suspend operator fun invoke(): MobileNavigationShellPayload = gateway.loadMobileShell()
}
