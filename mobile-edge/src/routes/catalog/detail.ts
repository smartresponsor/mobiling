import { loadCatalogNodeDetail } from '../../usecase/catalog/detail/loadCatalogNodeDetail';

// Marketing America Corp. Oleksandr Tishchenko
export async function catalogDetailRoute(nodeId: string) {
  return loadCatalogNodeDetail(nodeId);
}
