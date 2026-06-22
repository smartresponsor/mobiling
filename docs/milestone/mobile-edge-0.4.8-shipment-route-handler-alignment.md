# Mobile Edge 0.4.8 Shipment Route Handler Alignment Milestone

- Purpose: align shipment route modules with existing commerce/order manifest exports.
- Replaced legacy `Router()` exports with handler function exports.
- `shipmentTrackingRoute` now matches commerce/order manifest expectations.
- `shipmentDetailRoute` now matches commerce/order manifest expectations.
- Active Fastify `app.ts` route registration remains unchanged.
- Not included: shipment route deletion, Fastify rewrite, persistence changes, or shipment business logic changes.