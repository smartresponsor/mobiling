# Purchase (client → edge)
POST `/mobile/receipt/verify` с заголовками `Authorization` и `Idempotency-Key`.
Restore = перечисление местных покупок + verify каждой (батч можно через буфер).
