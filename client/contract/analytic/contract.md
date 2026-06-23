# Analytic contract (client → edge)
- Endpoint: `POST /mobile/analytic/batch-ndjson`
- Headers:
  - `X-API-Key: <KEY>`
  - `X-SR-Analytic-Signature: <HMAC_SHA256_HEX(body)>`
- Body: NDJSON; каждая строка — JSON объекта `{type, ts, ...}`
- Sampling: берём из server config: `sampling.{name}` [0..1]; override: `qa.analytics_on=true`
- Имена событий: snake_case, максимум 5k/батч.
- Политика: PII scrub на уровне текстов (см. Scrubber).
