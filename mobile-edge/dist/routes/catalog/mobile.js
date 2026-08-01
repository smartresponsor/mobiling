import { CatalogingApiClient } from "../../client/cataloging/catalogingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileCatalogListPayload, mobileCatalogMutationPayload, mobileCatalogMutationRequest, mobileCatalogNodeDetailPayload, mobileCatalogSearchPayload, } from "../../contract/catalog/index.js";
const catalogingApiClient = new CatalogingApiClient();
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    if ("string" === typeof cookie && "" !== cookie.trim()) {
        headers.cookie = cookie.trim();
    }
    if ("string" === typeof authorization && "" !== authorization.trim()) {
        headers.authorization = authorization.trim();
    }
    return headers;
}
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
}
function recordValue(value) {
    return isRecord(value) ? value : {};
}
function stringValue(value) {
    if ("string" === typeof value && "" !== value.trim()) {
        return value.trim();
    }
    if ("number" === typeof value && Number.isFinite(value)) {
        return String(value);
    }
    return null;
}
function integerValue(value, fallback = 0) {
    if ("number" === typeof value && Number.isFinite(value)) {
        return Math.max(0, Math.round(value));
    }
    if ("string" === typeof value && "" !== value.trim()) {
        const numeric = Number(value.trim());
        return Number.isFinite(numeric) ? Math.max(0, Math.round(numeric)) : fallback;
    }
    return fallback;
}
function normalizeErrorPayload(body) {
    if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
        return { code: body.code, message: body.message };
    }
    return { code: "cataloging_api_error", message: "Cataloging API returned an unexpected response." };
}
function catalogRoot(body) {
    if (!isRecord(body)) {
        return {};
    }
    return isRecord(body.data) ? body.data : body;
}
function catalogItems(root) {
    if (Array.isArray(root.nodes))
        return root.nodes;
    if (Array.isArray(root.items))
        return root.items;
    if (Array.isArray(root.categories))
        return root.categories;
    return [];
}
function catalogNodeId(value) {
    const item = recordValue(value);
    return stringValue(item.nodeId ?? item.catalogNodeId ?? item.categoryId ?? item.id);
}
function catalogChildren(value) {
    const item = recordValue(value);
    return Array.isArray(item.children) ? item.children : [];
}
function findCatalogNode(value, nodeId) {
    const item = recordValue(value);
    if (catalogNodeId(item) === nodeId) {
        return item;
    }
    for (const child of catalogChildren(item)) {
        const match = findCatalogNode(child, nodeId);
        if (match)
            return match;
    }
    return null;
}
function normalizeCatalogNode(value) {
    const item = recordValue(value);
    return {
        nodeId: stringValue(item.nodeId ?? item.catalogNodeId ?? item.categoryId ?? item.id) ?? "catalog-node-unavailable",
        parentNodeId: stringValue(item.parentNodeId ?? item.parentId),
        title: stringValue(item.title ?? item.name ?? item.label) ?? "Untitled catalog node",
        slug: stringValue(item.slug),
        childCount: integerValue(item.childCount ?? item.childrenCount, 0),
        productCount: integerValue(item.productCount, 0),
    };
}
function normalizeCatalogList(body) {
    const root = catalogRoot(body);
    return { nodes: catalogItems(root).map(normalizeCatalogNode), payload: root };
}
function normalizeCatalogDetail(body) {
    const root = catalogRoot(body);
    const node = normalizeCatalogNode(root.node ?? root.category ?? root.item ?? root);
    return {
        node,
        description: stringValue(root.description),
        breadcrumbLabels: Array.isArray(root.breadcrumbLabels) ? root.breadcrumbLabels.map(stringValue).filter(Boolean) : [],
        featuredProductIds: Array.isArray(root.featuredProductIds) ? root.featuredProductIds.map(stringValue).filter(Boolean) : [],
        payload: root,
    };
}
function normalizeCatalogMutation(body, fallbackStatus, catalogNodeId = null, attachmentId = null) {
    const root = catalogRoot(body);
    return {
        status: stringValue(root.status ?? root.state) ?? fallbackStatus,
        catalogNodeId: stringValue(root.catalogNodeId ?? root.categoryId ?? root.id) ?? catalogNodeId,
        attachmentId: stringValue(root.attachmentId ?? root.attachment_id) ?? attachmentId,
        payload: root,
    };
}
function querySuffix(query) {
    const source = recordValue(query);
    const search = new URLSearchParams();
    const parentNodeId = stringValue(source.parentNodeId);
    const q = stringValue(source.q ?? source.searchText);
    if (parentNodeId)
        search.set("parentNodeId", parentNodeId);
    if (q)
        search.set("q", q);
    return "" === search.toString() ? "" : `?${search.toString()}`;
}
export default async function route(app) {
    app.get("/catalog", { schema: { response: { 200: mobileCatalogListPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const catalogCode = stringValue(recordValue(request.query).catalogCode);
        if (!catalogCode) {
            return reply.code(400).send({ code: "catalog_code_required", message: "catalogCode is required." });
        }
        const result = await catalogingApiClient.get(`/api/catalog/${encodeURIComponent(catalogCode)}/category/tree`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        const normalized = normalizeCatalogList(result.body);
        const parentNodeId = stringValue(recordValue(request.query).parentNodeId);
        if (!parentNodeId) {
            return reply.code(200).send(normalized);
        }
        const root = catalogRoot(result.body);
        const parent = findCatalogNode(recordValue(root.root), parentNodeId);
        return reply.code(200).send({
            nodes: catalogChildren(parent).map(normalizeCatalogNode),
            payload: root,
        });
    });
    app.get("/catalog/node/:catalogNodeId", { schema: { response: { 200: mobileCatalogNodeDetailPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const catalogNodeId = encodeURIComponent(stringValue(params.catalogNodeId) ?? "");
        const result = await catalogingApiClient.get(`/api/catalog/category/${catalogNodeId}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogDetail(result.body));
    });
    app.get("/catalog/search", { schema: { response: { 200: mobileCatalogSearchPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await catalogingApiClient.get(`/api/catalog/category/search${querySuffix(request.query)}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send({ query: stringValue(recordValue(request.query).q), ...normalizeCatalogList(result.body) });
    });
    app.post("/catalog/node/:catalogNodeId/move", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const catalogNodeId = encodeURIComponent(stringValue(params.catalogNodeId) ?? "");
        const result = await catalogingApiClient.post(`/api/catalog/category/move/${catalogNodeId}`, request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogMutation(result.body, "moved", stringValue(params.catalogNodeId)));
    });
    app.post("/catalog/node/:catalogNodeId/publish", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const catalogNodeId = encodeURIComponent(stringValue(params.catalogNodeId) ?? "");
        const result = await catalogingApiClient.post(`/api/catalog/category/publish/${catalogNodeId}`, request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogMutation(result.body, "published", stringValue(params.catalogNodeId)));
    });
    app.post("/catalog/attachment/link", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 201: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await catalogingApiClient.post("/api/catalog/category/attachment", request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(201 === result.status ? 201 : 200).send(normalizeCatalogMutation(result.body, "linked"));
    });
    app.post("/catalog/attachment/detach", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const body = recordValue(request.body);
        const attachmentId = encodeURIComponent(stringValue(body.attachmentId ?? body.attachment_id) ?? "");
        const result = await catalogingApiClient.delete(`/api/catalog/category/attachment/${attachmentId}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogMutation(result.body, "detached", null, stringValue(body.attachmentId ?? body.attachment_id)));
    });
    app.post("/catalog/preview", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await catalogingApiClient.post("/api/catalog/category/virtual/preview", request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogMutation(result.body, "previewed"));
    });
    app.post("/catalog/apply/:catalogNodeId", { schema: { body: mobileCatalogMutationRequest, response: { 200: mobileCatalogMutationPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const catalogNodeId = encodeURIComponent(stringValue(params.catalogNodeId) ?? "");
        const result = await catalogingApiClient.post(`/api/catalog/category/virtual/apply/${catalogNodeId}`, request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogMutation(result.body, "applied", stringValue(params.catalogNodeId)));
    });
}
