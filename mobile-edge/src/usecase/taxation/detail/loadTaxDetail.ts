import type { TaxDetail } from '../../../contract/taxation/detail/TaxDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function loadTaxDetail(taxDocumentId: string): Promise<TaxDetail> {
  return {
    taxDocumentId,
    jurisdictionCode: 'US-TX',
    taxableAmountLabel: '$100.00',
    subtotalAmountLabel: '$100.00',
    taxAmountLabel: '$8.25',
    totalAmountLabel: '$108.25',
    rateLabel: '8.25%',
    breakdownLines: ['State sales tax: $6.25', 'Local sales tax: $2.00'],
  };
}
