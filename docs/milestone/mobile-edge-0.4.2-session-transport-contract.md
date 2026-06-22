# Mobile Edge 0.4.2 Session Transport Contract Milestone

- Purpose: define the mobile-edge session transport contract without inventing mobile-owned access tokens.
- Decision: Accessing remains the owner of authentication, session state, and session transport.
- Current Mobile Edge behavior:
  - forwards inbound `cookie` and `authorization` headers to Accessing API calls
  - preserves Accessing response status and normalized response body
  - exposes Accessing response cookies from `AccessingApiClient` as `responseCookie`
  - passes Accessing response cookies back through Fastify replies for access routes
  - documents Accessing-owned session transport in OpenAPI access route descriptions
  - does not mint `accessToken`, `refreshToken`, or authenticated identity data locally
- Affected routes:
  - `POST /mobile/access/signin`
  - `POST /mobile/access/register`
  - `POST /mobile/access/logout`
  - `GET /mobile/access/session`
- Verification target: `npm run typecheck` and `npm run build` in `mobile-edge`.
