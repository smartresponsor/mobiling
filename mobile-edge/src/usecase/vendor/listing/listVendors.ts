import type { ListVendorsQuery } from '../../../contract/vendor/listing/ListVendorsQuery';
import type { VendorCard } from '../../../contract/vendor/listing/VendorCard';

// Marketing America Corp. Oleksandr Tishchenko
export async function listVendors(query: ListVendorsQuery): Promise<VendorCard[]> {
  return [
    {
      vendorId: 'vendor-demo-1',
      displayName: 'Demo Vendor',
      categoryLabel: query.categoryCode ?? 'general',
      ratingLabel: '4.8',
      logoUrl: null,
      cityLabel: query.cityCode,
    },
  ];
}
