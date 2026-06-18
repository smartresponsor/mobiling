import type { SessionIdentityPayload } from '../../contract/identity/session/SessionIdentity';

export async function loadSessionIdentity(): Promise<SessionIdentityPayload> {
  return {
    userId: 'stub-user',
    accountId: 'stub-account',
    displayName: 'Stub User',
    activeRole: 'operator',
    authenticated: true,
  };
}
