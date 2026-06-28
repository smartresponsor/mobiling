const origin = (process.env.MOBILE_EDGE_SMOKE_ORIGIN || "http://127.0.0.1:8080").replace(/\/$/, "");
const vendorId = process.env.MOBILE_EDGE_SMOKE_VENDOR_ID || "vendor-demo-001";
const timeoutMs = Number.parseInt(process.env.MOBILE_EDGE_SMOKE_TIMEOUT_MS || "5000", 10);
const failures = [];

const routes = [
  { name: "health", path: "/health", ok: ["ok"] },
  { name: "navigationShell", path: "/navigation/mobile/shell", ok: ["schema", "channel", "platforms", "locations"] },
  { name: "vendorProfile", path: `/vendor/profile/${encodeURIComponent(vendorId)}`, ok: ["vendorId", "displayName", "brandName", "status", "completionPercent", "readyForPublishing", "nextAction", "avatarUrl", "coverUrl", "about", "website", "publicationStatus"] },
  { name: "vendorSummary", path: `/vendor/summary/${encodeURIComponent(vendorId)}`, ok: ["vendorId", "brandName", "status", "profileCompletionPercent", "nextAction", "payload"] },
  { name: "vendorStatement", path: `/vendor/statement/${encodeURIComponent(vendorId)}`, ok: ["vendorId", "statementStatus", "currency", "grossAmount", "netAmount", "payload"] },
  { name: "vendorPayout", path: `/vendor/payout/${encodeURIComponent(vendorId)}`, ok: ["vendorId", "payoutStatus", "currency", "availableAmount", "pendingAmount", "payoutAccountLabel", "payload"] },
  { name: "vendorTransaction", path: `/vendor/transaction/${encodeURIComponent(vendorId)}`, ok: ["vendorId", "transactions", "payload"] },
];

function fail(message) {
  failures.push(message);
}

function isRecord(value) {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function assertRequired(name, payload, fields) {
  if (!isRecord(payload)) {
    fail(`${name} response must be a JSON object.`);
    return;
  }

  for (const field of fields) {
    if (!Object.prototype.hasOwnProperty.call(payload, field)) {
      fail(`${name} response is missing required field '${field}'.`);
    }
  }
}

function assertError(name, payload) {
  if (!isRecord(payload)) {
    fail(`${name} error response must be a JSON object.`);
    return;
  }

  if ("string" !== typeof payload.code || "" === payload.code.trim()) {
    fail(`${name} error response is missing non-empty code.`);
  }

  if ("string" !== typeof payload.message || "" === payload.message.trim()) {
    fail(`${name} error response is missing non-empty message.`);
  }
}

async function request(route) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetch(`${origin}${route.path}`, {
      method: "GET",
      headers: { accept: "application/json" },
      redirect: "manual",
      signal: controller.signal,
    });
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();

    if ([301, 302, 307, 308].includes(response.status)) {
      const location = response.headers.get("location") || "<missing>";
      fail(`${route.name} redirected with status ${response.status} to ${location}.`);
      return;
    }

    const payload = "" === text.trim() ? null : JSON.parse(text);

    if (!contentType.includes("application/json")) {
      fail(`${route.name} returned non-JSON content-type '${contentType}'.`);
    }

    if ([301, 302, 307, 308].includes(response.status)) {
      const location = response.headers.get("location") || "<missing>";
      fail(`${route.name} redirected with status ${response.status} to ${location}.`);
      return;
    }

    if (200 === response.status) {
      assertRequired(route.name, payload, route.ok);
    } else if ([400, 404, 422, 500, 503].includes(response.status)) {
      assertError(route.name, payload);
    } else {
      fail(`${route.name} returned unexpected status ${response.status} from ${route.path}.`);
    }
  } catch (error) {
    fail(`${route.name} request failed for ${route.path}: ${error instanceof Error ? error.message : String(error)}`);
  } finally {
    clearTimeout(timeout);
  }
}

for (const route of routes) {
  await request(route);
}

if (failures.length > 0) {
  console.error(`Mobile live smoke failed against ${origin}:`);
  for (const failure of failures) {
    console.error(` - ${failure}`);
  }
  process.exitCode = 1;
} else {
  console.log(`Mobile live smoke passed against ${origin} using vendor ${vendorId}.`);
}
