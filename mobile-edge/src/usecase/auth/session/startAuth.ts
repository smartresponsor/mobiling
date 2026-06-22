import type { StartAuthBody } from '../../contract/auth/session/StartAuthBody';
import type { AuthSessionPayload } from '../../contract/auth/session/AuthSession';

export async function startAuth(_body: StartAuthBody): Promise<AuthSessionPayload> {
  return {
    status: 'unauthenticated',
    sessionId: null,
    authenticated: false,
    requiresVerification: false,
    requiresSecondFactor: false,
  };
}
