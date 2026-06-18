package com.smartresponsor.mobile.client.data.taxation.detail

import com.smartresponsor.mobile.client.contract.taxation.detail.taxdetailpayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface TaxDetailGateway {
    suspend fun loadTaxDetail(taxDocumentId: String): TaxDetailPayload
}
