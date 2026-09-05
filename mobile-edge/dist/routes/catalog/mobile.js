import { CatalogingApiClient } from "../../client/cataloging/catalogingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileCatalogListPayload, mobileCatalogMutationPayload, mobileCatalogMutationRequest, mobileCatalogNodeDetailPayload, mobileCatalogSearchPayload, } from "../../contract/catalog/index.js";
const catalogingApiClient = new CatalogingApiClient();
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    const applicationKey = request.headers["x-application-key"];
    const applicationEnvironment = request.headers["x-application-environment"];
    if ("string" === typeof cookie && "" !== cookie.trim()) {
        headers.cookie = cookie.trim();
    }
    if ("string" === typeof authorization && "" !== authorization.trim()) {
        headers.authorization = authorization.trim();
    }
    if ("string" === typeof applicationKey && "" !== applicationKey.trim()) {
        headers["x-application-key"] = applicationKey.trim();
    }
    if ("string" === typeof applicationEnvironment && "" !== applicationEnvironment.trim()) {
        headers["x-application-environment"] = applicationEnvironment.trim();
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
function responsePayload(body) {
    if (!isRecord(body)) {
        return body;
    }
    return undefined === body.data ? body : body.data;
}
function catalogItems(payload) {
    if (Array.isArray(payload))
        return payload;
    if (!isRecord(payload))
        return [];
    if (Array.isArray(payload.nodes))
        return payload.nodes;
    if (Array.isArray(payload.items))
        return payload.items;
    if (Array.isArray(payload.categories))
        return payload.categories;
    return [];
}
function nodeId(value) {
    const item = recordValue(value);
    return stringValue(item.nodeId ?? item.catalogNodeId ?? item.categoryId ?? item.id);
}
function directChildren(nodes, parentNodeId) {
    if (!parentNodeId) {
        return nodes;
    }
    const stack = [...nodes];
    while (stack.length > 0) {
        const candidate = stack.shift();
        if (!isRecord(candidate))
            continue;
        if (nodeId(candidate) === parentNodeId) {
            return Array.isArray(candidate.children) ? candidate.children : [];
        }
        if (Array.isArray(candidate.children)) {
            stack.push(...candidate.children);
        }
    }
    return [];
}
function normalizeCatalogNode(value) {
    const item = recordValue(value);
    const media = recordValue(item.media ?? item.image ?? item.icon);
    const children = Array.isArray(item.children) ? item.children : [];
    return {
        nodeId: nodeId(item) ?? "catalog-node-unavailable",
        parentNodeId: stringValue(item.parentNodeId ?? item.parentId ?? item.parent_id),
        title: stringValue(item.title ?? item.name ?? item.nameEntity ?? item.label) ?? "Untitled catalog node",
        slug: stringValue(item.slug),
        imageUrl: stringValue(item.imageUrl
            ?? item.iconUrl
            ?? item.icon_url
            ?? item.thumbnailUrl
            ?? item.thumbnail_url
            ?? media.url
            ?? media.imageUrl
            ?? media.iconUrl),
        childCount: integerValue(item.childCount ?? item.childrenCount, children.length),
        productCount: integerValue(item.productCount, 0),
    };
}
function normalizeCatalogList(body, parentNodeId = null) {
    const payload = responsePayload(body);
    const nodes = directChildren(catalogItems(payload), parentNodeId);
    return { nodes: nodes.map(normalizeCatalogNode), payload };
}
function normalizeCatalogDetail(body) {
    const payload = responsePayload(body);
    const root = recordValue(payload);
    const node = normalizeCatalogNode(root.node ?? root.category ?? root.item ?? root);
    return {
        node,
        description: stringValue(root.description),
        breadcrumbLabels: Array.isArray(root.breadcrumbLabels) ? root.breadcrumbLabels.map(stringValue).filter(Boolean) : [],
        featuredProductIds: Array.isArray(root.featuredProductIds) ? root.featuredProductIds.map(stringValue).filter(Boolean) : [],
        payload,
    };
}
function normalizeCatalogMutation(body, fallbackStatus, catalogNodeId = null, attachmentId = null) {
    const root = recordValue(responsePayload(body));
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
    const q = stringValue(source.q ?? source.searchText);
    if (q)
        search.set("q", q);
    return "" === search.toString() ? "" : `?${search.toString()}`;
}
export default async function route(app) {
    app.get("/catalog", { schema: { response: { 200: mobileCatalogListPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const query = recordValue(request.query);
        const parentNodeId = stringValue(query.parentNodeId);
        const result = await catalogingApiClient.get(`/api/catalog/category/store${querySuffix(query)}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogList(result.body, parentNodeId));
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
