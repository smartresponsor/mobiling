export async function startAuth(body) {
    return {
        accessToken: `stub-${body.login}-token`,
        refreshToken: 'stub-refresh-token',
        expiresAt: new Date(Date.now() + 3600_000).toISOString(),
        sessionId: 'stub-session',
    };
}
