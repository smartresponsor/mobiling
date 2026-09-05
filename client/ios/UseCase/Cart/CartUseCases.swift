import Foundation

public struct CartLoadCurrentUseCase: Sendable {
    private let reader: any CartReader

    public init(reader: any CartReader) {
        self.reader = reader
    }

    public func callAsFunction() async throws -> CartMobilePayload {
        try await reader.currentCart()
    }
}

public struct CartAddItemUseCase: Sendable {
    private let writer: any CartWriter

    public init(writer: any CartWriter) {
        self.writer = writer
    }

    public func callAsFunction(request: CartAddItemRequest) async throws -> CartMobilePayload {
        try await writer.addItem(request: request)
    }
}

public struct CartPrepareCheckoutHandoffUseCase: Sendable {
    private let gateway: any CartCheckoutGateway

    public init(gateway: any CartCheckoutGateway) {
        self.gateway = gateway
    }

    public func callAsFunction() async throws -> CartCheckoutHandoffPayload {
        try await gateway.prepareCheckoutHandoff()
    }
}
