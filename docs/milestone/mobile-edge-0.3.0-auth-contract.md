# Mobile Edge 0.3.0 Auth Contract Milestone

- Purpose: materialize the mobile-edge access/auth contract for Android and iOS clients without connecting to Accessing yet.
- Endpoints:
  - `POST /mobile/access/signin`
  - `POST /mobile/access/register`
  - `POST /mobile/access/logout`
  - `GET /mobile/access/session`
- Request/response model names:
  - `MobileAccessSignInRequest`
  - `MobileAccessRegisterRequest`
  - `MobileAccessIdentityPayload`
  - `MobileAccessSessionPayload`
  - `MobileAccessErrorPayload`
- Limitation: no real Accessing integration yet, no real token storage, and no production authentication behavior.
- Relationship to Android/iOS: this is the shared edge contract that follows the Android and iOS `0.2.0-access-entry-materialized` guest entry milestones.
- Verification: `npm run typecheck` from `D:\PhpstormProjects\www\Mobiling\mobile-edge` succeeded.
- Next recommended milestone: `0.4.0-mobile-edge-accessing-integration`.
