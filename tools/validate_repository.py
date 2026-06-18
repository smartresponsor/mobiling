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
ownership = json.loads((root / "mobile-client/contract/navigation/ownership.json").read_text(encoding="utf-8"))
for key, value in expected.items():
    if ownership.get(key) != value: error.append(f"ownership mismatch: {key}")
for relative in [
    "mobile-client/android/Contract/build.gradle.kts", "mobile-client/android/Data/build.gradle.kts",
    "mobile-client/android/UseCase/build.gradle.kts", "mobile-client/android/Navigation/build.gradle.kts",
    "mobile-client/android/UI/build.gradle.kts", "mobile-client/ios/Package.swift",
    "mobile-client/ios/project.yml", "mobile-edge/src/app.ts", "mobile-edge/src/repository/runtime.ts",
]:
    if not (root / relative).exists(): error.append(f"missing materialized surface: {relative}")
declaration = re.compile(r"\b(?:data\s+class|sealed\s+class|enum\s+class|class|interface|object|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)")
package = re.compile(r"(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*$")
symbols: dict[tuple[str, str], str] = {}
android = root / "mobile-client/android"
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
for route in ["Config", "Entitlement", "Push", "Receipt", "Analytic", "Sync", "ApiKey", "Admin", "Webhook"]:
    if f"route{route}(app)" not in edge: error.append(f"unregistered mobile-edge route: {route}")
if (root / ".materialize").exists(): error.append("bootstrap payload remains")
if error:
    print("Repository validation failed:")
    for item in error: print(f"- {item}")
    sys.exit(1)
print("Repository validation passed")
