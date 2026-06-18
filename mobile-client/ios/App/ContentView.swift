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
