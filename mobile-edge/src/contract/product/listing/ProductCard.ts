// Marketing America Corp. Oleksandr Tishchenko
export interface ProductCard {
  productId: string;
  title: string;
  subtitle?: string | null;
  priceLabel?: string | null;
  thumbnailUrl?: string | null;
  availabilityLabel?: string | null;
}
