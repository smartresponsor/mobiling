import { loadTaxDetail } from '../../usecase/taxation/detail/loadTaxDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function taxDetailRoute() {
  return loadTaxDetail('taxdoc-1001');
}
