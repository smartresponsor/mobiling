import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { loadProductDetail } from '../../usecase/product/detail/loadProductDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function productDetailRoute(req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  const productId = String(req.params.productId ?? '');
  const product = await loadProductDetail(productId);
  res.json({ product });
}
