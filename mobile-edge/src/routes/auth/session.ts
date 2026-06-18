import { Router } from 'express';
import { startAuth } from '../../usecase/auth/session/startAuth';

const router = Router();

router.post('/session', async (req, res) => {
  res.json(await startAuth(req.body));
});

export default router;
