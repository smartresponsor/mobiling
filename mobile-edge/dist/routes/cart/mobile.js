import { CartingApiClient } from "../../client/carting/cartingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileCartCheckoutHandoffPayload, mobileCartItemMutationRequest, mobileCartPayload, } from "../../contract/cart/index.js";
const cartingApiClient = new CartingApiClient();
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    const applicationKey = request.headers["x-application-key"];
    const applicationEnvironment = request.headers["x-application-environment"];
    const cartToken = request.headers["x-cart-token"];
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
    if ("string" === typeof cartToken && "" !== cartToken.trim()) {
        headers["x-cart-token"] = cartToken.trim();
    }
    return headers;
}
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
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
function recordValue(value) {
    return isRecord(value) ? value : {};
}
function normalizeErrorPayload(body) {
    if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
        return { code: body.code, message: body.message };
    }
    if (isRecord(body) && "string" === typeof body.error) {
        return { code: body.error, message: body.error.replace(/_/g, " ") };
    }
    return { code: "carting_api_error", message: "Carting API returned an unexpected response." };
}
function cartPayload(body) {
    if (!isRecord(body)) {
        return {};
    }
    return isRecord(body.data) ? body.data : body;
}
function normalizeCartItem(value) {
    const item = recordValue(value);
    const quantity = Math.max(1, integerValue(item.quantity, 1));
    const unitPriceMinor = integerValue(item.unitPriceMinor ?? item.unitPrice ?? item.priceMinor, 0);
    return {
        itemId: stringValue(item.itemId ?? item.id) ?? "",
        offerReference: stringValue(item.offerReference ?? item.offerId ?? item.productId) ?? "",
        title: stringValue(item.title ?? item.titleSnapshot ?? item.name) ?? "Cart item",
        unitPriceMinor,
        currencyCode: stringValue(item.currencyCode ?? item.currency) ?? "USD",
        quantity,
        lineTotalMinor: integerValue(item.lineTotalMinor ?? item.totalMinor, unitPriceMinor * quantity),
        metadata: recordValue(item.metadata),
    };
}
function normalizeCart(body) {
    const root = cartPayload(body);
    const items = Array.isArray(root.items) ? root.items.map(normalizeCartItem) : [];
    const subtotalMinor = integerValue(root.subtotalMinor ?? root.subtotal, items.reduce((sum, item) => sum + integerValue(item.lineTotalMinor), 0));
    return {
        cartId: stringValue(root.cartId ?? root.id),
        cartToken: stringValue(root.cartToken ?? root.token) ?? "cart-unavailable",
        ownerReference: stringValue(root.ownerReference ?? root.ownerId),
        status: stringValue(root.status) ?? "active",
        currencyCode: stringValue(root.currencyCode ?? root.currency) ?? "USD",
        itemCount: integerValue(root.itemCount, items.length),
        subtotalMinor,
        totalMinor: integerValue(root.totalMinor ?? root.total, subtotalMinor),
        items,
        expiresAt: stringValue(root.expiresAt),
        updatedAt: stringValue(root.updatedAt),
        payload: root,
    };
}
function normalizeCheckoutHandoff(body) {
    const root = cartPayload(body);
    return {
        cartId: stringValue(root.cartId ?? root.id),
        cartToken: stringValue(root.cartToken ?? root.token) ?? "cart-unavailable",
        handoffId: stringValue(root.handoffId ?? root.id) ?? "handoff-unavailable",
        checkoutUrl: stringValue(root.checkoutUrl ?? root.url),
        status: stringValue(root.status) ?? "prepared",
        expiresAt: stringValue(root.expiresAt),
        payload: root,
    };
}
export default async function route(app) {
    app.get("/cart/current", { schema: { response: { 200: mobileCartPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await cartingApiClient.getCurrentCart(forwardedHeaders(request));
        if (result.cartToken)
            reply.header("x-cart-token", result.cartToken);
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeCart(result.body));
    });
    app.post("/cart/item", { schema: { body: mobileCartItemMutationRequest, response: { 200: mobileCartPayload, 201: mobileCartPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await cartingApiClient.addItem(request.body, forwardedHeaders(request));
        if (result.cartToken)
            reply.header("x-cart-token", result.cartToken);
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(201 === result.status ? 201 : 200).send(normalizeCart(result.body));
    });
    app.post("/cart/checkout-handoff", { schema: { response: { 200: mobileCartCheckoutHandoffPayload, 201: mobileCartCheckoutHandoffPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await cartingApiClient.prepareCheckoutHandoff(forwardedHeaders(request));
        if (result.cartToken)
            reply.header("x-cart-token", result.cartToken);
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(201 === result.status ? 201 : 200).send(normalizeCheckoutHandoff(result.body));
    });
}
