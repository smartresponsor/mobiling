/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Order-owned subflow route manifest.
 *
 * It reflects order ownership over shipment and taxation behavior.
 */
export { orderListingRoute } from './listing';
export { orderDetailRoute } from './detail';
export { shipmentTrackingRoute } from '../shipment/tracking';
export { shipmentDetailRoute } from '../shipment/detail';
export { taxSummaryRoute } from '../taxation/summary';
export { taxDetailRoute } from '../taxation/detail';

export { orderOwnedFlows } from './ownership';

export { orderNavigation } from './navigation';
export { orderOwnedSubflows } from './subflows';
