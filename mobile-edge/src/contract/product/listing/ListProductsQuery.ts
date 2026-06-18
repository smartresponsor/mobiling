// Marketing America Corp. Oleksandr Tishchenko
export interface ListProductsQuery {
  searchTerm?: string | null;
  page: number;
  pageSize: number;
}
