// Marketing America Corp. Oleksandr Tishchenko
import { Router } from 'express';
import { listShipments } from '../../usecase/shipment/tracking/listShipments';
export const shipmentTrackingRouter = Router();
shipmentTrackingRouter.get('/', async (req, res) => {
    const payload = await listShipments({
        orderId: typeof req.query.orderId === 'string' ? req.query.orderId : undefined,
        statusCode: typeof req.query.statusCode === 'string' ? req.query.statusCode : undefined,
        cursor: typeof req.query.cursor === 'string' ? req.query.cursor : undefined,
        limit: typeof req.query.limit === 'string' ? Number.parseInt(req.query.limit, 10) || 20 : 20,
    });
    res.json(payload);
});
