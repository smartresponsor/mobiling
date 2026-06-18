import { listCatalogNodes } from '../../usecase/catalog/browse/listCatalogNodes';

// Marketing America Corp. Oleksandr Tishchenko
export async function catalogBrowseRoute() {
  return listCatalogNodes({
    parentNodeId: null,
    searchText: null,
    includeEmptyNodes: false,
  });
}
