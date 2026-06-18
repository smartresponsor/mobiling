// Marketing America Corp. Oleksandr Tishchenko
import type { ListShipmentsQuery } from '../../../contract/shipment/tracking/ListShipmentsQuery';

export const listShipments = async (query: ListShipmentsQuery) => ({
  items: [],
  query,
});
