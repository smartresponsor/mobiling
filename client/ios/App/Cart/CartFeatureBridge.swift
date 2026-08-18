import Foundation

public struct CartFeatureBridge: Sendable {
    private let reader: any CartReader
    private let writer: any CartWriter
    private let checkoutGateway: any CartCheckoutGateway

    public init(reader: any CartReader, writer: any CartWriter, checkoutGateway: any CartCheckoutGateway) {
        self.reader = reader
        self.writer = writer
        self.checkoutGateway = checkoutGateway
    }

    public func current() async throws -> CartMobilePayload {
        try await CartLoadCurrentUseCase(reader: reader)()
    }

    public func add(request: CartAddItemRequest) async throws -> CartMobilePayload {
        try await CartAddItemUseCase(writer: writer)(request: request)
    }

    public func checkoutHandoff() async throws -> CartCheckoutHandoffPayload {
        try await CartPrepareCheckoutHandoffUseCase(gateway: checkoutGateway)()
    }
}
