package com.smartresponsor.mobile.client.contract.system.billing

data class ReceiptUploadPayload(
    val token: String,
    val product: String,
)
