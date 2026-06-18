// Marketing America Corp. Oleksandr Tishchenko
export type OrderLineItem = {
  lineId: string;
  productId: string;
  productTitle: string;
  quantity: number;
  lineTotalLabel: string;
};

export type OrderDetail = {
  orderId: string;
  orderNumber: string;
  stateCode: string;
  totalLabel: string;
  subtotalLabel: string;
  taxationLabel: string | null;
  shippingLabel: string | null;
  placedAtIso: string;
  lineItems: OrderLineItem[];
};
