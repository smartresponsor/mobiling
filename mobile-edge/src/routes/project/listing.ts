// Marketing America Corp. Oleksandr Tishchenko
import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { listProjects } from '../../usecase/project/listing/listProjects';

export async function projectListingRoute(req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  const payload = await listProjects({
    searchTerm: typeof req.query.searchTerm === 'string' ? req.query.searchTerm : undefined,
    stateCode: typeof req.query.stateCode === 'string' ? req.query.stateCode : undefined,
    cursor: typeof req.query.cursor === 'string' ? req.query.cursor : undefined,
    limit: typeof req.query.limit === 'string' ? Number.parseInt(req.query.limit, 10) || 20 : 20,
  });

  res.json(payload);
}