package app.mobiling.client.data.navigation.shell

import app.mobiling.client.contract.navigation.shell.MobileNavigationShellPayload

interface NavigationShellGateway {
    suspend fun loadMobileShell(): MobileNavigationShellPayload
}
