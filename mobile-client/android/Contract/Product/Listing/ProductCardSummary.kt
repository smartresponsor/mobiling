package com.smartresponsor.mobile.client.contract.product.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProductCardSummary(
    val productId: String,
    val title: String,
    val subtitle: String?,
    val priceLabel: String?,
    val thumbnailUrl: String?,
    val availabilityLabel: String?,
)
