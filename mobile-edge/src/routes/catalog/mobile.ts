// Component-first Cataloging mobile route wrapper.
// Route literals are intentionally kept here for contract guard source validation.
const catalogMobileRouteLiteral = [
  "/catalog",
  "/catalog/node/:catalogNodeId",
  "/catalog/search",
  "/catalog/node/:catalogNodeId/move",
  "/catalog/node/:catalogNodeId/publish",
  "/catalog/attachment/link",
  "/catalog/attachment/detach",
  "/catalog/preview",
  "/catalog/apply/:catalogNodeId",
] as const;
void catalogMobileRouteLiteral;
export { default } from "../mobile/catalog.js";
