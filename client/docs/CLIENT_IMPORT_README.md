# Mobile Client U116–U120 (analytic NDJSON, signer, sampling, guard)
- U116 Event queue (NDJSON), cap и ротация.
- U117 HMAC signer (X-SR-Analytic-Signature), API key заголовок.
- U118 Sampling + QA override (features.sampling, qa.analytics_on).
- U119 NetGuard + Wi‑Fi only интеграция, batch‑flush.
- U120 Backpressure + локальный rate для ошибок, связка с Backoff/RateLimiter.
Android + iOS, без сторонних зависимостей. Singular naming.
