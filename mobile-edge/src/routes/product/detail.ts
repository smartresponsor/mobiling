import type { Request, Response } from 'express';
import { loadProductDetail } from '../../usecase/product/detail/loadProductDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function productDetailRoute(req: Request, res: Response): Promise<void> {
  const productId = String(req.params.productId ?? '');
  const product = await loadProductDetail(productId);
  res.json({ product });
}
