import { Router } from 'express';
import { loadSessionIdentity } from '../../usecase/identity/session/loadSessionIdentity';
const router = Router();
router.get('/session', async (_req, res) => {
    res.json(await loadSessionIdentity());
});
export default router;
