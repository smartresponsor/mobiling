import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadOrderDetailUseCase {
    let gateway: OrderDetailGateway

    func callAsFunction(orderId: String) async throws -> OrderDetailPayload {
        try await gateway.loadOrderDetail(orderId: orderId)
    }
}
