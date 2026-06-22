export interface AuthSessionPayload {
  status: string;
  sessionId: string | null;
  authenticated: boolean;
  requiresVerification: boolean;
  requiresSecondFactor: boolean;
}
