/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical vendor-level navigation manifest.
 */
import { vendorOwnedSubflows } from './subflows';
export const vendorNavigation = {
    root: 'vendor',
    ownedFlows: ['product', 'order', 'project', 'profile'],
    routes: vendorOwnedSubflows,
};
