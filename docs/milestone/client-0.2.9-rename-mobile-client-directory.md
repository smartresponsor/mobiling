# Client 0.2.9 Rename Mobile Client Directory Milestone

- Purpose: remove redundant filesystem naming after `/mobile` HTTP prefix cleanup.
- Renamed `mobile-client/` to `client/` in Git.
- Android client remains under `client/android`.
- iOS client remains under `client/ios`.
- Cross-platform contract metadata remains under `client/contract`.
- Package names such as `app.mobiling.client` are unchanged.
- Physical `mobile-client/` may require deletion after Windows releases filesystem locks.
- Not included: auth logic changes, UI behavior changes, Android package rename, iOS target rename, or network contract changes.