// Marketing America Corp. Oleksandr Tishchenko
import { Router } from 'express';
import { loadShipmentDetail } from '../../usecase/shipment/detail/loadShipmentDetail';

export const shipmentDetailRouter = Router();

shipmentDetailRouter.get('/:shipmentId', async (req, res) => {
  const payload = await loadShipmentDetail(req.params.shipmentId);
  res.json(payload);
});
