#!/usr/bin/env python3
"""One-time Android materialization for SmartResponsor Mobiling."""
from __future__ import annotations
import json
import re
from pathlib import Path
from textwrap import dedent

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "mobile-client/android"

def write(relative: str, text: str) -> None:
    path = ROOT / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(text).lstrip("\n").rstrip() + "\n", encoding="utf-8")

def remove(relative: str) -> None:
    path = ROOT / relative
    if path.exists():
        path.unlink()

def library(namespace: str, dependencies: str = "") -> str:
    return f'''\
plugins {{
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}}

android {{
    namespace = "{namespace}"
    compileSdk = 34
    defaultConfig {{ minSdk = 24 }}
    sourceSets["main"].java.srcDirs(".")
}}

{dependencies}
'''

def normalize_imports() -> int:
    package_re = re.compile(r"(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*$")
    declaration_re = re.compile(r"\b(?:data\s+class|sealed\s+class|enum\s+class|class|interface|object|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)")
    symbols: dict[tuple[str, str], str] = {}
    source = list(ANDROID.rglob("*.kt"))
    for path in source:
        text = path.read_text(encoding="utf-8")
        package = package_re.search(text)
        if package:
            for name in declaration_re.findall(text):
                symbols[(package.group(1), name.lower())] = name
    import_re = re.compile(r"(?m)^(\s*import\s+)([A-Za-z0-9_.]+)(\s*)$")
    changed = 0
    for path in source:
        text = path.read_text(encoding="utf-8")
        def replace(match: re.Match[str]) -> str:
            fqcn = match.group(2)
            if "." not in fqcn:
                return match.group(0)
            owner, name = fqcn.rsplit(".", 1)
            canonical = symbols.get((owner, name.lower()))
            return f"{match.group(1)}{owner}.{canonical}{match.group(3)}" if canonical and canonical != name else match.group(0)
        updated = import_re.sub(replace, text)
        if updated != text:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    return changed

