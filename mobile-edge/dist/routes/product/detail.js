import { loadProductDetail } from '../../usecase/product/detail/loadProductDetail';
// Marketing America Corp. Oleksandr Tishchenko
export async function productDetailRoute(req, res) {
    const productId = String(req.params.productId ?? '');
    const product = await loadProductDetail(productId);
    res.json({ product });
}
