# Client/Edge 0.2.18 Access Error State Hardening

- Purpose: remove stale milestone-only access failure copy and capture the current access endpoint mapping after recovery UI and edge wiring.
- Android uses production-safe unavailable copy for access API fallback states.
- iOS uses production-safe unavailable copy for access API fallback states.
- Access business rules remain owned by Accessing.
- Mobile-edge remains a transport proxy and response normalizer.

## Client route states

- `Welcome`
- `SignIn`
- `Register`
- `VerificationRequired`
- `SecondFactorRequired`
- `RecoveryRequest`
- `RecoveryReset`

## Mobile-edge public access endpoints

- `POST /access/signin`
- `POST /access/register`
- `POST /access/logout`
- `GET /access/session`
- `POST /access/verification/resend`
- `POST /access/verification/confirm`
- `POST /access/second-factor/challenge`
- `POST /access/second-factor/verify`
- `POST /access/recovery/request`
- `POST /access/recovery/reset`