def main() -> None:
    ownership = {
        "$schema": "https://smartresponsor.dev/schema/mobile-ownership-v1.json",
        "schema": "smartresponsor.mobile.ownership.v1",
        "root": "dashboard",
        "entry_flow": ["auth"],
        "primary_section": ["catalog", "message", "vendor"],
        "owner": {"vendor": ["product", "order", "project", "profile"], "order": ["shipment"]},
        "embedded": {"order": ["taxation"]},
        "internal": ["identity"],
    }
    ownership_path = ROOT / "mobile-client/contract/navigation/ownership.json"
    ownership_path.parent.mkdir(parents=True, exist_ok=True)
    ownership_path.write_text(json.dumps(ownership, indent=2) + "\n", encoding="utf-8")

    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/dashboard/DashboardPrimarySection.kt", '''
        package com.smartresponsor.mobile.app.dashboard
        enum class DashboardPrimarySection { CATALOG, MESSAGE, VENDOR }
    ''')
    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/dashboard/DashboardEntryFlow.kt", '''
        package com.smartresponsor.mobile.app.dashboard
        enum class DashboardEntryFlow { AUTH }
    ''')
    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/dashboard/DashboardRouteMap.kt", '''
        package com.smartresponsor.mobile.app.dashboard
        class DashboardRouteMap {
            fun primarySections(): List<DashboardPrimarySection> = listOf(
                DashboardPrimarySection.CATALOG,
                DashboardPrimarySection.MESSAGE,
                DashboardPrimarySection.VENDOR,
            )
            fun entryFlows(): List<DashboardEntryFlow> = listOf(DashboardEntryFlow.AUTH)
            fun navigationOwners(): Map<String, String> = mapOf(
                "catalog" to "catalog/navigation",
                "vendor" to "vendor/navigation",
                "order" to "order/ownership",
            )
        }
    ''')
    remove("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/dashboard/DashboardSecondaryFlow.kt")
    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/order/OrderOwnedFlow.kt", '''
        package com.smartresponsor.mobile.app.order
        enum class OrderOwnedFlow { SHIPMENT }
    ''')
    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/order/OrderEmbeddedCapability.kt", '''
        package com.smartresponsor.mobile.app.order
        enum class OrderEmbeddedCapability { TAXATION }
    ''')
    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/app/order/OrderOwnedRouteMap.kt", '''
        package com.smartresponsor.mobile.app.order
        class OrderOwnedRouteMap {
            fun ownedFlows(): List<OrderOwnedFlow> = listOf(OrderOwnedFlow.SHIPMENT)
            fun embeddedCapabilities(): List<OrderEmbeddedCapability> = listOf(OrderEmbeddedCapability.TAXATION)
        }
    ''')

    write("mobile-client/android/settings.gradle.kts", '''
        pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories { google(); mavenCentral() }
        }
        rootProject.name = "Mobiling"
        include(
            ":app", ":client-contract", ":client-data", ":client-usecase", ":client-navigation", ":client-ui",
            ":core:config", ":core:entitlement", ":core:billing", ":core:analytic", ":core:push", ":core:security",
        )
        project(":client-contract").projectDir = file("Contract")
        project(":client-data").projectDir = file("Data")
        project(":client-usecase").projectDir = file("UseCase")
        project(":client-navigation").projectDir = file("Navigation")
        project(":client-ui").projectDir = file("UI")
    ''')
    write("mobile-client/android/build.gradle.kts", '''
        plugins {
            id("com.android.application") version "8.2.2" apply false
            id("com.android.library") version "8.2.2" apply false
            id("org.jetbrains.kotlin.android") version "1.9.10" apply false
            id("org.jetbrains.kotlin.kapt") version "1.9.10" apply false
        }
    ''')
    write("mobile-client/android/Contract/build.gradle.kts", library("com.smartresponsor.mobile.client.contract"))
    write("mobile-client/android/Data/build.gradle.kts", library("com.smartresponsor.mobile.client.data", '''
        dependencies {
            implementation(project(":client-contract"))
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.datastore:datastore-preferences:1.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }
    '''))
    write("mobile-client/android/UseCase/build.gradle.kts", library("com.smartresponsor.mobile.client.usecase", '''
        dependencies {
            implementation(project(":client-contract"))
            implementation(project(":client-data"))
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }
    '''))
    write("mobile-client/android/Navigation/build.gradle.kts", library("com.smartresponsor.mobile.client.navigation"))
    write("mobile-client/android/UI/build.gradle.kts", library("com.smartresponsor.mobile.client.ui", '''
        dependencies { implementation(project(":client-contract")) }
    '''))
    write("mobile-client/android/app/build.gradle.kts", '''
        plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
        android {
            namespace = "com.smartresponsor.mobile"
            compileSdk = 34
            defaultConfig { applicationId = "com.smartresponsor.mobile"; minSdk = 24; targetSdk = 34; versionCode = 1; versionName = "0.1.0-materialized" }
            buildTypes {
                getByName("release") { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
                getByName("debug") { isMinifyEnabled = false }
            }
            buildFeatures { compose = true }
            composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
        }
        dependencies {
            implementation(project(":client-contract")); implementation(project(":client-data")); implementation(project(":client-usecase")); implementation(project(":client-navigation")); implementation(project(":client-ui"))
            implementation(project(":core:config")); implementation(project(":core:entitlement")); implementation(project(":core:billing")); implementation(project(":core:analytic")); implementation(project(":core:push")); implementation(project(":core:security"))
            implementation("androidx.core:core-ktx:1.12.0"); implementation("androidx.activity:activity-compose:1.8.2")
            implementation("androidx.compose.ui:ui:1.6.0"); implementation("androidx.compose.material3:material3:1.1.2")
            implementation("androidx.work:work-runtime-ktx:2.9.0")
        }
    ''')
    dependency_block = dedent('''
        dependencies {
            implementation(project(":client-contract"))
            implementation(project(":client-data"))
            implementation(project(":client-usecase"))
        }
    ''')
    for module in ("config", "entitlement", "billing", "analytic", "push", "security"):
        path = ANDROID / f"core/{module}/build.gradle.kts"
        if path.exists() and 'project(":client-contract")' not in path.read_text(encoding="utf-8"):
            path.write_text(path.read_text(encoding="utf-8").rstrip() + "\n" + dependency_block, encoding="utf-8")

    write("mobile-client/android/app/src/main/java/com/smartresponsor/mobile/MainActivity.kt", '''
        package com.smartresponsor.mobile
        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import androidx.compose.foundation.layout.Arrangement
        import androidx.compose.foundation.layout.Column
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.foundation.layout.padding
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.Surface
        import androidx.compose.material3.Text
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.unit.dp
        import com.smartresponsor.mobile.app.dashboard.DashboardRouteMap
        import com.smartresponsor.mobile.app.order.OrderOwnedRouteMap
        import com.smartresponsor.mobile.app.vendor.VendorOwnedRouteMap
        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { MaterializationScreen() } } }
            }
        }
        @Composable private fun MaterializationScreen() {
            val dashboard = DashboardRouteMap(); val vendor = VendorOwnedRouteMap(); val order = OrderOwnedRouteMap()
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("SmartResponsor Mobiling", style = MaterialTheme.typography.headlineSmall)
                Text("Repository materialization baseline")
                Text("Dashboard: ${dashboard.primarySections().joinToString()}")
                Text("Entry flow: ${dashboard.entryFlows().joinToString()}")
                Text("Vendor owns: ${vendor.ownedFlows().joinToString()}")
                Text("Order owns: ${order.ownedFlows().joinToString()}")
                Text("Order embeds: ${order.embeddedCapabilities().joinToString()}")
            }
        }
    ''')
    config = ANDROID / "Data/System/Config/RemoteConfigStore.kt"
    if config.exists():
        text = config.read_text(encoding="utf-8")
        text = text.replace('"{"ttlSec":1800,"flag":{"analyticEnabled":true,"billing.graceDay":3,"security.signingEnabled":true}}"', '"""{"ttlSec":1800,"flag":{"analyticEnabled":true,"billing.graceDay":3,"security.signingEnabled":true}}"""')
        config.write_text(text, encoding="utf-8")
    remove("mobile-client/android/core/billing/src/main/java/com/smartresponsor/core/billing/Shell.kt")
    print(f"Android materialized; normalized imports in {normalize_imports()} files")

if __name__ == "__main__":
    main()
