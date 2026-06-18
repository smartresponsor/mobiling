import { listVendors } from '../../usecase/vendor/listing/listVendors';

// Marketing America Corp. Oleksandr Tishchenko
export async function vendorListingRoute() {
  return listVendors({
    searchText: null,
    categoryCode: null,
    cityCode: null,
    page: 1,
    pageSize: 20,
  });
}
