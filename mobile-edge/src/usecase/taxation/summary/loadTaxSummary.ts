import type { LoadTaxSummaryQuery } from '../../../contract/taxation/summary/LoadTaxSummaryQuery';
import type { TaxSummary } from '../../../contract/taxation/summary/TaxSummary';

// Marketing America Corp. Oleksandr Tishchenko
export async function loadTaxSummary(query: LoadTaxSummaryQuery): Promise<TaxSummary> {
  return {
    jurisdictionCode: query.jurisdictionCode ?? 'US-TX',
    taxableAmountLabel: '$100.00',
    taxAmountLabel: '$8.25',
    totalAmountLabel: '$108.25',
    currencyCode: 'USD',
  };
}
