import type { SessionIdentityPayload } from '../../contract/identity/session/SessionIdentity';

export async function loadSessionIdentity(): Promise<SessionIdentityPayload> {
  return {
    vendorId: null,
    accountId: null,
    authenticated: false,
  };
}
