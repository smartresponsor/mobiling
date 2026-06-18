// Marketing America Corp. Oleksandr Tishchenko
export async function loadTaxSummary(query) {
    return {
        jurisdictionCode: query.jurisdictionCode ?? 'US-TX',
        taxableAmountLabel: '$100.00',
        taxAmountLabel: '$8.25',
        totalAmountLabel: '$108.25',
        currencyCode: 'USD',
    };
}
