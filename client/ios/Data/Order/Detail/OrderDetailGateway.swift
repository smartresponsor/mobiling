import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol OrderDetailGateway {
    func loadOrderDetail(orderId: String) async throws -> OrderDetailPayload
}
