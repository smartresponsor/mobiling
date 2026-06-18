#!/usr/bin/env python3
"""Finalize the SmartResponsor Mobiling materialization baseline."""
from pathlib import Path
from textwrap import dedent
import re

ROOT = Path(__file__).resolve().parents[1]

def write(relative: str, text: str) -> None:
    path = ROOT / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(text).lstrip("\n").rstrip() + "\n", encoding="utf-8")

def normalize_comments() -> int:
    cyrillic = re.compile(r"[\u0400-\u04ff]")
    changed = 0
    for root in (ROOT / "mobile-client", ROOT / "mobile-edge"):
        for path in root.rglob("*"):
            if not path.is_file() or path.suffix not in {".kt", ".swift", ".ts", ".js"}:
                continue
            original = path.read_text(encoding="utf-8")
            output: list[str] = []
            for line in original.splitlines():
                stripped = line.lstrip()
                if cyrillic.search(line) and (stripped.startswith("//") or stripped.startswith("*")):
                    continue
                output.append(line)
            updated = "\n".join(output) + ("\n" if original.endswith("\n") else "")
            if updated != original:
                path.write_text(updated, encoding="utf-8")
                changed += 1
    return changed

def main() -> None:
    write("README.md", '''
        # SmartResponsor Mobiling

        Canonical mobile workspace for Android, iOS, cross-platform contracts, and Mobile Edge.

        ## Active roots

        - `mobile-client/android` — Android application and canonical client layers.
        - `mobile-client/ios` — iOS application, `MobileClient` framework, and mirrored core frameworks.
        - `mobile-client/contract` — cross-platform contracts and ownership metadata.
        - `mobile-edge` — Fastify mobile boundary and runtime services.

        ## Ownership

        `Dashboard` is the only application root. `Auth` is an entry flow. Persistent sections are `Catalog`, `Message`, and `Vendor`. `Vendor` owns `Product`, `Order`, `Project`, and `Profile`. `Order` owns `Shipment` and embeds `Taxation`. `Identity` is internal.

        Machine-readable ownership is stored in `mobile-client/contract/navigation/ownership.json`.

        This baseline materializes build and runtime structure only. Product visual direction remains intentionally undecided.
    ''')
    write("docs/MATERIALIZATION_BASELINE.md", '''
        # Repository materialization baseline

        This baseline converts the curated donor slice into explicit build and runtime surfaces.

        ## Materialized state

        - Android layer folders participate in one Gradle graph.
        - Kotlin imports use canonical type casing.
        - iOS exposes a `MobileClient` framework boundary, mirrored core frameworks, Swift Package metadata, and an XcodeGen application definition.
        - Mobile Edge registers implemented public, system, and protected admin routes from one entry point.
        - Mobile Edge uses one complete process-local storage contract for the baseline; durable storage selection is deferred until runtime requirements are fixed.
        - Ownership is machine-readable in `mobile-client/contract/navigation/ownership.json`.

        ## Deliberately deferred

        No final visual language, component system, screen sequence, database engine, or deployment topology is selected by this wave. Android and iOS show only an ownership/materialization diagnostic surface, not product UI.
    ''')
    write("tools/validate_repository.py", r'''
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
    ''')
    write(".github/workflows/ci.yml", '''
        name: Mobiling CI
        on:
          push:
            branches: [master]
          pull_request:
            branches: [master]
        permissions:
          contents: read
        jobs:
          repository:
            runs-on: ubuntu-latest
            steps:
              - uses: actions/checkout@v4
              - uses: actions/setup-python@v5
                with: { python-version: "3.12" }
              - run: python tools/validate_repository.py
          edge:
            runs-on: ubuntu-latest
            defaults: { run: { working-directory: mobile-edge } }
            steps:
              - uses: actions/checkout@v4
              - uses: actions/setup-node@v4
                with: { node-version: "22" }
              - run: npm install --no-audit --no-fund
              - run: npm run build
          android:
            runs-on: ubuntu-latest
            defaults: { run: { working-directory: mobile-client/android } }
            steps:
              - uses: actions/checkout@v4
              - uses: actions/setup-java@v4
                with: { distribution: temurin, java-version: "17" }
              - uses: gradle/actions/setup-gradle@v4
                with: { gradle-version: "8.2" }
              - run: gradle :app:compileDebugKotlin --stacktrace
          ios-structure:
            runs-on: macos-latest
            defaults: { run: { working-directory: mobile-client/ios } }
            steps:
              - uses: actions/checkout@v4
              - run: swift package dump-package
              - run: brew install xcodegen
              - run: xcodegen generate
    ''')
    print(f"Finalized materialization; normalized comments in {normalize_comments()} files")

if __name__ == "__main__":
    main()
