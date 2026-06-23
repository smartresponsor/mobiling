import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ListOrdersUseCase {
    let gateway: OrderListingGateway

    func callAsFunction(query: ListOrdersQuery) async throws -> [OrderSummary] {
        try await gateway.listOrders(query: query)
    }
}
