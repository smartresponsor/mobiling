export default async function route(app:any){ app.get('/health', async ()=>({ok:true})); }
