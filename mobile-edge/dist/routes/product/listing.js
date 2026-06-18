import { listProducts } from '../../usecase/product/listing/listProducts';
// Marketing America Corp. Oleksandr Tishchenko
export async function productListingRoute(req, res) {
    const searchTerm = typeof req.query.searchTerm === 'string' ? req.query.searchTerm : null;
    const page = Number.parseInt(String(req.query.page ?? '1'), 10) || 1;
    const pageSize = Number.parseInt(String(req.query.pageSize ?? '20'), 10) || 20;
    const items = await listProducts({ searchTerm, page, pageSize });
    res.json({ items });
}
