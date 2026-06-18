export default async function route(app) { app.get('/health', async () => ({ ok: true })); }
