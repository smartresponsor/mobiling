// Marketing America Corp. Oleksandr Tishchenko
export type ListOrdersQuery = {
  stateCode: string | null;
  searchText: string | null;
  page: number;
  pageSize: number;
};
