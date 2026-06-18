import type { ProductCard } from '../../../contract/product/listing/ProductCard';
import type { ListProductsQuery } from '../../../contract/product/listing/ListProductsQuery';

// Marketing America Corp. Oleksandr Tishchenko
export async function listProducts(_query: ListProductsQuery): Promise<ProductCard[]> {
  return [];
}
