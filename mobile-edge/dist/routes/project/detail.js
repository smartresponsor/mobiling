// Marketing America Corp. Oleksandr Tishchenko
import { Router } from 'express';
import { loadProjectDetail } from '../../usecase/project/detail/loadProjectDetail';
export const projectDetailRouter = Router();
projectDetailRouter.get('/:projectId', async (req, res) => {
    const payload = await loadProjectDetail(req.params.projectId);
    res.json(payload);
});
