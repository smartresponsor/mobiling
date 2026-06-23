import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';
import { loadSessionIdentity } from '../../usecase/identity/session/loadSessionIdentity';

export async function identitySessionRoute(_req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  res.json(await loadSessionIdentity());
}