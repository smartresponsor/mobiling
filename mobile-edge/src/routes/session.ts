import type { FastifyInstance } from "fastify";

export default async function route(_app: FastifyInstance): Promise<void> {
  // Legacy local mobile session token minting is intentionally disabled.
  // Mobile authentication now uses Accessing-owned `/access/*` session transport.
}
