export interface AuthSessionPayload {
  accessToken: string;
  refreshToken?: string;
  expiresAt: string;
  sessionId: string;
}
