// Marketing America Corp. Oleksandr Tishchenko
export type ListProjectsQuery = {
  searchTerm?: string;
  stateCode?: string;
  cursor?: string;
  limit: number;
};
