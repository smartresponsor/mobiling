export async function loadSessionIdentity() {
    return {
        userId: 'stub-user',
        accountId: 'stub-account',
        displayName: 'Stub User',
        activeRole: 'operator',
        authenticated: true,
    };
}
