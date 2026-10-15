package app.mobiling.client.contract.order

data class OrderMobileItemPayload(val orderId: String, val reference: String, val status: String?, val totalLabel: String?)
