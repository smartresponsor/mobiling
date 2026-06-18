/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Order ownership manifest.
 *
 * Shipment and Taxation remain subordinate to Order.
 */
export const orderOwnedFlows = [
    'shipment',
    'taxation',
];
export { orderListingRoute } from './listing';
export { orderDetailRoute } from './detail';
export { shipmentTrackingRoute } from '../shipment/tracking';
export { shipmentDetailRoute } from '../shipment/detail';
export { taxSummaryRoute } from '../taxation/summary';
export { taxDetailRoute } from '../taxation/detail';
