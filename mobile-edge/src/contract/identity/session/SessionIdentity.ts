export interface SessionIdentityPayload {
  vendorId: string | null;
  accountId: string | null;
  displayName?: string;
  activeRole?: string;
  authenticated: boolean;
}
