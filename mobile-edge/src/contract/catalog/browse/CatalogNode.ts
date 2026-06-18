// Marketing America Corp. Oleksandr Tishchenko
export interface CatalogNode {
  nodeId: string;
  parentNodeId?: string | null;
  title: string;
  slug?: string | null;
  childCount: number;
  productCount?: number | null;
}
