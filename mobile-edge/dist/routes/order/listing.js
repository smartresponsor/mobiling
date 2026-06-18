import { listOrders } from '../../usecase/order/listing/listOrders';
// Marketing America Corp. Oleksandr Tishchenko
export async function orderListingRoute() {
    return listOrders({
        stateCode: null,
        searchText: null,
        page: 1,
        pageSize: 20,
    });
}
