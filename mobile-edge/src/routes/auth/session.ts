import type { RouteHandlerRequest, RouteHandlerResponse } from '../routeHandlerContext';

export async function authSessionRetiredRoute(_req: RouteHandlerRequest, res: RouteHandlerResponse): Promise<void> {
  res.status(410).json({
    code: 'legacy_auth_session_retired',
    message: 'Use /access/* session transport.',
  });
}