import type { CatalogNode } from '../browse/CatalogNode';

// Marketing America Corp. Oleksandr Tishchenko
export interface CatalogNodeDetail {
  node: CatalogNode;
  description?: string | null;
  breadcrumbLabels: string[];
  featuredProductIds: string[];
}
