const attachmentMobileRouteLiteral = [
  "/attachment",
  "/attachment/link",
  "/attachment/detach",
  "/attachment/file/:attachmentId",
  "/attachment/upload-handoff",
] as const;
void attachmentMobileRouteLiteral;

export { default } from "../mobile/attachment.js";
