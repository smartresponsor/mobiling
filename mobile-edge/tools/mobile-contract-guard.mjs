import { readdir, readFile, stat } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const failures = [];

function fail(message) {
  failures.push(message);
}

async function readText(relativePath) {
  return readFile(path.join(root, relativePath), "utf8");
}

async function readJson(relativePath) {
  return JSON.parse(await readText(relativePath));
}

async function exists(relativePath) {
  try {
    await stat(path.join(root, relativePath));
    return true;
  } catch {
    return false;
  }
}

async function collectFiles(relativeDirectory, extension) {
  const directory = path.join(root, relativeDirectory);
  const entries = await readdir(directory, { withFileTypes: true });
  const files = [];

  for (const entry of entries) {
    const childRelativePath = path.join(relativeDirectory, entry.name).replace(/\\/g, "/");

    if (entry.isDirectory()) {
      files.push(...await collectFiles(childRelativePath, extension));
      continue;
    }

    if (entry.isFile() && childRelativePath.endsWith(extension)) {
      files.push(childRelativePath);
    }
  }

  return files;
}

function toOpenApiPath(routePath) {
  return routePath.replace(/:([A-Za-z][A-Za-z0-9_]*)/g, "{$1}");
}

function routeKey(method, routePath) {
  return `${method.toUpperCase()} ${routePath}`;
}

function extractOpenApiBlock(openapi, routePath) {
  const openApiPath = toOpenApiPath(routePath);
  const marker = `  ${openApiPath}:`;
  const start = openapi.indexOf(marker);

  if (-1 === start) {
    return null;
  }

  const next = openapi.indexOf("\n  /", start + marker.length);

  return -1 === next ? openapi.slice(start) : openapi.slice(start, next);
}

function extractOpenApiComponentSchemas(openapi) {
  const normalizedOpenApi = openapi.replace(/\r\n/g, "\n");
  const marker = "\n  schemas:\n";
  const start = normalizedOpenApi.indexOf(marker);

  if (-1 === start) {
    return new Set();
  }

  const next = normalizedOpenApi.indexOf("\nsecuritySchemes:", start + marker.length);
  const body = -1 === next ? normalizedOpenApi.slice(start) : normalizedOpenApi.slice(start, next);

  return new Set([...body.matchAll(/\n    ([A-Za-z][A-Za-z0-9_]*):/g)].map((match) => match[1]));
}

