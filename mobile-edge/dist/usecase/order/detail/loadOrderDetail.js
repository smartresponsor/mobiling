// Marketing America Corp. Oleksandr Tishchenko
export async function loadOrderDetail(orderId) {
    return {
        orderId,
        orderNumber: 'ORD-1001',
        stateCode: 'processing',
        totalLabel: '$128.00',
        subtotalLabel: '$110.00',
        taxationLabel: '$8.00',
        shippingLabel: '$10.00',
        placedAtIso: '2026-03-22T00:00:00Z',
        lineItems: [
            {
                lineId: 'line-1',
                productId: 'prod-1',
                productTitle: 'Sample order line',
                quantity: 1,
                lineTotalLabel: '$128.00',
            },
        ],
    };
}
