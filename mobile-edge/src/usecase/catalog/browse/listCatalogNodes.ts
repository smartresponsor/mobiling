import type { CatalogNode } from '../../../contract/catalog/browse/CatalogNode';
import type { ListCatalogNodesQuery } from '../../../contract/catalog/browse/ListCatalogNodesQuery';

// Marketing America Corp. Oleksandr Tishchenko
export async function listCatalogNodes(query: ListCatalogNodesQuery): Promise<CatalogNode[]> {
  void query;
  return [];
}
