// Marketing America Corp. Oleksandr Tishchenko
export type ShipmentTrackingSummary = {
  shipmentId: string;
  shipmentNumber: string;
  carrierCode: string;
  trackingNumber: string;
  currentStatusCode: string;
  updatedAtIso: string;
};
