// Marketing America Corp. Oleksandr Tishchenko
import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { loadShipmentDetail } from '../../usecase/shipment/detail/loadShipmentDetail';

export async function shipmentDetailRoute(req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  const shipmentId = String(req.params.shipmentId ?? '');
  const payload = await loadShipmentDetail(shipmentId);
  res.json(payload);
}