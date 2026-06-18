// Marketing America Corp. Oleksandr Tishchenko
export type ListShipmentsQuery = {
  orderId?: string;
  statusCode?: string;
  cursor?: string;
  limit: number;
};
