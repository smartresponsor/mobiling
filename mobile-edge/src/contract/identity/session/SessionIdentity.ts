export interface SessionIdentityPayload {
  userId: string;
  accountId: string;
  displayName?: string;
  activeRole?: string;
  authenticated: boolean;
}
