import { loadTaxDetail } from '../../usecase/taxation/detail/loadTaxDetail';
// Marketing America Corp. Oleksandr Tishchenko
export async function taxationDetailRoute() {
    return loadTaxDetail('taxdoc-1001');
}
