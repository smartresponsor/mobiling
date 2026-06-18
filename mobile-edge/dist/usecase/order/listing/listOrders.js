// Marketing America Corp. Oleksandr Tishchenko
export async function listOrders(query) {
    return [
        {
            orderId: 'ord-1001',
            orderNumber: 'ORD-1001',
            stateCode: query.stateCode ?? 'processing',
            totalLabel: '$128.00',
            placedAtIso: '2026-03-22T00:00:00Z',
            itemCount: 3,
        },
    ];
}
