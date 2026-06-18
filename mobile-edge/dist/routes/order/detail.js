import { loadOrderDetail } from '../../usecase/order/detail/loadOrderDetail';
// Marketing America Corp. Oleksandr Tishchenko
export async function orderDetailRoute(orderId) {
    return loadOrderDetail(orderId);
}
