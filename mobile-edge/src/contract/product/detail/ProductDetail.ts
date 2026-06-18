// Marketing America Corp. Oleksandr Tishchenko
export interface ProductDetail {
  productId: string;
  title: string;
  description?: string | null;
  priceLabel?: string | null;
  mediaUrls: string[];
  vendorLabel?: string | null;
}
