// Marketing America Corp. Oleksandr Tishchenko
import type { ListProjectsQuery } from '../../../contract/project/listing/ListProjectsQuery';

export const listProjects = async (query: ListProjectsQuery) => ({
  items: [],
  query,
});
