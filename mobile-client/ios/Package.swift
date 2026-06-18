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
            "Contract",
        "Data",
        "UseCase",
        "Navigation",
        "UI",
        "App/Auth",
        "App/Catalog",
        "App/Dashboard",
        "App/Message",
        "App/Order",
        "App/Product",
        "App/Profile",
        "App/Project",
        "App/Shipment",
        "App/Taxation",
        "App/Vendor"
        ]),
        .target(name: "CoreConfig", dependencies: ["MobileClient"], path: "Sources/CoreConfig"),
        .target(name: "CoreEntitlement", dependencies: ["MobileClient"], path: "Sources/CoreEntitlement"),
        .target(name: "CoreBilling", dependencies: ["MobileClient"], path: "Sources/CoreBilling"),
        .target(name: "CoreAnalytic", dependencies: ["MobileClient"], path: "Sources/CoreAnalytic"),
        .target(name: "CorePush", dependencies: ["MobileClient"], path: "Sources/CorePush"),
        .target(name: "CoreSecurity", dependencies: ["MobileClient"], path: "Sources/CoreSecurity"),
    ]
)
