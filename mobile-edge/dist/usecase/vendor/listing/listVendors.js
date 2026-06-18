// Marketing America Corp. Oleksandr Tishchenko
export async function listVendors(query) {
    return [
        {
            vendorId: 'vendor-demo-1',
            displayName: 'Demo Vendor',
            categoryLabel: query.categoryCode ?? 'general',
            ratingLabel: '4.8',
            logoUrl: null,
            cityLabel: query.cityCode,
        },
    ];
}
