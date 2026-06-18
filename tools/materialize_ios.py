#!/usr/bin/env python3
"""One-time iOS materialization for SmartResponsor Mobiling."""
from pathlib import Path
from textwrap import dedent

ROOT = Path(__file__).resolve().parents[1]
IOS = ROOT / "mobile-client/ios"

def write(relative: str, text: str) -> None:
    path = ROOT / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(text).lstrip("\n").rstrip() + "\n", encoding="utf-8")

def remove(relative: str) -> None:
    path = ROOT / relative
    if path.exists():
        path.unlink()

def main() -> None:
    write("mobile-client/ios/App/Dashboard/DashboardPrimarySection.swift", '''
        import Foundation
        public enum DashboardPrimarySection: String, Sendable, CaseIterable { case catalog, message, vendor }
    ''')
    write("mobile-client/ios/App/Dashboard/DashboardEntryFlow.swift", '''
        import Foundation
        public enum DashboardEntryFlow: String, Sendable, CaseIterable { case auth }
    ''')
    write("mobile-client/ios/App/Dashboard/DashboardRouteMap.swift", '''
        import Foundation
        public struct DashboardRouteMap: Sendable {
            public init() {}
            public func primarySections() -> [DashboardPrimarySection] { [.catalog, .message, .vendor] }
            public func entryFlows() -> [DashboardEntryFlow] { [.auth] }
            public func navigationOwners() -> [String: String] {
                ["catalog": "catalog/navigation", "vendor": "vendor/navigation", "order": "order/ownership"]
            }
        }
    ''')
    remove("mobile-client/ios/App/Dashboard/DashboardSecondaryFlow.swift")
    write("mobile-client/ios/App/Order/OrderOwnedFlow.swift", '''
        import Foundation
        public enum OrderOwnedFlow: String, Sendable, CaseIterable { case shipment }
    ''')
    write("mobile-client/ios/App/Order/OrderEmbeddedCapability.swift", '''
        import Foundation
        public enum OrderEmbeddedCapability: String, Sendable, CaseIterable { case taxation }
    ''')
    write("mobile-client/ios/App/Order/OrderOwnedRouteMap.swift", '''
        import Foundation
        public struct OrderOwnedRouteMap: Sendable {
            public init() {}
            public func ownedFlows() -> [OrderOwnedFlow] { [.shipment] }
            public func embeddedCapabilities() -> [OrderEmbeddedCapability] { [.taxation] }
        }
    ''')
    sources = ["Contract", "Data", "UseCase", "Navigation", "UI", "App/Auth", "App/Catalog", "App/Dashboard", "App/Message", "App/Order", "App/Product", "App/Profile", "App/Project", "App/Shipment", "App/Taxation", "App/Vendor"]
    source_lines = ",\n                ".join(f'"{item}"' for item in sources)
    write("mobile-client/ios/Package.swift", f'''
        // swift-tools-version: 5.9
        import PackageDescription
        let package = Package(
            name: "Mobiling",
            platforms: [.iOS(.v15)],
            products: [
                .library(name: "MobileClient", targets: ["MobileClient"]),
                .library(name: "CoreConfig", targets: ["CoreConfig"]),
                .library(name: "CoreEntitlement", targets: ["CoreEntitlement"]),
                .library(name: "CoreBilling", targets: ["CoreBilling"]),
                .library(name: "CoreAnalytic", targets: ["CoreAnalytic"]),
                .library(name: "CorePush", targets: ["CorePush"]),
                .library(name: "CoreSecurity", targets: ["CoreSecurity"]),
            ],
            targets: [
                .target(name: "MobileClient", path: ".", exclude: ["Sources", "App/ContentView.swift", "App/MobileApp.swift", "App/Info.plist", "project.yml"], sources: [
                    {source_lines}
                ]),
                .target(name: "CoreConfig", dependencies: ["MobileClient"], path: "Sources/CoreConfig"),
                .target(name: "CoreEntitlement", dependencies: ["MobileClient"], path: "Sources/CoreEntitlement"),
                .target(name: "CoreBilling", dependencies: ["MobileClient"], path: "Sources/CoreBilling"),
                .target(name: "CoreAnalytic", dependencies: ["MobileClient"], path: "Sources/CoreAnalytic"),
                .target(name: "CorePush", dependencies: ["MobileClient"], path: "Sources/CorePush"),
                .target(name: "CoreSecurity", dependencies: ["MobileClient"], path: "Sources/CoreSecurity"),
            ]
        )
    ''')
    source_yml = "\n".join(f"      - path: {item}" for item in sources)
    write("mobile-client/ios/project.yml", f'''name: Mobiling
options:
  bundleIdPrefix: com.smartresponsor
  deploymentTarget:
    iOS: "15.0"
settings:
  base:
    SWIFT_VERSION: "5.9"
targets:
  MobileClient:
    type: framework
    platform: iOS
    sources:
{source_yml}
  CoreConfig:
    type: framework
    platform: iOS
    sources: [Sources/CoreConfig]
    dependencies: [{{ target: MobileClient }}]
  CoreEntitlement:
    type: framework
    platform: iOS
    sources: [Sources/CoreEntitlement]
    dependencies: [{{ target: MobileClient }}]
  CoreBilling:
    type: framework
    platform: iOS
    sources: [Sources/CoreBilling]
    dependencies: [{{ target: MobileClient }}]
  CoreAnalytic:
    type: framework
    platform: iOS
    sources: [Sources/CoreAnalytic]
    dependencies: [{{ target: MobileClient }}]
  CorePush:
    type: framework
    platform: iOS
    sources: [Sources/CorePush]
    dependencies: [{{ target: MobileClient }}]
  CoreSecurity:
    type: framework
    platform: iOS
    sources: [Sources/CoreSecurity]
    dependencies: [{{ target: MobileClient }}]
  Mobiling:
    type: application
    platform: iOS
    sources:
      - path: App/ContentView.swift
      - path: App/MobileApp.swift
    info:
      path: App/Info.plist
    settings:
      base:
        PRODUCT_BUNDLE_IDENTIFIER: com.smartresponsor.mobile
    dependencies:
      - target: MobileClient
      - target: CoreConfig
      - target: CoreEntitlement
      - target: CoreBilling
      - target: CoreAnalytic
      - target: CorePush
      - target: CoreSecurity
schemes:
  Mobiling:
    build:
      targets:
        Mobiling: all
    run:
      config: Debug
''')
    for path in (IOS / "Sources").rglob("*.swift"):
        text = path.read_text(encoding="utf-8")
        if "import MobileClient" not in text:
            path.write_text("import MobileClient\n" + text, encoding="utf-8")
    write("mobile-client/ios/App/ContentView.swift", '''
        import SwiftUI
        import MobileClient
        struct ContentView: View {
            private let dashboard = DashboardRouteMap()
            private let vendor = VendorOwnedRouteMap()
            private let order = OrderOwnedRouteMap()
            var body: some View {
                List {
                    Section("SmartResponsor Mobiling") { Text("Repository materialization baseline") }
                    Section("Dashboard") {
                        Text(dashboard.primarySections().map(\.rawValue).joined(separator: ", "))
                        Text("Entry: " + dashboard.entryFlows().map(\.rawValue).joined(separator: ", "))
                    }
                    Section("Vendor ownership") { Text(vendor.ownedFlows().map(\.rawValue).joined(separator: ", ")) }
                    Section("Order ownership") {
                        Text(order.ownedFlows().map(\.rawValue).joined(separator: ", "))
                        Text("Embedded: " + order.embeddedCapabilities().map(\.rawValue).joined(separator: ", "))
                    }
                }
            }
        }
    ''')
    write("mobile-client/ios/App/MobileApp.swift", '''
        import SwiftUI
        @main struct MobilingApp: App { var body: some Scene { WindowGroup { ContentView() } } }
    ''')
    print("iOS materialized")

if __name__ == "__main__":
    main()
