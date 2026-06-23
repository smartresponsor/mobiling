import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { loadProfileDetail } from '../../usecase/profile/detail/loadProfileDetail';

export async function profileDetailRoute(req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  const profileId = String(req.params.profileId ?? '');
  res.json(await loadProfileDetail(profileId));
}