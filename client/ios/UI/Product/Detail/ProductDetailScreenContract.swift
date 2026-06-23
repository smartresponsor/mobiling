import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol ProductDetailScreenContract: AnyObject {
    func render(product: ProductDetailPayload?, isLoading: Bool)
    func goBack()
}
