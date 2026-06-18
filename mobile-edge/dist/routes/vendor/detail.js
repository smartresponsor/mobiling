import { loadVendorDetail } from '../../usecase/vendor/detail/loadVendorDetail';
// Marketing America Corp. Oleksandr Tishchenko
export async function vendorDetailRoute(vendorId) {
    return loadVendorDetail(vendorId);
}
