package com.smartresponsor.mobile.client.usecase.taxation.detail

import com.smartresponsor.mobile.client.contract.taxation.detail.TaxDetailPayload
import com.smartresponsor.mobile.client.data.taxation.detail.TaxDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadTaxDetailUseCase(
    private val gateway: TaxDetailGateway,
) {
    suspend operator fun invoke(taxDocumentId: String): TaxDetailPayload = gateway.loadTaxDetail(taxDocumentId)
}
