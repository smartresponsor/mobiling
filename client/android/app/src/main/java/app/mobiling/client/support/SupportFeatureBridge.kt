package app.mobiling.client.support

import app.mobiling.client.contract.support.SupportPagePayload
import app.mobiling.client.data.support.SupportGateway

class SupportFeatureBridge(
    private val gateway: SupportGateway,
) {
    suspend fun load(path: String): SupportPagePayload = gateway.load(path)

    suspend fun submit(path: String, fields: Map<String, String>): SupportPagePayload =
        gateway.submit(path, fields)
}
