package com.smartresponsor.mobile.client.contract.product.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProductDetailPayload(
    val productId: String,
    val title: String,
    val description: String?,
    val priceLabel: String?,
    val mediaUrls: List<String>,
    val vendorLabel: String?,
)
