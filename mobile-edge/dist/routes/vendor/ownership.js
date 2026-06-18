/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor ownership manifest.
 *
 * Product, Order, Project, and Profile remain subordinate to Vendor.
 */
export const vendorOwnedFlows = [
    'product',
    'order',
    'project',
    'profile',
];
export { vendorListingRoute } from './listing';
export { vendorDetailRoute } from './detail';
export { productListingRoute } from '../product/listing';
export { productDetailRoute } from '../product/detail';
export { orderListingRoute } from '../order/listing';
export { orderDetailRoute } from '../order/detail';
export { projectListingRouter } from '../project/listing';
export { projectDetailRouter } from '../project/detail';
export { default as profileDetailRouter } from '../profile/detail';
