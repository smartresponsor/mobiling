import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct VendorListingScreenState {
    let query: ListVendorsQuery
    let vendors: [VendorCardSummary]
    let isLoading: Bool
}
