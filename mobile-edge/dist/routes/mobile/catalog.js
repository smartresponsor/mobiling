import { CatalogingApiClient } from "../../client/cataloging/catalogingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/mobile/access/error.js";
import { mobileCatalogListPayload, mobileCatalogNodeDetailPayload, mobileCatalogSearchPayload, } from "../../contract/mobile/catalog.js";
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
    app.get("/catalog", { schema: { response: { 200: mobileCatalogListPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await catalogingApiClient.get(`/api/category/storefront${querySuffix(request.query)}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogList(result.body));
    });
    app.get("/catalog/node/:catalogNodeId", { schema: { response: { 200: mobileCatalogNodeDetailPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const catalogNodeId = encodeURIComponent(stringValue(params.catalogNodeId) ?? "");
        const result = await catalogingApiClient.get(`/api/category/${catalogNodeId}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCatalogDetail(result.body));
    });
    app.get("/catalog/search", { schema: { response: { 200: mobileCatalogSearchPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await catalogingApiClient.get(`/api/category/search${querySuffix(request.query)}`, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send({ query: stringValue(recordValue(request.query).q), ...normalizeCatalogList(result.body) });
    });
}
