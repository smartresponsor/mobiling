import type { VendorDetail } from '../../../contract/vendor/detail/VendorDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function loadVendorDetail(vendorId: string): Promise<VendorDetail> {
  return {
    vendorId,
    displayName: 'Demo Vendor',
    description: 'Vendor profile placeholder for canonical mobile edge slice.',
    websiteUrl: null,
    contactEmail: null,
    phoneLabel: null,
    cityLabel: null,
    ratingLabel: '4.8',
  };
}
