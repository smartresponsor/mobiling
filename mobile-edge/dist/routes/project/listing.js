// Marketing America Corp. Oleksandr Tishchenko
import { Router } from 'express';
import { listProjects } from '../../usecase/project/listing/listProjects';
export const projectListingRouter = Router();
projectListingRouter.get('/', async (req, res) => {
    const payload = await listProjects({
        searchTerm: typeof req.query.searchTerm === 'string' ? req.query.searchTerm : undefined,
        stateCode: typeof req.query.stateCode === 'string' ? req.query.stateCode : undefined,
        cursor: typeof req.query.cursor === 'string' ? req.query.cursor : undefined,
        limit: typeof req.query.limit === 'string' ? Number.parseInt(req.query.limit, 10) || 20 : 20,
    });
    res.json(payload);
});
