// Marketing America Corp. Oleksandr Tishchenko
export interface ListVendorsQuery {
  searchText: string | null;
  categoryCode: string | null;
  cityCode: string | null;
  page: number;
  pageSize: number;
}
