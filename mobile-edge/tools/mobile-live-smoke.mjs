import "./mobile-live-smoke-check.mjs";
process.exit(0);

const origin = (process.env.MOBILE_EDGE_SMOKE_ORIGIN || "http://127.0.0.1:8080").replace(/\/$/, "");
const vendorId = process.env.MOBILE_EDGE_SMOKE_VENDOR_ID || "vendor-demo-001";
const timeoutMs = Number.parseInt(process.env.MOBILE_EDGE_SMOKE_TIMEOUT_MS || "5000", 10);
const failures = [];

function fail(message) {
  failures.push(message);
}

function isRecord(value) {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function requiredFieldsFor(routeName) {
  const fields = {
    health: ["ok"],
    navigationShell: ["schema", "channel", "platforms", "locations"],
    vendorProfile: [
      "vendorId",
      "displayName",
      "brandName",
      "status",
      "completionPercent",
      "readyForPublishing",
      "nextAction",
      "avatarUrl",
      "coverUrl",
      "about",
      "website",
      "publicationStatus",
    ],
    vendorSummary: ["vendorId", "brandName", "status", "profileCompletionPercent", "nextAction", "payload"],
    vendorStatement: ["vendorId", "statementStatus", "currency", "grossAmount", "netAmount", "payload"],
    vendorPayout: ["vendorId", "payoutStatus", "currency", "availableAmount", "pendingAmount", "payoutAccountLabel", "payload"],
    vendorTransaction: ["vendorId", "transactions", "payload"],
  };

  return fields[routeName] || [];
}

function assertRequiredFields(routeName, payload) {
  if (!isRecord(payload)) {
    fail(`${routeName} response must be a JSON object.`);
    return;
  }

  for (const field of requiredFieldsFor(routeName)) {
    if (!Object.prototype.hasOwnProperty.call(payload, field)) {
      fail(`${routeName} response is missing required field '${field}'.`);
    }
  }
}

function assertErrorPayload(routeName, payload) {
  if (!isRecord(payload)) {
    fail(`${routeName} error response must be a JSON object.`);
    return;
  }

  if ("string" !== typeof payload.code || "" === payload.code.trim()) {
    fail(`${routeName} error response is missing non-empty code.`);
  }

  if ("string" !== typeof payload.message || "" === payload.message.trim()) {
    fail(`${routeName} error response is missing non-empty message.`);
  }
}

async function requestJson(routeName, path) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetch(`${origin}${path}`, {
      method: "GET",
      headers: { accept: "application/json" },
      signal: controller.signal,
    });
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();
    let payload = null;

    if ("" !== text.trim()) {
      try {
        payload = JSON.parse(text);
      } catch {
        fail(`${routeName} returned non-JSON body from ${path}.`);
      }
    }

    return {
      ok: true,
      path,
      status: response.status,
      contentType,
      payload,
    };
  } catch (error) {
    fail(`${routeName} request failed for ${path}: ${error instanceof Error ? error.message : String(error)}`);

    return {
      ok: false,
      path,
      status: 0,
      contentType: "",
      payload: null,
    };
  } finally {
    clearTimeout(timeout);
  }
}

async function smokeRoute(routeName, path) {
  const result = await requestJson(routeName, path);

  if (!result.ok) {
    return;
  }

  if (!result.contentType.includes("application/json")) {
    fail(`${routeName} returned non-JSON content-type '${result.contentType}'.`);
  }

  if (200 === result.status) {
    assertRequiredFields(routeName, result.payload);
    return;
  }

  if ([400, 404, 422, 500, 503].includes(result.status)) {
    assertErrorPayload(routeName, result.payload);
    return;
  }

  fail(`${routeName} returned unexpected status ${result.status} from ${path}.`);
}

await smokeRoute("health", "/health");
await smokeRoute("navigationShell", "/navigation/mobile/shell");
await smokeRoute("vendorProfile", `/vendor/profile/${encodeURIComponent(vendorId)}`);
await smokeRoute("vendorSummary", `/vendor/summary/${encodeURIComponent(vendorId)}`);
