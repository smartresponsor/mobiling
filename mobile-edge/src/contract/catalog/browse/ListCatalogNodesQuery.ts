// Marketing America Corp. Oleksandr Tishchenko
export interface ListCatalogNodesQuery {
  parentNodeId?: string | null;
  searchText?: string | null;
  includeEmptyNodes: boolean;
}
