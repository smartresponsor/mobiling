// Marketing America Corp. Oleksandr Tishchenko
import type { Request, Response } from 'express';
import { loadShipmentDetail } from '../../usecase/shipment/detail/loadShipmentDetail';

export async function shipmentDetailRoute(req: Request, res: Response): Promise<void> {
  const payload = await loadShipmentDetail(req.params.shipmentId);
  res.json(payload);
}