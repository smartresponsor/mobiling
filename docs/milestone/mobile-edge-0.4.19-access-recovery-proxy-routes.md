# Mobile Edge 0.4.19 Access Recovery Proxy Routes

- Purpose: complete mobile-edge proxy routes for Accessing-owned recovery request and reset flows.
- Added Accessing API client recovery request payload.
- Added Accessing API client recovery reset payload.
- Added Accessing API client `requestRecovery`.
- Added Accessing API client `resetRecovery`.
- Added mobile access recovery request schema.
- Added mobile access recovery reset schema.
- Added `/access/recovery/request`.
- Added `/access/recovery/reset`.
- Mobile-edge forwards session transport and normalizes Accessing session/error payloads.
- Not included: recovery email delivery, code generation, code validation, password policy, or Accessing business rules.