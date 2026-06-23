import { loadTaxSummary } from '../../usecase/taxation/summary/loadTaxSummary';

// Marketing America Corp. Oleksandr Tishchenko
export async function taxSummaryRoute() {
  return loadTaxSummary({
    orderId: null,
    shipmentId: null,
    productId: null,
    jurisdictionCode: 'US-TX',
  });
}