function extractRoutes(source) {
  const routes = [];
  const pattern = /app\.(get|post|put|patch|delete)\(\s*["']([^"']+)["']/g;
  let match = pattern.exec(source);

  while (null !== match) {
    routes.push({ method: match[1].toUpperCase(), path: match[2] });
    match = pattern.exec(source);
  }

  return routes;
}

function responseSchemaExportName(responseSchema) {
  if (!responseSchema || "string" !== typeof responseSchema) {
    return null;
  }

  return `${responseSchema.slice(0, 1).toLowerCase()}${responseSchema.slice(1)}`;
}

function extractRequiredFields(contractSource, responseSchema) {
  const exportName = responseSchemaExportName(responseSchema);

  if (null === exportName) {
    return null;
  }

  const exportMarker = `export const ${exportName}`;
  const exportStart = contractSource.indexOf(exportMarker);

  if (-1 === exportStart) {
    return null;
  }

  const requiredStart = contractSource.indexOf("required:", exportStart);
  const propertiesStart = contractSource.indexOf("properties:", exportStart);

  if (-1 === requiredStart || (-1 !== propertiesStart && requiredStart > propertiesStart)) {
    return [];
  }

  const arrayStart = contractSource.indexOf("[", requiredStart);
  const arrayEnd = -1 === arrayStart ? -1 : contractSource.indexOf("]", arrayStart);

  if (-1 === arrayStart || -1 === arrayEnd) {
    return null;
  }

  return [...contractSource.slice(arrayStart, arrayEnd).matchAll(/"([^"]+)"/g)].map((match) => match[1]);
}

async function validateFixtureRequiredFields(route, key) {
  if (!route.contractFile || !await exists(route.contractFile)) {
    fail(`${key} is a screen route without a source contract file: ${route.contractFile ?? "<missing>"}`);
    return;
  }

  if (!route.fixture || !await exists(route.fixture)) {
    return;
  }

  const contractSource = await readText(route.contractFile);
  const requiredFields = extractRequiredFields(contractSource, route.responseSchema);

  if (null === requiredFields) {
    fail(`${key} could not extract required fields from ${route.contractFile} for ${route.responseSchema}.`);
    return;
  }

  let fixturePayload;

  try {
    fixturePayload = await readJson(route.fixture);
  } catch {
    fail(`${key} fixture is not valid JSON: ${route.fixture}`);
    return;
  }

  if (null === fixturePayload || "object" !== typeof fixturePayload || Array.isArray(fixturePayload)) {
    fail(`${key} fixture must be a JSON object: ${route.fixture}`);
    return;
  }

  for (const field of requiredFields) {
    if (!Object.prototype.hasOwnProperty.call(fixturePayload, field)) {
      fail(`${key} fixture ${route.fixture} is missing required field '${field}' from ${route.contractFile}.`);
    }
  }
}

const manifest = JSON.parse(await readText("contract/mobile-route-contract.json"));
const openapi = await readText("openapi/openapi.yaml");
const openApiComponentSchemas = extractOpenApiComponentSchemas(openapi);
const upstreamContract = await readJson("contract/mobile-upstream-contract.json");
const declaredRouteKeys = new Set();

if ("mobile-edge" !== upstreamContract.consumer) {
  fail("mobile upstream contract consumer must stay mobile-edge.");
}

if (!Array.isArray(upstreamContract.routes) || upstreamContract.routes.length < 6) {
  fail("mobile upstream contract must declare the baseline screen upstream routes and every added mobile business route.");
}

for (const file of ["src/client/carting/cartingApiClient.ts", "src/client/navigating/navigatingApiClient.ts", "src/client/vendoring/vendoringApiClient.ts"]) {
  const source = await readText(file);

  if (!source.includes("status >= 300 && status < 400") || !source.includes("resolve(this.unavailable())")) {
    fail(`${file} must handle upstream 3xx redirects inside mobile-edge.`);
  }
}

if ("contract-first" !== manifest.policy?.mode) {
  fail("mobile route contract policy mode must stay contract-first.");
}

if ("bff-only" !== manifest.policy?.mobileEdgeRole) {
  fail("mobile-edge role must stay bff-only.");
}

for (const route of manifest.routes ?? []) {
  const key = routeKey(route.method, route.path);
  declaredRouteKeys.add(key);

  if (!route.owner || "string" !== typeof route.owner) {
    fail(`${key} is missing an upstream owner.`);
  }

  if (route.responseSchema && !openApiComponentSchemas.has(route.responseSchema)) {
    fail(`${key} response schema ${route.responseSchema} is not declared in OpenAPI components.schemas.`);
  }

  if (!route.routeFile || !await exists(route.routeFile)) {
    fail(`${key} routeFile does not exist: ${route.routeFile}`);
  }

  if (route.implementationFile && !await exists(route.implementationFile)) {
    fail(`${key} implementationFile does not exist: ${route.implementationFile}`);
  }

  const routeSource = route.routeFile && await exists(route.routeFile) ? await readText(route.routeFile) : "";
  const implementationSource = route.implementationFile && await exists(route.implementationFile)
    ? await readText(route.implementationFile)
    : "";
  const combinedSource = `${routeSource}\n${implementationSource}`;

  if (!combinedSource.includes(`"${route.path}"`) && !combinedSource.includes(`'${route.path}'`)) {
    fail(`${key} is declared but not found in its route source.`);
  }

  const openApiBlock = extractOpenApiBlock(openapi, route.path);

  if (null === openApiBlock) {
    fail(`${key} is missing from openapi/openapi.yaml.`);
  } else {
    const methodPattern = new RegExp(`\\n    ${route.method.toLowerCase()}:`);

    if (!methodPattern.test(openApiBlock)) {
      fail(`${key} is missing its OpenAPI method block.`);
    }

    if (route.responseSchema && !openApiBlock.includes(`#/components/schemas/${route.responseSchema}`)) {
      fail(`${key} does not reference response schema ${route.responseSchema} in OpenAPI.`);
    }
  }

  if ("screen" === route.routeClass) {
    if (!route.screen) {
      fail(`${key} is a screen route without a screen identifier.`);
    }

    if (!route.fixture || !await exists(route.fixture)) {
      fail(`${key} is a screen route without a stable fixture: ${route.fixture ?? "<missing>"}`);
    }

    await validateFixtureRequiredFields(route, key);
  }
}

const routeFiles = await collectFiles("src/routes/mobile", ".ts");
const discoveredRouteKeys = new Set();
const forbiddenCrudFragments = ["/crud", "/create", "/update", "/delete", "/edit", "/remove"];
const forbiddenDatabasePatterns = [
  /from\s+["'](?:pg|mysql|mysql2|sqlite3|better-sqlite3|typeorm|knex|@prisma\/client)["']/,
  /new\s+PrismaClient\s*\(/,
  /createConnection\s*\(/,
];

for (const file of routeFiles) {
  const source = await readText(file);
  const discoveredRoutes = extractRoutes(source);

  for (const route of discoveredRoutes) {
    const key = routeKey(route.method, route.path);
    discoveredRouteKeys.add(key);

    if (!declaredRouteKeys.has(key)) {
      fail(`${key} is implemented in ${file} but is not declared in contract/mobile-route-contract.json.`);
    }

    if (["PUT", "PATCH", "DELETE"].includes(route.method)) {
      fail(`${key} uses a mutation verb forbidden for the current mobile route surface.`);
    }

    for (const fragment of forbiddenCrudFragments) {
      if (route.path.includes(fragment)) {
        fail(`${key} contains forbidden CRUD-like route fragment ${fragment}.`);
      }
    }

    if (route.path.includes("/mobile/session")) {
      fail(`${key} reintroduces retired /mobile/session transport.`);
    }
  }

  for (const pattern of forbiddenDatabasePatterns) {
    if (pattern.test(source)) {
      fail(`${file} imports or opens direct database access; mobile-edge routes must stay BFF-only.`);
    }
  }

  if (/const\s+mobile[A-Za-z0-9]+Payload\s*=\s*\{/.test(source)) {
    fail(`${file} declares an inline mobile response payload schema; place mobile route schemas under src/contract/mobile instead.`);
  }

  if (/contract\/mobile\/vendor[A-Z][A-Za-z0-9]*\.js/.test(source)) {
    fail(`${file} imports a flat vendor mobile contract; use src/contract/mobile/vendor/<name>.ts instead.`);
  }

  if (/contract\/mobile\/access\.js/.test(source)) {
    fail(`${file} imports a flat access mobile contract; use src/contract/mobile/access/<name>.ts instead.`);
  }

  if (/contract\/mobile\/navigationShell\.js/.test(source)) {
    fail(`${file} imports a flat navigation mobile contract; use src/contract/mobile/navigation/<name>.ts instead.`);
  }
}

for (const key of declaredRouteKeys) {
  if (!discoveredRouteKeys.has(key)) {
    const route = (manifest.routes ?? []).find((candidate) => routeKey(candidate.method, candidate.path) === key);

    if (!route?.implementationFile) {
      fail(`${key} is declared but no Fastify route implementation was discovered.`);
    }
  }
}

if (failures.length > 0) {
  console.error("Mobile contract guard failed:");

  for (const failure of failures) {
    console.error(` - ${failure}`);
  }

  process.exit(1);
}

console.log(`Mobile contract guard passed: ${declaredRouteKeys.size} routes, ${routeFiles.length} mobile route files.`);
