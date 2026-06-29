#!/usr/bin/env python3
from __future__ import annotations
import json
import re
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
error: list[str] = []
expected = {
    "root": "dashboard",
    "entry_flow": ["auth"],
    "primary_section": ["catalog", "message", "vendor"],
    "owner": {"vendor": ["product", "order", "project", "profile"], "order": ["shipment"]},
    "embedded": {"order": ["taxation"]},
    "internal": ["identity"],
}
ownership = json.loads((root / "client/contract/navigation/ownership.json").read_text(encoding="utf-8"))
for key, value in expected.items():
    if ownership.get(key) != value: error.append(f"ownership mismatch: {key}")
for relative in [
    "client/android/Contract/build.gradle.kts", "client/android/Data/build.gradle.kts",
    "client/android/UseCase/build.gradle.kts", "client/android/Navigation/build.gradle.kts",
    "client/android/UI/build.gradle.kts", "client/ios/Package.swift",
    "client/ios/project.yml", "mobile-edge/src/app.ts", "mobile-edge/src/repository/runtime.ts",
]:
    if not (root / relative).exists(): error.append(f"missing materialized surface: {relative}")
declaration = re.compile(r"\b(?:data\s+class|sealed\s+class|enum\s+class|class|interface|object|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)")
package = re.compile(r"(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*$")
symbols: dict[tuple[str, str], str] = {}
android = root / "client/android"
for path in android.rglob("*.kt"):
    text = path.read_text(encoding="utf-8"); match = package.search(text)
    if match:
        for name in declaration.findall(text): symbols[(match.group(1), name.lower())] = name
for path in android.rglob("*.kt"):
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("import com.smartresponsor."):
            imported = line.removeprefix("import ").strip(); owner, name = imported.rsplit(".", 1)
            canonical = symbols.get((owner, name.lower()))
            if canonical and canonical != name: error.append(f"non-canonical Kotlin import: {path.relative_to(root)}: {name} -> {canonical}")
edge = (root / "mobile-edge/src/app.ts").read_text(encoding="utf-8")
android_shell = (root / "client/android/app/src/main/java/app/mobiling/client/dashboard/DashboardMobileShell.kt").read_text(encoding="utf-8")
ios_shell = (root / "client/ios/App/Dashboard/MobileDashboardShellView.swift").read_text(encoding="utf-8")

def require_shell_contains(name: str, text: str, needle: str) -> None:
    if needle not in text:
        error.append(f"mobile shell fallback missing {name}: {needle}")

def require_shell_excludes(name: str, text: str, needle: str) -> None:
    if needle in text:
        error.append(f"mobile shell fallback contains forbidden {name}: {needle}")

for shell_name, shell_text in [("Android", android_shell), ("iOS", ios_shell)]:
    require_shell_contains(shell_name, shell_text, "vendor/profile")
    require_shell_contains(shell_name, shell_text, "access/password")
    require_shell_contains(shell_name, shell_text, "access/verification")
    require_shell_contains(shell_name, shell_text, "access.sign_out")
    require_shell_contains(shell_name, shell_text, "coming_soon")
    require_shell_contains(shell_name, shell_text, "component_disabled")
    require_shell_excludes(shell_name, shell_text, "access/change-password")

for route in ["dashboard", "vendor", "vendor/profile", "more"]:
    require_shell_contains("Android handled route", android_shell, f'"{route}"')
    require_shell_contains("iOS handled route", ios_shell, f'"{route}"')

for disabled_key in ["access_password", "access_verification", "catalog", "message"]:
    require_shell_contains("Android disabled item", android_shell, f'item("{disabled_key}"')
    require_shell_contains("iOS disabled item", ios_shell, f'item("{disabled_key}"')

edge_navigation_route = (root / "mobile-edge/src/routes/mobile/navigationShell.ts").read_text(encoding="utf-8")
edge_navigation_client = (root / "mobile-edge/src/client/navigating/navigatingApiClient.ts").read_text(encoding="utf-8")
edge_navigation_contract = (root / "mobile-edge/src/contract/mobile/navigationShell.ts").read_text(encoding="utf-8")

for needle in [
    'app.get("/navigation/mobile/shell"',
    "navigatingApiClient.getMobileShell",
    "forwardedHeaders",
    "headers.cookie",
    "return reply.code(200).send(result.body)",
    "normalizeErrorPayload",
]:
    require_shell_contains("mobile-edge navigation shell route", edge_navigation_route, needle)

for needle in [
    '"/api/navigation/mobile/shell"',
    "NAVIGATING_API_BASE_URL",
    "navigating_api_unavailable",
]:
    require_shell_contains("mobile-edge navigating API client", edge_navigation_client, needle)

for needle in [
    "mobileNavigationShellPayload",
    'required: ["schema", "channel", "platforms", "locations"]',
    'required: ["key", "label", "enabled", "visible", "status", "location"]',
    "disabledReason",
    "additionalProperties: true",
]:
    require_shell_contains("mobile-edge navigation shell schema", edge_navigation_contract, needle)

for route in ["Config", "Entitlement", "Push", "Receipt", "Analytic", "Sync", "ApiKey", "Admin", "Webhook"]:
    if f"route{route}(app)" not in edge: error.append(f"unregistered mobile-edge route: {route}")
if (root / ".materialize").exists(): error.append("bootstrap payload remains")
for active_item in [
    'item("dashboard", "Dashboard", "dashboard", true, "dashboard")',
    'item("vendor", "Vendor", "store", true, "vendor")',
    'item("more", "More", "menu", true, "more")',
    'item("vendor_profile", "My Profile", "person", true, "vendor/profile")',
    'item("vendor_overview", "My Vendor", "store", true, "vendor")',
]:
    require_shell_contains("Android active fallback item", android_shell, active_item)
    require_shell_contains("iOS active fallback item", ios_shell, active_item)

for disabled_item in [
    'item("access_password", "Change Password", "key", false, "access/password")',
    'item("access_verification", "Verification", "key", false, "access/verification")',
    'item("catalog", "Catalog", "catalog", false, "catalog")',
    'item("message", "Message", "message", false, "message")',
]:
    require_shell_contains("Android disabled fallback item", android_shell, disabled_item)
    require_shell_contains("iOS disabled fallback item", ios_shell, disabled_item)

require_shell_contains("Android sign out fallback item", android_shell, 'item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action = "access.sign_out")')
require_shell_contains("iOS sign out fallback item", ios_shell, 'item("access_sign_out", "Sign Out", "logout", true, "access/sign-out", action: "access.sign_out")')
if error:
    print("Repository validation failed:")
    for item in error: print(f"- {item}")
    sys.exit(1)
edge_vendor_profile_route = (root / "mobile-edge/src/routes/mobile/vendorProfile.ts").read_text(encoding="utf-8")
for needle in [
    'app.get("/vendor/profile/:vendorId"',
    "vendoringApiClient.getProfile",
    "profilePayload(body)",
    "isRecord(body.data) ? body.data : body",
    "normalizeProfile(vendorId, result.body)",
]:
    require_shell_contains("mobile-edge vendor profile route", edge_vendor_profile_route, needle)
edge_vendor_profile_client = (root / "mobile-edge/src/client/vendoring/vendoringApiClient.ts").read_text(encoding="utf-8")
require_shell_contains("mobile-edge vendoring profile show endpoint", edge_vendor_profile_client, "/api/vendor/profile/show/")

if error:
    print("Repository validation failed:")
    for item in error: print(f"- {item}")
    sys.exit(1)
print("Repository validation passed")
