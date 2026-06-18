import type { StartAuthBody } from '../../contract/auth/session/StartAuthBody';
import type { AuthSessionPayload } from '../../contract/auth/session/AuthSession';

export async function startAuth(body: StartAuthBody): Promise<AuthSessionPayload> {
  return {
    accessToken: `stub-${body.login}-token`,
    refreshToken: 'stub-refresh-token',
    expiresAt: new Date(Date.now() + 3600_000).toISOString(),
    sessionId: 'stub-session',
  };
}
