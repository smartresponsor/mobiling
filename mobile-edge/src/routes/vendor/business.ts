/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-centric business route manifest.
 *
 * It reflects vendor ownership over vendor-facing product, order,
 * project, and profile flows.
 */
export { vendorListingRoute } from './listing';
export { vendorDetailRoute } from './detail';
export { productListingRoute } from '../product/listing';
export { productDetailRoute } from '../product/detail';
export { orderListingRoute } from '../order/listing';
export { orderDetailRoute } from '../order/detail';
export { projectListingRouter } from '../project/listing';
export { projectDetailRouter } from '../project/detail';
export { default as profileDetailRouter } from '../profile/detail';

export { vendorOwnedFlows } from './ownership';
export { vendorNavigationManifest } from './navigation';
