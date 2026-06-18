import type { Request, Response } from 'express';
import { listProducts } from '../../usecase/product/listing/listProducts';

// Marketing America Corp. Oleksandr Tishchenko
export async function productListingRoute(req: Request, res: Response): Promise<void> {
  const searchTerm = typeof req.query.searchTerm === 'string' ? req.query.searchTerm : null;
  const page = Number.parseInt(String(req.query.page ?? '1'), 10) || 1;
  const pageSize = Number.parseInt(String(req.query.pageSize ?? '20'), 10) || 20;

  const items = await listProducts({ searchTerm, page, pageSize });
  res.json({ items });
}
