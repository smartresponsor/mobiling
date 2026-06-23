// Marketing America Corp. Oleksandr Tishchenko
import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { loadProjectDetail } from '../../usecase/project/detail/loadProjectDetail';

export async function projectDetailRoute(req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  const payload = await loadProjectDetail(String(req.params.projectId ?? ''));
  res.json(payload);
}