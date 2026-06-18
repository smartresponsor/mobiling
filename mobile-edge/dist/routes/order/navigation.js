/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical order-level navigation manifest.
 */
import { orderOwnedSubflows } from './subflows';
export const orderNavigation = {
    root: 'vendor/order',
    ownedFlows: ['shipment', 'taxation'],
    routes: orderOwnedSubflows,
};
