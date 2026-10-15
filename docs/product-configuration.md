# Mobile product configuration foundation

Mobiling has equivalent Android and iOS configuration concepts for product, brand, environment, initial destination, catalog policy, semantic text resolution, and centralized application composition.

The current defaults intentionally preserve checkpoint behavior:

- product and brand profile: `one_tasker`;
- Android Mobile Edge URL: `http://10.0.2.2:8080`;
- iOS Mobile Edge URL: `http://localhost:8080`;
- initial destination: `dashboard`;
- primary catalog: `service`;
- enabled catalogs: `service`, `product`, and `project`.

Android compile-time values are supplied through `BuildConfig`. iOS compile-time values are declared in `Config/Base.xcconfig`, with source defaults retained as a defensive fallback. Semantic text is prepared through native Android resources and the iOS String Catalog.

`MobileApplicationGraph` is the platform composition root. Android activities and iOS views consume already-constructed bridges and gateways, and every transport dependency derives its endpoint from the selected `EnvironmentProfile`.

`MobileTextResolver` is backward-compatible: a known semantic key resolves locally, while an absent or unknown key keeps the backend-provided label unchanged. This foundation does not switch domains, rename menu items, add translations, or activate another primary catalog.

Future product variants should override native build configuration rather than branch application code. Runtime remote configuration, staged rollout, and live brand switching remain separate post-RC capabilities.
