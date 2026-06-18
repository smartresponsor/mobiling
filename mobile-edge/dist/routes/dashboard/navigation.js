// Marketing America Corp. Oleksandr Tishchenko
// Dashboard navigation manifest.
import auth from '../auth';
import catalog from '../catalog';
import message from '../message';
import vendor from '../vendor';
export const dashboardNavigationManifest = {
    root: 'dashboard',
    primary: {
        catalog,
        message,
        vendor,
        auth,
    },
    secondary: {
        product: 'vendor/product',
    },
    owned: {
        catalog: ['vendor', 'product'],
        vendor: ['product', 'order', 'project', 'profile'],
        order: ['shipment', 'taxation'],
    },
    navigationOwners: {
        catalog: 'catalog/navigation',
        vendor: 'vendor/navigation',
        order: 'order/ownership',
    },
};
export default dashboardNavigationManifest;
