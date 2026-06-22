import { Router } from 'express';

const router = Router();

router.post('/session', async (_req, res) => {
  res.status(410).json({
    code: 'legacy_auth_session_retired',
    message: 'Use /mobile/access/* session transport.',
  });
});

export default router;
