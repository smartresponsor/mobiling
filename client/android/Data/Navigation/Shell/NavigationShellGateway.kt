package app.mobiling.client.data.navigation.shell

import app.mobiling.client.contract.navigation.shell.NavigationMobileShellPayload

interface NavigationShellGateway {
    suspend fun loadMobileShell(): NavigationMobileShellPayload
}
