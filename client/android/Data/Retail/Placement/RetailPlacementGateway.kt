package app.mobiling.client.data.retail.placement

data class RetailPlacementSnapshot(
    val retailId: String,
    val kind: String,
    val catalogCode: String?,
    val categoryId: String?,
    val title: String?,
    val status: String?,
    val nextStep: String,
    val requiresExactLocation: Boolean,
    val fulfillmentProfile: String?,
    val locationProfile: String?,
    val pricingProfile: String?,
)

interface RetailPlacementGateway {
    suspend fun snapshot(retailId: String): RetailPlacementSnapshot
    suspend fun configureFulfillment(retailId: String, fields: Map<String, String>): RetailPlacementSnapshot
    suspend fun configureLocation(retailId: String, fields: Map<String, String>): RetailPlacementSnapshot
    suspend fun configurePricing(retailId: String, fields: Map<String, String>): RetailPlacementSnapshot
    suspend fun publish(retailId: String): RetailPlacementSnapshot
}
