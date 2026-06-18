// swift-tools-version:5.9
import PackageDescription
let package = Package(
  name: "Mobile",
  platforms: [.iOS(.v15)],
  products: [
    .library(name: "CoreConfig", targets: ["CoreConfig"]),
    .library(name: "CoreEntitlement", targets: ["CoreEntitlement"]),
    .library(name: "CoreBilling", targets: ["CoreBilling"]),
    .library(name: "CoreAnalytic", targets: ["CoreAnalytic"]),
    .library(name: "CorePush", targets: ["CorePush"]),
    .library(name: "CoreSecurity", targets: ["CoreSecurity"]),
  ],
  targets: [
    .target(name: "CoreConfig"),
    .target(name: "CoreEntitlement"),
    .target(name: "CoreBilling"),
    .target(name: "CoreAnalytic"),
    .target(name: "CorePush"),
    .target(name: "CoreSecurity")
  ]
)
