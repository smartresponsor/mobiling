import { Router } from 'express';
import { loadProfileDetail } from '../../usecase/profile/detail/loadProfileDetail';

const router = Router();

router.get('/detail/:profileId', async (req, res) => {
  res.json(await loadProfileDetail(req.params.profileId));
});

export default router;
