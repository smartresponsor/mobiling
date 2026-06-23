import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct TaxDetailPayload {
    let jurisdictionCode: String
    let taxableAmountLabel: String
    let subtotalAmountLabel: String
    let taxAmountLabel: String
    let totalAmountLabel: String
    let rateLabel: String
    let breakdownLines: [String]
}
