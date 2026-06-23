import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol OrderListingGateway {
    func listOrders(query: ListOrdersQuery) async throws -> [OrderSummary]
}
